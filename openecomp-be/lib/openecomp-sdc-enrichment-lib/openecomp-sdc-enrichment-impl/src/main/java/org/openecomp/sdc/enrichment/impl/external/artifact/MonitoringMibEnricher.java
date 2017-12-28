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

package org.openecomp.sdc.enrichment.impl.external.artifact;

import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.enrichment.types.ComponentMonitoringUploadInfo;
import org.openecomp.core.enrichment.types.MonitoringArtifactInfo;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.enrichment.inter.ExternalArtifactEnricherInterface;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.Directive;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MonitoringUploadDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.openecomp.sdc.tosca.services.ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME;
import static org.openecomp.sdc.tosca.services.ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME;

public class MonitoringMibEnricher implements ExternalArtifactEnricherInterface {

  private EnrichedServiceModelDao enrichedServiceModelDao;
  private ComponentDao componentDao;
  private ComponentArtifactDao componentArtifactDao;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final String COMPONENT_PREFIX = "org.openecomp.resource.vfc.";

  private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * Enrich map.
   *
   * @param enrichmentInfo the enrichmentInfo
   * @return the map
   */
  public Map<String, List<ErrorMessage>> enrich(EnrichmentInfo enrichmentInfo,
                                                ToscaServiceModel serviceModel) {

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    String vspId = enrichmentInfo.getKey();
    Version version = enrichmentInfo.getVersion();

    Collection<ComponentEntity> components =
        getComponentDao().list(new ComponentEntity(vspId, version, null));
    components
        .forEach(componentEntry -> errors.putAll(enrichComponent(vspId, version, componentEntry,
            serviceModel)));

    return errors;
  }

  private Map<String, List<ErrorMessage>> enrichComponent(String vspId,
                                                  Version version,
                                                  ComponentEntity component,
                                                  ToscaServiceModel serviceModel) {
    Set<String> abstractNodeTypes =
        extractAbstractTypesFromSameTypeFromServiceModel(component, serviceModel);
    return enrichComponent(vspId, version, component, abstractNodeTypes);
  }

  private Set<String> extractAbstractTypesFromSameTypeFromServiceModel(ComponentEntity component,
                                                                       ToscaServiceModel serviceModel) {
    Set<String> abstractNodeTypes = new HashSet<>();
    Map<String, ServiceTemplate> serviceTemplates = serviceModel.getServiceTemplates();
    String typeToCheck =
        getComponentVfcTypeToCheck(component.getComponentCompositionData().getName());

    for (ServiceTemplate serviceTemplate : serviceTemplates.values()) {
      collectAllAbstractNodeTypesPointingToType(
          typeToCheck, serviceTemplate, serviceTemplates, abstractNodeTypes);
    }

    return abstractNodeTypes;
  }

  private String getComponentVfcTypeToCheck(String type) {
    return Objects.isNull(type) ? ""
        : type.replace(COMPONENT_PREFIX, COMPONENT_PREFIX + "compute.");
  }

  private void collectAllAbstractNodeTypesPointingToType(String typeToCheck,
                                                         ServiceTemplate serviceTemplate,
                                                         Map<String, ServiceTemplate> serviceTemplates,
                                                         Set<String> abstractNodeTypes) {
    Map<String, NodeTemplate> nodeTemplates =
        DataModelUtil.getNodeTemplates(serviceTemplate);

    for (Map.Entry<String, NodeTemplate> nodeTemplateEntry : nodeTemplates.entrySet()) {
      handleNodeTemplate(nodeTemplateEntry.getValue(), typeToCheck,
          serviceTemplates, abstractNodeTypes);
    }
  }

  private void handleNodeTemplate(NodeTemplate nodeTemplate,
                                  String typeToCheck,
                                  Map<String, ServiceTemplate> serviceTemplates,
                                  Set<String> abstractNodeTypes) {
    List<String> directives = DataModelUtil.getDirectives(nodeTemplate);
    if (directives.contains(Directive.SUBSTITUTABLE.getDisplayName())) {
      handleSubstitutionServiceTemplate(typeToCheck, nodeTemplate, serviceTemplates,
          abstractNodeTypes);
    }
  }

