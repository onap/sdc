/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.be.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.sdc.activityspec.api.rest.types.ActivitySpecAction;
import org.onap.sdc.activityspec.be.dao.ActivitySpecDao;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.onap.sdc.activityspec.be.datatypes.ActivitySpecParameter;
import org.onap.sdc.activityspec.errors.ActivitySpecNotFoundException;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.onap.sdc.activityspec.utils.ActivitySpecConstant.*;

public class ActivitySpecManagerImplTest {

    private static final String STRING_TYPE = "String";
    private static final String TEST_ERROR_MSG = "Test Error";
    private ActivitySpecEntity input;
    private static final Version VERSION01 = new Version("12345");
    private static final String ID = "ID1";

    @Spy
    @InjectMocks
    private ActivitySpecManagerImpl activitySpecManager;

    @Mock
    private ItemManager itemManagerMock;

    @Mock
    private VersioningManager versionManagerMock;

    @Mock
    private ActivitySpecDao activitySpecDaoMock;

    //This is used to mock UniqueValueUtil. This should not be removed.
    @Mock
    private UniqueValueDao uniqueValueDaoMock;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @AfterMethod
    public void tearDown() {
        activitySpecManager = null;
    }

    @Test
    public void testCreate() {

        SessionContextProviderFactory.getInstance().createInterface().create("testUser", "testTenant");

        ActivitySpecEntity activitySpecToCreate = new ActivitySpecEntity();
        activitySpecToCreate.setName("startserver");
        activitySpecToCreate.setDescription("start the server");
        activitySpecToCreate.setVersion(VERSION01);

        List<String> categoryList = new ArrayList<>();
        categoryList.add("category1");
        categoryList.add("category2");
        activitySpecToCreate.setCategoryList(categoryList);

        ActivitySpecParameter inputParams = new ActivitySpecParameter("dbhost", STRING_TYPE);
        inputParams.setValue("localhost");
        ActivitySpecParameter inputParams1 = new ActivitySpecParameter("dbname", STRING_TYPE);
        inputParams.setValue("prod");
        List<ActivitySpecParameter> inputs = new ArrayList<>();
        inputs.add(inputParams);
        inputs.add(inputParams1);
        activitySpecToCreate.setInputs(inputs);

        ActivitySpecParameter outputParams = new ActivitySpecParameter("status", STRING_TYPE);
        outputParams.setValue("started");
        List<ActivitySpecParameter> outputs = new ArrayList<>();
        outputs.add(outputParams);
        activitySpecToCreate.setOutputs(outputs);

        activitySpecToCreate.setId("ID1");
        activitySpecToCreate.setVersion(VERSION01);

        Item item = new Item();
        doReturn(item).when(itemManagerMock).create(anyObject());

        ActivitySpecEntity activitySpec = activitySpecManager.createActivitySpec(activitySpecToCreate);

        Assert.assertNotNull(activitySpec);
        activitySpec.setId("ID1");
        activitySpec.setStatus(VersionStatus.Draft.name());
        assertActivitySpecEquals(activitySpec, activitySpecToCreate);
    }


    @Test
    public void testList() {
        ActivitySpecEntity activitySpec = new ActivitySpecEntity();
        activitySpec.setName("stopServer");
        doReturn(Arrays.asList(activitySpec)).when(itemManagerMock).list(anyObject());
        final Collection<Item> activitySpecs = activitySpecManager.list("Certified");
        Assert.assertEquals(activitySpecs.size(), 1);
    }

    @Test
    public void testListInvalidFilter() {
        final Collection<Item> activitySpecs = activitySpecManager.list("invalid_status");
        Assert.assertEquals(activitySpecs.size(), 0);
    }

    @Test
    public void testListNoFilter() {
        final Collection<Item> activitySpecs = activitySpecManager.list(null);
        Assert.assertEquals(activitySpecs.size(), 0);
    }

    @Test
    public void testGet() {
        input = new ActivitySpecEntity();
        input.setId(ID);
        input.setVersion(VERSION01);

        doReturn(Arrays.asList(VERSION01)).when(versionManagerMock).list(anyObject());
        doReturn(input).when(activitySpecDaoMock).get(anyObject());
        VERSION01.setStatus(VersionStatus.Draft);
        doReturn(VERSION01).when(versionManagerMock).get(anyObject(), anyObject());
        ActivitySpecEntity retrieved = activitySpecManager.get(input);
        assertActivitySpecEquals(retrieved, input);
        Assert.assertEquals(retrieved.getStatus(), VersionStatus.Draft.name());


        input.setVersion(new Version(VERSION_ID_DEFAULT_VALUE));
        retrieved = activitySpecManager.get(input);
        assertActivitySpecEquals(retrieved, input);
        Assert.assertEquals(retrieved.getStatus(), VersionStatus.Draft.name());
    }

    @Test
    public void testGetActivitySpecDaoFail() {
        input = new ActivitySpecEntity();
        input.setId(ID);
        input.setVersion(VERSION01);
        doReturn(Arrays.asList(VERSION01)).when(versionManagerMock).list(anyObject());
        doReturn(input).when(activitySpecDaoMock).get(anyObject());
        doThrow(new SdcRuntimeException(TEST_ERROR_MSG)).when(activitySpecDaoMock).get(anyObject());
        try {
            activitySpecManager.get(input);
            Assert.fail();
        } catch (ActivitySpecNotFoundException exception) {
            Assert.assertEquals(exception.getMessage(), ACTIVITY_SPEC_NOT_FOUND);
        }
    }

