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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;

import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Nics;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicEntityToNicDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicRequestDtoToNicEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicToNicDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;


@Named
@Service("nics")
@Scope(value = "prototype")
public class NicsImpl implements Nics {
  @Autowired
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Override
  public Response list(String vspId, String componentId, String version, String user) {
    Collection<NicEntity> nics =
        vendorSoftwareProductManager.listNics(vspId, Version.valueOf(version), componentId, user);

    MapNicEntityToNicDto mapper = new MapNicEntityToNicDto();
    GenericCollectionWrapper<NicDto> results = new GenericCollectionWrapper<>();
    for (NicEntity nic : nics) {
      results.add(mapper.applyMapping(nic, NicDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response create(NicRequestDto request, String vspId, String componentId, String user) {
    NicEntity nic = new MapNicRequestDtoToNicEntity().applyMapping(request, NicEntity.class);
    nic.setVspId(vspId);
    nic.setComponentId(componentId);

    NicEntity createdNic = vendorSoftwareProductManager.createNic(nic, user);
    return Response.ok(createdNic != null ? new StringWrapperResponse(createdNic.getId()) : null)
        .build();
  }

  @Override
  public Response get(String vspId, String componentId, String nicId, String version, String user) {
    CompositionEntityResponse<Nic> response = vendorSoftwareProductManager
        .getNic(vspId, Version.valueOf(version), componentId, nicId, user);

    CompositionEntityResponseDto<NicDto> responseDto = new CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapNicToNicDto(), NicDto.class)
        .doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response delete(String vspId, String componentId, String nicId, String user) {
    vendorSoftwareProductManager.deleteNic(vspId, componentId, nicId, user);
    return Response.ok().build();
  }

  @Override
  public Response update(NicRequestDto request, String vspId, String componentId, String nicId,
                         String user) {
    NicEntity nicEntity = new MapNicRequestDtoToNicEntity().applyMapping(request, NicEntity.class);
    nicEntity.setVspId(vspId);
    nicEntity.setComponentId(componentId);
    nicEntity.setId(nicId);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateNic(nicEntity, user);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
            new MapCompositionEntityValidationDataToDto()
                .applyMapping(validationData, CompositionEntityValidationDataDto.class)).build() :
        Response.ok().build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String componentId, String nicId, String version,
                                   String user) {
    QuestionnaireResponse questionnaireResponse = vendorSoftwareProductManager
        .getNicQuestionnaire(vspId, Version.valueOf(version), componentId, nicId, user);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String componentId,
                                      String nicId, String user) {
    vendorSoftwareProductManager
        .updateNicQuestionnaire(vspId, componentId, nicId, questionnaireData, user);
    return Response.ok().build();
  }
}
