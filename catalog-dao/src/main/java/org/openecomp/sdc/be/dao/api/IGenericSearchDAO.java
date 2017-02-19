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

import org.elasticsearch.index.query.QueryBuilder;

/**
 * A Dao that supports search and/or filter based queries.
 *
 * @author luc boutier
 */
public interface IGenericSearchDAO extends IGenericIdDAO {

	/**
	 * Get the index in which a class belongs.
	 *
	 * @param clazz
	 *            The class for which to get the index.
	 * @return The name of the index in which the class lies.
	 */
	String getIndexForType(String type);

	long count(String indexName, String typeName, QueryBuilder query);
}
