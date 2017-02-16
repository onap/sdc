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

package org.openecomp.sdc.translator.services.heattotosca.globaltypes;


import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.services.ToscaUtil;

import java.util.HashMap;
import java.util.Map;

public class GlobalTypesUtil {


  /**
   * Create common import list map.
   *
   * @return the map
   */
  public static Map<String, Import> createCommonImportList() {
    Map<String, Import> importsMap = new HashMap<>();
    importsMap.put("common_definitions", CommonGlobalTypes.createCommonServiceTemplateImport());
    return importsMap;
  }

  /**
   * Create attachment capability capability definition.
   *
   * @return the capability definition
   */
  public static CapabilityDefinition createAttachmentCapability() {
    CapabilityDefinition capability = new CapabilityDefinition();
    capability.setType(ToscaCapabilityType.ATTACHMENT.getDisplayName());

    return capability;
  }

  /**
   * Create service template import import.
   *
   * @param serviceTemplateName the service template name
   * @return the import
   */
  public static Import createServiceTemplateImport(String serviceTemplateName) {
    Import serviceTemplateImport = new Import();
    serviceTemplateImport.setFile(ToscaUtil.getServiceTemplateFileName(serviceTemplateName));
    return serviceTemplateImport;
  }
}
