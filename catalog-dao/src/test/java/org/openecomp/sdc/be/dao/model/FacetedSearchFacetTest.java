package org.openecomp.sdc.be.dao.model;

import javax.annotation.Generated;

import org.junit.Test;


public class FacetedSearchFacetTest {

	private FacetedSearchFacet createTestSubject() {
		return new FacetedSearchFacet("", 0);
	}

	
	@Test
	public void testGetFacetValue() throws Exception {
		FacetedSearchFacet testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFacetValue();
	}

	
	@Test
	public void testSetFacetValue() throws Exception {
		FacetedSearchFacet testSubject;
		String facetValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFacetValue(facetValue);
	}

	
	@Test
	public void testGetCount() throws Exception {
		FacetedSearchFacet testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCount();
	}

	
	@Test
	public void testSetCount() throws Exception {
		FacetedSearchFacet testSubject;
		long count = 555;

		// default test
		testSubject = createTestSubject();
		testSubject.setCount(count);
	}
}