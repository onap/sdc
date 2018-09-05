package org.openecomp.sdc.be.config;

import com.google.code.beanmatchers.BeanMatchers;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;

public class DmaapConsumerConfigurationTest {

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(DmaapConsumerConfiguration.class, BeanMatchers.hasValidGettersAndSetters());
	}

    @Test
    public void shouldHaveValidCtor() {
        assertThat(DmaapConsumerConfiguration.class, BeanMatchers.hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveValidToString() {
        assertThat(DmaapConsumerConfiguration.class, BeanMatchers.hasValidBeanToString());
    }

    @Test
    public void shouldHaveValidGettersAndSettersNested() {
        assertThat(DmaapConsumerConfiguration.Credential.class, BeanMatchers.hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidCtorNested() {
        assertThat(DmaapConsumerConfiguration.Credential.class, BeanMatchers.hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveValidToStringNested() {
        assertThat(DmaapConsumerConfiguration.Credential.class, BeanMatchers.hasValidBeanToString());
    }
}