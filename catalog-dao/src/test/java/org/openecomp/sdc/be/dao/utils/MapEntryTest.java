package org.openecomp.sdc.be.dao.utils;

import javax.annotation.Generated;

import org.apache.tinkerpop.gremlin.structure.T;
import org.elasticsearch.common.recycler.Recycler.V;
import org.junit.Test;


public class MapEntryTest {

	private MapEntry createTestSubject() {
		return new MapEntry();
	}

	



	
	@Test
	public void testSetKey() throws Exception {
		MapEntry testSubject;
		T key = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setKey(key);
	}

	


	
	@Test
	public void testSetValue() throws Exception {
		MapEntry testSubject;
		V value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}
}