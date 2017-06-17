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

package org.openecomp.sdc.translator.services.heattotosca;

public class Constants {
  //Service Template - Template Names
  public static final String COMMON_GLOBAL_TEMPLATE_NAME = "CommonGlobalTypes";
  public static final String CINDER_VOLUME_TEMPLATE_NAME = "CinderVolumeGlobalTypes";
  public static final String CONTRAIL_VIRTUAL_NETWORK_TEMPLATE_NAME =
      "ContrailVirtualNetworkGlobalType";
  public static final String CONTRAIL_NETWORK_RULE_TEMPLATE_NAME = "ContrailNetworkRuleGlobalType";
  public static final String CONTRAILV2_VIRTUAL_NETWORK_TEMPLATE_NAME =
      "ContrailV2VirtualNetworkGlobalType";
  public static final String CONTRAILV2_NETWORK_RULE_TEMPLATE_NAME =
      "ContrailV2NetworkRuleGlobalType";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_TEMPLATE_NAME =
      "ContrailV2VirtualMachineInterfaceGlobalType";
  public static final String NEUTRON_NET_TEMPLATE_NAME = "NeutronNetGlobalTypes";
  public static final String NEUTRON_PORT_TEMPLATE_NAME = "NeutronPortGlobalTypes";
  public static final String NEUTRON_SECURITY_RULES_TEMPLATE_NAME =
      "NeutronSecurityRulesGlobalTypes";
  public static final String NOVA_SERVER_TEMPLATE_NAME = "NovaServerGlobalTypes";
  public static final String ABSTRACT_SUBSTITUTE_TEMPLATE_NAME = "AbstractSubstituteGlobalTypes";
  public static final String GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME = "GlobalSubstitutionTypes";
  public static final String CONTRAIL_COMPUTE_TEMPLATE_NAME = "ContrailComputeGlobalTypes";
  public static final String CONTRAIL_PORT_TEMPLATE_NAME = "ContrailPortGlobalTypes";
  public static final String CONTRAIL_ABSTRACT_SUBSTITUTE_TEMPLATE_NAME =
      "ContrailAbstractSubstituteGlobalTypes";
  public static final String MAIN_TEMPLATE_NAME = "Main";
  public static final String PORT_TEMPLATE_NAME = "PortGlobalTypes";
  public static final String COMPUTE_TEMPLATE_NAME = "ComputeGlobalTypes";
  public static final String NETWORK_TEMPLATE_NAME = "NetworkGlobalTypes";
  public static final String SUB_INTERFACE_TEMPLATE_NAME = "SubInterfaceGlobalTypes";
  public static final String CONTRAILV2_VLAN_SUB_INTERFACE_TEMPLATE_NAME =
          "ContrailV2VLANSubInterfaceGlobalType";
  //properties
  public static final String MAX_INSTANCES_PROPERTY_NAME = "max_instances";
  public static final String DESCRIPTION_PROPERTY_NAME = "description";
  public static final String NAME_PROPERTY_NAME = "name";
  public static final String RULES_PROPERTY_NAME = "rules";
  public static final String SECURITY_GROUPS_PROPERTY_NAME = "security_groups";
  public static final String PORT_PROPERTY_NAME = "port";
  //General
  public static final String PROP = "properties";
  public static final String ATTR = "attributes";
  public static final String SERVICE_INSTANCE_PORT_PREFIX = "port_";
  public static final String SERVICE_INSTANCE_LINK_PREFIX = "link_";
  //Unified model
  public static final String COMPUTE_IDENTICAL_VALUE_PROPERTY_PREFIX = "vm_";
  public static final String COMPUTE_IDENTICAL_VALUE_PROPERTY_SUFFIX = "_name";
  public static final String PORT_IDENTICAL_VALUE_PROPERTY_PREFIX = "port_";
  public static final String ABSTRACT_NODE_TEMPLATE_ID_PREFIX = "abstract_";

  private Constants() {
  }
}
