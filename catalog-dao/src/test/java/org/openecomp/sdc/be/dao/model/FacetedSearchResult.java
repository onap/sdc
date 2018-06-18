package org.openecomp.sdc.be.dao.model;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;

public class FacetedSearchResult {
	@Test
	public void shouldHaveValidGettersAndSetters(){
		assertThat(FacetedSearchResult.class, hasValidGettersAndSetters());
	}
}