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

package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkRequestDto;
import org.openecomp.sdcrests.vsp.rest.Networks;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNetworkEntityToNetworkDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNetworkRequestDtoToNetworkEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNetworkToNetworkDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("networks")
@Scope(value = "prototype")
public class NetworksImpl implements Networks {

  private NetworkManager networkManager =
      NetworkManagerFactory.getInstance().createInterface();

  @Override
  public Response list(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Network.toString());
    Collection<NetworkEntity> networks =
        networkManager.listNetworks(vspId, new Version(versionId));

    MapNetworkEntityToNetworkDto mapper = new MapNetworkEntityToNetworkDto();
    GenericCollectionWrapper<NetworkDto> results = new GenericCollectionWrapper<>();
    for (NetworkEntity network : networks) {
      results.add(mapper.applyMapping(network, NetworkDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response create(NetworkRequestDto request, String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_Network.toString());
    NetworkEntity network =
        new MapNetworkRequestDtoToNetworkEntity().applyMapping(request, NetworkEntity.class);
    network.setVspId(vspId);
    network.setVersion(new Version(versionId));
    NetworkEntity createdNetwork = networkManager.createNetwork(network);
    return Response
        .ok(createdNetwork != null ? new StringWrapperResponse(createdNetwork.getId()) : null)
        .build();
  }

  @Override
  public Response get(String vspId, String versionId, String networkId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Network.toString());
    CompositionEntityResponse<Network> response =
        networkManager.getNetwork(vspId,
            new Version(versionId), networkId
        );

    CompositionEntityResponseDto<NetworkDto> responseDto = new CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapNetworkToNetworkDto(), NetworkDto.class)
        .doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String networkId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Network.toString());
    networkManager
        .deleteNetwork(vspId, new Version(versionId),
            networkId);
    return Response.ok().build();
  }

  @Override
  public Response update(NetworkRequestDto request, String vspId, String versionId,
                         String networkId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Network.toString());
    NetworkEntity networkEntity =
        new MapNetworkRequestDtoToNetworkEntity().applyMapping(request, NetworkEntity.class);
    networkEntity.setVspId(vspId);
    networkEntity.setVersion(new Version(versionId));
    networkEntity.setId(networkId);

    CompositionEntityValidationData validationData =
        networkManager.updateNetwork(networkEntity);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
        new MapCompositionEntityValidationDataToDto()
            .applyMapping(validationData, CompositionEntityValidationDataDto.class)).build() :
        Response.ok().build();
  }
}
