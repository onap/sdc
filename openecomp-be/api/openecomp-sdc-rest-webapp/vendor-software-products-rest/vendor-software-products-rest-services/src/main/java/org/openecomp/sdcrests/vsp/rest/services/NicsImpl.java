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
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicCreationResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Nics;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicEntityToNicCreationResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicEntityToNicDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicRequestDtoToNicEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapNicToNicDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("nics")
@Scope(value = "prototype")
public class NicsImpl implements Nics {
  private NicManager nicManager = NicManagerFactory.getInstance().createInterface();
  private ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();

  @Override
  public Response list(String vspId, String versionId, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_nics.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    Collection<NicEntity> nics = nicManager.listNics(vspId, vspVersion, componentId);

    MapNicEntityToNicDto mapper = new MapNicEntityToNicDto();
    GenericCollectionWrapper<NicDto> results = new GenericCollectionWrapper<>();
    for (NicEntity nic : nics) {
      results.add(mapper.applyMapping(nic, NicDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response create(NicRequestDto request, String vspId, String versionId, String componentId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_nic.toString());
    NicEntity nic = new MapNicRequestDtoToNicEntity().applyMapping(request, NicEntity.class);
    nic.setVspId(vspId);
    nic.setVersion(new Version(versionId));
    nic.setComponentId(componentId);
    componentManager.validateComponentExistence(vspId, nic.getVersion(), componentId);

    NicEntity createdNic = nicManager.createNic(nic);
    MapNicEntityToNicCreationResponseDto mapping =
        new MapNicEntityToNicCreationResponseDto();
    NicCreationResponseDto createdNicDto = mapping.applyMapping(createdNic,
        NicCreationResponseDto.class);
    return Response.ok(createdNic != null ? createdNicDto : null)
        .build();
  }

  @Override
  public Response get(String vspId, String versionId, String componentId, String nicId,
                      String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_nic.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    CompositionEntityResponse<Nic> response =
        nicManager.getNic(vspId, vspVersion, componentId, nicId);

    CompositionEntityResponseDto<NicDto> responseDto = new CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapNicToNicDto(), NicDto.class)
        .doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String componentId, String nicId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_nic.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    nicManager.deleteNic(vspId, vspVersion, componentId, nicId);
    return Response.ok().build();
  }

  @Override
  public Response update(NicRequestDto request, String vspId, String versionId, String componentId,
                         String nicId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_nic.toString());
    NicEntity nicEntity = new MapNicRequestDtoToNicEntity().applyMapping(request, NicEntity.class);
    nicEntity.setVspId(vspId);
    nicEntity.setVersion(new Version(versionId));
    nicEntity.setComponentId(componentId);
    nicEntity.setId(nicId);

    componentManager.validateComponentExistence(vspId, nicEntity.getVersion(), componentId);
    CompositionEntityValidationData validationData =
        nicManager.updateNic(nicEntity);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
        new MapCompositionEntityValidationDataToDto()
            .applyMapping(validationData, CompositionEntityValidationDataDto.class)).build() :
        Response.ok().build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String componentId, String nicId,
                                   String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Questionnaire_nic.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    QuestionnaireResponse questionnaireResponse =
        nicManager.getNicQuestionnaire(vspId, vspVersion, componentId, nicId);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String versionId,
                                      String componentId,
                                      String nicId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Questionnaire_nic.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    nicManager
        .updateNicQuestionnaire(vspId, vspVersion, componentId, nicId, questionnaireData);
    return Response.ok().build();
  }
}
