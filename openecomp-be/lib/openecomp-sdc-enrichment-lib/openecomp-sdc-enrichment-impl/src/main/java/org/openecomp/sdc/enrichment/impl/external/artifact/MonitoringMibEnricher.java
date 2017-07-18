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
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MonitoringUploadDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MonitoringMibEnricher implements ExternalArtifactEnricherInterface {

  private EnrichedServiceModelDao enrichedServiceModelDao;
  private ComponentDao componentDao;
  private ComponentArtifactDao componentArtifactDao;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Enrich map.
   *
   * @param enrichmentInfo the enrichmentInfo
   * @return the map
   */
  public Map<String, List<ErrorMessage>> enrich(EnrichmentInfo enrichmentInfo) {

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    String vspId = enrichmentInfo.getKey();
    Version version = enrichmentInfo.getVersion();

    Collection<ComponentEntity> components =
        getComponentDao().list(new ComponentEntity(vspId, version, null));
    components
        .forEach(componentEntry -> errors.putAll(enrichComponent(componentEntry, vspId, version)));

    return errors;
  }

  Map<String, List<ErrorMessage>> enrichComponent(ComponentEntity componentEntry, String vspId,
                                                  Version version) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    ComponentMonitoringUploadInfo componentMonitoringUploadInfo =
        extractComponentMibInfo(componentEntry, vspId, version, errors);
    enrichComponentMib(componentMonitoringUploadInfo, vspId, version, errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return errors;
  }

  private ComponentMonitoringUploadInfo extractComponentMibInfo(ComponentEntity componentEntity,
                                                                String vspId,
                                                                Version version,
                                                                Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String componentId = componentEntity.getId();
    ComponentMonitoringUploadEntity entity = new ComponentMonitoringUploadEntity();

    entity.setVspId(vspId);
    entity.setVersion(version);
    entity.setComponentId(componentId);
    String componentName = componentEntity.getComponentCompositionData().getName();
    ComponentMonitoringUploadInfo componentMonitoringUploadInfo =
        new ComponentMonitoringUploadInfo();
    for (MonitoringUploadType monitoringUploadType : MonitoringUploadType.values()) {
      updComponentMibInfoByType(componentName, monitoringUploadType, entity,
          componentMonitoringUploadInfo,
          errors);
    }
//    updComponentMibInfoByType(componentName, MonitoringUploadType.SNMP_POLL, entity,
//        componentMonitoringUploadInfo,
//        errors);
//    updComponentMibInfoByType(componentName, MonitoringUploadType.SNMP_TRAP, entity,
//        componentMonitoringUploadInfo,
//        errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return componentMonitoringUploadInfo;
  }

  private void updComponentMibInfoByType(String componentName, MonitoringUploadType type,
                                         ComponentMonitoringUploadEntity componentMonitoringUploadEntity,
                                         ComponentMonitoringUploadInfo componentMonitoringUploadInfo,
                                         Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String path;
    componentMonitoringUploadEntity.setType(type);
    Optional<ComponentMonitoringUploadEntity> artifact =
        getComponentArtifactDao().getByType(componentMonitoringUploadEntity);

    if (!artifact.isPresent()) {
      return;
    }
    path = componentName + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName()
        + File.separator + type.name();
    MonitoringArtifactInfo monitoringArtifactInfo = new MonitoringArtifactInfo();
    monitoringArtifactInfo.setName(path);
    monitoringArtifactInfo.setContent(artifact.get().getArtifact().array());
    switch (type) { //todo as part of ATTASDC-4503
      case SNMP_POLL:
        componentMonitoringUploadInfo.setSnmpPoll(monitoringArtifactInfo);
        break;
      case SNMP_TRAP:
        componentMonitoringUploadInfo.setSnmpTrap(monitoringArtifactInfo);
        break;
      case VES_EVENTS:
        componentMonitoringUploadInfo.setVesEvent(monitoringArtifactInfo);
        break;
      default:
        break;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void enrichComponentMib(ComponentMonitoringUploadInfo componentMonitoringUploadInfo,
                                  String vspId,
                                  Version version,
                                  Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceArtifact mibServiceArtifact = new ServiceArtifact();
    mibServiceArtifact.setVspId(vspId);
    mibServiceArtifact.setVersion(version);
    enrichMibFiles(mibServiceArtifact, componentMonitoringUploadInfo, errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void enrichMibFiles(ServiceArtifact monitoringArtifact,
                              ComponentMonitoringUploadInfo componentMonitoringUploadInfo,
                              Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (componentMonitoringUploadInfo == null) {
      return;
    }
    //todo fix as part of ATTASDC-4503
    enrichMibByType(componentMonitoringUploadInfo.getSnmpTrap(), MonitoringUploadType.SNMP_TRAP,
        monitoringArtifact,
        errors);
    enrichMibByType(componentMonitoringUploadInfo.getSnmpPoll(), MonitoringUploadType.SNMP_POLL,
        monitoringArtifact,
        errors);
    enrichMibByType(componentMonitoringUploadInfo.getVesEvent(), MonitoringUploadType.VES_EVENTS,
        monitoringArtifact,
        errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void enrichMibByType(MonitoringArtifactInfo monitoringArtifactInfo,
                               MonitoringUploadType type,
                               ServiceArtifact mibServiceArtifact,
                               Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (monitoringArtifactInfo == null) {
      return;
    }
    FileContentHandler mibs;
    try {
      mibs = FileUtils
          .getFileContentMapFromZip(FileUtils.toByteArray(monitoringArtifactInfo.getContent()));
    } catch (IOException ioException) {
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

    mdcDataDebugMessage.debugExitMessage(null, null);
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
