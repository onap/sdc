package org.openecomp.sdc.be.info;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;

public class DistributionStatusListResponseTest {
    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(DistributionStatusListResponse.class, hasValidGettersAndSetters());
    }
}