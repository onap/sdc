package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.util.List;
import java.util.Map;


public class CategoryEventTableDescriptionTest {

	private CategoryEventTableDescription createTestSubject() {
		return new CategoryEventTableDescription();
	}

	
	@Test
	public void testPrimaryKeys() throws Exception {
		CategoryEventTableDescription testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.primaryKeys();
	}

	
	@Test
	public void testClusteringKeys() throws Exception {
		CategoryEventTableDescription testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.clusteringKeys();
	}

	

	
	@Test
	public void testGetKeyspace() throws Exception {
		CategoryEventTableDescription testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKeyspace();
	}

	
	@Test
	public void testGetTableName() throws Exception {
		CategoryEventTableDescription testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTableName();
	}
	
	@Test
	public void testGetColumnDescription() throws Exception {
		CategoryEventTableDescription testSubject;
		Map<String, ImmutablePair<DataType, Boolean>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getColumnDescription();
		
		CategoryEventTableDescription.CEFieldsDescription.ACTION.getType();
		CategoryEventTableDescription.CEFieldsDescription.ACTION.isIndexed();
	}
}