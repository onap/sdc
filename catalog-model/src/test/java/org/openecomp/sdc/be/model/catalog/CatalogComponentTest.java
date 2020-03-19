package org.openecomp.sdc.be.model.catalog;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class CatalogComponentTest {

    private CatalogComponent createTestSubject() {
        return new CatalogComponent();
    }

    @Test
    public void testGetTags() {
        CatalogComponent testSubject;
        List<String> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getTags();
        Assert.assertNotNull(result);
        Assert.assertEquals(result, new ArrayList<>());
    }

    @Test
    public void testSetTags() {
        CatalogComponent testSubject;
        List<String> result;

        // default test
        testSubject = createTestSubject();
        testSubject.setTags(new ArrayList<>());
        result = testSubject.getTags();
        Assert.assertNotNull(result);
        Assert.assertEquals(result, new ArrayList<>());
    }

    @Test
    public void testGetCategories() {
        CatalogComponent testSubject;
        List<CategoryDefinition> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getCategories();
        Assert.assertNotNull(result);
        Assert.assertEquals(result, new ArrayList<>());
    }
}