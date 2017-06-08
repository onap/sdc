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

package org.openecomp.sdc.vendorsoftwareproduct.types.composition;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtractCompositionDataContext {
  private List<Network> networks = new ArrayList<>();
  private List<Component> components = new ArrayList<>();
  private Map<String, Nic> nics = new HashMap<>();
  private Set<String> handledServiceTemplates = new HashSet<>();
  private Set<String> createdComponents = new HashSet<>();

  public Set<String> getCreatedComponents() {
    return createdComponents;
  }

  public void setCreatedComponents(Set<String> createdComponents) {
    this.createdComponents = createdComponents;
  }

  public Set<String> getHandledServiceTemplates() {
    return handledServiceTemplates;
  }

  public void setHandledServiceTemplates(Set<String> handledServiceTemplates) {
    this.handledServiceTemplates = handledServiceTemplates;
  }

  public void addHandledServiceTemplates(String handledServiceTemplate) {
    this.handledServiceTemplates.add(handledServiceTemplate);
  }

  public List<Network> getNetworks() {
    return networks;
  }

  public void setNetworks(List<Network> networks) {
    this.networks = networks;
  }

  /**
   * Add network.
   *
   * @param network the network
   */
  public void addNetwork(Network network) {
    if (network != null) {
      networks.add(network);
    }
  }

  /**
   * Add networks.
   *
   * @param network the network
   */
  public void addNetworks(List<Network> network) {
    if (networks != null) {
      networks.addAll(network);
    }
  }

  public List<Component> getComponents() {
    return components;
  }

  public void setComponents(List<Component> components) {
    this.components = components;
  }

  /**
   * Add component.
   *
   * @param component the component
   */
  public void addComponent(Component component) {
    if (component != null) {
      components.add(component);
    }
  }

  /**
   * Add components.
   *
   * @param components the components
   */
  public void addComponents(List<Component> components) {
    if (components != null) {
      this.components.addAll(components);
    }
  }

  public Map<String, Nic> getNics() {
    return nics;
  }

  public void setNics(Map<String, Nic> nics) {
    this.nics = nics;
  }

  public void addNic(String nicId, Nic nic) {
    this.nics.put(nicId, nic);
  }


}
