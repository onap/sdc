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
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.core.enrichment.types.ComponentMibInfo;
import org.openecomp.core.enrichment.types.MibInfo;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.MibDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MibDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
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
  private MibDao mibDao;
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
    ComponentMibInfo componentMibInfo =
        extractComponentMibInfo(componentEntry, vspId, version, errors);
    enrichComponentMib(componentMibInfo, vspId, version, errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return errors;
  }

  ComponentMibInfo extractComponentMibInfo(ComponentEntity componentEntity, String vspId,
                                           Version version,
                                           Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String componentId = componentEntity.getId();
    MibEntity entity = new MibEntity();

    entity.setVspId(vspId);
    entity.setVersion(version);
    entity.setComponentId(componentId);
    String componentName = componentEntity.getComponentCompositionData().getName();
    ComponentMibInfo componentMibInfo = new ComponentMibInfo();
    updComponentMibInfoByType(componentName, ArtifactType.SNMP_POLL, entity, componentMibInfo,
        errors);
    updComponentMibInfoByType(componentName, ArtifactType.SNMP_TRAP, entity, componentMibInfo,
        errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return componentMibInfo;
  }

  void updComponentMibInfoByType(String componentName, ArtifactType type,
                                 MibEntity mibEntity,
                                 ComponentMibInfo componentMibInfo,
                                 Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String path;
    mibEntity.setType(type);
    Optional<MibEntity> artifact =
        getMibDao().getByType(mibEntity);

    if (!artifact.isPresent()) {
      return;
    }
    path = componentName + File.separator + ArtifactCategory.DEPLOYMENT.getDisplayName()
        + File.separator + type.name();
    MibInfo mibInfo = new MibInfo();
    mibInfo.setName(path);
    mibInfo.setContent(artifact.get().getArtifact().array());
    switch (type) {
      case SNMP_POLL:
        componentMibInfo.setSnmpPoll(mibInfo);
        break;
      case SNMP_TRAP:
        componentMibInfo.setSnmpTrap(mibInfo);
        break;
      default:
        break;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  void enrichComponentMib(ComponentMibInfo componentMibInfo, String vspId, Version version,
                          Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceArtifact mibServiceArtifact = new ServiceArtifact();
    mibServiceArtifact.setVspId(vspId);
    mibServiceArtifact.setVersion(version);
    enrichMibFiles(mibServiceArtifact, componentMibInfo, errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  void enrichMibFiles(ServiceArtifact mibServiceArtifact, ComponentMibInfo componentMibInfo,
                      Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (componentMibInfo == null) {
      return;
    }
    enrichMibByType(componentMibInfo.getSnmpTrap(), ArtifactType.SNMP_TRAP, mibServiceArtifact,
        errors);
    enrichMibByType(componentMibInfo.getSnmpPoll(), ArtifactType.SNMP_POLL, mibServiceArtifact,
        errors);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  void enrichMibByType(MibInfo mibInfo, ArtifactType type, ServiceArtifact mibServiceArtifact,
                       Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (mibInfo == null) {
      return;
    }
    FileContentHandler mibs;
    try {
      mibs = FileUtils.getFileContentMapFromZip(FileUtils.toByteArray(mibInfo.getContent()));
    } catch (IOException ioException) {
      ErrorMessage.ErrorMessageUtil
          .addMessage(mibServiceArtifact.getName() + "." + type.name(), errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
      return;
    }
    Set<String> fileList = mibs.getFileList();
    for (String fileName : fileList) {
      mibServiceArtifact.setContentData(FileUtils.toByteArray(mibs.getFileContent(fileName)));
      mibServiceArtifact.setName(mibInfo.getName() + File.separator + fileName);
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

  private MibDao getMibDao() {
    if (mibDao == null) {
      mibDao = MibDaoFactory.getInstance().createInterface();
    }
    return mibDao;
  }

}
