package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;


public class ArtifactAccessInfoTest {


    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ArtifactAccessInfo.class, hasValidGettersAndSetters());
    }

    @Test
    public void testCtor() throws Exception {
        new ArtifactAccessInfo(new ESArtifactData());

    }
}