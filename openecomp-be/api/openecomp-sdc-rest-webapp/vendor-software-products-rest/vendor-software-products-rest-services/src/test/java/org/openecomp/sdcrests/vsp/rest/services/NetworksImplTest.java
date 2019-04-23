package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetworksImpl.class, NetworkManagerFactory.class})
public class NetworksImplTest {

  private Logger logger = LoggerFactory.getLogger(NetworksImplTest.class);

  @Mock
  private NetworkManagerFactory networkManagerFactory;

  @Mock
  private NetworkManager networkManager;


  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String networkId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(NetworkManagerFactory.class);
      when(NetworkManagerFactory.getInstance()).thenReturn(networkManagerFactory);
      when(networkManagerFactory.createInterface()).thenReturn(networkManager);


      NetworkEntity e = new NetworkEntity();
      e.setId(networkId);
      e.setVspId(vspId);
      e.setVersion(new Version(versionId));
      e.setCompositionData("{\"name\":\"nm\",\"description\":\"d\"}");


      Collection<NetworkEntity> lst = Collections.singletonList(e);
      when(networkManager.listNetworks(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any())).thenReturn(lst);

      when(networkManager.createNetwork(
              ArgumentMatchers.any())).thenReturn(e);

      CompositionEntityResponse<Network> r = new CompositionEntityResponse<>();
      r.setId(vspId);
      when(networkManager.getNetwork(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(networkId))).thenReturn(r);

      CompositionEntityType tpe = CompositionEntityType.component;
      CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
      when(networkManager.updateNetwork(
              ArgumentMatchers.any())).thenReturn(data);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    NetworksImpl bean = new NetworksImpl();

    Response rsp = bean.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<NetworkDto> results = (GenericCollectionWrapper<NetworkDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }


  @Test
  public void testCreate() {

    NetworkRequestDto dto = new NetworkRequestDto();
    dto.setName("name");
    dto.setDhcp(true);

    NetworksImpl bean = new NetworksImpl();
    Response rsp = bean.create(dto, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      StringWrapperResponse s = (StringWrapperResponse)e;
      Assert.assertEquals(networkId, s.getValue());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    NetworksImpl bean = new NetworksImpl();
    Response rsp = bean.delete(vspId, versionId, networkId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    NetworksImpl bean = new NetworksImpl();
    Response rsp = bean.get(vspId, versionId, networkId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    NetworksImpl bean = new NetworksImpl();
    NetworkRequestDto dto = new NetworkRequestDto();
    Response rsp = bean.update(dto, vspId, versionId, networkId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

}
