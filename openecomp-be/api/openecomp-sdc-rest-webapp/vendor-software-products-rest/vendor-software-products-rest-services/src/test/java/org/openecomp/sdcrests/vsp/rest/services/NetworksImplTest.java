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

package org.openecomp.sdcrests.vsp.rest.services;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
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

public class NetworksImplTest {

    @Mock
    private NetworkManager mockedNetworkManager;

    private final String vspId = UUID.randomUUID().toString();
    private final String versionId = UUID.randomUUID().toString();
    private final String networkId = "" + System.currentTimeMillis();
    private final String user = "cs0008";

    @Before
    public void setUp() {
        openMocks(this);

        NetworkEntity e = new NetworkEntity();
        e.setId(networkId);
        e.setVspId(vspId);
        e.setVersion(new Version(versionId));
        e.setCompositionData("{\"name\":\"nm\",\"description\":\"d\"}");

        Collection<NetworkEntity> lst = Collections.singletonList(e);
        when(mockedNetworkManager.listNetworks(
            ArgumentMatchers.eq(vspId),
            ArgumentMatchers.any())).thenReturn(lst);

        when(mockedNetworkManager.createNetwork(
            ArgumentMatchers.any())).thenReturn(e);

        CompositionEntityResponse<Network> r = new CompositionEntityResponse<>();
        r.setId(vspId);
        when(mockedNetworkManager.getNetwork(
            ArgumentMatchers.eq(vspId),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(networkId))).thenReturn(r);

        CompositionEntityType tpe = CompositionEntityType.component;
        CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
        when(mockedNetworkManager.updateNetwork(
            ArgumentMatchers.any())).thenReturn(data);
    }

    @Test
    public void testList() {
        NetworksImpl bean = new NetworksImpl(mockedNetworkManager);

        Response rsp = bean.list(vspId, versionId, user);
        Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
        Object e = rsp.getEntity();
        Assert.assertNotNull(e);
        @SuppressWarnings("unchecked")
        GenericCollectionWrapper<NetworkDto> results = (GenericCollectionWrapper<NetworkDto>) e;
        Assert.assertEquals("result length", 1, results.getListCount());
    }


    @Test
    public void testCreate() {

        NetworkRequestDto dto = new NetworkRequestDto();
        dto.setName("name");
        dto.setDhcp(true);

        NetworksImpl bean = new NetworksImpl(mockedNetworkManager);
        Response rsp = bean.create(dto, vspId, versionId, user);
        Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
        Object e = rsp.getEntity();
        Assert.assertNotNull(e);
        try {
            StringWrapperResponse s = (StringWrapperResponse) e;
            Assert.assertEquals(networkId, s.getValue());
        } catch (ClassCastException ex) {
            Assert.fail("unexpected class for DTO " + e.getClass().getName());
        }
    }


    @Test
    public void testDelete() {
        NetworksImpl bean = new NetworksImpl(mockedNetworkManager);
        Response rsp = bean.delete(vspId, versionId, networkId, user);
        Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
        Assert.assertNull(rsp.getEntity());
    }


    @Test
    public void testGet() {
        NetworksImpl bean = new NetworksImpl(mockedNetworkManager);
        Response rsp = bean.get(vspId, versionId, networkId, user);
        Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
        Assert.assertNotNull(rsp.getEntity());
    }

    @Test
    public void testUpdate() {
        NetworksImpl bean = new NetworksImpl(mockedNetworkManager);
        NetworkRequestDto dto = new NetworkRequestDto();
        Response rsp = bean.update(dto, vspId, versionId, networkId, user);
        Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
        Assert.assertNull(rsp.getEntity());
    }
}
