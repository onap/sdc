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

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CompositionEditNotAllowedErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentQuestionnaireSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentManagerImpl implements ComponentManager {
  private static final MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ComponentDao componentDao;
  private CompositionEntityDataManager compositionEntityDataManager;
  private NicManager nicManager;

  public ComponentManagerImpl(
      ComponentDao componentDao,
      CompositionEntityDataManager compositionEntityDataManager,
      NicManager nicManager) {
    this.componentDao = componentDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.nicManager = nicManager;
  }

  @Override
  public Collection<ComponentEntity> listComponents(String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return componentDao.list(new ComponentEntity(vspId, version, null));
  }

  @Override
  public void deleteComponents(String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
    if (!isManual(vspId, version)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_COMPONENT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete component");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
    }

    //componentDao.updateVspLatestModificationTime(vspId, version);
    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
  }

  @Override
  public ComponentEntity createComponent(ComponentEntity component, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", component.getId());

    if (!isManual(component.getVspId(), component.getVersion())) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_COMPONENT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't create component");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(component.getVspId(), component.getVersion())
              .build());

    }
    //componentDao.updateVspLatestModificationTime(component.getVspId(), component.getVersion());
    mdcDataDebugMessage.debugExitMessage("VSP id", component.getId());
    return null;
  }

  @Override
  public CompositionEntityValidationData updateComponent(ComponentEntity component, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", component
        .getVspId(), component.getId());
    ComponentEntity retrieved =
        getComponent(component.getVspId(), component.getVersion(), component.getId());

    ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
    schemaInput.setManual(isManual(component.getVspId(), component.getVersion()));
    schemaInput.setComponent(retrieved.getComponentCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(component, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      componentDao.update(component);
      //componentDao.updateVspLatestModificationTime(component.getVspId(), component.getVersion());
    }
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", component.getVspId(),
        component.getId());

    return validationData;
  }

  public CompositionEntityResponse<ComponentData> getComponent(String vspId, Version version,
                                                               String componentId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);
    ComponentEntity componentEntity = getComponent(vspId, version, componentId);
    ComponentData component = componentEntity.getComponentCompositionData();

    ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
    schemaInput.setManual(isManual(vspId, version));
    schemaInput.setComponent(component);

    CompositionEntityResponse<ComponentData> response = new CompositionEntityResponse<>();
    response.setId(componentId);
    response.setData(component);
    response.setSchema(getComponentCompositionSchema(schemaInput));
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return response;
  }

  @Override
  public void deleteComponent(String vspId, Version version, String componentId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    if (!isManual(vspId, version)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_COMPONENT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete component");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
    }

    //componentDao.updateVspLatestModificationTime(vspId, version);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public QuestionnaireResponse getQuestionnaire(String vspId, Version version,
                                                String componentId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    ComponentEntity component = componentDao.getQuestionnaireData(vspId, version, componentId);
    VersioningUtil
        .validateEntityExistence(component, new ComponentEntity(vspId, version, componentId),
            VspDetails.ENTITY_TYPE);

    questionnaireResponse.setData(component.getQuestionnaireData());
    List<String> nicNames = nicManager.listNics(vspId, version, componentId, user).stream()
        .map(nic -> nic.getNicCompositionData().getName()).collect(Collectors.toList());
    questionnaireResponse.setSchema(getComponentQuestionnaireSchema(
        new ComponentQuestionnaireSchemaInput(nicNames, questionnaireResponse.getData() == null
            ? null
            : JsonUtil.json2Object(questionnaireResponse.getData(), Map.class))));

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
    return questionnaireResponse;
  }

  @Override
  public void updateQuestionnaire(String vspId, Version version, String componentId,
                                  String questionnaireData, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);
    validateComponentExistence(vspId, version, componentId, user);

    componentDao.updateQuestionnaireData(vspId, version, componentId, questionnaireData);

    //componentDao.updateVspLatestModificationTime(vspId, version);
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public void validateComponentExistence(String vspId, Version version, String componentId,
                                         String user) {
    getComponent(vspId, version, componentId);
  }

  private ComponentEntity getComponent(String vspId, Version version, String componentId) {
    ComponentEntity retrieved = componentDao.get(new ComponentEntity(vspId, version, componentId));
    VersioningUtil
        .validateEntityExistence(retrieved, new ComponentEntity(vspId, version, componentId),
            VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  protected String getComponentCompositionSchema(ComponentCompositionSchemaInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.component, schemaInput);
  }

  protected String getComponentQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component,
            schemaInput);
  }

  private boolean isManual(String vspId, Version version) {
    return false;
  }
}
