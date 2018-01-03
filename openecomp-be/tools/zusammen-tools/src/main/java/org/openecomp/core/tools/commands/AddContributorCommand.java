package org.openecomp.core.tools.commands;

import org.openecomp.core.tools.concurrent.ItemAddContributorsTask;
import org.openecomp.core.tools.exceptions.AddContributorRuntimeException;
import org.openecomp.core.tools.loaders.FileLoader;
import org.openecomp.core.tools.store.ItemHandler;
import org.openecomp.core.tools.store.NotificationHandler;
import org.openecomp.core.tools.store.PermissionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AddContributorCommand {
  private static final int DEFAULT_THREAD_NUMBER = 10;
  private static List<ItemAddContributorsTask> tasks = new ArrayList<>();

  private AddContributorCommand(){}

  public static void add(String itemListPath, String userListPath) {

    List<String> itemList = getItemList(itemListPath);
    List<String> userList = load(userListPath, new FileLoader.SimpleListFileLoader());

    itemList.forEach(itemid -> addTask(itemid, userList));

    ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_NUMBER);
    executeAllTasks(executor);
    System.exit(1);
  }

  private static List<String> getItemList(String itemListPath) {
    List<String> itemList;
    if(itemListPath != null){
      itemList = load(itemListPath, new FileLoader.SimpleListFileLoader());
    }else{
      itemList = new ItemHandler().getItemList();
    }

    return itemList;
  }

  private static void executeAllTasks(ExecutorService executor) {
    List<Future<String>> futureTasks;
    try {
      futureTasks = executor.invokeAll(tasks);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AddContributorRuntimeException("unable to add contributors to item");

    }

    boolean isThreadOpen = true;
    while (isThreadOpen) {
      isThreadOpen = futureTasks.stream().anyMatch(future -> !future.isDone());
    }

  }

  private static void addTask(String itemId, List<String> users) {
    tasks.add(new ItemAddContributorsTask(new PermissionHandler(),new NotificationHandler(),
        itemId, users));
  }

  private static List<String> load(String filePath, FileLoader.AbstractFileLoader fileLoader) {
    FileLoader<List<String>> loader = new FileLoader(filePath, fileLoader);
    loader.load();
    return loader.get();
  }


}
