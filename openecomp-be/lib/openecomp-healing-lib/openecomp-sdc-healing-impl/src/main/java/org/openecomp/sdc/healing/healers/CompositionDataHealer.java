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

package org.openecomp.sdc.healing.healers;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionDataExtractorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CompositionDataHealer implements Healer {
  private static final Version VERSION00 = new Version(0, 0);
  private static final Version VERSION01 = new Version(0, 1);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private static final OrchestrationTemplateDao orchestrationTemplateDataDao =
      OrchestrationTemplateDaoFactory.getInstance().createInterface();

  private static ComponentDao componentDao = ComponentDaoFactory.getInstance().createInterface();
  private static NicDao nicDao = NicDaoFactory.getInstance().createInterface();
  private static NetworkDao networkDao = NetworkDaoFactory.getInstance().createInterface();

  private static final ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  private static CompositionDataExtractor compositionDataExtractor =
      CompositionDataExtractorFactory.getInstance().createInterface();
  private static CompositionEntityDataManager compositionEntityDataManager =
      CompositionEntityDataManagerFactory.getInstance().createInterface();

  public CompositionDataHealer() {
  }

  @Override
  public Optional<CompositionData> heal(Map<String, Object> healingParams) throws IOException {
    mdcDataDebugMessage.debugEntryMessage(null);

    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = VERSION00.equals(healingParams.get(SdcCommon.VERSION))
        ? VERSION01
        : (Version) healingParams.get(SdcCommon.VERSION);

    Collection<ComponentEntity> componentEntities =
        componentDao.list(new ComponentEntity(vspId, version, null));
    Collection<NicEntity> nicEntities = nicDao.listByVsp(vspId, version);
    Collection<NetworkEntity> networkEntities =
        networkDao.list(new NetworkEntity(vspId, version, null));

    Optional<ToscaServiceModel> serviceModelForHealing = getServiceModelForHealing(vspId, version);

    if (!doesVspNeedCompositionDataHealing(componentEntities, networkEntities,
        nicEntities)) {
      updateComponentsDisplayNames(componentEntities);
      mdcDataDebugMessage.debugExitMessage(null);
      return Optional.empty();
    }


    if (!serviceModelForHealing.isPresent()) {
      mdcDataDebugMessage.debugExitMessage(null);
      return Optional.empty();
    }

    CompositionData compositionData = healCompositionData(vspId, version, serviceModelForHealing);

    mdcDataDebugMessage.debugExitMessage(null);
    return Optional.of(compositionData);
  }

  private CompositionData healCompositionData(String vspId, Version version,
                                              Optional<ToscaServiceModel> serviceModelForHealing) {
    ToscaServiceModel toscaServiceModel = serviceModelForHealing.get();
    CompositionData compositionData =
        getCompositionDataForHealing(vspId, version, toscaServiceModel);
    compositionEntityDataManager.saveCompositionData(vspId, version, compositionData);
    return compositionData;
  }

  private boolean doesVspNeedCompositionDataHealing(Collection<ComponentEntity> componentEntities,
                                                    Collection<NetworkEntity> networkEntities,
                                                    Collection<NicEntity> nicEntities) {

    return (CollectionUtils.isEmpty(componentEntities) && CollectionUtils.isEmpty(nicEntities) &&
        CollectionUtils.isEmpty(networkEntities));
  }

  private CompositionData getCompositionDataForHealing(String vspId, Version version,
                                                       ToscaServiceModel toscaServiceModel) {
    mdcDataDebugMessage.debugEntryMessage(null);

    if (Objects.isNull(toscaServiceModel)) {
      return null;
    }

    CompositionData compositionData = new CompositionData();
    if (Objects.nonNull(toscaServiceModel)) {
      compositionData = compositionDataExtractor
          .extractServiceCompositionData(toscaServiceModel);
      serviceModelDao.storeServiceModel(vspId, version, toscaServiceModel);
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return compositionData;
  }

  private void updateComponentsDisplayNames(Collection<ComponentEntity> componentEntities) {
    if (CollectionUtils.isEmpty(componentEntities)) {
      return;
    }

    for (ComponentEntity component : componentEntities) {
      updateComponentName(component);
      componentDao.update(component);
    }
  }

  private void updateComponentName(ComponentEntity component) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", component.getVspId(), component
        .getId());

    ComponentData componentData =
        JsonUtil.json2Object(component.getCompositionData(), ComponentData.class);
    componentData
        .setDisplayName(compositionDataExtractor.getComponentDisplayName(componentData.getName()));
    String displayName = componentData.getDisplayName();
    componentData.setName(componentData.getName().replace("com.att.d2", "org.openecomp"));
    componentData.setVfcCode(displayName);
    component.setCompositionData(JsonUtil.object2Json(componentData));

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", component.getVspId(), component
        .getId());

  }

  private Optional<ToscaServiceModel> getServiceModelForHealing(String vspId, Version version)
      throws IOException {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    /*UploadDataEntity uploadData =
        vendorSoftwareProductDao.getUploadData(new UploadDataEntity(vspId, version));*/
    UploadDataEntity uploadData =
        orchestrationTemplateDataDao.getOrchestrationTemplate(vspId, version);

    if (Objects.isNull(uploadData) || Objects.isNull(uploadData.getContentData())) {
      return Optional.empty();
    }

    TranslatorOutput translatorOutput = getTranslatorOutputForHealing(uploadData);

    if (Objects.isNull(translatorOutput)) {
      return Optional.empty();
    }

    try {
      serviceModelDao.storeServiceModel(vspId, version,
          translatorOutput.getToscaServiceModel());
    }catch (Exception e){
      return Optional.empty();
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return Optional.of(translatorOutput.getToscaServiceModel());
  }

  private TranslatorOutput getTranslatorOutputForHealing(UploadDataEntity uploadData){

    FileContentHandler fileContentHandler;
    try {
      fileContentHandler =
          CommonUtil.loadUploadFileContent(uploadData.getContentData().array());
      return HeatToToscaUtil.loadAndTranslateTemplateData(fileContentHandler);
    }catch (Exception e){
      return null;
    }
  }
}
