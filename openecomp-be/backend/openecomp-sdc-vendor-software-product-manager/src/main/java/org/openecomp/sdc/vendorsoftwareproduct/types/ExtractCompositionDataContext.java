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

package org.openecomp.sdc.vendorsoftwareproduct.types;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type Extract composition data context.
 */
public class ExtractCompositionDataContext {
  private List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network> networks =
      new ArrayList<>();
  private List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component> components =
      new ArrayList<>();
  private Map<String, Nic> nics = new HashMap<>();
  private Set<String> handledServiceTemplates = new HashSet<>();
  private Set<String> createdComponents = new HashSet<>();

  /**
   * Gets created components.
   *
   * @return the created components
   */
  public Set<String> getCreatedComponents() {
    return createdComponents;
  }

  /**
   * Sets created components.
   *
   * @param createdComponents the created components
   */
  public void setCreatedComponents(Set<String> createdComponents) {
    this.createdComponents = createdComponents;
  }

  /**
   * Gets handled service templates.
   *
   * @return the handled service templates
   */
  public Set<String> getHandledServiceTemplates() {
    return handledServiceTemplates;
  }

  /**
   * Sets handled service templates.
   *
   * @param handledServiceTemplates the handled service templates
   */
  public void setHandledServiceTemplates(Set<String> handledServiceTemplates) {
    this.handledServiceTemplates = handledServiceTemplates;
  }

  /**
   * Add handled service templates.
   *
   * @param handledServiceTemplate the handled service template
   */
  public void addHandledServiceTemplates(String handledServiceTemplate) {
    this.handledServiceTemplates.add(handledServiceTemplate);
  }

  /**
   * Gets networks.
   *
   * @return the networks
   */
  public List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network> getNetworks() {
    return networks;
  }

  /**
   * Sets networks.
   *
   * @param networks the networks
   */
  public void setNetworks(
      List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network> networks) {
    this.networks = networks;
  }

  /**
   * Add network.
   *
   * @param network the network
   */
  public void addNetwork(
      org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network network) {
    if (network != null) {
      networks.add(network);
    }
  }

  /**
   * Add networks.
   *
   * @param network the network
   */
  public void addNetworks(
      List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network> network) {
    if (networks != null) {
      networks.addAll(network);
    }
  }

  /**
   * Gets components.
   *
   * @return the components
   */
  public List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component> getComponents() {
    return components;
  }

  /**
   * Sets components.
   *
   * @param components the components
   */
  public void setComponents(
      List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component> components) {
    this.components = components;
  }

  /**
   * Add component.
   *
   * @param component the component
   */
  public void addComponent(
      org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component component) {
    if (component != null) {
      components.add(component);
    }
  }

  /**
   * Add components.
   *
   * @param components the components
   */
  public void addComponents(
      List<org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component> components) {
    if (components != null) {
      this.components.addAll(components);
    }
  }

  /**
   * Gets nics.
   *
   * @return the nics
   */
  public Map<String, Nic> getNics() {
    return nics;
  }

  /**
   * Sets nics.
   *
   * @param nics the nics
   */
  public void setNics(Map<String, Nic> nics) {
    this.nics = nics;
  }

  /**
   * Add nic.
   *
   * @param nicId the nic id
   * @param nic   the nic
   */
  public void addNic(String nicId, Nic nic) {
    this.nics.put(nicId, nic);
  }


}
