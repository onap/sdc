package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class RevisionIdNotFoundErrorBuilderTest {
    @Test
    public void test() {
        RevisionIdNotFoundErrorBuilder builder = new RevisionIdNotFoundErrorBuilder();
        Assert.assertNotNull(builder);
    }
}