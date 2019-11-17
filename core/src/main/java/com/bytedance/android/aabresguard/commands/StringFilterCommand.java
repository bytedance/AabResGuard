package com.bytedance.android.aabresguard.commands;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.flags.ParsedFlags;
import com.android.tools.build.bundletool.model.AppBundle;
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException;
import com.bytedance.android.aabresguard.android.JarSigner;
import com.bytedance.android.aabresguard.bundle.AppBundleAnalyzer;
import com.bytedance.android.aabresguard.bundle.AppBundlePackager;
import com.bytedance.android.aabresguard.bundle.AppBundleSigner;
import com.bytedance.android.aabresguard.executors.BundleStringFilter;
import com.bytedance.android.aabresguard.model.xml.StringFilterConfig;
import com.bytedance.android.aabresguard.parser.StringFilterXmlParser;
import com.bytedance.android.aabresguard.utils.FileOperation;
import com.bytedance.android.aabresguard.utils.TimeClock;
import com.google.auto.value.AutoValue;

import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileDoesNotExist;
import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;
import static com.bytedance.android.aabresguard.utils.FileOperation.getNetFileSizeDescription;
import static com.bytedance.android.aabresguard.utils.exception.CommandExceptionPreconditions.checkFlagPresent;

/**
 * Created by jiangzilai on 2019-10-20.
 */
@AutoValue
public abstract class StringFilterCommand {
    public static final String COMMAND_NAME = "filter-string";
    private static final Logger logger = Logger.getLogger(StringFilterCommand.class.getName());

    private static final Flag<Path> BUNDLE_LOCATION_FLAG = Flag.path("bundle");
    private static final Flag<Path> OUTPUT_FLAG = Flag.path("output");
    private static final Flag<Path> CONFIG_LOCATION_FLAG = Flag.path("config");
    private static final Flag<Path> STORE_FILE_FLAG = Flag.path("storeFile");
    private static final Flag<String> STORE_PASSWORD_FLAG = Flag.string("storePassword");
    private static final Flag<String> KEY_ALIAS_FLAG = Flag.string("keyAlias");
    private static final Flag<String> KEY_PASSWORD_FLAG = Flag.string("keyPassword");

    public static CommandHelp help() {
        return CommandHelp.builder()
                .setCommandName(COMMAND_NAME)
                .setCommandDescription(
                        CommandHelp.CommandDescription.builder()
                                .setShortDescription("Filter some strings from an bundle file and update the pb if necessary.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(BUNDLE_LOCATION_FLAG.getName())
                                .setExampleValue("app.aab")
                                .setDescription("Path of the Android App Bundle to filter strings from.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(OUTPUT_FLAG.getName())
                                .setExampleValue("filtered.aab")
                                .setDescription("Path to where the file should be created after strings are filtered.")
                                .build())
                .addFlag(
                        CommandHelp.FlagDescription.builder()
                                .setFlagName(CONFIG_LOCATION_FLAG.getName())
                                .setExampleValue("config.xml")
                                .setDescription("Path of the config file.")
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

    public static StringFilterCommand.Builder builder() {
        return new AutoValue_StringFilterCommand.Builder();
    }

    public static StringFilterCommand fromFlags(ParsedFlags flags) {
        StringFilterCommand.Builder builder = builder();
        builder.setBundlePath(BUNDLE_LOCATION_FLAG.getRequiredValue(flags));
        builder.setConfigPath(CONFIG_LOCATION_FLAG.getRequiredValue(flags));
        builder.setOutputPath(OUTPUT_FLAG.getRequiredValue(flags));

        STORE_FILE_FLAG.getValue(flags).ifPresent(builder::setStoreFile);
        STORE_PASSWORD_FLAG.getValue(flags).ifPresent(builder::setStorePassword);
        KEY_ALIAS_FLAG.getValue(flags).ifPresent(builder::setKeyAlias);
        KEY_PASSWORD_FLAG.getValue(flags).ifPresent(builder::setKeyPassword);
        return builder.build();
    }

    public Path execute() throws IOException, DocumentException, InterruptedException {
        TimeClock timeClock = new TimeClock();

        AppBundle appBundle = new AppBundleAnalyzer(getBundlePath()).analyze();
        // parse config.xml
        StringFilterXmlParser parser = new StringFilterXmlParser(getConfigPath());
        StringFilterConfig config = parser.parse();
        if (!config.isActive()) {
            throw CommandExecutionException.builder()
                    .withMessage("parser attribute filter#isactive can not be 'false' in %s command",
                            COMMAND_NAME)
                    .build();
        }
        // filter bundle strings
        BundleStringFilter filter =
                new BundleStringFilter(getBundlePath(), appBundle, config.getPath(), config.getLanguageWhiteList());
        AppBundle filteredAppBundle = filter.filter();
        // package bundle
        AppBundlePackager packager = new AppBundlePackager(filteredAppBundle, getOutputPath());
        packager.execute();
        // sign bundle
        AppBundleSigner signer = new AppBundleSigner(getOutputPath());
        getStoreFile().ifPresent(storeFile -> {
            signer.setBundleSignature(new JarSigner.Signature(
                    storeFile, getStorePassword().get(), getKeyAlias().get(), getKeyPassword().get()
            ));
        });
        signer.execute();

        long rawSize = FileOperation.getFileSizes(getBundlePath().toFile());
        long filteredSize = FileOperation.getFileSizes(getOutputPath().toFile());
        System.out.println(String.format(
                "filter bundle strings done, coast %s\n" +
                        "-----------------------------------------\n" +
                        "Reduce bundle string size: %s, %s -> %s\n" +
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

    public abstract Path getConfigPath();

    public abstract Optional<Path> getStoreFile();

    public abstract Optional<String> getStorePassword();

    public abstract Optional<String> getKeyAlias();

    public abstract Optional<String> getKeyPassword();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract StringFilterCommand.Builder setBundlePath(Path bundlePath);

        public abstract StringFilterCommand.Builder setOutputPath(Path outputPath);

        public abstract StringFilterCommand.Builder setConfigPath(Path configPath);

        public abstract StringFilterCommand.Builder setStoreFile(Path storeFile);

        public abstract StringFilterCommand.Builder setStorePassword(String storePassword);

        public abstract StringFilterCommand.Builder setKeyAlias(String keyAlias);

        public abstract StringFilterCommand.Builder setKeyPassword(String keyPassword);

        abstract StringFilterCommand autoBuild();

        public StringFilterCommand build() {
            StringFilterCommand command = autoBuild();
            checkFileExistsAndReadable(command.getBundlePath());
            checkFileExistsAndReadable(command.getConfigPath());
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
            if (!command.getConfigPath().toFile().getName().endsWith(".xml")) {
                throw CommandExecutionException.builder()
                        .withMessage("Wrong properties: %s must end with '.xml'.",
                                CONFIG_LOCATION_FLAG)
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
