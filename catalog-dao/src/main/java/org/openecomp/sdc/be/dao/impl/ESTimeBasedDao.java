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

package org.openecomp.sdc.be.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.openecomp.sdc.be.config.Configuration.ElasticSearchConfig.IndicesTimeFrequencyEntry;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class ESTimeBasedDao {
	private static final String SCORE_SCRIPT = "_score * ((doc.containsKey('alienScore') && !doc['alienScore'].empty) ? doc['alienScore'].value : 1)";
	private static final int MAX_SEARCH_SIZE = 1000;
	private static Logger log = LoggerFactory.getLogger(ESTimeBasedDao.class.getName());

	private Gson gson;

	private Map<String, String> indexPrefix2CreationPeriod;

	private ConfigurationManager configurationManager;

	protected ESTimeBasedDao() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		configurationManager = ConfigurationManager.getConfigurationManager();
		setIndexPrefix2CreationPeriod();
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	@Resource(name = "elasticsearch-client")
	private ElasticSearchClient esClient;

	protected final Map<String, Class<?>> typesToClasses = new HashMap<String, Class<?>>();

	public abstract String getIndexPrefix();

	public ActionStatus write(String typeName, ESTimeBasedEvent data) {

		String indexPrefix = getIndexPrefix();
		String indexSuffix = getIndexSuffix(indexPrefix, data);
		StringBuilder sb = new StringBuilder();
		sb.append(indexPrefix);
		if (indexSuffix != null) {
			sb.append("-").append(indexSuffix);
		}

		ActionStatus res = ActionStatus.OK;
		JSONObject json = new JSONObject(data.getFields());
		try {
			IndexResponse actionGet = esClient.getClient().prepareIndex(sb.toString(), typeName)
					.setSource(json.toString()).setRefresh(true).execute().actionGet(TimeValue.timeValueSeconds(15));

			if (actionGet.isCreated()) {
				log.debug("Created record {}", data.toString());
				// typesToClasses.put(typeName, data.getClass());
			} else {
				log.error("Record {} couldn't be created", data.toString());
				res = ActionStatus.GENERAL_ERROR;
			}
		} catch (Exception e) {
			log.error("Couldn't serialize object of type {} | error:", typeName, e);
			res = ActionStatus.GENERAL_ERROR;
		}
		return res;
	}

	public ActionStatus write(ESTimeBasedEvent data) {

		String indexPrefix = getIndexPrefix();
		String indexSuffix = getIndexSuffix(indexPrefix, data);
		StringBuilder sb = new StringBuilder();
		sb.append(indexPrefix);
		if (indexSuffix != null) {
			sb.append("-").append(indexSuffix);
		}

		String typeName = data.getClass().getSimpleName().toLowerCase();
		ActionStatus res = ActionStatus.OK;
		String json = gson.toJson(data);
		try {
			IndexResponse actionGet = esClient.getClient().prepareIndex(sb.toString(), typeName).setSource(json)
					.setRefresh(true).execute().actionGet(TimeValue.timeValueSeconds(15));

			if (actionGet.isCreated()) {
				log.debug("Created record {}", data.toString());
				// typesToClasses.put(typeName, data.getClass());
			} else {
				log.error("Record {} couldn't be created", data.toString());
				res = ActionStatus.GENERAL_ERROR;
			}
		} catch (Exception e) {
			log.debug("Couldn't serialize object of type {}", typeName);
			res = ActionStatus.GENERAL_ERROR;
		}
		return res;
	}

	private void setIndexPrefix2CreationPeriod() {
		indexPrefix2CreationPeriod = new HashMap<String, String>();
		List<IndicesTimeFrequencyEntry> indicesTimeFrequencyEntries = configurationManager.getConfiguration()
				.getElasticSearch().getIndicesTimeFrequency();
		for (IndicesTimeFrequencyEntry entry : indicesTimeFrequencyEntries) {
			indexPrefix2CreationPeriod.put(entry.getIndexPrefix(), entry.getCreationPeriod());

		}
	}

	private String getIndexSuffix(String indexPrefix, ESTimeBasedEvent data) {
		String indexSuffix = indexPrefix2CreationPeriod.get(indexPrefix);
		String res = null;
		if (indexSuffix != null) {
			if (indexSuffix.equalsIgnoreCase(Constants.YEAR)) {
				res = data.calculateYearIndexSuffix();
			} else if (indexSuffix.equalsIgnoreCase(Constants.MONTH)) {
				res = data.calculateMonthIndexSuffix();
			} else if (indexSuffix.equalsIgnoreCase(Constants.DAY)) {
				res = data.calculateDayIndexSuffix();
			} else if (indexSuffix.equalsIgnoreCase(Constants.HOUR)) {
				res = data.calculateHourIndexSuffix();
			} else if (indexSuffix.equalsIgnoreCase(Constants.MINUTE)) {
				res = data.calculateMinuteIndexSuffix();
			} else if (indexSuffix.equalsIgnoreCase(Constants.NONE)) {
				// do nothing - no time-based behaviour. I wanted to ensure
				// proper syntax, that's why this clause is needed.
			}
		} else {
			// Default behaviour - time-based with month period
			res = data.calculateMonthIndexSuffix();
		}
		return res;
	}

	public <T> long count(Class<T> clazz, QueryBuilder query) {
		String indexName = getIndexPrefix() + "*";
		String typeName = clazz.getSimpleName().toLowerCase();
		SearchRequestBuilder searchRequestBuilder = esClient.getClient().prepareSearch(indexName).setTypes(typeName)
				.setSize(0);
		if (query != null) {
			searchRequestBuilder.setQuery(query);
		}

		SearchResponse response = searchRequestBuilder.execute().actionGet();
		return response.getHits().getTotalHits();
	}

	private <T> List<T> doCustomFind(Class<T> clazz, QueryBuilder query, SortBuilder sortBuilder, int size) {
		String indexName = getIndexPrefix() + "*";
		String typeName = clazz.getSimpleName().toLowerCase();
		SearchRequestBuilder searchRequestBuilder = esClient.getClient().prepareSearch(indexName).setTypes(typeName)
				.setSize(size);
		if (query != null) {
			searchRequestBuilder.setQuery(query);
		}
		if (sortBuilder != null) {
			searchRequestBuilder.addSort(sortBuilder);
		}
		SearchResponse response = searchRequestBuilder.execute().actionGet();
		if (!somethingFound(response)) {
			return null;
		} else {
			List<T> hits = new ArrayList<T>();
			for (int i = 0; i < response.getHits().getHits().length; i++) {
				String hit = response.getHits().getAt(i).sourceAsString();

				hits.add((T) gson.fromJson(hit, clazz));

			}
			return hits;
		}
	}

	private List<ESTimeBasedEvent> doCustomFindForEvent(String typeName, QueryBuilder query, SortBuilder sortBuilder,
			int size) {
		String indexName = getIndexPrefix() + "*";
		// String typeName = clazz.getSimpleName().toLowerCase();
		SearchRequestBuilder searchRequestBuilder = esClient.getClient().prepareSearch(indexName).setTypes(typeName)
				.setSize(size);
		if (query != null) {
			searchRequestBuilder.setQuery(query);
		}
		if (sortBuilder != null) {
			searchRequestBuilder.addSort(sortBuilder);
		}
		SearchResponse response = searchRequestBuilder.execute().actionGet();
		if (!somethingFound(response)) {
			return null;
		} else {
			List<ESTimeBasedEvent> hits = new ArrayList<ESTimeBasedEvent>();
			for (int i = 0; i < response.getHits().getHits().length; i++) {
				String hit = response.getHits().getAt(i).sourceAsString();

				ESTimeBasedEvent event;
				try {
					event = ESTimeBasedEvent.createEventFromJson(hit);
					hits.add(event);
				} catch (JSONException e) {
					log.warn("failed to parse hit from audit index. error: {}", e.getMessage());
					log.debug("failed to parse hit from audit. hit = {}", hit, e);
				}
			}
			return hits;
		}
	}

	public List<ESTimeBasedEvent> customFindEvent(String typeName, QueryBuilder query, SortBuilder sortBuilder)
			throws JSONException {
		List<ESTimeBasedEvent> results = doCustomFindForEvent(typeName, query, sortBuilder, MAX_SEARCH_SIZE);
		if (results == null) {
			results = new ArrayList<ESTimeBasedEvent>();
		}
		return results;
	}

	public <T> T customFind(Class<T> clazz, QueryBuilder query) {
		return customFind(clazz, query, null);
	}

	public <T> T customFind(Class<T> clazz, QueryBuilder query, SortBuilder sortBuilder) {
		List<T> results = doCustomFind(clazz, query, sortBuilder, 1);
		if (results == null || results.isEmpty()) {
			return null;
		} else {
			return results.iterator().next();
		}
	}

	public <T> List<T> customFindAll(Class<T> clazz, QueryBuilder query) {
		return customFindAll(clazz, query, null);
	}

	public <T> List<T> customFindAll(Class<T> clazz, QueryBuilder query, SortBuilder sortBuilder) {
		return doCustomFind(clazz, query, sortBuilder, Integer.MAX_VALUE);
	}

	private boolean somethingFound(final SearchResponse searchResponse) {
		if (searchResponse == null || searchResponse.getHits() == null || searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) {
			return false;
		}
		return true;
	}

	public String getEsHost() {
		String host = null;
		if (this.esClient != null) {
			host = this.esClient.getServerHost();
		} else {
			log.error("esClient is unavilable could not get host.");
		}
		return host;
	}

	public String getEsPort() {
		String port = null;
		if (this.esClient != null) {
			port = this.esClient.getServerPort();
		} else {
			log.error("esClient is unavilable could not get port.");
		}

		return port;
	}

}
