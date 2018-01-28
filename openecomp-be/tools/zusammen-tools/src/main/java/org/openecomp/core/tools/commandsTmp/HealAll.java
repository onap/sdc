package org.openecomp.core.tools.commandsTmp;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.tools.concurrent.ItemHealingTask;
import org.openecomp.core.tools.exceptions.HealingRuntimeException;
import org.openecomp.core.tools.loaders.VersionInfoCassandraLoader;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Created by ayalaben on 11/6/2017
 */
public class HealAll {

  private static final int DEFAULT_THREAD_NUMBER = 100;
  private static List<ItemHealingTask> tasks = new ArrayList<>();
  private static VendorSoftwareProductManager vspManager = VspManagerFactory
      .getInstance().createInterface();
  private static HealingManager healingManager = HealingManagerFactory.getInstance()
      .createInterface();

  private HealAll() {
  }

  public static void healAll(String threadNumber) {

    String logFileName = "healing.log";
    try (BufferedWriter log = new BufferedWriter(new FileWriter(logFileName, true))) {

      writeToLog("----starting healing------", log);
      Instant startTime = Instant.now();

      int numberOfThreads = Objects.nonNull(threadNumber) ? Integer.valueOf(threadNumber) :
          DEFAULT_THREAD_NUMBER;
      ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

      filterByEntityType(VersionInfoCassandraLoader.list(),
          VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE).forEach
          (HealAll::addTaskToTasks);

      executeAllTasks(executor, log);

      writeToLog("----finished healing------", log);
      Instant endTime = Instant.now();
      writeToLog("Total runtime was: " + Duration.between(startTime, endTime), log);
    } catch (IOException e) {
      throw new HealingRuntimeException("can't initial healing log file '" + logFileName + "'", e);
    }

    System.exit(1);
  }

  private static void executeAllTasks(ExecutorService executor, BufferedWriter log) {
    List<Future<String>> futureTasks;
    try {
      futureTasks = executor.invokeAll(tasks);
      futureTasks.forEach(future -> {
        try {
          log.write(future.get());
          log.newLine();
        } catch (Exception e) {
          writeToLog(e.getMessage(), log);
        }
      });
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      writeToLog("migration tasks failed with message: " + e.getMessage(), log);
      throw new HealingRuntimeException(e);
    }

    boolean isThreadOpen = true;
    while (isThreadOpen) {
      isThreadOpen = futureTasks.stream().anyMatch(future -> !future.isDone());
    }
  }


  private static Version resolveVersion(VersionInfoEntity versionInfoEntity) {
    if (Objects.nonNull(versionInfoEntity.getCandidate())) {
      return versionInfoEntity.getCandidate().getVersion();
    } else if (!CollectionUtils.isEmpty(versionInfoEntity.getViewableVersions())) {

      return versionInfoEntity.getViewableVersions().stream().max(Version::compateTo)
          .orElse(new Version());
    }
    return versionInfoEntity.getActiveVersion();
  }

  private static void writeToLog(String message, BufferedWriter log) {
    try {
      log.write(message);
      log.newLine();
    } catch (IOException e) {
      throw new HealingRuntimeException("unable to write to healing all log file.", e);
    }
  }

  private static Stream<VersionInfoEntity> filterByEntityType(
      Collection<VersionInfoEntity> versionInfoEntities, String entityType) {
    return versionInfoEntities.stream().filter(versionInfoEntity -> versionInfoEntity
        .getEntityType().equals(entityType));
  }

  private static void addTaskToTasks(VersionInfoEntity versionInfoEntity) {
    tasks.add(new ItemHealingTask(versionInfoEntity.getEntityId(), resolveVersion
        (versionInfoEntity).toString(),
        vspManager, healingManager));
  }

}