    @Test
    public void testListVersionFail() {
        input = new ActivitySpecEntity();
        input.setId(ID);
        input.setVersion(VERSION01);
        input.getVersion().setId(VERSION_ID_DEFAULT_VALUE);
        doThrow(new SdcRuntimeException(TEST_ERROR_MSG)).when(versionManagerMock).list(anyObject());
        try {
            activitySpecManager.get(input);
            Assert.fail();
        } catch (ActivitySpecNotFoundException exception) {
            Assert.assertEquals(exception.getMessage(), ACTIVITY_SPEC_NOT_FOUND);
        }
    }

    @Test
    public void testInvalidDeprecate() {
        try {
            activitySpecManager.actOnAction(ID, VERSION01.getId(), ActivitySpecAction.DEPRECATE);
        } catch (CoreException exception) {
            Assert.assertEquals(exception.getMessage(), INVALID_STATE);
        }
    }

    @Test
    public void testInvalidDelete() {
        try {
            activitySpecManager.actOnAction(ID, VERSION01.getId(), ActivitySpecAction.DELETE);
        } catch (CoreException exception) {
            Assert.assertEquals(exception.getMessage(), INVALID_STATE);
        }
    }

    @Test
    public void testCertify() {
        doReturn(Arrays.asList(VERSION01)).when(versionManagerMock).list(anyObject());
        doReturn(VERSION01).when(versionManagerMock).get(anyObject(), anyObject());
        activitySpecManager.actOnAction(ID, VERSION01.getId(), ActivitySpecAction.CERTIFY);

        verify(versionManagerMock).updateVersion(ID, VERSION01);
        verify(itemManagerMock).updateVersionStatus(ID, VersionStatus.Certified, VersionStatus.Draft);
        verify(versionManagerMock).publish(anyObject(), anyObject(), anyObject());
    }

    @Test
    public void testInvalidCertify() {
        try {
            activitySpecManager.actOnAction(ID, VERSION01.getId(), ActivitySpecAction.CERTIFY);
        } catch (CoreException exception) {
            Assert.assertEquals(exception.getMessage(), INVALID_STATE);
        }
    }

    @Test
    public void testGetVersionFailOnStatusChangeAction() {
        doReturn(Arrays.asList(VERSION01)).when(versionManagerMock).list(anyObject());
        doThrow(new SdcRuntimeException(TEST_ERROR_MSG)).when(versionManagerMock).get(anyObject(), anyObject());
        try {
            activitySpecManager.actOnAction(ID, VERSION01.getId(), ActivitySpecAction.CERTIFY);
            Assert.fail();
        } catch (ActivitySpecNotFoundException exception) {
            Assert.assertEquals(exception.getMessage(), ACTIVITY_SPEC_NOT_FOUND);
        }
    }

    @Test
    public void testDeprecate() {
        VERSION01.setStatus(VersionStatus.Certified);
        Version retrivedVersion = new Version("12");
        retrivedVersion.setStatus(VersionStatus.Certified);
        doReturn(Arrays.asList(VERSION01)).when(versionManagerMock).list(anyObject());
        doReturn(retrivedVersion).when(versionManagerMock).get(anyObject(), anyObject());
        activitySpecManager.actOnAction(ID, VERSION_ID_DEFAULT_VALUE, ActivitySpecAction.DEPRECATE);

        verify(versionManagerMock).updateVersion(ID, retrivedVersion);
        verify(itemManagerMock).updateVersionStatus(ID, VersionStatus.Deprecated, VersionStatus.Certified);
        verify(versionManagerMock).publish(anyObject(), anyObject(), anyObject());
    }

    @Test
    public void testDelete() {
        ActivitySpecEntity activitySpec = new ActivitySpecEntity();
        VERSION01.setStatus(VersionStatus.Deprecated);
        activitySpec.setName("stopServer");
        activitySpec.setVersion(VERSION01);

        Version retrivedVersion = new Version("12");
        retrivedVersion.setStatus(VersionStatus.Deprecated);

        doReturn(Arrays.asList(VERSION01)).when(versionManagerMock).list(anyObject());
        doReturn(retrivedVersion).when(versionManagerMock).get(anyObject(), anyObject());
        doReturn(activitySpec).when(activitySpecDaoMock).get(anyObject());
        activitySpecManager.actOnAction(ID, VERSION_ID_DEFAULT_VALUE, ActivitySpecAction.DELETE);

        verify(versionManagerMock).updateVersion(ID, VERSION01);
        verify(itemManagerMock).updateVersionStatus(ID, VersionStatus.Deleted, VersionStatus.Deprecated);
        verify(versionManagerMock).publish(anyObject(), anyObject(), anyObject());
    }

    private void assertActivitySpecEquals(ActivitySpecEntity actual, ActivitySpecEntity expected) {
        Assert.assertEquals(actual.getId(), expected.getId());
        Assert.assertEquals(actual.getName(), expected.getName());
        Assert.assertEquals(actual.getDescription(), expected.getDescription());
        Assert.assertEquals(actual.getCategoryList(), expected.getCategoryList());
        Assert.assertEquals(actual.getInputs(), expected.getInputs());
        Assert.assertEquals(actual.getOutputs(), expected.getOutputs());
    }

}
