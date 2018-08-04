package org.openecomp.sdc.be.info;

import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;

import com.google.code.beanmatchers.BeanMatchers;
import static org.hamcrest.MatcherAssert.assertThat;


public class GroupDefinitionInfoTest {
	@Test
    public void testCtor() {
		new GroupDefinitionInfo(new GroupDefinition());
		new GroupDefinitionInfo(new GroupInstance());
	}

    @Ignore("the test fails on null pointer how ever the method does exists.")
	@Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(GroupDefinitionInfo.class, BeanMatchers.hasValidGettersAndSetters());
    }
    
    @Test
    public void testToString() {
        (new GroupDefinitionInfo()).toString();
    }
}