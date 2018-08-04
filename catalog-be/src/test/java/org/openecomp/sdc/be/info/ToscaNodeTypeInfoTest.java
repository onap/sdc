package org.openecomp.sdc.be.info;

import org.junit.Test;

import com.google.code.beanmatchers.BeanMatchers;
import static org.hamcrest.MatcherAssert.assertThat;


public class ToscaNodeTypeInfoTest {


    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ToscaNodeTypeInfo.class, BeanMatchers.hasValidGettersAndSetters());
    }
    
    @Test
    public void allPropertiesShouldBeRepresentedInToStringOutput() {
        assertThat(ToscaNodeTypeInfo.class, BeanMatchers.hasValidBeanToString());
    }
}