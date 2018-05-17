package org.openecomp.sdc.be.info;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;


public class DistributionStatusOfServiceInfoTest {


	@Test
	public void testCtor() throws Exception {
		new DistributionStatusOfServiceInfo("","","","");
	}

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(DistributionStatusOfServiceInfo.class, hasValidGettersAndSetters());
	}

}