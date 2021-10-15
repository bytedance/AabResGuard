package com.bytedance.android.aabresguard.executors;

import com.android.aapt.Resources;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.BundleModuleName;
import com.android.tools.build.bundletool.model.InMemoryModuleEntry;
import com.android.tools.build.bundletool.model.ModuleEntry;
import com.android.tools.build.bundletool.model.ResourceTableEntry;
import com.android.tools.build.bundletool.model.ZipPath;
import com.android.tools.build.bundletool.model.utils.ResourcesUtils;
import com.bytedance.android.aabresguard.bundle.AppBundleUtils;
import com.bytedance.android.aabresguard.bundle.ResourcesTableBuilder;
import com.bytedance.android.aabresguard.bundle.ResourcesTableOperation;
import com.bytedance.android.aabresguard.model.ResourcesMapping;
import com.bytedance.android.aabresguard.obfuscation.ResGuardStringBuilder;
import com.bytedance.android.aabresguard.parser.ResourcesMappingParser;
import com.bytedance.android.aabresguard.utils.FileOperation;
import com.bytedance.android.aabresguard.utils.TimeClock;
import com.bytedance.android.aabresguard.utils.Utils;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static com.bytedance.android.aabresguard.bundle.AppBundleUtils.getEntryNameByResourceName;
import static com.bytedance.android.aabresguard.bundle.AppBundleUtils.getTypeNameByResourceName;
import static com.bytedance.android.aabresguard.bundle.ResourcesTableOperation.checkConfiguration;
import static com.bytedance.android.aabresguard.bundle.ResourcesTableOperation.updateEntryConfigValueList;
import static com.bytedance.android.aabresguard.utils.FileOperation.getFilePrefixByFileName;
import static com.bytedance.android.aabresguard.utils.FileOperation.getNameFromZipFilePath;
import static com.bytedance.android.aabresguard.utils.FileOperation.getParentFromZipFilePath;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class ResourcesObfuscator {
    public static final String RESOURCE_ANDROID_PREFIX = "android:";
    public static final String FILE_MAPPING_NAME = "resources-mapping.txt";
    private static final Logger logger = Logger.getLogger(ResourcesObfuscator.class.getName());

    private final AppBundle rawAppBundle;
    private final Set<String> whiteListRules;
    private final Path outputMappingPath;
    private final ZipFile bundleZipFile;
    private ResourcesMapping resourcesMapping;

    public ResourcesObfuscator(Path bundlePath, AppBundle rawAppBundle, Set<String> whiteListRules, Path outputLogLocationDir, Path mappingPath) throws IOException {
        if (mappingPath != null && mappingPath.toFile().exists()) {
            resourcesMapping = new ResourcesMappingParser(mappingPath).parse();
        } else {
            resourcesMapping = new ResourcesMapping();
        }

        this.bundleZipFile = new ZipFile(bundlePath.toFile());

        outputMappingPath = new File(outputLogLocationDir.toFile(), FILE_MAPPING_NAME).toPath();
        //checkFileDoesNotExist(outputMappingPath);
        if (Files.exists(outputMappingPath, new LinkOption[0])) {
            logger.warning("Mapping file: "+outputMappingPath+" already existing! Deleting...");
            Files.delete(outputMappingPath);
        }

        this.rawAppBundle = rawAppBundle;
        this.whiteListRules = whiteListRules;

    }

    public Path getOutputMappingPath() {
        return outputMappingPath;
    }

    public AppBundle obfuscate() throws IOException {
        TimeClock timeClock = new TimeClock();

        checkResMappingRules();
        Map<BundleModuleName, BundleModule> obfuscatedModules = new HashMap<>();
        // generate type entry mapping from mapping rule
        Map<String, Set<String>> typeEntryMapping = generateObfuscatedEntryFilesFromMapping();

        for (Map.Entry<BundleModuleName, BundleModule> entry : rawAppBundle.getModules().entrySet()) {
            BundleModule bundleModule = entry.getValue();
            BundleModuleName bundleModuleName = entry.getKey();
            // generate obfuscation resources mapping
            generateResourceMappingRule(bundleModule, typeEntryMapping);
            // obfuscate module entries
            Map<String, String> obfuscateModuleEntriesMap = obfuscateModuleEntries(bundleModule, typeEntryMapping);
            // obfuscate bundle module
            BundleModule obfuscatedModule = obfuscateBundleModule(bundleModule, obfuscateModuleEntriesMap);
            obfuscatedModules.put(bundleModuleName, obfuscatedModule);
        }

        AppBundle appBundle = rawAppBundle.toBuilder()
                .setModules(ImmutableMap.copyOf(obfuscatedModules))
                .build();

        System.out.println(String.format(
                "obfuscate resources done, cost %s",
                timeClock.getCost()
        ));

        // write mapping rules to file.
        resourcesMapping.writeMappingToFile(outputMappingPath);

        return appBundle;
    }

    private Map<String, Set<String>> generateObfuscatedEntryFilesFromMapping() {
        Map<String, Set<String>> typeEntryMapping = new HashMap<>();
        // generate obfuscated entry path from incremental mapping
        for (String path : resourcesMapping.getEntryFilesMapping().values()) {
            String parentPath = getParentFromZipFilePath(path);
            String name = getFilePrefixByFileName(getNameFromZipFilePath(path));
            Set<String> entryList = typeEntryMapping.get(parentPath);
            if (entryList == null) entryList = new HashSet<>();
            entryList.add(name);
            typeEntryMapping.put(parentPath, entryList);
        }
        // generate obfuscated entry name from incremental mapping
        for (String entry : resourcesMapping.getResourceMapping().values()) {
            String name = getEntryNameByResourceName(entry);
            String type = getTypeNameByResourceName(entry);
            Set<String> entryList = typeEntryMapping.get(type);
            if (entryList == null) entryList = new HashSet<>();
            entryList.add(name);
            typeEntryMapping.put(type, entryList);
        }
        return typeEntryMapping;
    }

    /**
     * Reads resourceTable and generate obfuscate mapping.
     */
    private void generateResourceMappingRule(BundleModule bundleModule, Map<String, Set<String>> typeEntryMapping) {
        if (!bundleModule.getResourceTable().isPresent()) {
            return;
        }
        ResGuardStringBuilder guardStringBuilder = new ResGuardStringBuilder();
        guardStringBuilder.reset(null);

        Resources.ResourceTable table = bundleModule.getResourceTable().get();
        // generate resource directory mapping
        ResourcesUtils.getAllFileReferences(table)
                .stream()
                .map(ZipPath::getParent)
                .filter(Objects::nonNull)
                .filter(path -> !resourcesMapping.getDirMapping().containsKey(path.toString()))
                .forEach(path -> {
                    guardStringBuilder.reset(null);
                    String name = guardStringBuilder.getReplaceString(resourcesMapping.getPathMappingNameList());
                    resourcesMapping.putDirMapping(path.toString(), BundleModule.RESOURCES_DIRECTORY.toString() + "/" + name);
                });
        // generate resource mapping
        ResourcesUtils.entries(table).forEach(entry -> {
            String resourceId = entry.getResourceId().toString();
            String resourceName = AppBundleUtils.getResourceFullName(entry);
            Set<String> obfuscationList = typeEntryMapping.get(entry.getType().getName());
            if (obfuscationList == null) {
                obfuscationList = new HashSet<>();
            }
            guardStringBuilder.reset(null);
            if (resourcesMapping.getResourceMapping().containsKey(resourceName)) {
                if (!shouldBeObfuscated(resourceName)) {
                    System.out.println(String.format(
                            "[whiteList] Found whiteList resource, removing from mapping: %s, id: %s",
                            resourceName,
                            resourceId
                    ));
                    resourcesMapping.getResourceMapping().remove(resourceName);
                } else {
                    //System.out.println("Obfuscating resource: " + resourceName);
                    String obfuscateResourceName = resourcesMapping.getResourceMapping().get(resourceName);
                    obfuscationList.add(AppBundleUtils.getEntryNameByResourceName(obfuscateResourceName));
                }
            } else {
                if (!shouldBeObfuscated(resourceName)) {
                    System.out.println(String.format(
                            "[whiteList] Found whiteList resource: %s, id: %s",
                            resourceName,
                            resourceId
                    ));
                } else {
                    //System.out.println("Obfuscating resource: " + resourceName);
                    String name = guardStringBuilder.getReplaceString(obfuscationList);
                    obfuscationList.add(name);
                    String obfuscatedResourceName = AppBundleUtils.getResourceFullName(entry.getPackage().getPackageName(), entry.getType().getName(), name);
                    resourcesMapping.putResourceMapping(resourceName, obfuscatedResourceName);
                }
            }
            typeEntryMapping.put(entry.getType().getName(), obfuscationList);
        });
    }

    /**
     * Obfuscate module entries and return the mapping rules.
     */
    private Map<String, String> obfuscateModuleEntries(BundleModule bundleModule, Map<String, Set<String>> typeMappingMap) {
        ResGuardStringBuilder guardStringBuilder = new ResGuardStringBuilder();
        guardStringBuilder.reset(null);
        Map<String, String> obfuscateEntries = new HashMap<>();

        bundleModule.getEntries().stream()
                .filter(entry -> entry.getPath().startsWith(BundleModule.RESOURCES_DIRECTORY))
                .forEach(entry -> {
                    guardStringBuilder.reset(null);
                    String entryDir = entry.getPath().getParent().toString();
                    String obfuscateDir = resourcesMapping.getDirMapping().get(entryDir);
                    if (obfuscateDir == null) {
                        throw new RuntimeException(String.format("can not find resource directory: %s", entryDir));
                    }
                    Set<String> mapping = typeMappingMap.get(obfuscateDir);
                    if (mapping == null) {
                        mapping = new HashSet<>();
                    }

                    String bundleRawPath = bundleModule.getName().getName() + "/" + entry.getPath().toString();
                    String bundleObfuscatedPath = resourcesMapping.getEntryFilesMapping().get(bundleRawPath);
                    if (bundleObfuscatedPath == null) {
                        //System.out.println(": "+bundleRawPath);
                        if (!shouldBeObfuscated(bundleRawPath)) {
                            System.out.println(String.format(
                                    "[whiteList] find whiteList resource file, resource: %s",
                                    bundleRawPath
                            ));
                            return;
                        } else {
                            String fileSuffix = FileOperation.getFileSuffix(entry.getPath());
                            String obfuscatedName = guardStringBuilder.getReplaceString(mapping);
                            mapping.add(obfuscatedName);
                            bundleObfuscatedPath = obfuscateDir + "/" + obfuscatedName + fileSuffix;
                            //System.out.println(" -> "+bundleObfuscatedPath);
                            resourcesMapping.putEntryFileMapping(bundleRawPath, bundleObfuscatedPath);
                        }
                    }
                    if (obfuscateEntries.values().contains(bundleObfuscatedPath)) {
                        throw new IllegalArgumentException(
                                String.format("Multiple entries with same key: %s -> %s",
                                        bundleRawPath, bundleObfuscatedPath)
                        );
                    }
                    obfuscateEntries.put(bundleRawPath, bundleObfuscatedPath);
                    typeMappingMap.put(obfuscateDir, mapping);
                });
        return obfuscateEntries;
    }

    /**
     * obfuscate bundle module.
     * 1. obfuscate bundle entries.
     * 2. obfuscate resourceTable.
     */
    private BundleModule obfuscateBundleModule(BundleModule bundleModule, Map<String, String> obfuscatedEntryMap) throws IOException {
        BundleModule.Builder builder = bundleModule.toBuilder();

        // obfuscate module entries
        List<ModuleEntry> obfuscateEntries = new ArrayList<>();
        for (ModuleEntry entry : bundleModule.getEntries()) {
            String bundleRawPath = bundleModule.getName().getName() + "/" + entry.getPath().toString();
            String obfuscatedPath = obfuscatedEntryMap.get(bundleRawPath);
            if (obfuscatedPath != null) {
                ModuleEntry obfuscatedEntry = InMemoryModuleEntry.ofFile(obfuscatedPath, AppBundleUtils.readByte(bundleZipFile, entry, bundleModule));
                obfuscateEntries.add(obfuscatedEntry);
            } else {
                obfuscateEntries.add(entry);
            }
        }
        builder.setRawEntries(obfuscateEntries);

        // obfuscate resourceTable
        Resources.ResourceTable obfuscatedResTable = obfuscateResourceTable(bundleModule, obfuscatedEntryMap);
        if (obfuscatedResTable != null) {
            builder.setResourceTable(obfuscatedResTable);
        }
        return builder.build();
    }

    /**
     * Obfuscate resourceTable.
     */
    private Resources.ResourceTable obfuscateResourceTable(BundleModule bundleModule, Map<String, String> obfuscatedEntryMap) {
        if (!bundleModule.getResourceTable().isPresent()) {
            return null;
        }
        Resources.ResourceTable resourceTable = bundleModule.getResourceTable().get();

        ResourcesTableBuilder resourcesTableBuilder = new ResourcesTableBuilder();
        ResourcesUtils.entries(resourceTable).map(entry -> {
            String resourceName = AppBundleUtils.getResourceFullName(entry);
            String resourceId = entry.getResourceId().toString();
            String obfuscatedResName = resourcesMapping.getResourceMapping().get(resourceName);
            resourcesMapping.addResourceNameAndId(resourceName, resourceId);

            Resources.Entry obfuscatedEntry = entry.getEntry();
            if (obfuscatedResName != null) {
                // update entry name
                String entryName = getEntryNameByResourceName(obfuscatedResName);
                obfuscatedEntry = ResourcesTableOperation.updateEntryName(obfuscatedEntry, entryName);
            }

            // update config values
            List<Resources.ConfigValue> configValues = Stream.of(obfuscatedEntry)
                    .map(Resources.Entry::getConfigValueList)
                    .flatMap(Collection::stream)
                    .map(configValue -> {
                        if (!configValue.getValue().getItem().hasFile()) {
                            return configValue;
                        }
                        String rawPath = configValue.getValue().getItem().getFile().getPath();
                        String bundleRawPath = bundleModule.getName().getName() + "/" + rawPath;
                        String obfuscatedPath = obfuscatedEntryMap.get(bundleRawPath);
                        if (obfuscatedPath != null) {
                            resourcesMapping.addResourcePathAndId(bundleRawPath, resourceId);
                            resourcesMapping.putEntryFileMapping(bundleRawPath, obfuscatedPath);
                            return ResourcesTableOperation.replaceEntryPath(configValue, obfuscatedPath);
                        }
                        return configValue;
                    })
                    .collect(Collectors.toList());
            if (configValues.size() > 0) {
                obfuscatedEntry = updateEntryConfigValueList(obfuscatedEntry, configValues);
            }

            return ResourceTableEntry.create(entry.getPackage(), entry.getType(), obfuscatedEntry);
        }).forEach(entry -> {
            checkConfiguration(entry.getEntry());
            resourcesTableBuilder.addPackage(entry.getPackage()).addResource(entry.getType(), entry.getEntry());
        });

        return resourcesTableBuilder.build();
    }

    private void checkResMappingRules() {
        resourcesMapping.getDirMapping().values().stream()
                .map(ZipPath::create)
                .forEach(path -> {
                    if (!path.startsWith(BundleModule.RESOURCES_DIRECTORY)) {
                        throw new IllegalArgumentException(String.format(
                                "Module files can be only in pre-defined directories, the mapping obfuscation rule is %s",
                                path
                        ));
                    }
                });
    }

    private boolean shouldBeObfuscated(String resourceName) {
        // android system resources should not be obfuscated
        if (resourceName.startsWith(RESOURCE_ANDROID_PREFIX)) {
            return false;
        }
        for (String rule : whiteListRules) {
            Pattern filterPattern = Pattern.compile(Utils.convertToPatternString(rule));
            if (filterPattern.matcher(resourceName).matches()) {
                return false;
            }
        }
        return true;
    }
}
