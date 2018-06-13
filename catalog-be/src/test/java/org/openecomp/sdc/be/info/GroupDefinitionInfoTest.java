package org.openecomp.sdc.be.info;

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
	
	@Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(GroupDefinitionInfo.class, BeanMatchers.hasValidGettersAndSetters());
    }
    
    @Test
    public void testToString() {
        (new GroupDefinitionInfo()).toString();
    }
}