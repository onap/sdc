package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class EcompOperationalEnvironmentEventTableDescTest {

	private EcompOperationalEnvironmentEventTableDesc createTestSubject() {
		return new EcompOperationalEnvironmentEventTableDesc();
	}

	@Test
	public void testPrimaryKeys() throws Exception {
		EcompOperationalEnvironmentEventTableDesc testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.primaryKeys();
	}

	@Test
	public void testClusteringKeys() throws Exception {
		EcompOperationalEnvironmentEventTableDesc testSubject;
		List<ImmutablePair<String, DataType>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.clusteringKeys();
	}

	@Test
	public void testGetKeyspace() throws Exception {
		EcompOperationalEnvironmentEventTableDesc testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKeyspace();
	}

	@Test
	public void testGetColumnDescription() throws Exception {
		EcompOperationalEnvironmentEventTableDesc testSubject;
		Map<String, ImmutablePair<DataType, Boolean>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getColumnDescription();
	}

	@Test
	public void testGetTableName() throws Exception {
		EcompOperationalEnvironmentEventTableDesc testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTableName();
	}
}