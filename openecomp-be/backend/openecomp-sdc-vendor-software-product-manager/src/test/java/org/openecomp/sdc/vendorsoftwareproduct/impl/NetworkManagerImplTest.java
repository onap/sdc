/*
 * Copyright © 2018 European Support Limited
 *
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
*/

package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NetworkManagerImplTest {

  private static final String VSP_ID = "vsp";
  private static final String USER_ID = "test_user1";
  private static final Version VERSION = new Version("version_id");
  private static final String NETWORK1_ID = "network1";
  private static final String NETWORK2_ID = "network2";
  private static final String tenant = "dox";

  @Mock
  private NetworkDao networkDaoMock;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;

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

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    SessionContextProviderFactory.getInstance().createInterface().create(USER_ID, tenant);
  }

  @After
  public void tearDown() {
    networkManager = null;
    SessionContextProviderFactory.getInstance().createInterface().close();
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
        .when(networkDaoMock).list(any());

    Collection<NetworkEntity> actual = networkManager.listNetworks(VSP_ID, VERSION);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test
  public void testCreateOnUploadVsp_negative() {
    testCreate_negative(new NetworkEntity(VSP_ID, VERSION, null),
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testUpdateNonExistingNetworkId_negative() {
    testUpdate_negative(VSP_ID, VERSION, NETWORK1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testIllegalUpdateOnUploadVsp() {
    doReturn(createNetwork(VSP_ID, VERSION, NETWORK1_ID))
        .when(networkDaoMock).get(any());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.network, NETWORK1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());
    doReturn(false).when(vendorSoftwareProductInfoDao).isManual(any(),any());

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
        .when(networkDaoMock).get(any());
    doReturn("schema string").when(networkManager).getCompositionSchema(any());
    doReturn(false).when(vendorSoftwareProductInfoDao).isManual(any(),any());

    CompositionEntityResponse<Network> response =
        networkManager.getNetwork(VSP_ID, VERSION, NETWORK1_ID);
    Assert.assertEquals(response.getId(), network.getId());
    Assert.assertEquals(response.getData(), network.getNetworkCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  @Test
  public void testDeleteOnUploadVsp_negative() {
    testList();
    testDelete_negative(VSP_ID, VERSION, NETWORK1_ID,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  private void testCreate_negative(NetworkEntity network, String expectedErrorCode) {
    try {
      doReturn(false).when(vendorSoftwareProductInfoDao).isManual(any(),any());
      networkManager.createNetwork(network);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String networkId,
                                String expectedErrorCode) {
    try {
      networkManager.getNetwork(vspId, version, networkId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String networkId,
                                   String expectedErrorCode) {
    try {
      networkManager.updateNetwork(new NetworkEntity(vspId, version, networkId));
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String expectedErrorCode) {
    try {
      networkManager.listNetworks(vspId, version);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String networkId,
                                   String expectedErrorCode) {
    try {
      doReturn(false).when(vendorSoftwareProductInfoDao).isManual(any(),any());
      networkManager.deleteNetwork(vspId, version, networkId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }
}
