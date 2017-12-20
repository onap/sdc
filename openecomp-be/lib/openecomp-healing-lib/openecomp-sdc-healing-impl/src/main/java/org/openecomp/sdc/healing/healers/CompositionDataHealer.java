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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionDataExtractorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.util.Collection;
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
  private static ComputeDao computeDao = ComputeDaoFactory.getInstance().createInterface();
  private static DeploymentFlavorDao deloymentFlavorDao = DeploymentFlavorDaoFactory.getInstance()
      .createInterface();
  private static ImageDao imageDao = ImageDaoFactory.getInstance().createInterface();

  private static final ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  private static CompositionDataExtractor compositionDataExtractor =
      CompositionDataExtractorFactory.getInstance().createInterface();
  private static CompositionEntityDataManager compositionEntityDataManager =
      CompositionEntityDataManagerFactory.getInstance().createInterface();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  public CompositionDataHealer() {
  }

  @Override
  public Optional<CompositionData> heal(String vspId,
                                        Version version) throws IOException {
    mdcDataDebugMessage.debugEntryMessage(null);

    Collection<ComponentEntity> componentEntities =
        componentDao.list(new ComponentEntity(vspId, version, null));
    Collection<NicEntity> nicEntities = nicDao.listByVsp(vspId, version);
    Collection<NetworkEntity> networkEntities =
        networkDao.list(new NetworkEntity(vspId, version, null));

    Optional<Pair<ToscaServiceModel, ToscaServiceModel>> serviceModels =
        getServiceModelForHealing(vspId, version);
    CompositionData compositionData = null;
    if (!doesVspNeedCompositionDataHealing(componentEntities, networkEntities, nicEntities)) {
      updateComponentsDisplayNames(componentEntities);
      mdcDataDebugMessage.debugExitMessage(null, null);
    } else {
      if (!serviceModels.isPresent()) {
        mdcDataDebugMessage.debugExitMessage(null, null);
        return Optional.empty();
      }
      compositionData = serviceModels.isPresent() ? healCompositionData(vspId, version,
          serviceModels.get()) : null;
    }

    if (serviceModels.isPresent()) {
      compositionData =
          getCompositionDataForHealing(vspId, version, serviceModels.get());
      HealNfodData(vspId, version, compositionData);
    }
    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.ofNullable(compositionData);
  }

  private void HealNfodData(String vspId, Version version, CompositionData compositionData) {
    Collection<ComponentEntity> componentEntities;
    componentEntities = componentDao.list(new ComponentEntity(vspId, version, null));

    Collection<ComputeEntity> computeEntities = computeDao.listByVsp(vspId, version);
    Collection<ImageEntity> imageEntities = imageDao.listByVsp(vspId, version);

    if (CollectionUtils.isEmpty(computeEntities) && CollectionUtils.isEmpty(imageEntities)) {
      for (Component component : compositionData.getComponents()) {
        String componentId = null;
        for (ComponentEntity componentEntity : componentEntities) {
          if (componentEntity.getComponentCompositionData().getName().equals(component.getData()
              .getName())) {
            componentId = componentEntity.getId();
            break;
          }
        }
        compositionEntityDataManager.saveComputesFlavorByComponent(vspId, version, component,
            componentId);
        compositionEntityDataManager.saveImagesByComponent(vspId, version, component,
            componentId);
      }

    }
  }

  private CompositionData healCompositionData(String vspId, Version version,
                                              Pair<ToscaServiceModel, ToscaServiceModel> toscaServiceModels) {
    CompositionData compositionData =
        getCompositionDataForHealing(vspId, version, toscaServiceModels);
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
                                                       Pair<ToscaServiceModel, ToscaServiceModel> toscaServiceModels) {
    mdcDataDebugMessage.debugEntryMessage(null);

    if (Objects.isNull(toscaServiceModels)) {
      return null;
    }

    CompositionData compositionData = compositionDataExtractor
        .extractServiceCompositionData(toscaServiceModels.getRight());
    serviceModelDao.storeServiceModel(vspId, version, toscaServiceModels.getLeft());

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

  private Optional<Pair<ToscaServiceModel, ToscaServiceModel>> getServiceModelForHealing(String
                                                                                             vspId,
                                                                                         Version
                                                                                             version)
      throws IOException {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    /*OrchestrationTemplateEntity uploadData =
        vendorSoftwareProductDao.getUploadData(new OrchestrationTemplateEntity(vspId, version));*/
    OrchestrationTemplateEntity uploadData = orchestrationTemplateDataDao.get(vspId, version);

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
    } catch (Exception e) {
      log.debug("", e);
      return Optional.empty();
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return Optional.of(new ImmutablePair<>(translatorOutput.getToscaServiceModel(), translatorOutput
        .getNonUnifiedToscaServiceModel()));
  }

  private TranslatorOutput getTranslatorOutputForHealing(OrchestrationTemplateEntity uploadData) {

    FileContentHandler fileContentHandler;
    try {
      fileContentHandler =
          CommonUtil.validateAndUploadFileContent(
              OnboardingTypesEnum.ZIP, uploadData.getContentData().array());
      return HeatToToscaUtil.loadAndTranslateTemplateData(fileContentHandler);
    } catch (Exception e) {
      log.debug("", e);
      return null;
    }
  }
}
