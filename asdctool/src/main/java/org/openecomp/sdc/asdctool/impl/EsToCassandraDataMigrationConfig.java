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

package org.openecomp.sdc.asdctool.impl;

import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsToCassandraDataMigrationConfig {
	@Bean(name = "DataMigrationBean")
	public DataMigration dataMigration() {
		return new DataMigration();
	}

	@Bean(name = "artifact-cassandra-dao")
	public ArtifactCassandraDao artifactCassandraDao() {
		return new ArtifactCassandraDao();
	}

	@Bean(name = "audit-cassandra-dao")
	public AuditCassandraDao auditCassandraDao() {
		return new AuditCassandraDao();
	}

	@Bean(name = "cassandra-client")
	public CassandraClient cassandraClient() {
		return new CassandraClient();
	}

}
