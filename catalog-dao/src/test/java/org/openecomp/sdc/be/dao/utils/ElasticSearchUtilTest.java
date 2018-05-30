package org.openecomp.sdc.be.dao.utils;

import org.elasticsearch.action.search.SearchResponse;
import org.junit.Assert;
import org.junit.Test;

public class ElasticSearchUtilTest {

	@Test
	public void testIsResponseEmpty() throws Exception {
		SearchResponse searchResponse = null;
		boolean result;

		// test 1
		searchResponse = null;
		result = ElasticSearchUtil.isResponseEmpty(searchResponse);
		Assert.assertEquals(true, result);
	}
}