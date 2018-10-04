package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class RequestedVersionInvalidErrorBuilderTest {
    @Test
    public void test() {
        RequestedVersionInvalidErrorBuilder builder = new RequestedVersionInvalidErrorBuilder();
        Assert.assertNotNull(builder);
    }
}