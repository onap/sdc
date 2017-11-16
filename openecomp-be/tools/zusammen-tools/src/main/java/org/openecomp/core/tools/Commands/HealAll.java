package org.openecomp.core.tools.Commands;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.tools.concurrent.ItemHealingTask;
import org.openecomp.core.tools.loaders.VersionInfoCassandraLoader;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
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

  private static final String HEALING_USER = "healing_user";
  private static final int defaulThreadNumber =100;
  private static BufferedWriter log;
  private static List<ItemHealingTask> tasks = new ArrayList<>();
  private static VendorSoftwareProductManager vspManager = VspManagerFactory
      .getInstance().createInterface();
  private static VersioningManager versioningManager = VersioningManagerFactory.getInstance()
      .createInterface();

  static {
    try {
      log =
          new BufferedWriter(new FileWriter("healing.log",true));
    } catch (IOException e) {
      if (log != null) {
        try {
          log.close();
        } catch (IOException e1) {
          throw new RuntimeException("can't initial healing log file: " + e1.getMessage());
        }
      }
      throw new RuntimeException("can't initial healing log file: " + e.getMessage());
    }
  }

  public static void healAll(String threadNumber) {

    writeToLog("----starting healing------");
    Instant startTime = Instant.now();

    int numberOfThreads = Objects.nonNull(threadNumber) ? Integer.valueOf(threadNumber) :
        defaulThreadNumber;
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

   filterByEntityType(VersionInfoCassandraLoader.list(),
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE).forEach
       (HealAll::addTaskToTasks);

    executeAllTasks(executor);

    writeToLog("----finished healing------" );
    Instant endTime = Instant.now();
    writeToLog("Total runtime was: " + Duration.between(startTime, endTime));

    try {
      if (log != null) {
        log.close();
      }
    } catch (IOException e) {
      writeToLog("Error:" + e.getMessage());
    }

    System.exit(1);
  }

  private static void executeAllTasks(ExecutorService executor) {
    List<Future<String>> futureTasks;
    try {
      futureTasks = executor.invokeAll(tasks);
      futureTasks.forEach(future -> {
        try {
          log.write(future.get());
          log.newLine();
        } catch (Exception e) {
          writeToLog(e.getMessage());
        }
      });
    } catch (InterruptedException e) {
      writeToLog("migration tasks failed with message: " + e.getMessage());
      throw new RuntimeException(e);
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
     return versionInfoEntity.getViewableVersions().stream().max(Version::compateTo).get();
    }
    return versionInfoEntity.getActiveVersion();
  }

  private static void writeToLog(String message) {
    try {
      log.write(message);
      log.newLine();
    } catch (IOException e) {
      throw new RuntimeException("unable to write to healing all log file.");
    }
  }

  private static Stream<VersionInfoEntity> filterByEntityType(Collection<VersionInfoEntity>
                                                             versionInfoEntities,
                                                     String entityType){
    return versionInfoEntities.stream().filter(versionInfoEntity -> versionInfoEntity
        .getEntityType().equals(entityType));
  }
  private static void addTaskToTasks(VersionInfoEntity versionInfoEntity){
    tasks.add(new ItemHealingTask(versionInfoEntity.getEntityId(), resolveVersion
            (versionInfoEntity).toString(), versionInfoEntity.getCandidate() == null ?
            HEALING_USER : versionInfoEntity.getCandidate().getUser(),
            vspManager, versioningManager));
  }

}
