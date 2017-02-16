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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Components;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDataToComponentDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentEntityToComponentDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentRequestDtoToComponentEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
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
@Service("components")
@Scope(value = "prototype")
public class ComponentsImpl implements Components {
  @Autowired
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Override
  public Response list(String vspId, String version, String user) {
    Collection<ComponentEntity> components =
        vendorSoftwareProductManager.listComponents(vspId, Version.valueOf(version), user);

    MapComponentEntityToComponentDto mapper = new MapComponentEntityToComponentDto();
    GenericCollectionWrapper<ComponentDto> results = new GenericCollectionWrapper<>();
    for (ComponentEntity component : components) {
      results.add(mapper.applyMapping(component, ComponentDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response deleteList(String vspId, String user) {
    vendorSoftwareProductManager.deleteComponents(vspId, user);
    return Response.ok().build();
  }

  @Override
  public Response create(ComponentRequestDto request, String vspId, String user) {
    ComponentEntity component =
        new MapComponentRequestDtoToComponentEntity().applyMapping(request, ComponentEntity.class);
    component.setVspId(vspId);
    ComponentEntity createdComponent =
        vendorSoftwareProductManager.createComponent(component, user);
    return Response
        .ok(createdComponent != null ? new StringWrapperResponse(createdComponent.getId()) : null)
        .build();
  }

  @Override
  public Response get(String vspId, String componentId, String version, String user) {
    CompositionEntityResponse<ComponentData> response = vendorSoftwareProductManager
        .getComponent(vspId, Version.valueOf(version), componentId, user);

    CompositionEntityResponseDto<ComponentDto> responseDto = new CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapComponentDataToComponentDto(),
        ComponentDto.class).doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response delete(String vspId, String componentId, String user) {
    vendorSoftwareProductManager.deleteComponent(vspId, componentId, user);
    return Response.ok().build();
  }

  @Override
  public Response update(ComponentRequestDto request, String vspId, String componentId,
                         String user) {
    ComponentEntity componentEntity =
        new MapComponentRequestDtoToComponentEntity().applyMapping(request, ComponentEntity.class);
    componentEntity.setVspId(vspId);
    componentEntity.setId(componentId);

    CompositionEntityValidationData validationData =
        vendorSoftwareProductManager.updateComponent(componentEntity, user);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
            new MapCompositionEntityValidationDataToDto()
                .applyMapping(validationData, CompositionEntityValidationDataDto.class)).build() :
        Response.ok().build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String componentId, String version, String user) {
    QuestionnaireResponse questionnaireResponse = vendorSoftwareProductManager
        .getComponentQuestionnaire(vspId, Version.valueOf(version), componentId, user);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String componentId,
                                      String user) {
    vendorSoftwareProductManager
        .updateComponentQuestionnaire(vspId, componentId, questionnaireData, user);
    return Response.ok().build();
  }
}
