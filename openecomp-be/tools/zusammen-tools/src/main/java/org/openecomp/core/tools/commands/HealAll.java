/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.core.tools.commands;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.tools.exceptions.HealingRuntimeException;
import org.openecomp.core.tools.loaders.VersionInfoCassandraLoader;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

/**
 * Created by ayalaben on 11/6/2017
 */
public class HealAll extends Command {

    private static final int DEFAULT_THREAD_NUMBER = 100;
    private static final String THREAD_NUM_OPTION = "t";
    private VendorSoftwareProductManager vspManager;
    private HealingManager healingManager;

    HealAll() {
        options.addOption(Option.builder(THREAD_NUM_OPTION).hasArg().argName("number").desc("number of threads").build());
    }

    private static void executeAllTasks(ExecutorService executor, BufferedWriter log) {
    }

    private static Version resolveVersion(VersionInfoEntity versionInfoEntity) {
        if (Objects.nonNull(versionInfoEntity.getCandidate())) {
            return versionInfoEntity.getCandidate().getVersion();
        } else if (!CollectionUtils.isEmpty(versionInfoEntity.getViewableVersions())) {
            return versionInfoEntity.getViewableVersions().stream().max(Version::compareTo).orElse(new Version());
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

    private static Stream<VersionInfoEntity> filterByEntityType(Collection<VersionInfoEntity> versionInfoEntities, String entityType) {
        return versionInfoEntities.stream().filter(versionInfoEntity -> versionInfoEntity.getEntityType().equals(entityType));
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);
        vspManager = VspManagerFactory.getInstance().createInterface();
        healingManager = HealingManagerFactory.getInstance().createInterface();
        String logFileName = "healing.log";
        try (BufferedWriter log = new BufferedWriter(new FileWriter(logFileName, true))) {
            writeToLog("----starting healing------", log);
            Instant startTime = Instant.now();
            int numberOfThreads = cmd.hasOption(THREAD_NUM_OPTION) && Objects.nonNull(cmd.getOptionValue(THREAD_NUM_OPTION)) ? Integer
                .valueOf(cmd.getOptionValue(THREAD_NUM_OPTION)) : DEFAULT_THREAD_NUMBER;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            filterByEntityType(VersionInfoCassandraLoader.list(), VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE)
                .forEach(this::addTaskToTasks);
            executeAllTasks(executor, log);
            writeToLog("----finished healing------", log);
            Instant endTime = Instant.now();
            writeToLog("Total runtime was: " + Duration.between(startTime, endTime), log);
        } catch (IOException e) {
            throw new HealingRuntimeException("can't initial healing log file '" + logFileName + "'", e);
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return CommandName.HEAL_ALL;
    }

    private void addTaskToTasks(VersionInfoEntity versionInfoEntity) {
    }
}
