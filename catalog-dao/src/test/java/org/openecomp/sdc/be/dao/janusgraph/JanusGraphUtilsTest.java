package org.openecomp.sdc.be.dao.janusgraph;

import org.janusgraph.graphdb.query.JanusGraphPredicate;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
public class JanusGraphUtilsTest {

	@Test
	public void testBuildNotInPredicate() throws Exception {
		String propKey = "";
		Collection<T> notInCollection = null;
		Map<String, Entry<JanusGraphPredicate, Object>> result;

		// default test
		result = JanusGraphUtils.buildNotInPredicate(propKey, notInCollection);
	}
}