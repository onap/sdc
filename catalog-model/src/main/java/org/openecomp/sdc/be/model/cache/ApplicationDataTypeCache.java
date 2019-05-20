/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.cache;

import fj.data.Either;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheInfo;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component("application-datatype-cache")
public class ApplicationDataTypeCache implements ApplicationCache<DataTypeDefinition>, Runnable {

    private static final String APPLICATION_DATA_TYPES_CACHE = "ApplicationDataTypesCache";
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    private Map<String, DataTypeDefinition> data = new HashMap<>();

    private ScheduledExecutorService scheduledPollingService = Executors.newScheduledThreadPool(1,
            new BasicThreadFactory.Builder().namingPattern("ApplicationDataTypeCacheThread-%d").build());
    ScheduledFuture<?> scheduledFuture = null;

    private static final Logger log = Logger.getLogger(ApplicationDataTypeCache.class.getName());

    private int firstRunDelayInSec = 30;
    private int pollingIntervalInSec = 60;

    @Resource
    private PropertyOperation propertyOperation;

    @PostConstruct
    public void init() {

        ApplicationL1CacheConfig applicationL1CacheConfig = ConfigurationManager.getConfigurationManager()
                .getConfiguration().getApplicationL1Cache();
        if (applicationL1CacheConfig != null) {
            if (applicationL1CacheConfig.getDatatypes() != null) {
                ApplicationL1CacheInfo datatypesInfo = applicationL1CacheConfig.getDatatypes();
                if (datatypesInfo.getEnabled()) {
                    Integer intervalInSec = datatypesInfo.getPollIntervalInSec();
                    if (intervalInSec != null) {
                        pollingIntervalInSec = intervalInSec;
                    }
                    Integer firstRunDelay = datatypesInfo.getFirstRunDelay();
                    if (firstRunDelay != null) {
                        firstRunDelayInSec = firstRunDelay;
                    }
                    log.trace("ApplicationDataTypesCache polling interval is {} seconds.", pollingIntervalInSec);
                    if (scheduledPollingService != null) {
                        log.debug("Start ApplicationDataTypeCache polling task. polling interval {} seconds",
                                pollingIntervalInSec);
                        scheduledFuture = scheduledPollingService.scheduleAtFixedRate(this, firstRunDelayInSec,
                                pollingIntervalInSec, TimeUnit.SECONDS);
                    }

                }
            } else {
                BeEcompErrorManager.getInstance().logInternalFlowError(APPLICATION_DATA_TYPES_CACHE, "Cache is disabled",
                        ErrorSeverity.INFO);
            }
        } else {
            BeEcompErrorManager.getInstance().logInternalFlowError(APPLICATION_DATA_TYPES_CACHE, "Cache is disabled",
                    ErrorSeverity.INFO);
        }

    }

