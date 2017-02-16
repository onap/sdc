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
import java.util.List;

import javax.annotation.Resource;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.dao.utils.Exceptions;

/**
 * Elastic search dao that manages search operations.
 *
 * @author luc boutier
 */
public class ESGenericSearchDAO extends ESGenericIdDAO implements IGenericSearchDAO {

	private static final int MAX_SEARCH_SIZE = 1000;

	@Resource(name = "elasticsearch-client")
	private ElasticSearchClient esClient;

	@Override
	public long count(String indexName, String typeName, QueryBuilder query) {

		SearchRequestBuilder searchRequestBuilder = esClient.getClient().prepareSearch(indexName).setTypes(typeName)
				.setSize(0);
		if (query != null) {
			searchRequestBuilder.setQuery(query);
		}

		SearchResponse response = searchRequestBuilder.execute().actionGet();
		if (!somethingFound(response)) {
			return 0;
		} else {
			return response.getHits().getTotalHits();
		}
	}

	/**
	 * Convert a SearchResponse into a list of objects (json deserialization.)
	 *
	 * @param searchResponse
	 *            The actual search response from elastic-search.
	 * @param clazz
	 *            The type of objects to de-serialize.
	 * @return A list of instances that contains de-serialized data.
	 */
	public <T> List<T> toGetListOfData(SearchResponse searchResponse, Class<T> clazz) {
		// return null if no data has been found in elastic search.
		if (!somethingFound(searchResponse)) {
			return null;
		}

		List<T> result = new ArrayList<>();

		for (int i = 0; i < searchResponse.getHits().getHits().length; i++) {
			try {
				result.add(getJsonMapper().readValue(searchResponse.getHits().getAt(i).getSourceAsString(), clazz));
			} catch (IOException e) {
				Exceptions.convertToRuntimeEx(e);
			}
		}

		return result;
	}

	public <T> List<T> doCustomFind(Class<T> clazz, String indexName, String typeName, QueryBuilder query,
			SortBuilder sortBuilder) {

		List<T> result = new ArrayList<T>();
		SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(indexName).setTypes(typeName)
				.setSize(MAX_SEARCH_SIZE);
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
			for (int i = 0; i < response.getHits().getHits().length; i++) {
				String hit = response.getHits().getAt(i).sourceAsString();

				T val = null;
				try {
					val = getJsonMapper().readValue(hit, clazz);
					result.add(val);
				} catch (IOException e) {
					Exceptions.convertToRuntimeEx(e);
				}
			}
			return result;
		}
	}

	private boolean somethingFound(final SearchResponse searchResponse) {
		if (searchResponse == null || searchResponse.getHits() == null || searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) {
			return false;
		}
		return true;
	}

}
