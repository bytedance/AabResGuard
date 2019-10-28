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
import com.bytedance.android.aabresguard.model.xml.FileFilterConfig;
import com.bytedance.android.aabresguard.parser.FileFilterXmlParser;
import com.bytedance.android.aabresguard.utils.FileOperation;
import com.bytedance.android.aabresguard.utils.TimeClock;
import com.google.auto.value.AutoValue;

import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.bytedance.android.aabresguard.utils.FileOperation.getNetFileSizeDescription;
import static com.bytedance.android.aabresguard.utils.exception.CommandExceptionPreconditions.checkFlagPresent;

/**
 * Created by YangJing on 2019/10/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
@AutoValue
public abstract class FileFilterCommand {
    public static final String COMMAND_NAME = "filter-file";
    private static final Logger logger = Logger.getLogger(FileFilterCommand.class.getName());

    private static final Flag<Path> BUNDLE_LOCATION_FLAG = Flag.path("bundle");
    private static final Flag<Path> OUTPUT_FLAG = Flag.path("output");
    private static final Flag<Path> CONFIG_LOCATION_FLAG = Flag.path("config");

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
                                .setShortDescription("Filter some files from an bundle file and update the pb if necessary.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(BUNDLE_LOCATION_FLAG.getName())
                                .setExampleValue("app.aab")
                                .setDescription("Path of the Android App Bundle to filter files from.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(OUTPUT_FLAG.getName())
                                .setExampleValue("filtered.aab")
                                .setDescription("Path to where the file should be created after files are filtered.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(CONFIG_LOCATION_FLAG.getName())
                                .setExampleValue("config.xml")
                                .setDescription("Path of the config file.")
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
        return new AutoValue_FileFilterCommand.Builder();
    }

    public static FileFilterCommand fromFlags(ParsedFlags flags) throws DocumentException {
        Builder builder = builder();
        builder.setBundlePath(BUNDLE_LOCATION_FLAG.getRequiredValue(flags));
        builder.setOutputPath(OUTPUT_FLAG.getRequiredValue(flags));

        // parse config
        Optional<Path> configOptional = CONFIG_LOCATION_FLAG.getValue(flags);
        if (configOptional.isPresent()) {
            Path configPath = configOptional.get();
            if (!configPath.toFile().getName().endsWith(".xml")) {
                throw CommandExecutionException.builder()
                        .withMessage("Wrong properties: %s must end with '.xml'.",
                                CONFIG_LOCATION_FLAG)
                        .build();
            }
            FileFilterXmlParser parser = new FileFilterXmlParser(configPath);
            FileFilterConfig fileFilter = parser.parse();
            if (!fileFilter.isActive()) {
                throw CommandExecutionException.builder()
                        .withMessage("parser attribute filter#isactive can not be 'false' in %s command",
                                COMMAND_NAME)
                        .build();
            }
            builder.setFileFilterRules(fileFilter.getRules());
        }

        DISABLE_SIGN_FLAG.getValue(flags).ifPresent(builder::setDisableSign);
        STORE_FILE_FLAG.getValue(flags).ifPresent(builder::setStoreFile);
        STORE_PASSWORD_FLAG.getValue(flags).ifPresent(builder::setStorePassword);
        KEY_ALIAS_FLAG.getValue(flags).ifPresent(builder::setKeyAlias);
        KEY_PASSWORD_FLAG.getValue(flags).ifPresent(builder::setKeyPassword);
        return builder.build();
    }

    public Path execute() throws IOException, DocumentException, InterruptedException {
        TimeClock timeClock = new TimeClock();

        AppBundle appBundle = new AppBundleAnalyzer(getBundlePath()).analyze();
        // filter bundle files
        BundleFileFilter filter = new BundleFileFilter(getBundlePath(), appBundle, getFileFilterRules());
        AppBundle filteredAppBundle = filter.filter();
        // package bundle
        AppBundlePackager packager = new AppBundlePackager(filteredAppBundle, getOutputPath());
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
                "filter bundle files done, coast %s\n" +
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

    public abstract Path getBundlePath();

    public abstract Path getOutputPath();

    public abstract Set<String> getFileFilterRules();

    public abstract Optional<Boolean> getDisableSign();

    public abstract Optional<Path> getStoreFile();

    public abstract Optional<String> getStorePassword();

    public abstract Optional<String> getKeyAlias();

    public abstract Optional<String> getKeyPassword();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setBundlePath(Path bundlePath);

        public abstract Builder setOutputPath(Path outputPath);

        public abstract Builder setFileFilterRules(Set<String> fileFilterRules);

        public abstract Builder setDisableSign(Boolean disableSign);

        public abstract Builder setStoreFile(Path storeFile);

        public abstract Builder setStorePassword(String storePassword);

        public abstract Builder setKeyAlias(String keyAlias);

        public abstract Builder setKeyPassword(String keyPassword);

        abstract FileFilterCommand autoBuild();

        public FileFilterCommand build() {
            FileFilterCommand command = autoBuild();
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
                                OUTPUT_FLAG)
                        .build();
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