    @PreDestroy
    void destroy() {

        if (scheduledFuture != null) {
            boolean result = scheduledFuture.cancel(true);
            log.debug("Stop polling task. result = {}", result);

            scheduledFuture = null;
        }
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        if (scheduledPollingService == null)
            return;

        scheduledPollingService.shutdown(); // Disable new tasks from being
                                            // submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledPollingService.shutdownNow(); // Cancel currently
                                                        // executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS))
                    log.debug("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            scheduledPollingService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> getAllDataTypesFromGraph() {

        return propertyOperation
                .getAllDataTypes();

    }

    @Override
    public Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> getAll() {

        try {

            r.lock();
            if (data == null || data.isEmpty()) {
                return getAllDataTypesFromGraph();
            }

            return Either.left(data);

        } finally {
            r.unlock();
        }
    }

    @Override
    public Either<DataTypeDefinition, JanusGraphOperationStatus> get(String uniqueId) {

        try {
            r.lock();

            if (data == null || data.isEmpty()) {
                return propertyOperation
                        .getDataTypeByUid(uniqueId);
            } else {
                DataTypeDefinition dataTypeDefinition = data.values().stream()
                        .filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
                if (dataTypeDefinition == null) {
                    return propertyOperation
                            .getDataTypeByUid(uniqueId);
                } else {
                    return Either.left(dataTypeDefinition);
                }
            }
        } finally {
            r.unlock();
        }
    }

    @Override
    public void run() {
        log.trace("run() method. polling db to fetch data types");

        try {

            Long start = System.currentTimeMillis();
            log.trace("Start fetching all data types from db");
            Either<List<DataTypeData>, JanusGraphOperationStatus> allDataTypeNodes = propertyOperation.getAllDataTypeNodes();
            Long end = System.currentTimeMillis();
            log.trace("Finish fetching all data types from db. Took {} Milliseconds", (end - start));
            if (allDataTypeNodes.isRight()) {
                JanusGraphOperationStatus status = allDataTypeNodes.right().value();
                if (status != JanusGraphOperationStatus.OK) {
                    log.debug("ApplicationDataTypesCache - Failed to fetch all data types nodes");
                    BeEcompErrorManager.getInstance().logInternalConnectionError("FetchDataTypes",
                            "Failed to fetch data types from graph(cache)", ErrorSeverity.INFO);
                }
            } else {

                List<DataTypeData> list = allDataTypeNodes.left().value();
                if (list != null) {

                    Map<String, ImmutablePair<Long, Long>> dataTypeNameToModificationTime = list.stream()
                            .collect(Collectors.toMap(p -> p.getDataTypeDataDefinition().getName(),
                                    p -> new ImmutablePair<>(p.getDataTypeDataDefinition().getCreationTime(),
                                            p.getDataTypeDataDefinition().getModificationTime())));

                    Map<String, ImmutablePair<Long, Long>> currentDataTypeToModificationTime = new HashMap<>();
                    try {
                        r.lock();
                        if (data != null) {
                            currentDataTypeToModificationTime = data.values().stream().collect(Collectors.toMap(
                                    DataTypeDataDefinition::getName,
                                    p -> new ImmutablePair<>(p.getCreationTime(), p.getModificationTime())));

                        }
                    } finally {
                        r.unlock();
                    }

                    boolean isChanged = compareDataTypes(dataTypeNameToModificationTime,
                            currentDataTypeToModificationTime);
                    if (isChanged) {
                        replaceAllData();
                    }

                }
            }

        } catch (Exception e) {
            log.debug("unexpected error occured", e);

            BeEcompErrorManager.getInstance().logInternalUnexpectedError(APPLICATION_DATA_TYPES_CACHE,
                    "Failed to run refresh data types job", ErrorSeverity.INFO);
        } finally {
            try {
                propertyOperation.getJanusGraphGenericDao().commit();
            } catch (Exception e) {
                log.trace("Failed to commit ApplicationDataTypeCache", e);
            }
        }

    }

    private boolean compareDataTypes(Map<String, ImmutablePair<Long, Long>> dataTypeNameToModificationTime,
            Map<String, ImmutablePair<Long, Long>> currentDataTypeToModificationTime) {
        if (dataTypeNameToModificationTime.size() != currentDataTypeToModificationTime.size()) {
            return true;
        } else {

            Set<String> currentkeySet = currentDataTypeToModificationTime.keySet();
            Set<String> keySet = dataTypeNameToModificationTime.keySet();

            if (currentkeySet.containsAll(keySet)) {

                for (Entry<String, ImmutablePair<Long, Long>> entry : dataTypeNameToModificationTime.entrySet()) {
                    String dataTypeName = entry.getKey();
                    ImmutablePair<Long, Long> creationAndModificationTimes = entry.getValue();
                    long creationTime = creationAndModificationTimes.getLeft() == null ? 0
                            : creationAndModificationTimes.getLeft().longValue();
                    long modificationTime = creationAndModificationTimes.getRight() == null ? 0
                            : creationAndModificationTimes.getRight().longValue();

                    ImmutablePair<Long, Long> currentEntry = currentDataTypeToModificationTime.get(dataTypeName);
                    long currentCreationTime = currentEntry.getLeft() == null ? 0 : currentEntry.getLeft().longValue();
                    long currentModificationTime = currentEntry.getRight() == null ? 0
                            : currentEntry.getRight().longValue();

                    if (creationTime > currentCreationTime || modificationTime > currentModificationTime) {
                        log.debug("Datatype {} was updated. Creation Time  {} vs {}. Modification Time {} vs {}",
                                dataTypeName, currentCreationTime, creationTime, currentModificationTime,
                                modificationTime);
                        return true;
                    }
                }
            } else {
                return true;
            }

        }

        return false;
    }

    private void replaceAllData() {

        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = propertyOperation
                .getAllDataTypes();

        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus status = allDataTypes.right().value();
            log.debug("Failed to fetch all data types from db. Status is {}", status);
        } else {

            try {
                w.lock();

                data = allDataTypes.left().value();

                BeEcompErrorManager.getInstance().logInternalFlowError("ReplaceDataTypesCache",
                        "Succeed to replace the data types cache", ErrorSeverity.INFO);

            } finally {
                w.unlock();
            }

        }

    }

}
