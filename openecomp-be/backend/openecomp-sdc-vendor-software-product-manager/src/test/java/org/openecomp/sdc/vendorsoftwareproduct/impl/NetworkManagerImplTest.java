/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NetworkManagerImplTest {

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  private static final String VSP_ID = "vsp";
  private static final Version VERSION = new Version("version_id");
  private static final String NETWORK1_ID = "network1";
  private static final String NETWORK2_ID = "network2";

  @Mock
  private NetworkDao networkDaoMock;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @InjectMocks
  @Spy
  private NetworkManagerImpl networkManager;

  static NetworkEntity createNetwork(String vspId, Version version, String networkId) {
    NetworkEntity networkEntity = new NetworkEntity(vspId, version, networkId);
    Network networkData = new Network();
    networkData.setName(networkId + " name");
    networkData.setDhcp(true);
    networkEntity.setNetworkCompositionData(networkData);
    return networkEntity;
  }

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListWhenNone() {
    Collection<NetworkEntity> networks =
        networkManager.listNetworks(VSP_ID, null);
    Assert.assertEquals(networks.size(), 0);
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createNetwork(VSP_ID, VERSION, NETWORK1_ID),
        createNetwork(VSP_ID, VERSION, NETWORK2_ID)))
        .when(networkDaoMock).list(anyObject());

    Collection<NetworkEntity> actual = networkManager.listNetworks(VSP_ID, VERSION);
    Assert.assertEquals(actual.size(), 2);
  }

/*    @Test(dependsOnMethods = "testListWhenNone")
    public void testCreate() {
        NETWORK1_ID = testCreate(VSP_ID);
    }

    private String testCreate(String vspId) {
        NetworkEntity expected = new NetworkEntity(vspId, null, null);
        Network networkData = new Network();
        networkData.setName("network1 name");
        networkData.setDhcp(true);
        expected.setNetworkCompositionData(networkData);


        NetworkEntity created = networkManager.createNetwork(expected);
        Assert.assertNotNull(created);
        expected.setId(created.getId());
        expected.setVersion(VERSION01);

        NetworkEntity actual = networkDaoMock.getNetwork(vspId, VERSION01, created.getId());

        Assert.assertEquals(actual, expected);
        return created.getId();
    }

    @Test(dependsOnMethods = {"testCreate"})
    public void testCreateWithExistingName_negative() {
        NetworkEntity network = new NetworkEntity(VSP_ID, null, null);
        Network networkData = new Network();
        networkData.setName("network1 name");
        networkData.setDhcp(true);
        network.setNetworkCompositionData(networkData);
        testCreate_negative(network, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

  @Test
  public void testCreateOnUploadVsp_negative() {
    testCreate_negative(new NetworkEntity(VSP_ID, VERSION, null),
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  /*    @Test(dependsOnMethods = {"testCreate"})
      public void testCreateWithExistingNameUnderOtherVsp() {
          testCreate(vsp2Id);
      }
  */

  @Test
  public void testUpdateNonExistingNetworkId_negative() {
    testUpdate_negative(VSP_ID, VERSION, NETWORK1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testIllegalUpdateOnUploadVsp() {
    doReturn(createNetwork(VSP_ID, VERSION, NETWORK1_ID))
        .when(networkDaoMock).get(anyObject());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.network, NETWORK1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(), anyObject(), anyObject());

    NetworkEntity networkEntity = new NetworkEntity(VSP_ID, VERSION, NETWORK1_ID);
    Network networkData = new Network();
    networkData.setName(NETWORK1_ID + " name updated");
    networkData.setDhcp(false);
    networkEntity.setNetworkCompositionData(networkData);

    CompositionEntityValidationData validationData =
        networkManager.updateNetwork(networkEntity);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);

    verify(networkDaoMock, never()).update(networkEntity);
  }

  @Test
  public void testGetNonExistingNetworkId_negative() {
    testGet_negative(VSP_ID, VERSION, NETWORK1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGet() {
    NetworkEntity network = createNetwork(VSP_ID, VERSION, NETWORK1_ID);
    doReturn(network)
        .when(networkDaoMock).get(anyObject());
    doReturn("schema string").when(networkManager).getCompositionSchema(anyObject());

    CompositionEntityResponse<Network> response =
        networkManager.getNetwork(VSP_ID, VERSION, NETWORK1_ID);
    Assert.assertEquals(response.getId(), network.getId());
    Assert.assertEquals(response.getData(), network.getNetworkCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

    /*
           @Test(dependsOnMethods = {"testUpdateOnUploadVsp", "testList"})
           public void testCreateWithERemovedName() {
               testCreate(VSP_ID);
           }

    @Test(dependsOnMethods = "testList")
    public void testDeleteNonExistingNetworkId_negative() {
        testDelete_negative(VSP_ID, "non existing network id", VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

/*
           @Test(dependsOnMethods = "testList")
           public void testDelete() {
               networkManager.deleteNetwork(VSP_ID, NETWORK1_ID);
               NetworkEntity actual = networkDaoMock.getNetwork(VSP_ID, VERSION01, NETWORK1_ID);
               Assert.assertNull(actual);
           }



           @Test(dependsOnMethods = "testDelete")
           public void testDeleteList() {
               NetworkEntity network3 = new NetworkEntity(VSP_ID, null, null);
               network3.setName("network3 name");
               network3.setDescription("network3 desc");
               networkManager.createNetwork(network3);

               networkManager.deleteNetworks(VSP_ID);

               Collection<NetworkEntity> actual = networkManager.listNetworks(VSP_ID, null);
               Assert.assertEquals(actual.size(), 0);
           }*/

  @Test(dependsOnMethods = "testList")
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(VSP_ID, VERSION, NETWORK1_ID,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  private void testCreate_negative(NetworkEntity network, String expectedErrorCode) {
    try {
      networkManager.createNetwork(network);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("", exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String networkId,
                                String expectedErrorCode) {
    try {
      networkManager.getNetwork(vspId, version, networkId);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("", exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String networkId,
                                   String expectedErrorCode) {
    try {
      networkManager.updateNetwork(new NetworkEntity(vspId, version, networkId));
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("", exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String expectedErrorCode) {
    try {
      networkManager.listNetworks(vspId, version);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("", exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String networkId,
                                   String expectedErrorCode) {
    try {
      networkManager.deleteNetwork(vspId, version, networkId);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("", exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }
}
