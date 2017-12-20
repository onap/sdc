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
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Components;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDataToComponentDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentEntityToComponentCreationDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentEntityToComponentDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentRequestDtoToComponentEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("components")
@Scope(value = "prototype")
public class ComponentsImpl implements Components {
  private ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();

  @Override
  public Response list(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Components.toString());
    Collection<ComponentEntity> components =
        componentManager.listComponents(vspId, new Version(versionId));

    MapComponentEntityToComponentDto mapper = new MapComponentEntityToComponentDto();
    GenericCollectionWrapper<ComponentDto> results = new GenericCollectionWrapper<>();
    for (ComponentEntity component : components) {
      results.add(mapper.applyMapping(component, ComponentDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response deleteList(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_List_Components.toString());
    componentManager.deleteComponents(vspId, new Version(versionId));
    return Response.ok().build();
  }

  @Override
  public Response create(ComponentRequestDto request, String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_Component.toString());
    ComponentEntity component =
        new MapComponentRequestDtoToComponentEntity().applyMapping(request, ComponentEntity.class);
    component.setVspId(vspId);
    component.setVersion(new Version(versionId));

    ComponentEntity createdComponent = componentManager.createComponent(component);
    MapComponentEntityToComponentCreationDto mapping =
        new MapComponentEntityToComponentCreationDto();
    ComponentCreationDto createdComponentDto = mapping.applyMapping(createdComponent,
        ComponentCreationDto.class);
    return Response
        .ok(createdComponent != null ? createdComponentDto : null)
        .build();
  }

  @Override
  public Response get(String vspId, String versionId, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Component.toString());
    CompositionEntityResponse<ComponentData> response =
        componentManager.getComponent(vspId, new Version(versionId), componentId);

    CompositionEntityResponseDto<ComponentDto> responseDto = new CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapComponentDataToComponentDto(),
        ComponentDto.class).doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Component.toString());
    componentManager.deleteComponent(vspId, new Version(versionId), componentId);
    return Response.ok().build();
  }

  @Override
  public Response update(ComponentRequestDto request, String vspId, String versionId,
                         String componentId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Component.toString());
    ComponentEntity componentEntity =
        new MapComponentRequestDtoToComponentEntity().applyMapping(request, ComponentEntity.class);
    componentEntity.setVspId(vspId);
    componentEntity.setVersion(new Version(versionId));
    componentEntity.setId(componentId);

    CompositionEntityValidationData validationData =
        componentManager.updateComponent(componentEntity);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
        new MapCompositionEntityValidationDataToDto().applyMapping(validationData,
            CompositionEntityValidationDataDto.class)).build() : Response.ok().build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String componentId,
                                   String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Questionnaire_Component.toString());
    QuestionnaireResponse questionnaireResponse =
        componentManager.getQuestionnaire(vspId, new Version(versionId), componentId);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String versionId,
                                      String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Questionnaire_Component.toString());
    componentManager
        .updateQuestionnaire(vspId, new Version(versionId), componentId, questionnaireData);
    return Response.ok().build();
  }
}
