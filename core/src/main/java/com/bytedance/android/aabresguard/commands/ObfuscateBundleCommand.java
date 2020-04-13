package com.bytedance.android.aabresguard.commands;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.flags.ParsedFlags;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException;
import com.bytedance.android.aabresguard.android.JarSigner;
import com.bytedance.android.aabresguard.bundle.AppBundleAnalyzer;
import com.bytedance.android.aabresguard.bundle.AppBundlePackager;
import com.bytedance.android.aabresguard.bundle.AppBundleSigner;
import com.bytedance.android.aabresguard.executors.BundleFileFilter;
import com.bytedance.android.aabresguard.executors.BundleStringFilter;
import com.bytedance.android.aabresguard.executors.DuplicatedResourcesMerger;
import com.bytedance.android.aabresguard.executors.ResourcesObfuscator;
import com.bytedance.android.aabresguard.model.xml.AabResGuardConfig;
import com.bytedance.android.aabresguard.parser.AabResGuardXmlParser;
import com.bytedance.android.aabresguard.utils.FileOperation;
import com.bytedance.android.aabresguard.utils.TimeClock;
import com.google.auto.value.AutoValue;

import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.bytedance.android.aabresguard.utils.FileOperation.getNetFileSizeDescription;
import static com.bytedance.android.aabresguard.utils.exception.CommandExceptionPreconditions.checkFlagPresent;

/**
 * Command responsible for obfuscate an App Bundle's resources from App Bundle file.
 * <p>
 * Created by YangJing on 2019/10/09 .
 * Email: yangjing.yeoh@bytedance.com
 */
@AutoValue
public abstract class ObfuscateBundleCommand {
    public static final String COMMAND_NAME = "obfuscate-bundle";
    private static final Logger logger = Logger.getLogger(ObfuscateBundleCommand.class.getName());

    private static final Flag<Path> BUNDLE_LOCATION_FLAG = Flag.path("bundle");
    private static final Flag<Path> OUTPUT_FILE_FLAG = Flag.path("output");
    private static final Flag<Path> CONFIG_FLAG = Flag.path("config");
    private static final Flag<Path> MAPPING_FLAG = Flag.path("mapping");

    private static final Flag<Boolean> MERGE_DUPLICATED_RES_FLAG = Flag.booleanFlag("merge-duplicated-res");

    private static final Flag<Boolean> DISABLE_SIGN_FLAG = Flag.booleanFlag("disable-sign");
    private static final Flag<Path> STORE_FILE_FLAG = Flag.path("storeFile");
    private static final Flag<String> STORE_PASSWORD_FLAG = Flag.string("storePassword");
    private static final Flag<String> KEY_ALIAS_FLAG = Flag.string("keyAlias");
    private static final Flag<String> KEY_PASSWORD_FLAG = Flag.string("keyPassword");

