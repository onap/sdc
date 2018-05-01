package org.openecomp.sdc.be.dao.titan;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;

import com.thinkaurelius.titan.graphdb.query.TitanPredicate;

import java.util.Map.Entry;
public class TitanUtilsTest {

	@Test
	public void testBuildNotInPredicate() throws Exception {
		String propKey = "";
		Collection<T> notInCollection = null;
		Map<String, Entry<TitanPredicate, Object>> result;

		// default test
		result = TitanUtils.buildNotInPredicate(propKey, notInCollection);
	}
}