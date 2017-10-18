package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import com.datastax.driver.core.DataType;


public class ArtifactTableDescriptionTest {

	private ArtifactTableDescription createTestSubject() {
		return new ArtifactTableDescription();
	}

	
	@Test
	public void testPrimaryKeys() throws Exception {
		ArtifactTableDescription testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.primaryKeys();
	}

	
	@Test
	public void testGetColumnDescription() throws Exception {
		ArtifactTableDescription testSubject;
		Map<String, ImmutablePair<DataType, Boolean>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getColumnDescription();
	}

	
	@Test
	public void testGetKeyspace() throws Exception {
		ArtifactTableDescription testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKeyspace();
	}

	
	@Test
	public void testGetTableName() throws Exception {
		ArtifactTableDescription testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTableName();
	}

	
	@Test
	public void testClusteringKeys() throws Exception {
		ArtifactTableDescription testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.clusteringKeys();
	}
}