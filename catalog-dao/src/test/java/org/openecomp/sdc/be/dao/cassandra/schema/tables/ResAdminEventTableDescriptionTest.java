package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import com.datastax.driver.core.DataType;


public class ResAdminEventTableDescriptionTest {

	private ResAdminEventTableDescription createTestSubject() {
		return new ResAdminEventTableDescription();
	}

	
	@Test
	public void testPrimaryKeys() throws Exception {
		ResAdminEventTableDescription testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.primaryKeys();
	}

	
	@Test
	public void testClusteringKeys() throws Exception {
		ResAdminEventTableDescription testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.clusteringKeys();
	}

	


	
	@Test
	public void testGetKeyspace() throws Exception {
		ResAdminEventTableDescription testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKeyspace();
	}

	
	@Test
	public void testGetTableName() throws Exception {
		ResAdminEventTableDescription testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTableName();
	}
}