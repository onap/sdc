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
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;


@Named
@Service("networks")
@Scope(value = "prototype")
public class NetworksImpl implements Networks {
  @Autowired
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Override
  public Response list(String vspId, String version, String user) {
    Collection<NetworkEntity> networks =
        vendorSoftwareProductManager.listNetworks(vspId, Version.valueOf(version), user);

    MapNetworkEntityToNetworkDto mapper = new MapNetworkEntityToNetworkDto();
    GenericCollectionWrapper<NetworkDto> results = new GenericCollectionWrapper<>();
    for (NetworkEntity network : networks) {
      results.add(mapper.applyMapping(network, NetworkDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response create(NetworkRequestDto request, String vspId, String user) {
    NetworkEntity network =
        new MapNetworkRequestDtoToNetworkEntity().applyMapping(request, NetworkEntity.class);
    network.setVspId(vspId);
    NetworkEntity createdNetwork = vendorSoftwareProductManager.createNetwork(network, user);
    return Response
        .ok(createdNetwork != null ? new StringWrapperResponse(createdNetwork.getId()) : null)
        .build();
  }

  @Override
  public Response get(String vspId, String networkId, String version, String user) {
    CompositionEntityResponse<Network> response =
        vendorSoftwareProductManager.getNetwork(vspId, Version.valueOf(version), networkId, user);

    CompositionEntityResponseDto<NetworkDto> responseDto = new CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapNetworkToNetworkDto(), NetworkDto.class)
        .doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response delete(String vspId, String networkId, String user) {
    vendorSoftwareProductManager.deleteNetwork(vspId, networkId, user);
    return Response.ok().build();
  }

  @Override
  public Response update(NetworkRequestDto request, String vspId, String networkId, String user) {
    NetworkEntity networkEntity =
        new MapNetworkRequestDtoToNetworkEntity().applyMapping(request, NetworkEntity.class);
    networkEntity.setVspId(vspId);
    networkEntity.setId(networkId);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateNetwork(networkEntity, user);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
            new MapCompositionEntityValidationDataToDto()
                .applyMapping(validationData, CompositionEntityValidationDataDto.class)).build() :
        Response.ok().build();
  }
}
