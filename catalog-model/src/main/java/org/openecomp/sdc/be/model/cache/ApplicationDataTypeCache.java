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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheInfo;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.DataTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("application-datatype-cache")
public class ApplicationDataTypeCache implements ApplicationCache<DataTypeDefinition>, Runnable {

    private static final String APPLICATION_DATA_TYPES_CACHE = "ApplicationDataTypesCache";
    private static final Logger log = Logger.getLogger(ApplicationDataTypeCache.class);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final PropertyOperation propertyOperation;
    private final ApplicationEventPublisher applicationEventPublisher;
    @Getter(AccessLevel.PACKAGE)
    private final ScheduledExecutorService scheduledPollingService;
    @Getter(AccessLevel.PACKAGE)
    private ScheduledFuture<?> scheduledFuture = null;
    private Map<String, Map<String, DataTypeDefinition>> dataTypesByModelCacheMap = new HashMap<>();
    private final DataTypeOperation dataTypeOperation;
    private int firstRunDelayInSec = 30;
    private int pollingIntervalInSec = 60;

    public ApplicationDataTypeCache(final PropertyOperation propertyOperation, final ApplicationEventPublisher applicationEventPublisher,
                                    final DataTypeOperation dataTypeOperation) {
        this.propertyOperation = propertyOperation;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dataTypeOperation = dataTypeOperation;
        scheduledPollingService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("ApplicationDataTypeCacheThread-%d").build());
    }

    @PostConstruct
    void init() {
        final Optional<ApplicationL1CacheInfo> dataTypeCacheConfigOptional = getDataTypeCacheConfig();
        if (dataTypeCacheConfigOptional.isEmpty()) {
            BeEcompErrorManager.getInstance()
                .logInternalFlowError(APPLICATION_DATA_TYPES_CACHE, "Data types cache is not configured and will be disabled", ErrorSeverity.INFO);
            return;
        }
        final ApplicationL1CacheInfo dataTypesCacheInfo = dataTypeCacheConfigOptional.get();
        if (!Boolean.TRUE.equals(dataTypesCacheInfo.getEnabled())) {
            BeEcompErrorManager.getInstance().logInternalFlowError(APPLICATION_DATA_TYPES_CACHE, "Data types cache is disabled", ErrorSeverity.INFO);
            return;
        }
        loadConfigurationValues(dataTypesCacheInfo);
        if (scheduledPollingService != null) {
            log.debug("Starting ApplicationDataTypeCache polling task. Initial delay {}s and polling interval {}s",
                firstRunDelayInSec, pollingIntervalInSec);
            scheduledFuture = scheduledPollingService
                .scheduleAtFixedRate(this, firstRunDelayInSec, pollingIntervalInSec, TimeUnit.SECONDS);
        }
    }

    private void loadConfigurationValues(final ApplicationL1CacheInfo dataTypesCacheInfo) {
        final Integer firstRunDelay = dataTypesCacheInfo.getFirstRunDelay();
        if (firstRunDelay != null) {
            firstRunDelayInSec = firstRunDelay;
        }
        log.trace("ApplicationDataTypesCache initial delay configured to {} seconds.", firstRunDelayInSec);

        final Integer intervalInSec = dataTypesCacheInfo.getPollIntervalInSec();
        if (intervalInSec != null) {
            pollingIntervalInSec = intervalInSec;
        }
        log.trace("ApplicationDataTypesCache polling interval configured to {} seconds.", pollingIntervalInSec);
    }

    private Optional<ApplicationL1CacheInfo> getDataTypeCacheConfig() {
        final var applicationL1CacheConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getApplicationL1Cache();
        if (applicationL1CacheConfig == null || applicationL1CacheConfig.getDatatypes() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(applicationL1CacheConfig.getDatatypes());
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
        if (scheduledPollingService == null) {
            return;
        }
        scheduledPollingService.shutdown(); // Disable new tasks from being

        // submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledPollingService.shutdownNow(); // Cancel currently

                // executing tasks

                // Wait a while for tasks to respond to being cancelled
                if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.debug("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            scheduledPollingService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private Either<Map<String, Map<String, DataTypeDefinition>>, JanusGraphOperationStatus> getAllDataTypesFromGraph() {
        return propertyOperation.getAllDataTypes();
    }

    public Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> getAll(final String model) {
        try {
            readWriteLock.readLock().lock();
            if (MapUtils.isEmpty(dataTypesByModelCacheMap) || !dataTypesByModelCacheMap.containsKey(model)) {
                final var dataTypesFound = getAllDataTypesFromGraph();
                if (dataTypesFound.isRight()) {
                    return Either.right(dataTypesFound.right().value());
                }
                dataTypesByModelCacheMap = dataTypesFound.left().value();
            }
            return Either.left(getDataTypeDefinitionMapByModel(model));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Either<DataTypeDefinition, JanusGraphOperationStatus> get(final String model, final String uniqueId) {
        try {
            readWriteLock.readLock().lock();
            if (MapUtils.isEmpty(dataTypesByModelCacheMap)) {
                return propertyOperation.getDataTypeByUid(uniqueId);
            }
            final Optional<DataTypeDefinition> dataTypeDefinition = getDataTypeDefinitionMapByModel(model).values().stream()
                .filter(p -> p.getUniqueId().equals(uniqueId)).findFirst();
            if (dataTypeDefinition.isEmpty()) {
                return propertyOperation.getDataTypeByUid(uniqueId);
            }
            return Either.left(new DataTypeDefinition(dataTypeDefinition.get()));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private Map<String, DataTypeDefinition> getDataTypeDefinitionMapByModel(final String model) {
        return dataTypesByModelCacheMap.containsKey(model) ? dataTypesByModelCacheMap.get(model) : new HashMap<>();
    }

    @Override
    public void run() {
        try {
            refreshDataTypesCacheIfStale();
        } catch (final Exception e) {
            var errorMsg = "Failed to run refresh data types cache job";
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, ApplicationDataTypeCache.class.getName(), errorMsg, e);
            BeEcompErrorManager.getInstance().logInternalUnexpectedError(APPLICATION_DATA_TYPES_CACHE, errorMsg, ErrorSeverity.INFO);
        } finally {
            try {
                propertyOperation.getJanusGraphGenericDao().commit();
            } catch (final Exception e) {
                log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, ApplicationDataTypeCache.class.getName(),
                    "Failed to commit ApplicationDataTypeCache", e);
            }
        }
    }

    private boolean hasDataTypesChanged() {
        final List<DataTypeData> dataTypeListFromDatabase = findAllDataTypesLazy();
        final int dataTypesCacheCopyMap = dataTypesCacheMapSize();
        if (dataTypeListFromDatabase.size() != dataTypesCacheCopyMap) {
            log.debug("Total of cached data types '{}' differs from the actual '{}'", dataTypeListFromDatabase.size(),  dataTypesCacheCopyMap);
            return true;
        }

        if (CollectionUtils.isEmpty(dataTypeListFromDatabase)) {
            log.debug("Both data type cache and database are empty");
            return false;
        }

        return hasDataTypesChanged(dataTypeListFromDatabase, copyDataTypeCache());
    }

    private int dataTypesCacheMapSize() {
        var count = 0;
        for (var i = 0; i < copyDataTypeCache().size(); i++) {
            count += new ArrayList<>(copyDataTypeCache().values()).get(i).size();

        }
        return count;
    }

    private boolean hasDataTypesChanged(final List<DataTypeData> dataTypeListFromDatabase, final Map<String, Map<String, DataTypeDefinition>> dataTypesCacheCopyMap) {
        return dataTypeListFromDatabase.stream().map(DataTypeData::getDataTypeDataDefinition).anyMatch(actualDataTypeDefinition -> {
            final String dataTypeName = actualDataTypeDefinition.getName();
            final String model = actualDataTypeDefinition.getModel();
            final DataTypeDefinition cachedDataTypeDefinition = dataTypesCacheCopyMap.get(model).get(dataTypeName);
            if (cachedDataTypeDefinition == null) {
                log.debug("Datatype '{}' is not present in the cache. ", dataTypeName);
                return true;
            }

            final long cachedCreationTime = cachedDataTypeDefinition.getCreationTime() == null ? 0 : cachedDataTypeDefinition.getCreationTime();
            final long actualCreationTime = actualDataTypeDefinition.getCreationTime() == null ? 0 : actualDataTypeDefinition.getCreationTime();
            if (cachedCreationTime != actualCreationTime) {
                log.debug("Datatype '{}' with model '{}' was updated. Cache/database creation time '{}'/'{}'.",
                    dataTypeName, model, cachedCreationTime, actualCreationTime);
                return true;
            }
            final long cachedModificationTime =
                cachedDataTypeDefinition.getModificationTime() == null ? 0 : cachedDataTypeDefinition.getModificationTime();
            final long actualModificationTime =
                actualDataTypeDefinition.getModificationTime() == null ? 0 : actualDataTypeDefinition.getModificationTime();
            if (cachedModificationTime != actualModificationTime) {
                log.debug("Datatype '{}' was updated. Cache/database modification time '{}'/'{}'.",
                    dataTypeName, cachedModificationTime, actualModificationTime);
                return true;
            }

            return false;
        });
    }

    private Map<String, Map<String, DataTypeDefinition>> copyDataTypeCache() {
        try {
            readWriteLock.readLock().lock();
            return new HashMap<>(this.dataTypesByModelCacheMap);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public void refreshDataTypesCacheIfStale() {
        final long startTime = System.currentTimeMillis();
        log.trace("Starting refresh data types cache");
        if (hasDataTypesChanged()) {
            log.info("Detected changes in the data types, updating the data type cache.");
            refreshDataTypesCache();
        }
        log.trace("Finished refresh data types cache. Finished in {}ms", (System.currentTimeMillis() - startTime));
    }

    private void refreshDataTypesCache() {
        final Map<String, Map<String, DataTypeDefinition>> dataTypesDefinitionMap = findAllDataTypesEager();
        if (dataTypesDefinitionMap.isEmpty()) {
            return;
        }
        try {
            readWriteLock.writeLock().lock();
            dataTypesByModelCacheMap = dataTypesDefinitionMap;
            onDataChangeEventEmit();
            BeEcompErrorManager.getInstance()
                .logInternalFlowError("ReplaceDataTypesCache", "Succeed to replace the data types cache", ErrorSeverity.INFO);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private Map<String, Map<String, DataTypeDefinition>> findAllDataTypesEager() {
        log.trace("Fetching data types from database, eager mode");
        final long startTime = System.currentTimeMillis();
        final Either<Map<String, Map<String, DataTypeDefinition>>, JanusGraphOperationStatus> allDataTypes = propertyOperation.getAllDataTypes();
        log.trace("Finish fetching data types from database. Took {}ms", (System.currentTimeMillis() - startTime));
        if (allDataTypes.isRight()) {
            final JanusGraphOperationStatus status = allDataTypes.right().value();
            var errorMsg= String.format("Failed to fetch data types from database. Status is %s", status);
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, ApplicationDataTypeCache.class.getName(), errorMsg);
            BeEcompErrorManager.getInstance().logInternalConnectionError(APPLICATION_DATA_TYPES_CACHE, errorMsg, ErrorSeverity.ERROR);
            return Collections.emptyMap();
        }
        return allDataTypes.left().value();
    }

    private List<DataTypeData> findAllDataTypesLazy() {
        log.trace("Fetching data types from database, lazy mode");
        final long startTime = System.currentTimeMillis();
        final List<DataTypeData> allDataTypes = dataTypeOperation.getAllDataTypeNodes();
        log.trace("Finish fetching data types from database. Took {}ms", (System.currentTimeMillis() - startTime));
        return allDataTypes;
    }

    private void onDataChangeEventEmit() {
        log.trace("Data type cache has changed, sending DataTypesCacheChangedEvent.");
        applicationEventPublisher.publishEvent(new DataTypesCacheChangedEvent(this, copyDataTypeCache()));
    }

    /**
     * Custom event to notify all interested in cached data changes
     */
    public static class DataTypesCacheChangedEvent extends ApplicationEvent {

        @Getter
        private final Map<String,  Map<String, DataTypeDefinition>> newData;

        public DataTypesCacheChangedEvent(final Object source, final Map<String,  Map<String, DataTypeDefinition>> newData) {
            super(source);
            this.newData = newData;
        }
    }

}
