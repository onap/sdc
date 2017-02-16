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

import org.openecomp.sdc.tosca.services.ToscaConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * The enum Tosca node type.
 */
public enum ToscaNodeType {

  /**
   * Compute tosca node type.
   */
  COMPUTE("tosca.nodes.Compute"),
  /**
   * Root tosca node type.
   */
  ROOT("tosca.nodes.Root"),
  /**
   * Block storage tosca node type.
   */
  BLOCK_STORAGE("tosca.nodes.BlockStorage"),
  /**
   * Network tosca node type.
   */
  NETWORK("tosca.nodes.network.Network"),
  /**
   * Network port tosca node type.
   */
  NETWORK_PORT("tosca.nodes.network.Port"),
  /**
   * Nova server tosca node type.
   */
  NOVA_SERVER(ToscaConstants.NODES_PREFIX + "nova.Server"),
  /**
   * Cinder volume tosca node type.
   */
  CINDER_VOLUME(ToscaConstants.NODES_PREFIX + "cinder.Volume"),
  /**
   * Neutron net tosca node type.
   */
  NEUTRON_NET("org.openecomp.resource.vl.nodes.heat.network.neutron.Net"),
  /**
   * Neutron port tosca node type.
   */
  NEUTRON_PORT("org.openecomp.resource.cp.nodes.heat.network.neutron.Port"),
  /**
   * Neutron security rules tosca node type.
   */
  NEUTRON_SECURITY_RULES("org.openecomp.resource.vfc.rules.nodes"
      + ".heat.network.neutron.SecurityRules"),
  /**
   * Contrail virtual network tosca node type.
   */
  CONTRAIL_VIRTUAL_NETWORK("org.openecomp.resource.vl.nodes.heat.network.contrail.VirtualNetwork"),
  /**
   * Contrail network rule tosca node type.
   */
  CONTRAIL_NETWORK_RULE("org.openecomp.resource.vfc."
      + "rules.nodes.heat.network.contrail.NetworkRules"),
  /**
   * Contrailv 2 virtual network tosca node type.
   */
  CONTRAILV2_VIRTUAL_NETWORK("org.openecomp.resource.vl.nodes."
      + "heat.network.contrailV2.VirtualNetwork"),
  /**
   * Contrailv 2 network rule tosca node type.
   */
  CONTRAILV2_NETWORK_RULE(
      "org.openecomp.resource.vfc.rules.nodes.heat.network.contrailV2.NetworkRules"),
  /**
   * Contrailv 2 virtual machine interface tosca node type.
   */
  CONTRAILV2_VIRTUAL_MACHINE_INTERFACE(
      "org.openecomp.resource.cp.nodes.heat.contrailV2.VirtualMachineInterface"),
  /**
   * Abstract substitute tosca node type.
   */
  ABSTRACT_SUBSTITUTE("org.openecomp.resource.abstract.nodes.AbstractSubstitute"),
  /**
   * Contrail compute tosca node type.
   */
  CONTRAIL_COMPUTE(ToscaConstants.NODES_PREFIX + "contrail.Compute"),
  /**
   * Contrail port tosca node type.
   */
  CONTRAIL_PORT("org.openecomp.resource.cp.nodes.heat.network.contrail.Port"),
  /**
   * Contrail abstract substitute tosca node type.
   */
  CONTRAIL_ABSTRACT_SUBSTITUTE("org.openecomp.resource.abstract."
      + "nodes.contrail.AbstractSubstitute"),;

  private static final Map<String, ToscaNodeType> mMap =
      Collections.unmodifiableMap(initializeMapping());
  private String displayName;

  ToscaNodeType(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Initialize mapping map.
   *
   * @return the map
   */
  public static Map<String, ToscaNodeType> initializeMapping() {
    Map<String, ToscaNodeType> toscaMap = new HashMap<>();
    for (ToscaNodeType v : ToscaNodeType.values()) {
      toscaMap.put(v.displayName, v);
    }
    return toscaMap;
  }

  /**
   * Gets tosca node type by display name.
   *
   * @param displayName the display name
   * @return the tosca node type by display name
   */
  public static ToscaNodeType getToscaNodeTypeByDisplayName(String displayName) {
    if (mMap.containsKey(displayName)) {
      return mMap.get(displayName);
    }
    return null;
  }

  /**
   * Gets display name.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }


}
