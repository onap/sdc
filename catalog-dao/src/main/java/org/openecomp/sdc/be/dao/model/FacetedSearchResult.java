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

package org.openecomp.sdc.be.dao.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains results for a search query.
 * 
 */

@SuppressWarnings("PMD.UnusedPrivateField")
public class FacetedSearchResult extends GetMultipleDataResult {
	private static final long serialVersionUID = 1L;

	private Map<String, FacetedSearchFacet[]> facets;

	/**
	 * Argument constructor.
	 * 
	 * @param from
	 *            The start index of the returned elements.
	 * @param to
	 *            The end index of the returned elements.
	 * @param queryDuration
	 *            The duration of the query.
	 * @param totalResults
	 *            The total results for this query.
	 * @param types
	 *            The types of data found.
	 * @param data
	 *            The found data.
	 * @param hashMap
	 *            The facets if any for the query.
	 */
	public FacetedSearchResult(final int from, final int to, final long queryDuration, final long totalResults,
			final String[] types, final Object[] data, final HashMap<String, FacetedSearchFacet[]> hashMap) {
		super(types, data, queryDuration, totalResults, from, to);
		this.facets = hashMap;
	}

	public Map<String, FacetedSearchFacet[]> getFacets() {
		return facets;
	}

	public void setFacets(Map<String, FacetedSearchFacet[]> facets) {
		this.facets = facets;
	}

	public FacetedSearchResult() {
	}

}
