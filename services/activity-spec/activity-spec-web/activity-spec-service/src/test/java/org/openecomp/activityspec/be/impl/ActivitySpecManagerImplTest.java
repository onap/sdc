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

package org.openecomp.activityspec.be.impl;

import java.util.Collection;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.activityspec.api.rest.types.ActivitySpecAction;
import org.openecomp.activityspec.be.dao.ActivitySpecDao;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.activityspec.be.datatypes.ActivitySpecParameter;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.activityspec.mocks.ActivitySpecDaoMock;
import org.openecomp.activityspec.mocks.ItemManagerMock;
import org.openecomp.activityspec.mocks.UniqueValueDaoMock;
import org.openecomp.activityspec.mocks.VersionManagerMock;
import org.openecomp.sdc.common.errors.CoreException;
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
import java.util.List;

public class ActivitySpecManagerImplTest {

  private static final String STRING_TYPE = "String";
  ActivitySpecEntity activitySpec;
  private ActivitySpecEntity retrieved;
  private ActivitySpecEntity input;
  private ActivitySpecEntity activitySpecToCreate;

  @Spy
  @InjectMocks
  private ActivitySpecManagerImpl activitySpecManager;


  private ActivitySpecDao activitySpecDaoMock = new ActivitySpecDaoMock();


  private ItemManager itemManagerMock = new ItemManagerMock();


  private VersioningManager versionManagerMock = new VersionManagerMock() {
  };

