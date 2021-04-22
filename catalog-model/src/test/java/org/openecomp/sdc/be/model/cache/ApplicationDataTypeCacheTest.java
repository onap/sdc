/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheInfo;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.unittests.utils.ModelConfDependentTest;
import org.springframework.context.ApplicationEventPublisher;

class ApplicationDataTypeCacheTest extends ModelConfDependentTest {

    @Mock
    private PropertyOperation propertyOperation;

    @Mock
	private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ApplicationDataTypeCache applicationDataTypeCache;

    private Map<String, DataTypeDefinition> dataTypeDefinitionMap;

    private int schedulerFirstRunDelay = 0;
    private int schedulerPollIntervalInSec = 2;
    private boolean schedulerIsEnabled = true;

	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void afterEach() {
		final ScheduledExecutorService scheduledPollingService = applicationDataTypeCache.getScheduledPollingService();
		if (scheduledPollingService == null) {
			return;
		}

		if (scheduledPollingService.isShutdown()) {
			return;
		}

		scheduledPollingService.shutdownNow();
	}

	@Test
	void testInitSuccess() {
		defaultInit();
		assertNotNull(applicationDataTypeCache.getScheduledFuture(), "The job should have been triggered");
	}

	@Test
	void testDestroySuccess() {
		defaultInit();
		assertNotNull(applicationDataTypeCache.getScheduledFuture(), "The job should have been triggered");
		applicationDataTypeCache.destroy();
		assertNull(applicationDataTypeCache.getScheduledFuture(), "The job should have been stopped");
		assertTrue(applicationDataTypeCache.getScheduledPollingService().isShutdown(), "The scheduler should have been stopped");
	}

	@Test
	void testDestroyWithoutSchedulerInitialization() {
		mockEmptyConfiguration();
		applicationDataTypeCache.init();
		assertNotNull(applicationDataTypeCache.getScheduledPollingService(), "The scheduler should have been created");
		assertFalse(applicationDataTypeCache.getScheduledPollingService().isShutdown(), "The scheduler should have been running");
		assertNull(applicationDataTypeCache.getScheduledFuture(), "The job should not have been triggered");
		applicationDataTypeCache.destroy();
		assertTrue(applicationDataTypeCache.getScheduledPollingService().isShutdown(), "The scheduler should have been stopped");
	}

	@Test
	void testInitEmptyConfiguration() {
		mockEmptyConfiguration();
		applicationDataTypeCache.init();
		assertNull(applicationDataTypeCache.getScheduledFuture(), "The scheduler should not have started");
	}

	@Test
	void testInitCacheDisabled() {
		final var applicationL1CacheInfo = new ApplicationL1CacheInfo();
		applicationL1CacheInfo.setEnabled(false);
		mockConfiguration(applicationL1CacheInfo);
		applicationDataTypeCache.init();
		assertNull(applicationDataTypeCache.getScheduledFuture(), "The scheduler should not have started");
	}

