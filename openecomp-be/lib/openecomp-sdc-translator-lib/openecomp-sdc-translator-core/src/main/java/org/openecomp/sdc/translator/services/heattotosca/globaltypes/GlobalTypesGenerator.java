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

import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaNativeTypesServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.HashMap;
import java.util.Map;

public class GlobalTypesGenerator {

  private GlobalTypesGenerator() {
  }

  /**
   * Gets global types service template.
   *
   * @return the global types service template
   */
  public static Map<String, ServiceTemplate> getGlobalTypesServiceTemplate() {
    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();

    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        CommonGlobalTypes.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        NovaServerGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        CinderVolumeGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailVirtualNetworkGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailV2VirtualNetworkGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailV2VirtualMachineInterfaceGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        NeutronNetGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        NeutronPortGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailNetworkRuleGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailV2NetworkRuleGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        NeutronSecurityRulesGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        AbstractSubstituteGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ToscaNativeTypesServiceTemplate.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailComputeGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailPortGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailV2NetworkRuleGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailV2VirtualNetworkGlobalType.createServiceTemplate());
    ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates,
        ContrailAbstractSubstituteGlobalType.createServiceTemplate());
    return serviceTemplates;
  }

  private static void addGlobalServiceTemplate(Map<String, ServiceTemplate> serviceTemplates,
                                               ServiceTemplate commonServiceTemplate) {
    serviceTemplates
        .put(ToscaUtil.getServiceTemplateFileName(commonServiceTemplate), commonServiceTemplate);
  }

  /**
   * Gets global types import list.
   *
   * @return the global types import list
   */
  public static Map<String, Import> getGlobalTypesImportList() {
    Map<String, Import> globalImportMap = new HashMap<>();
    globalImportMap.put(Constants.COMMON_GLOBAL_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.COMMON_GLOBAL_TEMPLATE_NAME));
    globalImportMap.put(Constants.NOVA_SERVER_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.NOVA_SERVER_TEMPLATE_NAME));
    globalImportMap.put(Constants.NEUTRON_PORT_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.NEUTRON_PORT_TEMPLATE_NAME));
    globalImportMap.put(Constants.NEUTRON_SECURITY_RULES_TEMPLATE_NAME, GlobalTypesUtil
        .createServiceTemplateImport(Constants.NEUTRON_SECURITY_RULES_TEMPLATE_NAME));
    globalImportMap.put(Constants.NEUTRON_NET_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.NEUTRON_NET_TEMPLATE_NAME));
    globalImportMap.put(Constants.CINDER_VOLUME_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.CINDER_VOLUME_TEMPLATE_NAME));
    globalImportMap.put(Constants.CONTRAIL_VIRTUAL_NETWORK_TEMPLATE_NAME, GlobalTypesUtil
        .createServiceTemplateImport(Constants.CONTRAIL_VIRTUAL_NETWORK_TEMPLATE_NAME));
    globalImportMap.put(Constants.CONTRAIL_NETWORK_RULE_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.CONTRAIL_NETWORK_RULE_TEMPLATE_NAME));
    globalImportMap.put(Constants.ABSTRACT_SUBSTITUTE_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.ABSTRACT_SUBSTITUTE_TEMPLATE_NAME));
    globalImportMap.put(Constants.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(
            Constants.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_TEMPLATE_NAME));
    globalImportMap.put(Constants.CONTRAIL_COMPUTE_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.CONTRAIL_COMPUTE_TEMPLATE_NAME));
    globalImportMap.put(Constants.CONTRAIL_PORT_TEMPLATE_NAME,
        GlobalTypesUtil.createServiceTemplateImport(Constants.CONTRAIL_PORT_TEMPLATE_NAME));
    globalImportMap.put(Constants.CONTRAIL_ABSTRACT_SUBSTITUTE_TEMPLATE_NAME, GlobalTypesUtil
        .createServiceTemplateImport(Constants.CONTRAIL_ABSTRACT_SUBSTITUTE_TEMPLATE_NAME));
    return globalImportMap;
  }


}
