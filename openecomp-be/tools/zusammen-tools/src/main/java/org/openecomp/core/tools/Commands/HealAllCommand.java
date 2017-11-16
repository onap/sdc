package org.openecomp.core.tools.Commands;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Created by ayalaben on 11/6/2017
 */
public class HealAllCommand {

  private static final String HEALING_USER = "healing_user";
  private static BufferedWriter log;
  private static VendorSoftwareProductManager vspManager = VspManagerFactory
      .getInstance().createInterface();
  private static VersioningManager versioningManager = VersioningManagerFactory.getInstance()
      .createInterface();

  static {
    try {
      log =
          new BufferedWriter(new FileWriter("healingLog_" + System.currentTimeMillis() + ".log",
              true));
    } catch (IOException e) {
      throw new RuntimeException("can't initial healing log file:" + e.getMessage());
    }
  }

  public static void healAll(String threahNumber) {

    writeToLog("----starting healing------>" + System.currentTimeMillis());
    int numberOfThreds = Objects.nonNull(threahNumber) ? Integer.valueOf(threahNumber) : 100;

    Stream<VersionInfoEntity> vsps = VersionInfoCassandraLoader.list().stream()
        .filter(versionInfoEntity -> versionInfoEntity.getEntityType().equals(
            VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE));

    List<ItemHealingTask> tasks = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreds);
    List<Future<String>> futureTasks;

    vsps.forEach(versionInfoEntity -> tasks.add(new ItemHealingTask
        (versionInfoEntity.getEntityId(), resolveVersion
            (versionInfoEntity).toString(), versionInfoEntity.getCandidate() == null ?
            HEALING_USER : versionInfoEntity.getCandidate().getUser(),
            vspManager, versioningManager)));


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
      writeToLog("migration tasks failed.");
      throw new RuntimeException(e);
    }
    boolean isThreadOpen = true;
    while (isThreadOpen) {
      isThreadOpen = futureTasks.stream().anyMatch(future -> !future.isDone());
    }
    writeToLog("----finished healing------>" + System.currentTimeMillis());
    try {
      if (log != null) {
        log.close();
      }
    } catch (IOException e) {
      writeToLog("Error:" + e.getMessage());
    }

    System.exit(1);
  }

  private static Version resolveVersion(VersionInfoEntity versionInfoEntity) {
    if (Objects.nonNull(versionInfoEntity.getCandidate())) {
      return versionInfoEntity.getCandidate().getVersion();
    } else if (Objects.nonNull(versionInfoEntity.getViewableVersions()) &&
        !versionInfoEntity
            .getViewableVersions().isEmpty()) {

      Optional<Version> version =
          versionInfoEntity.getViewableVersions().stream().max((o1, o2) -> o1.getMajor() >
              o2.getMajor() ? 1 : (o1.getMajor() == o2.getMajor() ? (Integer
              .compare(o1.getMinor(), o2.getMinor())) : -1));
      return version.orElseGet(() -> new Version(0, 1));
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
}
