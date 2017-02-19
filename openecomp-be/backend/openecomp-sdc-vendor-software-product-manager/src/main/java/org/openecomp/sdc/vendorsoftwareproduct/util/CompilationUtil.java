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

package org.openecomp.sdc.vendorsoftwareproduct.util;

import org.openecomp.core.enrichment.types.ComponentArtifactType;
import org.openecomp.core.enrichment.types.ComponentCeilometerInfo;
import org.openecomp.core.enrichment.types.ComponentMibInfo;
import org.openecomp.core.enrichment.types.MibInfo;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfig;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfigFactory;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.impl.tosca.ComponentInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentArtifactEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The type Compilation util.
 */
public class CompilationUtil {

  private static final ApplicationConfig applicationConfig =
      ApplicationConfigFactory.getInstance().createInterface();
  private static final ComponentArtifactDao componentArtifactDao =
      ComponentArtifactDaoFactory.getInstance().createInterface();

  /**
   * Add monitoring info.
   *
   * @param componentInfo the component info
   * @param compileErrors the compile errors
   */
  public static void addMonitoringInfo(ComponentInfo componentInfo,
                                       Map<String, List<ErrorMessage>> compileErrors) {

    String ceilometerJson =
        applicationConfig.getConfigurationData("vsp.monitoring", "component.ceilometer").getValue();
    ComponentCeilometerInfo ceilometerInfo =
        JsonUtil.json2Object(ceilometerJson, ComponentCeilometerInfo.class);
    componentInfo.setCeilometerInfo(ceilometerInfo);
  }

  /**
   * Add mib info.
   *
   * @param vspId           the vsp id
   * @param version         the version
   * @param componentEntity the component entity
   * @param componentInfo   the component info
   * @param compileErrors   the compile errors
   */
  public static void addMibInfo(String vspId, Version version, org.openecomp.sdc
      .vendorsoftwareproduct.dao.type.ComponentEntity componentEntity,
      ComponentInfo componentInfo,
      Map<String, List<ErrorMessage>> compileErrors) {

    String componentId = componentEntity.getId();

    ComponentArtifactEntity entity = new ComponentArtifactEntity();
    entity.setVspId(vspId);
    entity.setVersion(version);
    entity.setComponentId(componentId);

    ComponentMibInfo componentMibInfo = new ComponentMibInfo();

    extractAndInsertMibContentToComponentInfo(componentId, ComponentArtifactType.SNMP_POLL, entity,
        componentMibInfo, compileErrors);
    extractAndInsertMibContentToComponentInfo(componentId, ComponentArtifactType.SNMP_TRAP, entity,
        componentMibInfo, compileErrors);
    componentInfo.setMibInfo(componentMibInfo);
  }

  private static void extractAndInsertMibContentToComponentInfo(String componentId,
                                         ComponentArtifactType type,
                                         ComponentArtifactEntity componentArtifactEntity,
                                         ComponentMibInfo componentMibInfo,
                                         Map<String, List<ErrorMessage>> compileErrors) {
    String path;
    componentArtifactEntity.setType(type);
    ComponentArtifactEntity artifact =
        componentArtifactDao.getArtifactByType(componentArtifactEntity);

    if (artifact == null) {
      return;
    }
    path = componentId + File.separator + type.name();
    MibInfo mibInfo = new MibInfo();
    mibInfo.setName(path);
    mibInfo.setContent(artifact.getArtifact().array());
    switch (type) {
      case SNMP_POLL:
        componentMibInfo.setSnmpPoll(mibInfo);
        break;
      case SNMP_TRAP:
        componentMibInfo.setSnmpTrap(mibInfo);
        break;
      default:
    }


  }
}