	@Test
	void testGetAllAfterInitialization() {
		defaultInit();
		final ScheduledFuture<?> scheduledFuture = applicationDataTypeCache.getScheduledFuture();
		//waiting the cache to be filled
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) != 0);
		assertDataTypeCache(dataTypeDefinitionMap);
	}

	@Test
	void testCacheChangeWithDataTypeChange() {
		defaultInit();
		final ScheduledFuture<?> scheduledFuture = applicationDataTypeCache.getScheduledFuture();
		//waiting the cache to be filled
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) != 0);
		assertDataTypeCache(dataTypeDefinitionMap);

		final Map<String, DataTypeDefinition> modifiedDataTypeDefinitionMap = new HashMap<>();
		final DataTypeDefinition testDataType1 = new DataTypeDefinition();
		testDataType1.setName("test.data.type1");
		testDataType1.setCreationTime(101L);
		testDataType1.setModificationTime(1000L);
		modifiedDataTypeDefinitionMap.put(testDataType1.getName(), testDataType1);
		final DataTypeDefinition testDataType2 = new DataTypeDefinition();
		testDataType2.setName("test.data.type2");
		testDataType2.setCreationTime(101L);
		testDataType2.setModificationTime(1002L);
		modifiedDataTypeDefinitionMap.put(testDataType2.getName(), testDataType2);
		when(propertyOperation.getAllDataTypes()).thenReturn(Either.left(modifiedDataTypeDefinitionMap));
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) == 0);
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) != 0);
		assertDataTypeCache(modifiedDataTypeDefinitionMap);
	}

	@Test
	void testCacheChangeWithAddedDataType() {
		defaultInit();
		final ScheduledFuture<?> scheduledFuture = applicationDataTypeCache.getScheduledFuture();
		//waiting the cache to be filled
		await().until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) != 0);
		assertDataTypeCache(dataTypeDefinitionMap);

		final Map<String, DataTypeDefinition> modifiedDataTypeDefinitionMap = new HashMap<>();
		final DataTypeDefinition testDataType1 = new DataTypeDefinition();
		testDataType1.setName("test.data.type1");
		testDataType1.setCreationTime(1L);
		testDataType1.setModificationTime(1L);
		modifiedDataTypeDefinitionMap.put(testDataType1.getName(), testDataType1);
		final DataTypeDefinition testDataType3 = new DataTypeDefinition();
		testDataType3.setName("test.data.type3");
		testDataType3.setCreationTime(1L);
		testDataType3.setModificationTime(1L);
		modifiedDataTypeDefinitionMap.put(testDataType3.getName(), testDataType3);
		when(propertyOperation.getAllDataTypes()).thenReturn(Either.left(modifiedDataTypeDefinitionMap));
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) == 0);
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) != 0);
		assertDataTypeCache(modifiedDataTypeDefinitionMap);
	}

	@Test
	void testGetAllWithNoInitialization() {
		final Map<String, DataTypeDefinition> dataTypeDefinitionMap = new HashMap<>();
		when(propertyOperation.getAllDataTypes()).thenReturn(Either.left(dataTypeDefinitionMap));
		final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> response = applicationDataTypeCache.getAll();
		assertNotNull(response);
		assertTrue(response.isLeft());
	}

	@Test
	void testGetWhenCacheIsEmpty() {
		var dataTypeDefinition = new DataTypeDefinition();
		when(propertyOperation.getDataTypeByUid("uniqueId")).thenReturn(Either.left(dataTypeDefinition));
		final Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeEither = applicationDataTypeCache.get("uniqueId");
		assertNotNull(dataTypeEither);
		assertTrue(dataTypeEither.isLeft());
		assertEquals(dataTypeDefinition, dataTypeEither.left().value());
	}

	@Test
	void testGetCacheHit() {
		defaultInit();
		final ScheduledFuture<?> scheduledFuture = applicationDataTypeCache.getScheduledFuture();
		await().atMost(Duration.ofSeconds(schedulerPollIntervalInSec + 1)).until(() -> scheduledFuture.getDelay(TimeUnit.SECONDS) != 0);
		final Either<DataTypeDefinition, JanusGraphOperationStatus> dataTypeEither = applicationDataTypeCache.get("test.data.type1");
		assertNotNull(dataTypeEither);
		assertTrue(dataTypeEither.isLeft());
		final DataTypeDefinition actualDataTypeDefinition = dataTypeEither.left().value();
		final DataTypeDefinition expectedDataTypeDefinition = dataTypeDefinitionMap.get("test.data.type1");
		assertEquals(expectedDataTypeDefinition.getName(), actualDataTypeDefinition.getName());
		assertEquals(expectedDataTypeDefinition.getUniqueId(), actualDataTypeDefinition.getUniqueId());
		assertEquals(expectedDataTypeDefinition.getCreationTime(), actualDataTypeDefinition.getCreationTime());
		assertEquals(expectedDataTypeDefinition.getModificationTime(), actualDataTypeDefinition.getModificationTime());
	}

	private void defaultInit() {
		var applicationL1CacheInfo = new ApplicationL1CacheInfo();
		applicationL1CacheInfo.setEnabled(schedulerIsEnabled);
		applicationL1CacheInfo.setFirstRunDelay(schedulerFirstRunDelay);
		applicationL1CacheInfo.setPollIntervalInSec(schedulerPollIntervalInSec);
		mockConfiguration(applicationL1CacheInfo);

		dataTypeDefinitionMap = new HashMap<>();
		final DataTypeDefinition testDataType1 = new DataTypeDefinition();
		testDataType1.setName("test.data.type1");
		testDataType1.setUniqueId(testDataType1.getName());
		testDataType1.setCreationTime(100L);
		testDataType1.setModificationTime(1000L);
		dataTypeDefinitionMap.put(testDataType1.getName(), testDataType1);
		final DataTypeDefinition testDataType2 = new DataTypeDefinition();
		testDataType2.setName("test.data.type2");
		testDataType2.setUniqueId(testDataType2.getName());
		testDataType2.setCreationTime(101L);
		testDataType2.setModificationTime(1001L);
		dataTypeDefinitionMap.put(testDataType2.getName(), testDataType2);
		when(propertyOperation.getAllDataTypes()).thenReturn(Either.left(dataTypeDefinitionMap));
		applicationDataTypeCache.init();
	}

	private void mockConfiguration(final ApplicationL1CacheInfo applicationL1CacheInfo) {
		final var applicationL1CacheConfig = new ApplicationL1CacheConfig();
		applicationL1CacheConfig.setDatatypes(applicationL1CacheInfo);
		final var configuration = new Configuration();
		configuration.setApplicationL1Cache(applicationL1CacheConfig);
		final var configurationManager = new ConfigurationManager();
		configurationManager.setConfiguration(configuration);
	}

	private void mockEmptyConfiguration() {
		final var applicationL1CacheConfig = new ApplicationL1CacheConfig();
		final var configuration = new Configuration();
		configuration.setApplicationL1Cache(applicationL1CacheConfig);
		final var configurationManager = new ConfigurationManager();
		configurationManager.setConfiguration(configuration);
	}

	public void assertDataTypeCache(final Map<String, DataTypeDefinition> expectedDataTypeCache) {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypeCacheMapEither = applicationDataTypeCache.getAll();
		assertNotNull(dataTypeCacheMapEither);
		assertTrue(dataTypeCacheMapEither.isLeft());
		final Map<String, DataTypeDefinition> actualDataTypeMap = dataTypeCacheMapEither.left().value();
		expectedDataTypeCache.forEach((dataType, dataTypeDefinition) -> {
			final DataTypeDefinition actualDataTypeDefinition = actualDataTypeMap.get(dataType);
			assertNotNull(actualDataTypeDefinition);
			assertEquals(dataTypeDefinition.getCreationTime(), actualDataTypeDefinition.getCreationTime());
			assertEquals(dataTypeDefinition.getModificationTime(), actualDataTypeDefinition.getModificationTime());
		});
	}
}
