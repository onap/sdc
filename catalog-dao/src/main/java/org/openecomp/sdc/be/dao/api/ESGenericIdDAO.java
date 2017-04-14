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

package org.openecomp.sdc.be.dao.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.Client;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.exception.IndexingServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ESGenericIdDAO implements IGenericIdDAO {

	private static Logger log = LoggerFactory.getLogger(ESGenericIdDAO.class.getName());

	@Resource(name = "elasticsearch-client")
	private ElasticSearchClient esClient;

	private ObjectMapper jsonMapper = new ObjectMapper();
	private final Map<String, String> typesToIndices = new HashMap<String, String>();

	public Client getClient() {
		return this.esClient.getClient();
	}

	public ElasticSearchClient getEsClient() {
		return esClient;
	}

	public ObjectMapper getJsonMapper() {
		return jsonMapper;
	}

	public void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public void addToIndicesMap(String type, String index) {
		typesToIndices.put(type, index);
	}

	public String getIndexForType(String type) {
		return typesToIndices.get(type);
	}

	@Override
	public <T> T findById(String typeName, String id, Class<T> clazz) {

		String indexName = getIndexForType(typeName);
		GetResponse response = getClient().prepareGet(indexName, typeName, id).execute().actionGet();

		if (response == null || !response.isExists()) {
			log.debug("Nothing found in index <{}>, type <{}>, for Id <{}>.", indexName, typeName, id);
			return null;
		}

		log.debug("Found one in index <{}>, type <{}>, for Id <{}>.", indexName, typeName, id);

		T ret = null;
		try {
			ret = (T) jsonMapper.readValue(response.getSourceAsString(), clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	@Override
	public <T> List<T> findByIds(String typeName, Class<T> clazz, String... ids) {
		String indexName = getIndexForType(typeName);
		MultiGetResponse response = getClient().prepareMultiGet().add(indexName, typeName, ids).execute().actionGet();

		if (response == null || response.getResponses() == null || response.getResponses().length == 0) {
			log.debug("Nothing found in index <{}>, type <{}>, for Ids <{}>.", indexName, typeName,
					Arrays.toString(ids));
			return null;
		}

		List<T> result = new ArrayList<>();
		for (MultiGetItemResponse getItemResponse : response.getResponses()) {
			if (getItemResponse.getResponse().isExists()) {
				T val = null;
				try {
					val = jsonMapper.readValue(getItemResponse.getResponse().getSourceAsString(), clazz);
					result.add(val);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return result;
	}

	protected void saveResourceData(String typeName, Object data, String id) throws JsonProcessingException {
		String indexName = getIndexForType(typeName);

		log.debug("ESGenericIdDAO saveResourceData resource indexName: {} typeName is: {}", indexName, typeName);

		String json = getJsonMapper().writeValueAsString(data);
		log.debug("ESGenericIdDAO saveResourceData resource id is: {}", id);
		try {
			getClient().prepareIndex(indexName, typeName, id).setSource(json).setRefresh(true).execute().actionGet();
		} catch (Exception e) {
			log.error("failed to write data with id {} to elasticsearch type {}. error: {}", id, typeName,
					e.getMessage());
			throw e;
		}
	}

	@Override
	public void delete(String typeName, String id) {
		assertIdNotNullFor(id, "delete");
		String indexName = getIndexForType(typeName);
		getClient().prepareDelete(indexName, typeName, id).setRefresh(true).execute().actionGet();
	}

	public void deleteIndex(String indexName) {
		DeleteIndexResponse actionGet = getClient().admin().indices().delete(new DeleteIndexRequest(indexName))
				.actionGet();
		if (!actionGet.isAcknowledged()) {
			log.error("failed to delete index {}", indexName);
		}
	}

	private void assertIdNotNullFor(String id, String operation) {
		if (id == null || id.trim().isEmpty()) {
			log.error("Null or empty Id is not allowed for operation <" + operation + ">.");
			throw new IndexingServiceException("Null or empty Id is not allowed for operation <" + operation + ">.");
		}
	}

	public static String indexTypeFromClass(Class<?> clazz) {
		return clazz.getSimpleName().toLowerCase();
	}
}
