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

package org.openecomp.sdc.be.dao.utils;

import org.elasticsearch.action.search.SearchResponse;

/**
 * Utility class to work with elastic search responses.
 * 
 */
public final class ElasticSearchUtil {
	private ElasticSearchUtil() {
	}

	/**
	 * Checks if a search response from elastic search contains results or not.
	 * 
	 * @param searchResponse
	 *            The ES search response object.
	 * @return True if the response does not contain any result, false if the
	 *         response does contains results.
	 */
	public static boolean isResponseEmpty(SearchResponse searchResponse) {
		if (searchResponse == null || searchResponse.getHits() == null || searchResponse.getHits().getHits() == null
				|| searchResponse.getHits().getHits().length == 0) {
			return true;
		}
		return false;
	}
}
