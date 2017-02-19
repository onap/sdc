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

import java.util.List;

/**
 * A DAO that allows accessing data by Id or / and multiple Ids.
 * 
 * @author Igor Ngouagna
 */
public interface IGenericIdDAO {

	/**
	 * Find an instance from the given class.
	 * 
	 * @param clazz
	 *            The class of the object to find.
	 * @param id
	 *            The id of the object.
	 * @return The object that has the given id or null if no object matching
	 *         the request is found.
	 */
	<T> T findById(String typeName, String id, Class<T> clazz);

	/**
	 * Find instances by id
	 * 
	 * @param clazz
	 *            The class for which to find an instance.
	 * @param ids
	 *            array of id of the data to find.
	 * @return List of Objects that has the given ids or empty list if no object
	 *         matching the request is found.
	 */
	<T> List<T> findByIds(String typeName, Class<T> clazz, String... ids);

	/**
	 * Delete an instance from the given class.
	 * 
	 * @param clazz
	 *            The class of the object to delete.
	 * @param id
	 *            The id of the object to delete.
	 */
	void delete(String typeName, String id);

}
