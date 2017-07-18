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
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CompositionEditNotAllowedErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DeleteNicErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DuplicateNicInComponentErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NicInternalNetworkErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NicNetworkIdNotAllowedExternalNetworkErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NotSupportedHeatOnboardMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NicCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class NicManagerImpl implements NicManager {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private NicDao nicDao;
  private CompositionEntityDataManager compositionEntityDataManager;
  private NetworkManager networkManager;
  private  VendorSoftwareProductInfoDao vspInfoDao;

  public NicManagerImpl(NicDao nicDao,
                        CompositionEntityDataManager compositionEntityDataManager,
                        NetworkManager networkManager,
                        VendorSoftwareProductInfoDao vspInfoDao) {
    this.nicDao = nicDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.networkManager = networkManager;
    this.vspInfoDao = vspInfoDao;
  }

  @Override
  public Collection<NicEntity> listNics(String vspId, Version version, String componentId,
                                        String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    Collection<NicEntity> nics = nicDao.list(new NicEntity(vspId, version, componentId, null));

    if (!nics.isEmpty()) {
      Map<String, String> networksNameById = listNetworksNameById(vspId, version, user);
      nics.forEach(nicEntity -> {
        Nic nic = nicEntity.getNicCompositionData();
        nic.setNetworkName(networksNameById.get(nic.getNetworkId()));
        nicEntity.setNicCompositionData(nic);
      });
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return nics;
  }

  private Map<String, String> listNetworksNameById(String vspId, Version version, String user) {
    Collection<NetworkEntity> networks = networkManager.listNetworks(vspId, version, user);
    return networks.stream().collect(Collectors.toMap(NetworkEntity::getId,
        networkEntity -> networkEntity.getNetworkCompositionData().getName()));
  }

  @Override
  public NicEntity createNic(NicEntity nic, String user) {
    NicEntity createdNic = null;
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", nic.getVspId(),
        nic.getComponentId());

    if (!vspInfoDao.isManual(nic.getVspId(), nic.getVersion())) {
      ErrorCode onboardingMethodUpdateErrorCode = NotSupportedHeatOnboardMethodErrorBuilder
          .getAddNicNotSupportedHeatOnboardMethodErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_NIC, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          onboardingMethodUpdateErrorCode.message());
      throw new CoreException(onboardingMethodUpdateErrorCode);
    } else {
      validateNic(nic, user);
      createdNic = createNic(nic);
    }

    //nicDao.updateVspLatestModificationTime(nic.getVspId(), nic.getVersion());

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", nic.getVspId(), nic.getComponentId());

    return createdNic;
  }

  private NicEntity createNic(NicEntity nic) {
    return compositionEntityDataManager.createNic(nic);
  }

  private void validateNic(NicEntity nic, String user) {
    Collection<NicEntity> listNics = listNics(nic.getVspId(), nic.getVersion(), nic
        .getComponentId(), user);
    String networkId = nic.getNicCompositionData().getNetworkId();
    NetworkType networkType = nic.getNicCompositionData().getNetworkType();
    String networkDescription = nic.getNicCompositionData().getNetworkDescription();
    listNics.forEach(nicEntity -> {
      Nic nicdata = nicEntity.getNicCompositionData();
      if (nic.getNicCompositionData().getName().equalsIgnoreCase(nicdata.getName())) {
        final ErrorCode duplicateNicInComponentErrorBuilder =
            new DuplicateNicInComponentErrorBuilder(nic.getNicCompositionData().getName(),
                nic.getComponentId()).build();
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CREATE_NIC, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(),
            duplicateNicInComponentErrorBuilder.message());
        throw new CoreException(duplicateNicInComponentErrorBuilder);
      }

    });

    if (networkType.equals(NetworkType.Internal)) {
      if (!(networkId == null || networkId.equals(""))) {
        //NetworkEntity ne = getNetwork(nic.getVspId(), activeVersion, networkId);
        final CompositionEntityResponse<Network> network =
            networkManager.getNetwork(nic.getVspId(), nic.getVersion(), networkId,
                user);
      }

      if (!(networkDescription == null || networkDescription.equals(""))) {
        final ErrorCode nicNetworkDescriptionErrorBuilder =
            new NicInternalNetworkErrorBuilder()
                .getNetworkDescriptionInternalNetworkErrorBuilder();
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CREATE_NIC, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), nicNetworkDescriptionErrorBuilder.message());
        throw new CoreException(nicNetworkDescriptionErrorBuilder);
      }

    } else if (networkType.equals(NetworkType.External)) {
      if (!(networkId == null || networkId.equals(""))) {
        final ErrorCode nicNetworkIdNotAllowedExternalNetworkErrorBuilder =
            new NicNetworkIdNotAllowedExternalNetworkErrorBuilder().build();
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CREATE_NIC, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(),
            nicNetworkIdNotAllowedExternalNetworkErrorBuilder.message());
        throw new CoreException(nicNetworkIdNotAllowedExternalNetworkErrorBuilder);
      }
    }
  }

  @Override
  public CompositionEntityResponse<Nic> getNic(String vspId, Version version, String componentId,
                                               String nicId, String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id, nic id", vspId, componentId, nicId);

    NicEntity nicEntity = getNic(vspId, version, componentId, nicId);
    Nic nic = nicEntity.getNicCompositionData();

    NicCompositionSchemaInput schemaInput = new NicCompositionSchemaInput();
    schemaInput.setManual(vspInfoDao.isManual(vspId, version));
    schemaInput.setNic(nic);
    Map<String, String> networksNameById = listNetworksNameById(vspId, version, user);
    nic.setNetworkName(networksNameById.get(nic.getNetworkId()));
    schemaInput.setNetworkIds(networksNameById.keySet());

    CompositionEntityResponse<Nic> response = new CompositionEntityResponse<>();
    response.setId(nicId);
    response.setData(nic);
    response.setSchema(getNicCompositionSchema(schemaInput));

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", vspId, componentId, nicId);

    return response;
  }


  private NicEntity getNic(String vspId, Version version, String componentId, String nicId) {
    NicEntity retrieved = nicDao.get(new NicEntity(vspId, version, componentId, nicId));
    VersioningUtil
        .validateEntityExistence(retrieved, new NicEntity(vspId, version, componentId, nicId),
            VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteNic(String vspId, Version version, String componentId, String nicId,
                        String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id", vspId, componentId, nicId);

    if (!vspInfoDao.isManual(vspId, version)) {
      final ErrorCode deleteNicErrorBuilder =
          new DeleteNicErrorBuilder().getDeleteNicForHeatOnboardedVspErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_NIC, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          deleteNicErrorBuilder.message());
      throw new CoreException(deleteNicErrorBuilder);
    }

    NicEntity nicEntity = getNic(vspId, version, componentId, nicId);
    if (nicEntity != null) {
      nicDao.delete(nicEntity);
    }

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", vspId, componentId, nicId);
  }

  @Override
  public CompositionEntityValidationData updateNic(NicEntity nic, String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id", nic.getVspId(), nic.getComponentId(),
            nic.getId());

    NicEntity retrieved =
        getNic(nic.getVspId(), nic.getVersion(), nic.getComponentId(), nic.getId());

    NicCompositionSchemaInput schemaInput = new NicCompositionSchemaInput();
    schemaInput.setManual(vspInfoDao.isManual(nic.getVspId(), nic.getVersion()));
    schemaInput.setNic(retrieved.getNicCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(nic, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      nicDao.update(nic);
    }

    //nicDao.updateVspLatestModificationTime(nic.getVspId(), nic.getVersion());

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", nic.getVspId(), nic.getComponentId(),
            nic.getId());

    return validationData;
  }

  @Override
  public QuestionnaireResponse getNicQuestionnaire(String vspId, Version version,
                                                   String componentId, String nicId, String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id", vspId, componentId, nicId);

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    NicEntity nicQuestionnaire = nicDao.getQuestionnaireData(vspId, version, componentId, nicId);
    VersioningUtil.validateEntityExistence(nicQuestionnaire,
        new NicEntity(vspId, version, componentId, nicId), VspDetails.ENTITY_TYPE);

    questionnaireResponse.setData(nicQuestionnaire.getQuestionnaireData());
    questionnaireResponse.setSchema(getNicQuestionnaireSchema(null));

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", vspId, componentId, nicId);

    return questionnaireResponse;
  }

  @Override
  public void updateNicQuestionnaire(String vspId, Version version, String componentId,
                                     String nicId, String questionnaireData, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId, nicId);

    getNic(vspId, version, componentId, nicId);

    nicDao.updateQuestionnaireData(vspId, version, componentId, nicId, questionnaireData);

    //nicDao.updateVspLatestModificationTime(vspId, version);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId, nicId);
  }

  protected String getNicQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.nic, schemaInput);
  }

  protected String getNicCompositionSchema(NicCompositionSchemaInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.nic, schemaInput);
  }
  // todo *************************** move to reusable place! *************************

  /*private boolean isManual(String vspId, Version version) {
    return false;
  }*/
}
