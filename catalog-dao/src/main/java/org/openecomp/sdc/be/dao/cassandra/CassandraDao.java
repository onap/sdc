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

package org.openecomp.sdc.be.dao.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.MappingManager;

import fj.data.Either;

public abstract class CassandraDao {

	private static Logger logger = LoggerFactory.getLogger(CassandraDao.class.getName());

	protected Session session;
	protected MappingManager manager;

	@Autowired
	protected CassandraClient client;

	/**
	 * the method checks if the given table is empty under the keyspace the
	 * session was opened to.
	 * 
	 * @param tableName
	 *            the name of the table we want to check
	 * @return returns true if the table was empty
	 */
	protected Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {

		Statement select = QueryBuilder.select().countAll().from(tableName).limit(10);
		try {
			ResultSet res = session.execute(select);
			return Either.left((res.one().getLong("count") != 0 ? false : true));

		} catch (Exception e) {
			logger.debug("Failed check if table is empty", e);
			return Either.right(CassandraOperationStatus.GENERAL_ERROR);
		}
	}

}
