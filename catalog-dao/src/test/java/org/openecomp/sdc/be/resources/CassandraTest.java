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

import java.net.InetSocketAddress;

import org.openecomp.sdc.be.dao.Account;
import org.openecomp.sdc.common.log.wrappers.Logger;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

public class CassandraTest {
	private static Logger log = Logger.getLogger(CassandraTest.class.getName());
	private CqlSession session;

	// #\@Test
	public void testCrud() {
		String node = "mtanjv9sdcg44";
		session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(node, 9042))
                .withLocalDatacenter("datacenter1") // required in v4
                .build();

		// Query
		String query = "CREATE KEYSPACE IF NOT EXISTS dstest WITH replication "
				+ "= {'class':'SimpleStrategy', 'replication_factor':1};";

		

		
		// Executing the query
		session.execute(query);
		// //using the KeySpace
		session.execute("USE dstest");
		String queryTable = "CREATE TABLE IF NOT EXISTS accounts(email varchar  PRIMARY KEY, name varchar);";
		session.execute(queryTable);

	
		Account account = new Account("John Doe", "jd@example.com");
		session.execute("INSERT INTO accounts (email, name) VALUES (?, ?)",
                account.getEmail(), account.getName());
		ResultSet rs = session.execute("SELECT email, name FROM accounts WHERE email = ?", account.getEmail());
        Row row = rs.one();
        if (row != null) {
            log.debug("Account name: {}", row.getString("name"));
        }

		account.setName("Samanta Smit");
		session.execute("UPDATE accounts SET name = ? WHERE email = ?", account.getName(), account.getEmail());
		
		rs = session.execute("SELECT email, name FROM accounts WHERE email = ?", account.getEmail());
        row = rs.one();
        if (row != null) {
            log.debug("Updated account name: {}", row.getString("name"));
        }


		session.execute("DELETE FROM accounts WHERE email = ?", account.getEmail());

		rs = session.execute("SELECT email, name FROM accounts WHERE email = ?", account.getEmail());
        row = rs.one();
        if (row == null) {
            log.debug("Account successfully deleted");
        }

        session.close();
	}
}
