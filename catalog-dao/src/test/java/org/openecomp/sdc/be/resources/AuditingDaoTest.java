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

package org.openecomp.sdc.be.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.ElasticSearchConfig.IndicesTimeFrequencyEntry;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAccessEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAdminEvent;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class })
public class AuditingDaoTest {
	private static Logger log = LoggerFactory.getLogger(AuditingDaoTest.class.getName());
	@Resource(name = "elasticsearch-client")
	private ElasticSearchClient esclient;

	@Resource(name = "auditingDao")
	private AuditingDao auditingDao;

	private static ConfigurationManager configurationManager;
	// private static Map<AuditingFieldsKeysEnum, String> auditField2esField;

	@BeforeClass
	public static void setupBeforeClass() {

		ExternalConfiguration.setAppName("catalog-dao");
		String appConfigDir = "src/test/resources/config/catalog-dao";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);
		// initAudit2EsMap();
	}

	@After
	public void tearDown() {
		deleteOldIndexes();
	}

	@Before
	public void setup() {
		auditingDao.setConfigurationManager(configurationManager);
		deleteOldIndexes();
	}

	private void deleteOldIndexes() {
		DeleteIndexResponse deleteResponse = esclient.getClient().admin().indices()
				.prepareDelete(auditingDao.getIndexPrefix() + "*").execute().actionGet();
		if (!deleteResponse.isAcknowledged()) {
			log.debug("Couldn't delete old auditing indexes!");
			assertTrue(false);
		}
	}

	// @Test
	public void testAddUpdateAdminEventMinute() {

		String timestamp = "2015-06-23 13:34:53.123";

		String creationPeriod = Constants.MINUTE;
		String expectedIndexName = auditingDao.getIndexPrefix() + "-2015-06-23-13-34";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName).execute().actionGet()
				.isExists());
		Map<AuditingFieldsKeysEnum, Object> params = getUserAdminEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAdminEvent.class);
		params = getUserAccessEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAccessEvent.class);
		params = getResourceAdminEventParams(timestamp, "addResource");
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, ResourceAdminEvent.class);
	}

	// @Test
	public void testAddUpdateAdminEventYearly() {

		String timestamp = "2016-06-23 13:34:53.123";
		String creationPeriod = Constants.YEAR;
		String expectedIndexName = auditingDao.getIndexPrefix() + "-2016";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName).execute().actionGet()
				.isExists());
		Map<AuditingFieldsKeysEnum, Object> params = getUserAdminEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAdminEvent.class);
		params = getUserAccessEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAccessEvent.class);
		params = getResourceAdminEventParams(timestamp, "addResource");
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, ResourceAdminEvent.class);
	}

	@Test
	public void testGetDistributionStatusEvent() {

		String timestamp1 = "2016-06-23 13:34:53.123";
		String creationPeriod = Constants.MONTH;
		String expectedIndexName1 = auditingDao.getIndexPrefix() + "-2016-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName1).execute().actionGet()
				.isExists());
		Map<AuditingFieldsKeysEnum, Object> params = getDistributionStatusEventParams(timestamp1);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName1, DistributionStatusEvent.class);
		String timestamp2 = "2015-06-23 13:34:53.123";

		String expectedIndexName2 = auditingDao.getIndexPrefix() + "-2015-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName2).execute().actionGet()
				.isExists());
		Map<AuditingFieldsKeysEnum, Object> params2 = getDistributionStatusEventParams(timestamp2);
		testCreationPeriodScenario(params2, creationPeriod, expectedIndexName2, DistributionStatusEvent.class);
		Either<List<ESTimeBasedEvent>, ActionStatus> status = auditingDao.getListOfDistributionStatuses("123-456");
		assertEquals(2, status.left().value().size());
	}

	@Test
	public void testGetCountAdminEventMonthly() {

		String timestamp1 = "2016-06-23 13:34:53.123";
		String timestamp2 = "2015-06-23 13:34:53.123";
		String creationPeriod = Constants.MONTH;
		String expectedIndexName1 = auditingDao.getIndexPrefix() + "-2016-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName1).execute().actionGet()
				.isExists());
		String expectedIndexName2 = auditingDao.getIndexPrefix() + "-2015-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName2).execute().actionGet()
				.isExists());

		Map<AuditingFieldsKeysEnum, Object> params1 = getUserAdminEventParams(timestamp1);
		testCreationPeriodScenario(params1, creationPeriod, expectedIndexName1, UserAdminEvent.class);
		Map<AuditingFieldsKeysEnum, Object> params2 = getUserAdminEventParams(timestamp2);
		testCreationPeriodScenario(params2, creationPeriod, expectedIndexName2, UserAdminEvent.class);

		long count = auditingDao.count(UserAdminEvent.class, new MatchAllQueryBuilder());
		log.debug("Testing auditing count {}", count);
		assertEquals(2, count);
	}

	@Test
	public void testServiceDistributionStatuses() {

		String timestamp = "2016-06-23 13:34:53.123";
		String creationPeriod = Constants.MONTH;
		String expectedIndexName = auditingDao.getIndexPrefix() + "-2016-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName).execute().actionGet()
				.isExists());
		Map<AuditingFieldsKeysEnum, Object> params = getUserAdminEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAdminEvent.class);
		params = getUserAccessEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAccessEvent.class);
		params = getResourceAdminEventParams(timestamp, "DRequest");
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, ResourceAdminEvent.class);
		params = getDistributionNotificationEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, DistributionNotificationEvent.class);
		Either<List<ESTimeBasedEvent>, ActionStatus> status = auditingDao
				.getServiceDistributionStatusesList("SeviceId");
		log.debug("Testing auditing count {}", status);
	}

	@Test
	public void testAddUpdateAdminEventMonthly() {

		String timestamp = "2016-06-23 13:34:53.123";
		String creationPeriod = Constants.MONTH;
		String expectedIndexName = auditingDao.getIndexPrefix() + "-2016-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName).execute().actionGet()
				.isExists());
		Map<AuditingFieldsKeysEnum, Object> params = getUserAdminEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAdminEvent.class);
		params = getUserAccessEventParams(timestamp);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, UserAccessEvent.class);
		params = getResourceAdminEventParams(timestamp, "addResource");
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, ResourceAdminEvent.class);
	}

	private SearchResponse testCreationPeriodScenario(Map<AuditingFieldsKeysEnum, Object> params, String creationPeriod,
			String expectedIndexName, Class<? extends AuditingGenericEvent> clazz) {

		String typeName = clazz.getSimpleName().toLowerCase();
		log.debug("Testing auditing type {}", typeName);
		setCreationPeriod(creationPeriod);
		ActionStatus saveUserAdminEvent = auditingDao.addRecord(params, typeName);
		assertEquals(ActionStatus.OK, saveUserAdminEvent);
		assertTrue(esclient.getClient().admin().indices().prepareExists(expectedIndexName).execute().actionGet()
				.isExists());
		MatchAllQueryBuilder matchAllQueryBuilder = new MatchAllQueryBuilder();

		SearchResponse searchResponse = esclient.getClient().prepareSearch(expectedIndexName).setTypes(typeName)
				.setQuery(matchAllQueryBuilder).execute().actionGet();

		SearchHits hits = searchResponse.getHits();
		assertEquals(1, hits.getTotalHits());
		log.debug("Checking that all expected fields are properly persisted");
		validateHitValues(params, hits.getAt(0));
		log.debug("testCreationPeriodScenario successful");
		return searchResponse;
	}

	private void validateHitValues(Map<AuditingFieldsKeysEnum, Object> params, SearchHit searchHit) {
		Map<String, Object> source = searchHit.getSource();
		log.debug("Hit source is {}", searchHit.sourceAsString());
		for (Entry<AuditingFieldsKeysEnum, Object> paramsEntry : params.entrySet()) {
			AuditingFieldsKeysEnum key = paramsEntry.getKey();
			log.debug("Testing auditing field {}", key.name());
			Object value = paramsEntry.getValue();
			// assertEquals(value, source.get(auditField2esField.get(key)));
			assertEquals(value, source.get(key.getDisplayName()));
		}
	}

	private void setCreationPeriod(String creationPeriod) {
		Configuration configuration = configurationManager.getConfiguration();
		List<IndicesTimeFrequencyEntry> indicesTimeFrequencyEntries = new ArrayList<>();
		IndicesTimeFrequencyEntry indicesTimeFrequencyEntry = new IndicesTimeFrequencyEntry();
		indicesTimeFrequencyEntry.setIndexPrefix("auditingevents");
		indicesTimeFrequencyEntry.setCreationPeriod(creationPeriod);
		configuration.getElasticSearch().setIndicesTimeFrequency(indicesTimeFrequencyEntries);
	}

	private Map<AuditingFieldsKeysEnum, Object> getUserAdminEventParams(String timestamp) {

		Map<AuditingFieldsKeysEnum, Object> params = new HashMap<AuditingFieldsKeysEnum, Object>();
		String action = "updateUser";
		String modifierName = "moshe moshe";
		String modifierUid = "mosheUid";
		String userUid = "mosheUid";
		String userBeforeName = "moshe moshe";
		String userBeforeEmail = "moshe@moshe1.com";
		String userBeforeRole = "TESTER";
		String userAfterName = "moshe moshe";
		String userAfterEmail = "moshe@moshe2.com";
		String userAfterRole = "TESTER";
		String userStatus = "200";
		String userDesc = "OK";

		params.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action);
		params.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifierName + '(' + modifierUid + ')');
		params.put(AuditingFieldsKeysEnum.AUDIT_USER_UID, userUid);
		params.put(AuditingFieldsKeysEnum.AUDIT_USER_BEFORE,
				userUid + ", " + userBeforeName + ", " + userBeforeEmail + ", " + userBeforeRole);
		params.put(AuditingFieldsKeysEnum.AUDIT_USER_AFTER,
				userUid + ", " + userAfterName + ", " + userAfterEmail + ", " + userAfterRole);
		params.put(AuditingFieldsKeysEnum.AUDIT_STATUS, userStatus);
		params.put(AuditingFieldsKeysEnum.AUDIT_DESC, userDesc);
		params.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, timestamp);

		return params;
	}

	private Map<AuditingFieldsKeysEnum, Object> getUserAccessEventParams(String timestamp) {

		Map<AuditingFieldsKeysEnum, Object> params = new HashMap<AuditingFieldsKeysEnum, Object>();
		String action = "userAccess";
		String userUid = "mosheUid";
		String userName = "moshe moshe";
		String userStatus = "200";
		String userDesc = "OK";

		params.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action);
		params.put(AuditingFieldsKeysEnum.AUDIT_USER_UID, userName + '(' + userUid + ')');
		params.put(AuditingFieldsKeysEnum.AUDIT_STATUS, userStatus);
		params.put(AuditingFieldsKeysEnum.AUDIT_DESC, userDesc);
		params.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, timestamp);

		return params;
	}

	private Map<AuditingFieldsKeysEnum, Object> getResourceAdminEventParams(String timestamp, String action) {

		Map<AuditingFieldsKeysEnum, Object> params = new HashMap<AuditingFieldsKeysEnum, Object>();

		String modifierName = "moshe moshe";
		String modifierUid = "mosheUid";
		String resourceName = "Centos";
		String resourceType = "Resource";
		String currState = "READY_FOR_CERTIFICATION";
		String prevState = "CHECKED_OUT";
		String currVersion = "1.1.4";
		String prevVersion = "1.1.3";
		String status = "200";
		String desc = "OK";
		String distributionId = "123-456";
		String serviceId = "SeviceId";

		params.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action);
		params.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifierName);
		params.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifierUid);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceName);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, resourceType);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, currState);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, prevState);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, currVersion);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, prevVersion);
		params.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		params.put(AuditingFieldsKeysEnum.AUDIT_DESC, desc);
		params.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, timestamp);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);
		params.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, serviceId);

		return params;
	}

	private Map<AuditingFieldsKeysEnum, Object> getDistributionStatusEventParams(String timestamp) {

		Map<AuditingFieldsKeysEnum, Object> params = new HashMap<AuditingFieldsKeysEnum, Object>();
		String action = "DStatus";
		String modifierName = "moshe moshe";
		String modifierUid = "mosheUid";
		String topicName = "Centos";
		String serviceId = "SeviceId";
		String resourceUrl = "resourceUrl";
		String distributionId = "123-456";

		String status = "200";
		String desc = "OK";

		params.put(AuditingFieldsKeysEnum.AUDIT_DESC, desc);
		params.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, timestamp);
		params.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		params.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, modifierUid);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, topicName);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, resourceUrl);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME, timestamp);
		params.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, serviceId);

		return params;
	}

	// @Test
	public void getListOfDistributionByActionTest() {

		String timestamp = "2016-06-23 13:34:53.123";
		String distributionId = "123-456";

		String creationPeriod = Constants.MONTH;
		String expectedIndexName = auditingDao.getIndexPrefix() + "-2016-06";
		assertTrue(!esclient.getClient().admin().indices().prepareExists(expectedIndexName).execute().actionGet()
				.isExists());

		// Client client = esclient.getClient();
		// final CreateIndexRequestBuilder createIndexRequestBuilder =
		// client.admin().indices().prepareCreate(expectedIndexName);
		// final XContentBuilder mappingBuilder =
		// jsonBuilder().startObject().startObject("resourceadminevent")
		// .startObject("_ttl").field("enabled", "true").field("default",
		// "1s").endObject().endObject()
		// .endObject();
		// System.out.println(mappingBuilder.string());
		// createIndexRequestBuilder.addMapping(documentType, mappingBuilder);
		//
		// // MAPPING DONE
		// createIndexRequestBuilder.execute().actionGet();
		//
		//

		Map<AuditingFieldsKeysEnum, Object> params = getResourceAdminEventParams(timestamp, "DRequest");
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);
		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, ResourceAdminEvent.class);
		params = getDistributionNotificationEventParams(timestamp);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionId);

		testCreationPeriodScenario(params, creationPeriod, expectedIndexName, DistributionNotificationEvent.class);

		Either<List<ESTimeBasedEvent>, ActionStatus> distributionByAction = auditingDao
				.getListOfDistributionByAction(distributionId, "DRequest", "200", ResourceAdminEvent.class);
		assertTrue(distributionByAction.isLeft());
		assertFalse(distributionByAction.left().value().isEmpty());

		distributionByAction = auditingDao.getListOfDistributionByAction(distributionId, "DNotify", "200",
				DistributionNotificationEvent.class);
		assertTrue(distributionByAction.isLeft());
		assertFalse(distributionByAction.left().value().isEmpty());

	}

	private Map<AuditingFieldsKeysEnum, Object> getDistributionNotificationEventParams(String timestamp) {

		Map<AuditingFieldsKeysEnum, Object> params = new HashMap<AuditingFieldsKeysEnum, Object>();

		String action = "DNotify";
		String modifierName = "moshe moshe";
		String modifierUid = "mosheUid";
		String resourceName = "Centos";
		String resourceType = "Resource";

		String currVersion = "1.1.4";
		String currState = "READY_FOR_CERTIFICATION";
		String status = "200";
		String desc = "OK";
		String did = "1027";
		String topicName = "Centos";
		String serviceId = "SeviceId";
		String requestId = "12364";

		params.put(AuditingFieldsKeysEnum.AUDIT_ACTION, action);
		params.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, requestId);
		params.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifierUid);
		params.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifierName);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceName);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, resourceType);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, currState);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME, topicName);
		params.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, currVersion);
		params.put(AuditingFieldsKeysEnum.AUDIT_STATUS, status);
		params.put(AuditingFieldsKeysEnum.AUDIT_DESC, desc);
		params.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, timestamp);
		params.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, did);
		params.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, serviceId);
		return params;
	}

}
