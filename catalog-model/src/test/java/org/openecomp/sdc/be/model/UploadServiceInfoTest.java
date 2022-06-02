/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class UploadServiceInfoTest {

    private UploadServiceInfo createTestSubject() {
        return new UploadServiceInfo();
    }

    @Test
    public void testCtor() {
        new UploadServiceInfo("mock", "mock", "mock", new LinkedList<>(), "mock", "mock", "mock", "mock/mock/mock",
                "mock", "mock", "mock", "mock", "mock", "mock", "mock", "mock", "mock", "mock", new LinkedList<>(), "mock", "mock", "mock",
                "mock", "mock", "mock", "mock", "mock", "mock", "mock", new HashMap<>(), "mock", "mock");
    }

    @Test
    public void testAddSubCategory() {
        UploadServiceInfo testSubject;
        String category = "";
        String subCategory = "";

        // test 1
        testSubject = createTestSubject();
        category = null;
        subCategory = null;
        testSubject.addSubCategory(category, subCategory);
        List<CategoryDefinition> categories = testSubject.getCategories();
        Assert.assertNull(categories);

        // test 2
        testSubject = createTestSubject();
        category = "";
        subCategory = null;
        testSubject.addSubCategory(category, subCategory);
        List<CategoryDefinition> categories2 = testSubject.getCategories();
        for (CategoryDefinition categoryDefinition : categories2) {
            Assert.assertEquals("", categoryDefinition.getName());
        }

        // test 3
        testSubject = createTestSubject();
        subCategory = "";
        category = null;
        testSubject.addSubCategory(category, subCategory);
        List<CategoryDefinition> categories3 = testSubject.getCategories();
        for (CategoryDefinition categoryDefinition : categories3) {
            List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
            for (SubCategoryDefinition subcategory : subcategories) {
                Assert.assertEquals("", subcategory.getName());
            }
        }

        // test 4
        testSubject = createTestSubject();
        subCategory = "mock";
        category = "mock";
        testSubject.addSubCategory(category, subCategory);
        List<CategoryDefinition> categories4 = testSubject.getCategories();
        for (CategoryDefinition categoryDefinition : categories4) {
            Assert.assertEquals("mock", categoryDefinition.getName());
        }
    }
}