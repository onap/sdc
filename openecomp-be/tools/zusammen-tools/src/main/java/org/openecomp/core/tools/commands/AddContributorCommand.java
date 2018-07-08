package org.openecomp.core.tools.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.openecomp.core.tools.concurrent.ItemAddContributorsTask;
import org.openecomp.core.tools.exceptions.CommandExecutionRuntimeException;
import org.openecomp.core.tools.store.ItemHandler;
import org.openecomp.core.tools.store.NotificationHandler;
import org.openecomp.core.tools.store.PermissionHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class AddContributorCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddContributorCommand.class);
    private static final String ITEMS_PATH_OPTION = "p";
    private static final String UUSERS_PATH_OPTION = "u";
    private static final int DEFAULT_THREAD_NUMBER = 8;
    private static final String ERROR_TRYING_TO_READ_FILE = "Error while trying to read item list";
    private static final String COMMAND_ADD_CONTRIBUTOR_FAILED = "Command AddContributor execution failed.";

    AddContributorCommand() {
        options.addOption(Option.builder(ITEMS_PATH_OPTION).hasArg().argName("file")
                                .desc("file containing list of item ids, mandatory").build());
        options.addOption(Option.builder(UUSERS_PATH_OPTION).hasArg().argName("file")
                                .desc("file containing list of users, mandatory").build());
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);

        if (!cmd.hasOption(ITEMS_PATH_OPTION) || !cmd.hasOption(UUSERS_PATH_OPTION)) {
            LOGGER.error("Arguments p and u are mandatory");
            return false;
        }

        String itemListPath = cmd.getOptionValue(ITEMS_PATH_OPTION);
        String userListPath = cmd.getOptionValue(UUSERS_PATH_OPTION);

        List<String> itemList;
        try {
            itemList = getItemList(itemListPath);
        } catch (IOException e) {
            throw new CommandExecutionRuntimeException(ERROR_TRYING_TO_READ_FILE + "from:" + itemListPath, e);
        }
        List<String> userList;
        try {
            userList = load(userListPath).collect(Collectors.toList());
        } catch (IOException e) {
            throw new CommandExecutionRuntimeException(ERROR_TRYING_TO_READ_FILE + "from:" + userListPath, e);
        }

        List<ItemAddContributorsTask> tasks =
                itemList.stream().map(itemid -> createTask(itemid, userList)).collect(Collectors.toList());

        ExecutorService executor = null;

        try {
            executor = Executors.newFixedThreadPool(DEFAULT_THREAD_NUMBER);
            executeAllTasks(executor, tasks);
        } catch (InterruptedException e) {
            throw new CommandExecutionRuntimeException(COMMAND_ADD_CONTRIBUTOR_FAILED, e);
        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return CommandName.ADD_CONTRIBUTOR;
    }

    private static List<String> getItemList(String itemListPath) throws IOException {
        List<String> itemList;
        if (itemListPath != null) {
            itemList = load(itemListPath).collect(Collectors.toList());
        } else {
            itemList = new ItemHandler().getItemList();
        }

        return itemList;
    }

    private static void executeAllTasks(ExecutorService executor, Collection<? extends Callable<String>> tasks)
            throws InterruptedException {
        List<Future<String>> futureTasks;
        futureTasks = executor.invokeAll(tasks);
        boolean isThreadOpen = true;
        while (isThreadOpen) {
            isThreadOpen = futureTasks.stream().anyMatch(future -> !future.isDone());

        }
    }


    private static ItemAddContributorsTask createTask(String itemId, List<String> users) {
        return new ItemAddContributorsTask(new PermissionHandler(), new NotificationHandler(), itemId, users);
    }

    private static Stream<String> load(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath));

    }


}
