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

public class ToscaDataType {

  private static Configuration config = ConfigurationManager.lookup();

  public static final String DATA_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_DATA_TYPE);

  //TOSCA native types
  public static final String NATIVE_ROOT = "tosca.datatypes.Root";
  public static final String NATIVE_NETWORK_NETWORK_INFO = "tosca.datatypes.network.NetworkInfo";
  public static final String NATIVE_NETWORK_PORT_INFO = "tosca.datatypes.network.PortInfo";

  //Additional types
  public static final String NOVA_SERVER_PORT_EXTRA_PROPERTIES =
      DATA_TYPE_PREFIX + "heat.novaServer.network.PortExtraProperties";
  public static final String NETWORK_ADDRESS_PAIR = DATA_TYPE_PREFIX + "heat.network.AddressPair";
  public static final String NEUTRON_PORT_FIXED_IPS = DATA_TYPE_PREFIX + "heat.neutron.port.FixedIps";
  public static final String CONTRAIL_NETWORK_RULE = DATA_TYPE_PREFIX + "heat.contrail.network.rule.Rule";
  public static final String CONTRAIL_NETWORK_RULE_LIST =
      DATA_TYPE_PREFIX + "heat.contrail.network.rule.RuleList";
  public static final String CONTRAIL_NETWORK_RULE_PORT_PAIRS =
      DATA_TYPE_PREFIX + "heat.contrail.network.rule.PortPairs";
  public static final String CONTRAIL_NETWORK_RULE_VIRTUAL_NETWORK =
      DATA_TYPE_PREFIX + "heat.contrail.network.rule.VirtualNetwork";
  public static final String CONTRAILV2_NETWORK_RULE =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.Rule";
  public static final String CONTRAILV2_NETWORK_RULE_LIST =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.RuleList";
  public static final String CONTRAILV2_NETWORK_RULE_SRC_PORT_PAIRS =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.SrcPortPairs";
  public static final String CONTRAILV2_NETWORK_RULE_DST_PORT_PAIRS =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.DstPortPairs";
  public static final String CONTRAILV2_NETWORK_RULE_DST_VIRTUAL_NETWORK =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.DstVirtualNetwork";
  public static final String CONTRAILV2_NETWORK_RULE_SRC_VIRTUAL_NETWORK =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.SrcVirtualNetwork";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_PROPERTIES =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.machine.interface.Properties";
  public static final String CONTRAILV2_NETWORK_RULE_ACTION_LIST =
      DATA_TYPE_PREFIX + "heat.contrailV2.network.rule.ActionList";
  public static final String CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.network.rule.IpamRefData";
  public static final String CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET_LIST =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.network.rule.ref.data.IpamSubnetList";
  public static final String CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.network.rule.ref.data.IpamSubnet";
  public static final String CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.network.rule.RefData";
  public static final String CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA_SEQUENCE =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.network.rule.RefDataSequence";
  public static final String NOVA_SERVER_NETWORK_ADDRESS_INFO =
      DATA_TYPE_PREFIX + "heat.novaServer.network.AddressInfo";
  public static final String NEUTRON_SUBNET = DATA_TYPE_PREFIX + "heat.network.neutron.Subnet";
  public static final String NETWORK_ALLOCATION_POOL = DATA_TYPE_PREFIX + "heat.network.AllocationPool";
  public static final String NETWORK_HOST_ROUTE = DATA_TYPE_PREFIX + "heat.network.subnet.HostRoute";
  public static final String SUBSTITUTION_FILTERING =
      DATA_TYPE_PREFIX + "heat.substitution.SubstitutionFiltering";
  public static final String NEUTRON_SECURITY_RULES_RULE =
      DATA_TYPE_PREFIX + "heat.network.neutron.SecurityRules.Rule";
  public static final String CONTRAIL_STATIC_ROUTE =
      DATA_TYPE_PREFIX + "heat.network.contrail.port.StaticRoute";
  public static final String CONTRAIL_ADDRESS_PAIR =
      DATA_TYPE_PREFIX + "heat.network.contrail.AddressPair";
  public static final String CONTRAIL_INTERFACE_DATA =
      DATA_TYPE_PREFIX + "heat.network.contrail.InterfaceData";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_SUB_INTERFACE_PROPERTIES =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.machine.subInterface.Properties";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_SUB_INTERFACE_MAC_ADDRESS =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.machine.subInterface.MacAddress";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_SUB_INTERFACE_ADDRESS_PAIRS =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.machine.subInterface.AddressPairs";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_SUB_INTERFACE_ADDRESS_PAIR =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.machine.subInterface.AddressPair";
  public static final String CONTRAILV2_VIRTUAL_MACHINE_SUB_INTERFACE_ADDRESS_PAIR_IP =
      DATA_TYPE_PREFIX + "heat.contrailV2.virtual.machine.subInterface.AddressPairIp";
}
