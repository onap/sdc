package org.openecomp.sdc.be.dao.model;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;

public class GetMultipleDataResultTest {
	
	@Test
	public void testCtor() throws Exception {
		new GetMultipleDataResult<>(new String [1], new Object[1]);
		new GetMultipleDataResult<>(new String [1], new String [1], 0L, 0L, 1, 1);
	}
  
	@Test
	public void shouldHaveValidGettersAndSetters(){
		assertThat(GetMultipleDataResultTest.class, hasValidGettersAndSetters());
	}
}