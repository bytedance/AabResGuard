package com.bytedance.android.aabresguard;


import com.android.tools.build.bundletool.flags.FlagParser;
import com.android.tools.build.bundletool.flags.ParsedFlags;
import com.bytedance.android.aabresguard.commands.CommandHelp;
import com.bytedance.android.aabresguard.commands.DuplicatedResourcesMergerCommand;
import com.bytedance.android.aabresguard.commands.FileFilterCommand;
import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand;
import com.bytedance.android.aabresguard.commands.StringFilterCommand;
import com.bytedance.android.aabresguard.model.version.AabResGuardVersion;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

/**
 * Main entry point of the AabResGuard.
 * <p>
 * Created by YangJing on 2019/10/09 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AabResGuardMain {
    private static final String HELP_CMD = "help";

    public static void main(String[] args) {
        main(args, Runtime.getRuntime());
    }

    /**
     * Parses the flags and routes to the appropriate commands handler.
     */
    private static void main(String[] args, Runtime runtime) {
        final ParsedFlags flags;
        try {
            flags = new FlagParser().parse(args);
        } catch (FlagParser.FlagParseException e) {
            System.err.println("Error while parsing the flags: " + e.getMessage());
            runtime.exit(1);
            return;
        }
        Optional<String> command = flags.getMainCommand();
        if (!command.isPresent()) {
            System.err.println("Error: You have to specify a commands.");
            help();
            runtime.exit(1);
            return;
        }
        try {
            switch (command.get()) {
                case ObfuscateBundleCommand.COMMAND_NAME:
                    ObfuscateBundleCommand.fromFlags(flags).execute();
                    break;
                case DuplicatedResourcesMergerCommand.COMMAND_NAME:
                    DuplicatedResourcesMergerCommand.fromFlags(flags).execute();
                    break;
                case FileFilterCommand.COMMAND_NAME:
                    FileFilterCommand.fromFlags(flags).execute();
                    break;
                case StringFilterCommand.COMMAND_NAME:
                    StringFilterCommand.fromFlags(flags).execute();
                    break;
                case HELP_CMD:
                    if (flags.getSubCommand().isPresent()) {
                        help(flags.getSubCommand().get(), runtime);
                    } else {
                        help();
                    }
                    break;
                default:
                    System.err.printf("Error: Unrecognized command '%s'.%n%n%n", command.get());
                    help();
                    runtime.exit(1);
                    return;
            }
        } catch (Exception e) {
            System.err.println(
                    "[BT:" + AabResGuardVersion.getCurrentVersion() + "] Error: " + e.getMessage());
            e.printStackTrace();
            runtime.exit(1);
            return;
        }
        runtime.exit(0);
    }

    /**
     * Displays a general help.
     */
    public static void help() {
        ImmutableList<CommandHelp> commandHelps =
                ImmutableList.of(
                        ObfuscateBundleCommand.help(),
                        DuplicatedResourcesMergerCommand.help(),
                        FileFilterCommand.help(),
                        StringFilterCommand.help()
                );
        System.out.println("Synopsis: aabResGuard <command> ...");
        System.out.println();
        System.out.println("Use 'aabResGuard help <command>' to learn more about the given command.");
        System.out.println();
        commandHelps.forEach(commandHelp -> commandHelp.printSummary(System.out));
    }

    /**
     * Displays help about a given commands.
     */
    public static void help(String commandName, Runtime runtime) {
        CommandHelp commandHelp;
        switch (commandName) {
            case ObfuscateBundleCommand.COMMAND_NAME:
                commandHelp = ObfuscateBundleCommand.help();
                break;
            case DuplicatedResourcesMergerCommand.COMMAND_NAME:
                commandHelp = DuplicatedResourcesMergerCommand.help();
                break;
            case FileFilterCommand.COMMAND_NAME:
                commandHelp = FileFilterCommand.help();
                break;
                case StringFilterCommand.COMMAND_NAME:
                commandHelp = StringFilterCommand.help();
                break;
            default:
                System.err.printf("Error: Unrecognized command '%s'.%n%n%n", commandName);
                help();
                runtime.exit(1);
                return;
        }
        commandHelp.printDetails(System.out);
    }
}
