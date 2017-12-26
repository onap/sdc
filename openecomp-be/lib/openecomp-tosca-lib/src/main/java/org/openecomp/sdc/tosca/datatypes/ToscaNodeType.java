/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.tosca.datatypes;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.sdc.tosca.services.ConfigConstants;


public class ToscaNodeType {

  private static Configuration config = ConfigurationManager.lookup();

  public static final String VFC_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_VFC);
  public static final String CP_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_CP);
  public static final String EXTERNAL_CP_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_EXTERNAL_CP);
  public static final String NETWORK_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_NETWORK);
  public static final String ABSTRACT_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_ABSTARCT);
  public static final String RULE_NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_NODE_TYPE_RULE);
  public static final String NODE_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX);

  //TOSCA native types
  public static final String NATIVE_COMPUTE = "tosca.nodes.Compute";
  public static final String NATIVE_ROOT = "tosca.nodes.Root";
  public static final String NATIVE_BLOCK_STORAGE = "tosca.nodes.BlockStorage";
  public static final String NATIVE_NETWORK = "tosca.nodes.network.Network";
  public static final String NATIVE_NETWORK_PORT = "tosca.nodes.network.Port";

  //Additional types
  public static final String NOVA_SERVER = VFC_NODE_TYPE_PREFIX + "heat.nova.Server";
  public static final String CINDER_VOLUME = VFC_NODE_TYPE_PREFIX + "heat.cinder.Volume";
  public static final String COMPUTE = VFC_NODE_TYPE_PREFIX + "Compute";
  public static final String CONTRAIL_COMPUTE = VFC_NODE_TYPE_PREFIX + "heat.contrail.Compute";

  public static final String NEUTRON_SECURITY_RULES =
      RULE_NODE_TYPE_PREFIX + "heat.network.neutron.SecurityRules";
  public static final String CONTRAILV2_NETWORK_RULE =
      RULE_NODE_TYPE_PREFIX + "heat.network.contrailV2.NetworkRules";
  public static final String CONTRAIL_NETWORK_RULE =
      RULE_NODE_TYPE_PREFIX + "heat.network.contrail.NetworkRules";

  public static final String NEUTRON_NET = NETWORK_NODE_TYPE_PREFIX + "heat.network.neutron.Net";
  public static final String CONTRAILV2_VIRTUAL_NETWORK =
      NETWORK_NODE_TYPE_PREFIX + "heat.network.contrailV2.VirtualNetwork";
  public static final String CONTRAIL_VIRTUAL_NETWORK =
      NETWORK_NODE_TYPE_PREFIX + "heat.network.contrail.VirtualNetwork";
  public static final String NETWORK = NETWORK_NODE_TYPE_PREFIX + "network.Network";

  public static final String NEUTRON_PORT = CP_NODE_TYPE_PREFIX + "heat.network.neutron.Port";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_INTERFACE =
      CP_NODE_TYPE_PREFIX + "heat.contrailV2.VirtualMachineInterface";
  public static final String CONTRAIL_PORT = CP_NODE_TYPE_PREFIX + "heat.network.contrail.Port";
  public static final String NETWORK_PORT = CP_NODE_TYPE_PREFIX + "network.Port";
  public static final String NETWORK_SUB_INTERFACE = CP_NODE_TYPE_PREFIX + "network.SubInterface";
  public static final String CONTRAILV2_VLAN_SUB_INTERFACE = CP_NODE_TYPE_PREFIX
      + "heat.network.contrailV2.VLANSubInterface";
  //Port Mirroring external node types
  public static final String EXTERNAL_CP = EXTERNAL_CP_NODE_TYPE_PREFIX + "extCP";
  public static final String EXTERNAL_CONTRAIL_PORT = EXTERNAL_CP_NODE_TYPE_PREFIX
      + "extContrailCP";
  public static final String EXTERNAL_NEUTRON_PORT = EXTERNAL_CP_NODE_TYPE_PREFIX + "extNeutronCP";

  public static final String ABSTRACT_SUBSTITUTE = ABSTRACT_NODE_TYPE_PREFIX + "AbstractSubstitute";
  public static final String VFC_ABSTRACT_SUBSTITUTE = ABSTRACT_NODE_TYPE_PREFIX + "VFC";
  public static final String CONTRAIL_ABSTRACT_SUBSTITUTE =
      ABSTRACT_NODE_TYPE_PREFIX + "contrail.AbstractSubstitute";
  public static final String COMPLEX_VFC_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX + "ComplexVFC";
  //Questionnaire to Tosca Types
  public static final String VNF_CONFIG_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX + "VnfConfiguration";
  public static final String MULTIFLAVOR_VFC_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX + "MultiFlavorVFC";
  public static final String MULTIDEPLOYMENTFLAVOR_NODE_TYPE = ABSTRACT_NODE_TYPE_PREFIX
      + "MultiDeploymentFlavor.CVFC";
  public static final String COMPUTE_TYPE_PREFIX = "org.openecomp.resource.vfc.compute.nodes.heat";
  public static final String VFC_TYPE_PREFIX = "org.openecomp.resource.vfc.nodes.heat";

  private ToscaNodeType() {}
}

