package org.openecomp.sdc.be.dao.cassandra;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;

import fj.data.Either;


public class ComponentCassandraDaoTest {

	private ComponentCassandraDao createTestSubject() {
		return new ComponentCassandraDao();
	}
	
	@Test
	public void testGetAllComponentIdTimeAndType() throws Exception {
		ComponentCassandraDao testSubject;
		Either<List<ComponentCacheData>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllComponentIdTimeAndType();
	}

	


	

	
	@Test
	public void testIsTableEmpty() throws Exception {
		ComponentCassandraDao testSubject;
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isTableEmpty(tableName);
	}

	

	

}