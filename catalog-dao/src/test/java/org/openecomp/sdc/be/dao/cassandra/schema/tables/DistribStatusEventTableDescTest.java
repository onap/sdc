package org.openecomp.sdc.be.dao.cassandra.schema.tables;

import com.datastax.driver.core.DataType;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.openecomp.sdc.be.dao.cassandra.schema.tables.DistribStatusEventTableDesc.DSEFieldsDescription;

import java.util.HashMap;
import java.util.Map;

public class DistribStatusEventTableDescTest {

	private DistribStatusEventTableDesc createTestSubject() {
		return new DistribStatusEventTableDesc();
	}

	@Test
	public void testUpdateColumnDistribDescription() throws Exception {
		DistribStatusEventTableDesc testSubject;
		Map<String, ImmutablePair<DataType, Boolean>> columns = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "updateColumnDistribDescription", columns);
	}

	@Test
	public void testGetTableName() throws Exception {
		DistribStatusEventTableDesc testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTableName();
	}
	
	@Test
	public void testDSEFieldsDescription() throws Exception {
		DSEFieldsDescription testSubject = DistribStatusEventTableDesc.DSEFieldsDescription.CONSUMER_ID;
		
		testSubject.getName();
		testSubject.getType();
		testSubject.isIndexed();
	}
}