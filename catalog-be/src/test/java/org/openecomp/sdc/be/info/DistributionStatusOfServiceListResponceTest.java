package org.openecomp.sdc.be.info;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;


public class DistributionStatusOfServiceListResponceTest {
    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(DistributionStatusOfServiceListResponce.class, hasValidGettersAndSetters());
    }
}