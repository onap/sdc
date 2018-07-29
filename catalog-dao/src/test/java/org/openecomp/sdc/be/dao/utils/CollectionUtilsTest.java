package org.openecomp.sdc.be.dao.utils;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CollectionUtilsTest {

	@Test
	public void testMerge() throws Exception {
		Set<T> source = null;
		Set<T> target = null;
		Set<T> result;

		// test 1
		target = null;
		result = CollectionUtils.merge(source, target);
		Assert.assertEquals(null, result);

		// test 2
		source = null;
		result = CollectionUtils.merge(source, target);
		Assert.assertEquals(null, result);
	}

	@Test
	public void testMerge_1() throws Exception {
		Map<String, String> source = new HashMap();
		Map<String, String> target = new HashMap();
		boolean override = false;
		Map<String, String> result;

		result = CollectionUtils.merge(source, target, override);
		Assert.assertEquals(null, result);
		
		// test 1
		target = null;
		result = CollectionUtils.merge(source, target, override);
		Assert.assertEquals(null, result);

		// test 2
		source = null;
		result = CollectionUtils.merge(source, target, override);
		Assert.assertEquals(null, result);
	}

	@Test
	public void testMerge_2() throws Exception {
		List<T> source = new LinkedList<>();
		List<T> target = new LinkedList<>();
		List<T> result;

		// test 1
		result = CollectionUtils.merge(source, target);
		Assert.assertEquals(target, result);
	}
}