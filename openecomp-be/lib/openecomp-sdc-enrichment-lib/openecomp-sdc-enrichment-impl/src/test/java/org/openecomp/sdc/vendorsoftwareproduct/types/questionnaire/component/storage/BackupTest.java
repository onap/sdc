package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.storage;

import org.junit.Test;
import org.mockito.Mockito;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;

public class BackupTest {

    @Test
    public void testConstructor() {
        assertThat(Backup.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(Backup.class, hasValidGettersAndSetters());
    }
}