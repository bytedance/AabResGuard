package com.bytedance.android.aabresguard.executors;

import com.android.aapt.Resources;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.BundleModule;
import com.android.tools.build.bundletool.model.BundleModuleName;
import com.android.tools.build.bundletool.model.ModuleEntry;
import com.android.tools.build.bundletool.model.ZipPath;
import com.android.tools.build.bundletool.model.utils.ResourcesUtils;
import com.bytedance.android.aabresguard.bundle.AppBundleUtils;
import com.bytedance.android.aabresguard.bundle.ResourcesTableBuilder;
import com.bytedance.android.aabresguard.bundle.ResourcesTableOperation;
import com.bytedance.android.aabresguard.utils.TimeClock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.bytedance.android.aabresguard.utils.FileOperation.getNetFileSizeDescription;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Created by YangJing on 2019/10/10 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class DuplicatedResourcesMerger {
    public static final String SUFFIX_FILE_DUPLICATED_LOGGER = "-duplicated.txt";
    private static final Logger logger = Logger.getLogger(DuplicatedResourcesMerger.class.getName());
    private final Path outputLogLocationDir;
    private final ZipFile bundleZipFile;
    private final AppBundle rawAppBundle;

    private Map<String, ZipPath> md5FileList = new HashMap<>();
    private Map<ZipPath, String> duplicatedFileList = new HashMap<>();
    private int mergeDuplicatedTotalSize = 0;
    private int mergeDuplicatedTotalCount = 0;

    public DuplicatedResourcesMerger(Path bundlePath, AppBundle appBundle, Path outputLogLocationDir) throws IOException {
        checkFileExistsAndReadable(bundlePath);
        this.outputLogLocationDir = outputLogLocationDir;

        bundleZipFile = new ZipFile(bundlePath.toFile());
        rawAppBundle = appBundle;
    }

    @SuppressWarnings("UnstableApiUsage")
    public AppBundle merge() throws IOException {
        TimeClock timeClock = new TimeClock();

        List<BundleModule> mergedBundleModuleList = new ArrayList<>();
        for (Map.Entry<BundleModuleName, BundleModule> moduleEntry : rawAppBundle.getModules().entrySet()) {
            mergedBundleModuleList.add(mergeBundleModule(moduleEntry.getValue()));
        }
        AppBundle mergedAppBundle = AppBundle.buildFromModules(
                mergedBundleModuleList.stream().collect(toImmutableList()),
                rawAppBundle.getBundleConfig(),
                rawAppBundle.getBundleMetadata()
        );

        System.out.println(String.format(
                "merge duplicated resources done, coast %s\n" +
                        "-----------------------------------------\n" +
                        "Reduce file count: %s\n" +
                        "Reduce file size: %s\n" +
                        "-----------------------------------------",
                timeClock.getCoast(),
                mergeDuplicatedTotalCount,
                getNetFileSizeDescription(mergeDuplicatedTotalSize)
        ));
        return mergedAppBundle;
    }

    /**
     * merge duplicated resources.
     */
    private BundleModule mergeBundleModule(BundleModule bundleModule) throws IOException {
        File logFile = new File(outputLogLocationDir.toFile(), bundleModule.getName().getName() + SUFFIX_FILE_DUPLICATED_LOGGER);
        checkFileDoesNotExist(logFile.toPath());

        Resources.ResourceTable table = bundleModule.getResourceTable().orElse(Resources.ResourceTable.getDefaultInstance());
        if (table.getPackageList().isEmpty() || bundleModule.getEntries().isEmpty()) {
            return bundleModule;
        }

        md5FileList.clear();
        duplicatedFileList.clear();

        List<ModuleEntry> mergedModuleEntry = new ArrayList<>();
        for (ModuleEntry entry : bundleModule.getEntries()) {
            if (!entry.getPath().startsWith(BundleModule.RESOURCES_DIRECTORY)) {
                mergedModuleEntry.add(entry);
                continue;
            }
            String md5 = AppBundleUtils.getEntryMd5(bundleZipFile, entry, bundleModule);
            if (md5FileList.containsKey(md5)) {
                duplicatedFileList.put(entry.getPath(), md5);
            } else {
                md5FileList.put(md5, entry.getPath());
                mergedModuleEntry.add(entry);
            }
        }
        generateDuplicatedLog(logFile, bundleModule);

        Resources.ResourceTable mergedTable = mergeResourcesTable(table);
        return bundleModule.toBuilder()
                .setResourceTable(mergedTable)
                .setRawEntries(mergedModuleEntry)
                .build();
    }

    /**
     * merge resourcesTable, remove duplicated resources.
     */
    private Resources.ResourceTable mergeResourcesTable(Resources.ResourceTable resourceTable) {
        ResourcesTableBuilder resourcesTableBuilder = new ResourcesTableBuilder();
        ResourcesUtils.entries(resourceTable).forEach(entry -> {
            ResourcesTableBuilder.PackageBuilder packageBuilder = resourcesTableBuilder.addPackage(entry.getPackage());
            // replace the duplicated path
            List<Resources.ConfigValue> configValues = getDuplicatedMergedConfigValues(entry.getEntry());
            Resources.Entry mergedEntry = ResourcesTableOperation.updateEntryConfigValueList(entry.getEntry(), configValues);
            packageBuilder.addResource(entry.getType(), mergedEntry);
        });
        return resourcesTableBuilder.build();
    }

    private List<Resources.ConfigValue> getDuplicatedMergedConfigValues(Resources.Entry entry) {
        return Stream.of(entry.getConfigValueList())
                .flatMap(Collection::stream)
                .map(configValue -> {
                    if (!configValue.getValue().getItem().hasFile()) {
                        return configValue;
                    }
                    ZipPath zipPath = ZipPath.create(configValue.getValue().getItem().getFile().getPath());
                    if (duplicatedFileList.containsKey(zipPath)) {
                        zipPath = md5FileList.get(duplicatedFileList.get(zipPath));
                    }
                    return ResourcesTableOperation.replaceEntryPath(configValue, zipPath.toString());
                }).collect(Collectors.toList());
    }

    private void generateDuplicatedLog(File logFile, BundleModule bundleModule) throws IOException {
        int duplicatedSize = 0;
        checkFileDoesNotExist(logFile.toPath());
        Writer writer = new BufferedWriter(new FileWriter(logFile, false));
        writer.write("res filter path mapping:\n");
        writer.flush();
        for (Map.Entry<ZipPath, String> entry : duplicatedFileList.entrySet()) {
            ZipPath keepPath = md5FileList.get(entry.getValue());
            System.out.println(String.format("[merge duplicated] found duplicated file, path: %s", bundleModule.getName().getName() + "/" + entry.getKey().toString()));
            ModuleEntry moduleEntry = bundleModule.getEntry(entry.getKey()).get();
            long fileSize = AppBundleUtils.getZipEntrySize(bundleZipFile, moduleEntry, bundleModule);
            duplicatedSize += fileSize;
            writer.write(
                    "\t" + entry.getKey().toString()
                            + " -> "
                            + keepPath.toString()
                            + " (size " + getNetFileSizeDescription(fileSize) + ")"
                            + "\n"
            );
        }
        writer.write(
                "removed: count(" + duplicatedFileList.size() + "), totalSize("
                        + getNetFileSizeDescription(duplicatedSize) + ")"
        );
        writer.close();
        System.out.println(String.format(
                "[merge duplicated] duplicated count %s, total size: %s",
                String.valueOf(duplicatedFileList.size()),
                getNetFileSizeDescription(duplicatedSize)
        ));
        mergeDuplicatedTotalSize += duplicatedSize;
        mergeDuplicatedTotalCount += duplicatedFileList.size();
    }
}
