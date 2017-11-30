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

import org.openecomp.sdc.be.dao.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

public class CassandraTest {
	private static Logger log = LoggerFactory.getLogger(CassandraTest.class.getName());
	private Cluster cluster;

	// #\@Test
	public void testCrud() {
		String node = "mtanjv9sdcg44";

		cluster = Cluster.builder().addContactPoint(node).build();

		// Query
		String query = "CREATE KEYSPACE IF NOT EXISTS dstest WITH replication "
				+ "= {'class':'SimpleStrategy', 'replication_factor':1};";

		String queryTable = "CREATE TABLE IF NOT EXISTS accounts(email varchar  PRIMARY KEY, name varchar);";

		Session session = cluster.connect();
		// Executing the query
		session.execute(query);
		// //using the KeySpace
		session.execute("USE dstest");
		session.execute(queryTable);

		Mapper<Account> mapper = new MappingManager(session).mapper(Account.class);
		Account account = new Account("John Doe", "jd@example.com");
		// Class<? extends Account> class1 = account.getClass();
		// Class class2 = Account.class;
		mapper.save(account);

		Account whose = mapper.get("jd@example.com");
		log.debug("Account name: {}", whose.getName());

		account.setName("Samanta Smit");
		mapper.save(account);
		whose = mapper.get("jd@example.com");
		log.debug("Account name: {}", whose.getName());

		mapper.delete(account);
		whose = mapper.get("jd@example.com");

		cluster.close();
	}
}
