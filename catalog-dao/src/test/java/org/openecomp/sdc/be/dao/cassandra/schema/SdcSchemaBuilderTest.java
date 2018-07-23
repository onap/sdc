package org.openecomp.sdc.be.dao.cassandra.schema;

import com.datastax.driver.core.Session;
import mockit.Deencapsulation;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.Configuration.CassandrConfig.KeyspaceConfig;
import org.openecomp.sdc.be.dao.cassandra.schema.SdcSchemaBuilder.ReplicationStrategy;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SdcSchemaBuilderTest extends DAOConfDependentTest{

	private SdcSchemaBuilder createTestSubject() {
		return new SdcSchemaBuilder();
	}

	@Test
	public void testHandle1707OSMigration() throws Exception {
		Map<String, Map<String, List<String>>> cassndraMetadata = new HashMap<>();
		Map<String, List<ITableDescription>> schemeData = new HashMap<>();

		// default test
		Deencapsulation.invoke(SdcSchemaBuilder.class, "handle1707OSMigration", cassndraMetadata, schemeData);
	}

	@Test
	public void testCreateSchema() throws Exception {
		boolean result;

		// default test
		result = SdcSchemaBuilder.createSchema();
	}

	@Test
	public void testDeleteSchema() throws Exception {
		boolean result;

		// default test
		result = SdcSchemaBuilder.deleteSchema();
	}

	@Test
	public void testCreateIndexName() throws Exception {
		String table = "";
		String column = "";
		String result;

		// default test
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createIndexName", table, column);
	}

	@Test
	public void testCreateKeyspace() throws Exception {
		String keyspace = "mock";
		Map<String, Map<String, List<String>>> cassndraMetadata = new HashMap<>();
		Session session = Mockito.mock(Session.class);
		boolean result;

		// default test
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createKeyspace",
				keyspace, cassndraMetadata, session);
		
		cassndraMetadata.put(keyspace, new HashMap<>());
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createKeyspace",
				keyspace, cassndraMetadata, session);
	}

	@Test
	public void testGetSchemeData() throws Exception {
		Map<String, List<ITableDescription>> result;

		// default test
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "getSchemeData");
	}

	@Test
	public void testCreateKeyspaceQuereyString() throws Exception {
		String keyspace = "mock";
		KeyspaceConfig keyspaceInfo = new KeyspaceConfig();
		String result;

		// default test
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createKeyspaceQuereyString", keyspace, keyspaceInfo);
		
		keyspaceInfo.setReplicationStrategy(ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getName());
		LinkedList<String> replicationInfo = new LinkedList<>();
		keyspaceInfo.setReplicationInfo(replicationInfo);
		//Test1
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createKeyspaceQuereyString", keyspace, keyspaceInfo);
		replicationInfo.add("mock1");
		replicationInfo.add("mock2");
		
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createKeyspaceQuereyString", keyspace, keyspaceInfo);
		
		//Test2
		keyspaceInfo.setReplicationStrategy(ReplicationStrategy.SIMPLE_STRATEGY.getName());
		result = Deencapsulation.invoke(SdcSchemaBuilder.class, "createKeyspaceQuereyString", keyspace, keyspaceInfo);
	}
}