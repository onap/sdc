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


public enum ToscaDataType {

  ROOT("tosca.datatypes.Root"),
  NETWORK_NETWORK_INFO("tosca.datatypes.network.NetworkInfo"),
  NETWORK_PORT_INFO("tosca.datatypes.network.PortInfo"),
  NOVA_SERVER_PORT_EXTRA_PROPERTIES(
      "org.openecomp.datatypes.heat.novaServer.network.PortExtraProperties"),
  NETWORK_ADDRESS_PAIR("org.openecomp.datatypes.heat.network.AddressPair"),
  NEUTRON_PORT_FIXED_IPS("org.openecomp.datatypes.heat.neutron.port.FixedIps"),
  CONTRAIL_NETWORK_RULE("org.openecomp.datatypes.heat.contrail.network.rule.Rule"),
  CONTRAIL_NETWORK_RULE_LIST("org.openecomp.datatypes.heat.contrail.network.rule.RuleList"),
  CONTRAIL_NETWORK_RULE_PORT_PAIRS("org.openecomp.datatypes.heat.contrail.network.rule.PortPairs"),
  CONTRAIL_NETWORK_RULE_VIRTUAL_NETWORK(
      "org.openecomp.datatypes.heat.contrail.network.rule.VirtualNetwork"),
  CONTRAILV2_NETWORK_RULE("org.openecomp.datatypes.heat.contrailV2.network.rule.Rule"),
  CONTRAILV2_NETWORK_RULE_LIST("org.openecomp.datatypes.heat.contrailV2.network.rule.RuleList"),
  CONTRAILV2_NETWORK_RULE_SRC_PORT_PAIRS(
      "org.openecomp.datatypes.heat.contrailV2.network.rule.SrcPortPairs"),
  CONTRAILV2_NETWORK_RULE_DST_PORT_PAIRS(
      "org.openecomp.datatypes.heat.contrailV2.network.rule.DstPortPairs"),
  CONTRAILV2_NETWORK_RULE_DST_VIRTUAL_NETWORK(
      "org.openecomp.datatypes.heat.contrailV2.network.rule.DstVirtualNetwork"),
  CONTRAILV2_NETWORK_RULE_SRC_VIRTUAL_NETWORK(
      "org.openecomp.datatypes.heat.contrailV2.network.rule.SrcVirtualNetwork"),
  CONTRAILV2_VIRTUAL_MACHINE_INTERFACE_PROPERTIES(
      "org.openecomp.datatypes.heat.contrailV2.virtual.machine.interface.Properties"),
  CONTRAILV2_NETWORK_RULE_ACTION_LIST(
      "org.openecomp.datatypes.heat.contrailV2.network.rule.ActionList"),
  CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA(
      "org.openecomp.datatypes.heat.contrailV2.virtual.network.rule.IpamRefData"),
  CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET_LIST(
      "org.openecomp.datatypes.heat.contrailV2.virtual.network.rule.ref.data.IpamSubnetList"),
  CONTRAILV2_VIRTUAL_NETWORK_IPAM_REF_DATA_IPAM_SUBNET(
      "org.openecomp.datatypes.heat.contrailV2.virtual.network.rule.ref.data.IpamSubnet"),
  CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA(
      "org.openecomp.datatypes.heat.contrailV2.virtual.network.rule.RefData"),
  CONTRAILV2_VIRTUAL_NETWORK_POLICY_REF_DATA_SEQUENCE(
      "org.openecomp.datatypes.heat.contrailV2.virtual.network.rule.RefDataSequence"),
  NOVA_SERVER_NETWORK_ADDRESS_INFO("org.openecomp.datatypes.heat.novaServer.network.AddressInfo"),
  NEUTRON_SUBNET("org.openecomp.datatypes.heat.network.neutron.Subnet"),
  NETWORK_ALLOCATION_POOL("org.openecomp.datatypes.heat.network.AllocationPool"),
  NETWORK_HOST_ROUTE("org.openecomp.datatypes.heat.network.subnet.HostRoute"),
  SUBSTITUTION_FILTER("org.openecomp.datatypes.heat.substitution.SubstitutionFilter"),
  SUBSTITUTION_FILTERING("org.openecomp.datatypes.heat.substitution.SubstitutionFiltering"),
  NEUTRON_SECURITY_RULES_RULE("org.openecomp.datatypes.heat.network.neutron.SecurityRules.Rule"),
  CONTRAIL_STATIC_ROUTE("org.openecomp.datatypes.heat.network.contrail.port.StaticRoute"),
  CONTRAIL_ADDRESS_PAIR("org.openecomp.datatypes.heat.network.contrail.AddressPair"),
  CONTRAIL_INTERFACE_DATA("org.openecomp.datatypes.heat.network.contrail.InterfaceData");

  private String displayName;

  ToscaDataType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }


}