  private void handleSubstitutionServiceTemplate(String typeToCheck,
                                                 NodeTemplate nodeTemplate,
                                                 Map<String, ServiceTemplate> serviceTemplates,
                                                 Set<String> abstractNodeTypes) {
    Object serviceTemplateFilter =
        DataModelUtil.getPropertyValue(nodeTemplate, SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
    if (Objects.nonNull(serviceTemplateFilter) && serviceTemplateFilter instanceof Map) {
      String substituteServiceTemplateName =
          (String) ((Map<String, Object>) serviceTemplateFilter)
              .get(SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME);
      ServiceTemplate substituteServiceTemplate =
          serviceTemplates.get(substituteServiceTemplateName);
      if (doesNodeTypeExistInSubServiceTemplate(typeToCheck, substituteServiceTemplate)) {
        abstractNodeTypes.add(nodeTemplate.getType());
      }
    }
  }

  private boolean doesNodeTypeExistInSubServiceTemplate(String nodeTypeId,
                                                        ServiceTemplate substituteServiceTemplate) {
    return Objects
        .nonNull(DataModelUtil.getNodeType(substituteServiceTemplate, nodeTypeId));
  }

  Map<String, List<ErrorMessage>> enrichComponent(String vspId,
                                                  Version version,
                                                  ComponentEntity componentEntry,
                                                  Set<String> abstractNodeTypes) {


    mdcDataDebugMessage.debugEntryMessage(null);

    Map<String, List<ErrorMessage>> errors = new HashMap<>();

    List<ComponentMonitoringUploadInfo> componentMonitoringUploadInfoList =
        extractComponentMibInfo(vspId, version, componentEntry, abstractNodeTypes);

    componentMonitoringUploadInfoList.forEach(
        componentUploadInfo -> enrichComponentMib(vspId, version, componentUploadInfo, errors));

    mdcDataDebugMessage.debugExitMessage(null);
    return errors;
  }

  private List<ComponentMonitoringUploadInfo> extractComponentMibInfo(String vspId, Version version,
                                                                      ComponentEntity componentEntity,
                                                                      Set<String> abstractNodeTypes) {


    mdcDataDebugMessage.debugEntryMessage(null);

    String componentId = componentEntity.getId();
    ComponentMonitoringUploadEntity entity = new ComponentMonitoringUploadEntity();

    entity.setVspId(vspId);
    entity.setVersion(version);
    entity.setComponentId(componentId);
    List<ComponentMonitoringUploadInfo> componentMonitoringUploadInfoList = new ArrayList<>();

    abstractNodeTypes.forEach(unifiedComponentNodeType -> componentMonitoringUploadInfoList
        .add(updComponentMibInfoByType(unifiedComponentNodeType, entity)));

    mdcDataDebugMessage.debugExitMessage(null);
    return componentMonitoringUploadInfoList;
  }

  private ComponentMonitoringUploadInfo updComponentMibInfoByType(String componentName,
                                                                  ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {


    mdcDataDebugMessage.debugEntryMessage(null);

    ComponentMonitoringUploadInfo componentMonitoringUploadInfo =
        new ComponentMonitoringUploadInfo();

    for (MonitoringUploadType type : MonitoringUploadType.values()) {
      componentMonitoringUploadEntity.setType(type);
      Optional<ComponentMonitoringUploadEntity> artifact =
          getComponentArtifactDao().getByType(componentMonitoringUploadEntity);

      if (!artifact.isPresent()) {
        continue;
      }
      ComponentMonitoringUploadEntity mibArtifact = artifact.get();
      updateComponentMonitoringUploadInfoWithMib(getArtifactPath(type, componentName), type,
          mibArtifact,
          componentMonitoringUploadInfo);
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return componentMonitoringUploadInfo;
  }

  private String getArtifactPath(MonitoringUploadType type, String unifiedComponentNodeType) {
    return unifiedComponentNodeType + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName()
        + File.separator + type.name();
  }

  private void updateComponentMonitoringUploadInfoWithMib(String path,
                                                          MonitoringUploadType type,
                                                          ComponentMonitoringUploadEntity mibArtifact,
                                                          ComponentMonitoringUploadInfo componentMonitoringUploadInfo) {
    MonitoringArtifactInfo monitoringArtifactInfo = new MonitoringArtifactInfo();
    monitoringArtifactInfo.setName(path);
    monitoringArtifactInfo.setContent(mibArtifact.getArtifact().array());
    componentMonitoringUploadInfo.setMonitoringArtifactFile(type, monitoringArtifactInfo);
  }

  private void enrichComponentMib(String vspId,
                                  Version version,
                                  ComponentMonitoringUploadInfo componentUploadInfo,
                                  Map<String, List<ErrorMessage>> errors) {
    mdcDataDebugMessage.debugEntryMessage(null);

    ServiceArtifact mibServiceArtifact = new ServiceArtifact();
    mibServiceArtifact.setVspId(vspId);
    mibServiceArtifact.setVersion(version);
    enrichMibFiles(mibServiceArtifact, componentUploadInfo, errors);


    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void enrichMibFiles(ServiceArtifact monitoringArtifact,
                              ComponentMonitoringUploadInfo componentMonitoringUploadInfo,
                              Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (componentMonitoringUploadInfo == null) {
      return;
    }
    enrichMibByType(componentMonitoringUploadInfo.getSnmpTrap(), MonitoringUploadType.SNMP_TRAP,
        monitoringArtifact,
        errors);
    enrichMibByType(componentMonitoringUploadInfo.getSnmpPoll(), MonitoringUploadType.SNMP_POLL,
        monitoringArtifact,
        errors);
    enrichMibByType(componentMonitoringUploadInfo.getVesEvent(), MonitoringUploadType.VES_EVENTS,
        monitoringArtifact,
        errors);

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void enrichMibByType(MonitoringArtifactInfo monitoringArtifactInfo,
                               MonitoringUploadType type,
                               ServiceArtifact mibServiceArtifact,
                               Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (monitoringArtifactInfo == null) {
      return;
    }
    FileContentHandler mibs;
    try {
      mibs = FileUtils
          .getFileContentMapFromZip(FileUtils.toByteArray(monitoringArtifactInfo.getContent()));
    } catch (IOException ioException) {
      LOG.debug("", ioException);
      ErrorMessage.ErrorMessageUtil
          .addMessage(mibServiceArtifact.getName() + "." + type.name(), errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
      return;
    }
    Set<String> fileList = mibs.getFileList();
    for (String fileName : fileList) {
      mibServiceArtifact.setContentData(FileUtils.toByteArray(mibs.getFileContent(fileName)));
      mibServiceArtifact.setName(monitoringArtifactInfo.getName() + File.separator + fileName);
      getEnrichedServiceModelDao().storeExternalArtifact(mibServiceArtifact);
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private EnrichedServiceModelDao getEnrichedServiceModelDao() {
    if (enrichedServiceModelDao == null) {
      enrichedServiceModelDao = EnrichedServiceModelDaoFactory.getInstance().createInterface();
    }
    return enrichedServiceModelDao;
  }

  private ComponentDao getComponentDao() {
    if (componentDao == null) {
      componentDao = ComponentDaoFactory.getInstance().createInterface();
    }
    return componentDao;
  }

  private ComponentArtifactDao getComponentArtifactDao() {
    if (componentArtifactDao == null) {
      componentArtifactDao = MonitoringUploadDaoFactory.getInstance().createInterface();
    }
    return componentArtifactDao;
  }

}