    public static CommandHelp help() {
        return CommandHelp.builder()
                .setCommandName(COMMAND_NAME)
                .setCommandDescription(
                        CommandHelp.CommandDescription.builder()
                                .setShortDescription("Obfuscates an bundle file's resources from an bundle file.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(BUNDLE_LOCATION_FLAG.getName())
                                .setExampleValue("bundle.aab")
                                .setDescription("Path of the Android App Bundle to obfuscate resources from.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(OUTPUT_FILE_FLAG.getName())
                                .setExampleValue("obfuscated.aab")
                                .setDescription("Path to where the obfuscate bundle file should be created.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(CONFIG_FLAG.getName())
                                .setExampleValue("config.xml")
                                .setDescription(
                                        "Path of the Obfuscate configuration parser file, priority is lower than the command " +
                                                "line.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(MAPPING_FLAG.getName())
                                .setExampleValue("resources-mapping.txt")
                                .setOptional(true)
                                .setDescription("The mapping file path for resource increment obfuscation.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(MERGE_DUPLICATED_RES_FLAG.getName())
                                .setExampleValue("merge-duplicated-res=true")
                                .setOptional(true)
                                .setDescription("If set, the duplicate resource files will be removed.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(DISABLE_SIGN_FLAG.getName())
                                .setExampleValue("disable-sign=true")
                                .setOptional(true)
                                .setDescription("If set true, the bundle file will not be signed after package.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(STORE_FILE_FLAG.getName())
                                .setExampleValue("store.keystore")
                                .setOptional(true)
                                .setDescription("Path of the keystore file.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(STORE_PASSWORD_FLAG.getName())
                                .setOptional(true)
                                .setDescription("Path of the keystore password.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(KEY_ALIAS_FLAG.getName())
                                .setOptional(true)
                                .setDescription("Path of the key alias name.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(KEY_PASSWORD_FLAG.getName())
                                .setOptional(true)
                                .setDescription("Path of the key password.")
                                .build())
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ObfuscateBundleCommand.Builder();
    }

    public static ObfuscateBundleCommand fromFlags(ParsedFlags flags) throws DocumentException {
        Builder builder = builder();
        builder.setEnableObfuscate(true);
        builder.setBundlePath(BUNDLE_LOCATION_FLAG.getRequiredValue(flags));
        // config
        Path path = CONFIG_FLAG.getRequiredValue(flags);
        AabResGuardConfig config = new AabResGuardXmlParser(path).parse();
        builder.setWhiteList(config.getWhiteList());
        if (config.getFileFilter() != null) {
            builder.setFilterFile(config.getFileFilter().isActive());
            builder.setFileFilterRules(config.getFileFilter().getRules());
        }
        MAPPING_FLAG.getValue(flags).ifPresent(builder::setMappingPath);

        if (config.getStringFilterConfig() != null) {
            builder.setRemoveStr(config.getStringFilterConfig().isActive());
            builder.setUnusedStrPath(config.getStringFilterConfig().getPath());
            builder.setLanguageWhiteList(config.getStringFilterConfig().getLanguageWhiteList());
        }

        builder.setOutputPath(OUTPUT_FILE_FLAG.getRequiredValue(flags));

        MERGE_DUPLICATED_RES_FLAG.getValue(flags).ifPresent(builder::setMergeDuplicatedResources);

        DISABLE_SIGN_FLAG.getValue(flags).ifPresent(builder::setDisableSign);
        STORE_FILE_FLAG.getValue(flags).ifPresent(builder::setStoreFile);
        STORE_PASSWORD_FLAG.getValue(flags).ifPresent(builder::setStorePassword);
        KEY_ALIAS_FLAG.getValue(flags).ifPresent(builder::setKeyAlias);
        KEY_PASSWORD_FLAG.getValue(flags).ifPresent(builder::setKeyPassword);
        return builder.build();
    }

    public Path execute() throws IOException, InterruptedException {
        TimeClock timeClock = new TimeClock();

        AppBundle appBundle = new AppBundleAnalyzer(getBundlePath()).analyze();
        // filter file
        if (getFilterFile().isPresent() && getFilterFile().get()) {
            Set<String> fileFilterRules = new HashSet<>();
            if (getFileFilterRules().isPresent()) {
                fileFilterRules = getFileFilterRules().get();
            }
            BundleFileFilter filter = new BundleFileFilter(getBundlePath(), appBundle, fileFilterRules);
            appBundle = filter.filter();
        }

        // remove unused strings, need execute before obfuscate
        if (getRemoveStr().isPresent() && getRemoveStr().get()) {
            File unusedFile = new File("");
            if (getUnusedStrPath().isPresent()) {
                File file = new File(getUnusedStrPath().get());
                if (file.exists()) {
                    unusedFile = file;
                } else {
                    System.out.println("unusedFile is not exists!");
                }
            }
            Set<String> languageWhiteList = new HashSet<>();
            if (getLanguageWhiteList().isPresent()) {
                languageWhiteList = getLanguageWhiteList().get();
            }
            BundleStringFilter filter =
                    new BundleStringFilter(getBundlePath(), appBundle, unusedFile.getPath(), languageWhiteList);
            appBundle = filter.filter();
        }

        // merge duplicated resources
        if (getMergeDuplicatedResources().isPresent() && getMergeDuplicatedResources().get()) {
            DuplicatedResourcesMerger merger = new DuplicatedResourcesMerger(getBundlePath(), appBundle, getOutputPath().getParent());
            appBundle = merger.merge();
        }
        // obfuscate bundle
        if (getEnableObfuscate()) {
            Path mappingPath = null;
            if (getMappingPath().isPresent()) {
                mappingPath = getMappingPath().get();
            }
            ResourcesObfuscator obfuscator = new ResourcesObfuscator(getBundlePath(), appBundle, getWhiteList(), getOutputPath().getParent(), mappingPath);
            appBundle = obfuscator.obfuscate();
        }
        // package bundle
        AppBundlePackager packager = new AppBundlePackager(appBundle, getOutputPath());
        packager.execute();
        // sign bundle
        if (!getDisableSign().isPresent() || !getDisableSign().get()) {
            AppBundleSigner signer = new AppBundleSigner(getOutputPath());
            getStoreFile().ifPresent(storeFile -> {
                signer.setBundleSignature(new JarSigner.Signature(
                        storeFile, getStorePassword().get(), getKeyAlias().get(), getKeyPassword().get()
                ));
            });
            signer.execute();
        }

        long rawSize = FileOperation.getFileSizes(getBundlePath().toFile());
        long filteredSize = FileOperation.getFileSizes(getOutputPath().toFile());
        System.out.println(String.format(
                "obfuscate resources done, coast %s\n" +
                        "-----------------------------------------\n" +
                        "Reduce bundle file size: %s, %s -> %s\n" +
                        "-----------------------------------------",
                timeClock.getCoast(),
                getNetFileSizeDescription(rawSize - filteredSize),
                getNetFileSizeDescription(rawSize),
                getNetFileSizeDescription(filteredSize)
        ));
        return getOutputPath();
    }

    public abstract Boolean getEnableObfuscate();

    public abstract Path getBundlePath();

    public abstract Path getOutputPath();

    public abstract Optional<Path> getMappingPath();

    public abstract Optional<Path> getStoreFile();

    public abstract Optional<String> getStorePassword();

    public abstract Optional<String> getKeyAlias();

    public abstract Optional<String> getKeyPassword();

    public abstract Optional<Boolean> getMergeDuplicatedResources();

    public abstract Optional<Boolean> getDisableSign();

    public abstract Set<String> getWhiteList();

    public abstract Optional<Set<String>> getFileFilterRules();

    public abstract Optional<Boolean> getFilterFile();

    public abstract Optional<Boolean> getRemoveStr();

    public abstract Optional<String> getUnusedStrPath();

    public abstract Optional<Set<String>> getLanguageWhiteList();


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setEnableObfuscate(Boolean enable);

        public abstract Builder setBundlePath(Path bundlePath);

        public abstract Builder setOutputPath(Path outputPath);

        public abstract Builder setWhiteList(Set<String> whiteList);

        public abstract Builder setRemoveStr(Boolean removeStr);

        public abstract Builder setUnusedStrPath(String unusedStrPath);

        public abstract Builder setLanguageWhiteList(Set<String> countryFilterSet);

        public abstract Builder setFilterFile(Boolean filterFile);

        public abstract Builder setFileFilterRules(Set<String> fileFilterRules);

        public abstract Builder setMappingPath(Path configPath);

        public abstract Builder setMergeDuplicatedResources(Boolean mergeDuplicatedResources);

        public abstract Builder setDisableSign(Boolean disableSign);

        public abstract Builder setStoreFile(Path storeFile);

        public abstract Builder setStorePassword(String storePassword);

        public abstract Builder setKeyAlias(String keyAlias);

        public abstract Builder setKeyPassword(String keyPassword);

        abstract ObfuscateBundleCommand autoBuild();

        public ObfuscateBundleCommand build() {
            ObfuscateBundleCommand command = autoBuild();
            checkFileExistsAndReadable(command.getBundlePath());
            checkFileDoesNotExist(command.getOutputPath());

            if (!command.getBundlePath().toFile().getName().endsWith(".aab")) {
                throw CommandExecutionException.builder()
                        .withMessage("Wrong properties: %s must end with '.aab'.",
                                BUNDLE_LOCATION_FLAG)
                        .build();
            }
            if (!command.getOutputPath().toFile().getName().endsWith(".aab")) {
                throw CommandExecutionException.builder()
                        .withMessage("Wrong properties: %s must end with '.aab'.",
                                OUTPUT_FILE_FLAG)
                        .build();
            }
            if (command.getMappingPath().isPresent()) {
                File file = command.getMappingPath().get().toFile();
                checkFileExistsAndReadable(file.toPath());
                if (!file.getName().endsWith(".txt")) {
                    throw CommandExecutionException.builder()
                            .withMessage("Wrong properties: %s must end with '.txt'.",
                                    MAPPING_FLAG)
                            .build();
                }
            }

            if (command.getStoreFile().isPresent()) {
                checkFlagPresent(command.getKeyAlias(), KEY_ALIAS_FLAG);
                checkFlagPresent(command.getKeyPassword(), KEY_PASSWORD_FLAG);
                checkFlagPresent(command.getStorePassword(), STORE_PASSWORD_FLAG);
            }
            return command;
        }
    }
}
