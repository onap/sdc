package org.openecomp.core.tools.commands;

import org.openecomp.core.tools.concurrent.ItemAddContributorsTask;
import org.openecomp.core.tools.exceptions.CommandExecutionRuntimeException;
import org.openecomp.core.tools.store.ItemHandler;
import org.openecomp.core.tools.store.NotificationHandler;
import org.openecomp.core.tools.store.PermissionHandler;

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

public class AddContributorCommand {


  private static final int DEFAULT_THREAD_NUMBER = 8;

  private AddContributorCommand() {
    // it's a utility class, prevent instantiation
  }

  public static void add(String itemListPath, String userListPath) {

    List<String> itemList;
    try {
      itemList = getItemList(itemListPath);
    } catch (IOException e) {
      throw new CommandExecutionRuntimeException("Error while trying to read item list " +
          "from:" + itemListPath, e);
    }
    List<String> userList;
    try {
      userList = load(userListPath).collect(Collectors.toList());
    } catch (IOException e) {
      throw new CommandExecutionRuntimeException("Error while trying to read user list " +
          "from:" + userListPath, e);
    }

    List<ItemAddContributorsTask> tasks =
        itemList.stream().map(itemid -> createTask(itemid, userList)).collect(Collectors.toList());

    ExecutorService executor = null;

    try {
      executor = Executors.newFixedThreadPool(DEFAULT_THREAD_NUMBER);
      executeAllTasks(executor, tasks);
    } catch (InterruptedException e) {
      throw new CommandExecutionRuntimeException("Command AddContributor execution failed.", e);
    } finally {
      if (executor != null) {
        executor.shutdownNow();
      }
    }
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

  private static void executeAllTasks(ExecutorService executor,
                                      Collection<? extends Callable<String>> tasks)
      throws InterruptedException {
    List<Future<String>> futureTasks;
    futureTasks = executor.invokeAll(tasks);
    boolean isThreadOpen = true;
    while (isThreadOpen) {
      isThreadOpen = futureTasks.stream().anyMatch(future -> !future.isDone());

    }
  }



  private static ItemAddContributorsTask createTask(String itemId, List<String> users) {
    return new ItemAddContributorsTask(new PermissionHandler(), new NotificationHandler(),
        itemId, users);
  }

  private static Stream<String> load(String filePath)
      throws IOException {
    return Files.lines(Paths.get(filePath));

  }


}
