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

package org.openecomp.sdc.tosca.datatypes;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.sdc.tosca.services.ConfigConstants;


public class ToscaNodeType {

  private static Configuration config = ConfigurationManager.lookup();

  public static String VFC_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_VFC);
  public static String CP_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_CP);
  public static String NETWORK_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_NETWORK);
  public static String ABSTRACT_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_ABSTARCT);
  public static String RULE_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_RULE);
  public static String NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX);

  //TOSCA native types
  public static String NATIVE_COMPUTE = "tosca.nodes.Compute";
  public static String NATIVE_ROOT = "tosca.nodes.Root";
  public static String NATIVE_BLOCK_STORAGE = "tosca.nodes.BlockStorage";
  public static String NATIVE_NETWORK = "tosca.nodes.network.Network";
  public static String NATIVE_NETWORK_PORT = "tosca.nodes.network.Port";

  //Additional types
  public static String NOVA_SERVER = VFC_NODE_TYPE_PREFIX + "heat.nova.Server";
  public static String CINDER_VOLUME = VFC_NODE_TYPE_PREFIX + "heat.cinder.Volume";
  public static String COMPUTE = VFC_NODE_TYPE_PREFIX + "Compute";
  public static String CONTRAIL_COMPUTE = VFC_NODE_TYPE_PREFIX + "heat.contrail.Compute";

  public static String NEUTRON_SECURITY_RULES =
      RULE_NODE_TYPE_PREFIX + "heat.network.neutron.SecurityRules";
  public static String CONTRAILV2_NETWORK_RULE =
      RULE_NODE_TYPE_PREFIX + "heat.network.contrailV2.NetworkRules";
  public static String CONTRAIL_NETWORK_RULE =
      RULE_NODE_TYPE_PREFIX + "heat.network.contrail.NetworkRules";

  public static String NEUTRON_NET = NETWORK_NODE_TYPE_PREFIX + "heat.network.neutron.Net";
  public static String CONTRAILV2_VIRTUAL_NETWORK =
      NETWORK_NODE_TYPE_PREFIX + "heat.network.contrailV2.VirtualNetwork";
  public static String CONTRAIL_VIRTUAL_NETWORK =
      NETWORK_NODE_TYPE_PREFIX + "heat.network.contrail.VirtualNetwork";
  public static String NETWORK = NETWORK_NODE_TYPE_PREFIX + "network.Network";

  public static String NEUTRON_PORT = CP_NODE_TYPE_PREFIX + "heat.network.neutron.Port";
  public static String CONTRAILV2_VIRTUAL_MACHINE_INTERFACE =
      CP_NODE_TYPE_PREFIX + "heat.contrailV2.VirtualMachineInterface";
  public static String CONTRAIL_PORT = CP_NODE_TYPE_PREFIX + "heat.network.contrail.Port";
  public static String NETWORK_PORT = CP_NODE_TYPE_PREFIX + "network.Port";
  public static String NETWORK_SUB_INTERFACE = CP_NODE_TYPE_PREFIX + "network.SubInterface";
  public static String CONTRAILV2_VLAN_SUB_INTERFACE = CP_NODE_TYPE_PREFIX
      + "heat.network.contrailV2.VLANSubInterface";

  public static String ABSTRACT_SUBSTITUTE = ABSTRACT_NODE_TYPE_PREFIX + "AbstractSubstitute";
  public static String VFC_ABSTRACT_SUBSTITUTE = ABSTRACT_NODE_TYPE_PREFIX + "VFC";
  public static String CONTRAIL_ABSTRACT_SUBSTITUTE =
      ABSTRACT_NODE_TYPE_PREFIX + "contrail.AbstractSubstitute";
  public static String COMPLEX_VFC_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX + "ComplexVFC";
  //Questionnaire to Tosca Types
  public static String VNF_CONFIG_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX + "VnfConfiguration";
  public static String MULTIFLAVOR_VFC_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX + "MultiFlavorVFC";
  public static String MULTIDEPLOYMENTFLAVOR_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX
      + "MultiDeploymentFlavor";
}

