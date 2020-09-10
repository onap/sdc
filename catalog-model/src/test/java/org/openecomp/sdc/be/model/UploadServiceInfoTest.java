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
import org.openecomp.sdc.common.api.UploadArtifactInfo;

import java.util.LinkedList;
import java.util.List;


public class UploadServiceInfoTest {

    private UploadServiceInfo createTestSubject() {
        return new UploadServiceInfo();
    }

    @Test
    public void testCtor() {
        new UploadServiceInfo("mock", "mock", "mock", new LinkedList<>(), "mock", "mock", "mock", "mock/mock/mock",
                "mock", "mock", "mock", "mock", "mock", "mock", "mock", new LinkedList<>(), "mock", "mock", "mock",
                "mock", "mock", "mock", "mock", "mock", "mock");
    }

    @Test
    public void testGetPayloadData() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getPayloadData();
    }

    @Test
    public void testSetPayloadData() {
        UploadServiceInfo testSubject;
        String payload = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setPayloadData(payload);
    }

    @Test
    public void testGetPayloadName() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getPayloadName();
    }

    @Test
    public void testSetPayloadName() {
        UploadServiceInfo testSubject;
        String payloadName = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setPayloadName(payloadName);
    }

    @Test
    public void testGetDescription() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getDescription();
    }

    @Test
    public void testSetDescription() {
        UploadServiceInfo testSubject;
        String description = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setDescription(description);
    }

    @Test
    public void testGetTags() {
        UploadServiceInfo testSubject;
        List<String> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getTags();
    }

    @Test
    public void testSetTags() {
        UploadServiceInfo testSubject;
        List<String> tags = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setTags(tags);
    }

    @Test
    public void testGetArtifactList() {
        UploadServiceInfo testSubject;
        List<UploadArtifactInfo> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getArtifactList();
    }

    @Test
    public void testSetArtifactList() {
        UploadServiceInfo testSubject;
        List<UploadArtifactInfo> artifactsList = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setArtifactList(artifactsList);
    }

    @Test
    public void testGetInvariantUUID() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getInvariantUUID();
    }

    @Test
    public void testSetInvariantUUID() {
        UploadServiceInfo testSubject;
        String invariantUUID = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setInvariantUUID(invariantUUID);
    }

    @Test
    public void testGetUUID() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getUUID();
    }

    @Test
    public void testSetUUID() {
        UploadServiceInfo testSubject;
        String UUID = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setUUID(UUID);
    }

    @Test
    public void testGetType() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getType();
    }

    @Test
    public void testSetType() {
        UploadServiceInfo testSubject;
        String type = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setType(type);
    }

    @Test
    public void testGetCategory() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getCategory();
    }

    @Test
    public void testSetCategory() {
        UploadServiceInfo testSubject;
        String category = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setCategory(category);
    }

    @Test
    public void testGetSubcategory() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getSubcategory();
    }

    @Test
    public void testSetSubcategory() {
        UploadServiceInfo testSubject;
        String subcategory = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setSubcategory(subcategory);
    }

    @Test
    public void testGetResourceVendor() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getResourceVendor();
    }

    @Test
    public void testSetResourceVendor() {
        UploadServiceInfo testSubject;
        String resourceVendor = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setResourceVendor(resourceVendor);
    }

    @Test
    public void testGetResourceVendorRelease() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getResourceVendorRelease();
    }

    @Test
    public void testSetResourceVendorRelease() {
        UploadServiceInfo testSubject;
        String resourceVendorRelease = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setResourceVendorRelease(resourceVendorRelease);
    }

    @Test
    public void testGetServiceRole() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getServiceRole();
    }

    @Test
    public void testSetServiceRole() {
        UploadServiceInfo testSubject;
        String serviceRole = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setServiceRole(serviceRole);
    }

    @Test
    public void testGetServiceEcompNaming() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getServiceEcompNaming();
    }

    @Test
    public void testSetServiceEcompNaming() {
        UploadServiceInfo testSubject;
        String serviceEcompNaming = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setServiceEcompNaming(serviceEcompNaming);
    }

    @Test
    public void testGetEcompGeneratedNaming() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getEcompGeneratedNaming();
    }

    @Test
    public void testSetEcompGeneratedNaming() {
        UploadServiceInfo testSubject;
        String ecompGeneratedNaming = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
    }

    @Test
    public void testGetNamingPolicy() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getNamingPolicy();
    }

    @Test
    public void testSetNamingPolicy() {
        UploadServiceInfo testSubject;
        String namingPolicy = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setNamingPolicy(namingPolicy);
    }

    @Test
    public void testGetIcon() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getIcon();
    }

    @Test
    public void testSetIcon() {
        UploadServiceInfo testSubject;
        String icon = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setIcon(icon);
    }

    @Test
    public void testHashCode() {
        UploadServiceInfo testSubject;
        int result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.hashCode();
    }

    @Test
    public void testEquals() {
        UploadServiceInfo testSubject;
        Object obj = null;
        boolean result;

        // test 1
        testSubject = createTestSubject();
        result = testSubject.equals(obj);
        Assert.assertEquals(false, result);
        result = testSubject.equals(new Object());
        Assert.assertEquals(false, result);
        result = testSubject.equals(testSubject);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testGetContactId() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getContactId();
    }

    @Test
    public void testSetContactId() {
        UploadServiceInfo testSubject;
        String contactId = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setContactId(contactId);
    }

    @Test
    public void testGetName() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getName();
    }

    @Test
    public void testSetName() {
        UploadServiceInfo testSubject;
        String name = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setName(name);
    }

    @Test
    public void testGetServiceIconPath() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getServiceIconPath();
    }

    @Test
    public void testSetServiceIconPath() {
        UploadServiceInfo testSubject;
        String serviceIconPath = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setServiceIconPath(serviceIconPath);
    }

    @Test
    public void testGetVendorName() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getVendorName();
    }

    @Test
    public void testSetVendorName() {
        UploadServiceInfo testSubject;
        String vendorName = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setVendorName(vendorName);
    }

    @Test
    public void testGetVendorRelease() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getVendorRelease();
    }

    @Test
    public void testSetVendorRelease() {
        UploadServiceInfo testSubject;
        String vendorRelease = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setVendorRelease(vendorRelease);
    }

    @Test
    public void testGetServiceVendorModelNumber() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getServiceVendorModelNumber();
    }

    @Test
    public void testSetServiceVendorModelNumber() {
        UploadServiceInfo testSubject;
        String serviceVendorModelNumber = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setServiceVendorModelNumber(serviceVendorModelNumber);
    }

    @Test
    public void testGetServiceType() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getServiceType();
    }

    @Test
    public void testSetServiceType() {
        UploadServiceInfo testSubject;
        String serviceType = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setServiceType(serviceType);
    }

    @Test
    public void testGetCategories() {
        UploadServiceInfo testSubject;
        List<CategoryDefinition> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getCategories();
    }

    @Test
    public void testSetCategories() {
        UploadServiceInfo testSubject;
        List<CategoryDefinition> categories = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setCategories(categories);
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

        // test 2
        testSubject = createTestSubject();
        category = "";
        subCategory = null;
        testSubject.addSubCategory(category, subCategory);

        // test 3
        testSubject = createTestSubject();
        subCategory = "";
        category = null;
        testSubject.addSubCategory(category, subCategory);

        // test 4
        testSubject = createTestSubject();
        subCategory = "mock";
        category = "mock";
        testSubject.addSubCategory(category, subCategory);
        testSubject.addSubCategory(category, subCategory);
    }

    @Test
    public void testGetProjectCode() {
        UploadServiceInfo testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getProjectCode();
    }

    @Test
    public void testSetProjectCode() {
        UploadServiceInfo testSubject;
        String projectCode = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setProjectCode(projectCode);
    }
}