  private UniqueValueDao uniqueValueDaoMock = new UniqueValueDaoMock();
  private ActivitySpecEntity retrivedAfterNameUpdate;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    activitySpecManager = new ActivitySpecManagerImpl(itemManagerMock, versionManagerMock,
        activitySpecDaoMock, uniqueValueDaoMock);
  }

  @AfterMethod
  public void tearDown() {
    activitySpecManager = null;
  }


  public static final Version VERSION01 = new Version("12345");

  @Test
  public void testCreate() {

    SessionContextProviderFactory.getInstance().createInterface().create("testUser", "testTenant");

    activitySpecToCreate = new ActivitySpecEntity();
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
    activitySpecToCreate.setInputParameters(inputs);

    ActivitySpecParameter outputParams = new ActivitySpecParameter("status", STRING_TYPE);
    outputParams.setValue("started");
    List<ActivitySpecParameter> outputs = new ArrayList<>();
    outputs.add(outputParams);
    activitySpecToCreate.setOutputParameters(outputs);

    activitySpec = activitySpecManager.createActivitySpec
        (activitySpecToCreate);

    Assert.assertNotNull(activitySpec);
    activitySpecToCreate.setId(activitySpec.getId());
    activitySpecToCreate.setVersion(VERSION01);
    assertActivitySpecEquals(activitySpec, activitySpecToCreate);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testList () {
    //List
    final Collection<Item> activitySpecs = activitySpecManager.list("Certified");
    Assert.assertEquals(activitySpecs.size(), 1);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testGet () {
    //Get
    input = new ActivitySpecEntity();
    input.setId(activitySpec.getId());
    input.setVersion(activitySpec.getVersion());
    retrieved = activitySpecManager.get(input);
    assertActivitySpecEquals(retrieved, activitySpec);
    Assert.assertEquals(retrieved.getStatus(), VersionStatus.Draft.name());

    input.setVersion(new Version("LATEST"));
    retrieved = activitySpecManager.get(input);
    assertActivitySpecEquals(retrieved, activitySpec);
    Assert.assertEquals(retrieved.getStatus(), VersionStatus.Draft.name());
  }

  @Test(dependsOnMethods = "testGet")
  public void testInvalidDeprecate () {
    try {
      activitySpecManager.actOnAction(retrieved.getId(),
          VERSION01.getId(), ActivitySpecAction.DEPRECATE);
    }
    catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), "STATUS_NOT_"+VersionStatus.Certified.name()
          .toUpperCase());
    }
  }

  @Test(dependsOnMethods = "testGet")
  public void testInvalidDelete () {
    try {
      activitySpecManager.actOnAction(retrieved.getId(),
          VERSION01.getId(), ActivitySpecAction.DELETE);
    }
    catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), "STATUS_NOT_"+VersionStatus.Deprecated.name()
          .toUpperCase());
    }
  }

  @Test(dependsOnMethods = "testGet")
  public void testUpdate () {
    //Update
    retrieved.setDescription("Updated_install");
    activitySpecManager.update(retrieved);

    final ActivitySpecEntity retrivedAfterUpdate = activitySpecManager.get(input);
    assertActivitySpecEquals(retrivedAfterUpdate, activitySpecToCreate);

    //Update Name
    ActivitySpecEntity activitySpecToUpdate = new ActivitySpecEntity();
    activitySpecToUpdate.setId(activitySpec.getId());
    activitySpecToUpdate.setName("Updated_start_server");
    activitySpecToUpdate.setVersion(activitySpec.getVersion());

    activitySpecManager.update(activitySpecToUpdate);

    retrivedAfterNameUpdate = activitySpecManager.get(input);
    assertActivitySpecEquals(retrivedAfterNameUpdate, activitySpecToUpdate);
    Assert.assertEquals(retrivedAfterNameUpdate.getStatus(), VersionStatus.Draft.name());
  }

  @Test(dependsOnMethods = "testUpdate")
  public void testCertify () {
    activitySpecManager.actOnAction(retrivedAfterNameUpdate.getId(),
        VERSION01.getId(), ActivitySpecAction.CERTIFY);

    final ActivitySpecEntity retrivedAfterCertify = activitySpecManager.get(retrivedAfterNameUpdate);
    assertActivitySpecEquals(retrivedAfterCertify, retrivedAfterNameUpdate );
    Assert.assertEquals(retrivedAfterCertify.getStatus(), VersionStatus.Certified.name());
  }

  @Test(dependsOnMethods = "testCertify")
  public void testInvalidCertify () {
    try {
      activitySpecManager.actOnAction(retrieved.getId(),
          VERSION01.getId(), ActivitySpecAction.CERTIFY);
    }
    catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), "STATUS_NOT_"+VersionStatus.Draft.name()
          .toUpperCase());
    }
  }

  @Test(dependsOnMethods = "testCertify")
  public void testDeprecate () {
    activitySpecManager.actOnAction(retrivedAfterNameUpdate.getId(),
        retrivedAfterNameUpdate.getVersion().getId(), ActivitySpecAction.DEPRECATE);

    final ActivitySpecEntity retrivedAfterDeprecate = activitySpecManager.get(retrivedAfterNameUpdate);
    assertActivitySpecEquals(retrivedAfterDeprecate, retrivedAfterNameUpdate );
    Assert.assertEquals(retrivedAfterDeprecate.getStatus(), VersionStatus.Deprecated.name());
  }

  @Test(dependsOnMethods = "testDeprecate")
  public void testDelete () {
    activitySpecManager.actOnAction(retrivedAfterNameUpdate.getId(),
        retrivedAfterNameUpdate.getVersion().getId(), ActivitySpecAction.DELETE);

    final ActivitySpecEntity retrivedAfterDelete = activitySpecManager.get(retrivedAfterNameUpdate);
    assertActivitySpecEquals(retrivedAfterDelete, retrivedAfterNameUpdate );
    Assert.assertEquals(retrivedAfterDelete.getStatus(), VersionStatus.Deleted.name());
  }

  private void assertActivitySpecEquals(ActivitySpecEntity actual, ActivitySpecEntity expected) {
    Assert.assertEquals(actual.getId(), expected.getId());
    Assert.assertEquals(actual.getName(), expected.getName());
    Assert.assertEquals(actual.getDescription(), expected.getDescription());
    Assert.assertEquals(actual.getCategoryList(), expected.getCategoryList());
    Assert.assertEquals(actual.getInputParameters(), expected.getInputParameters());
    Assert.assertEquals(actual.getOutputParameters(), expected.getOutputParameters());
  }
}
