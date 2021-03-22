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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExtractCompositionDataContext {

    private List<Network> networks = new ArrayList<>();
    private List<Component> components = new ArrayList<>();
    private Map<String, Nic> nics = new HashMap<>();
    private Map<String, Image> images = new HashMap<>();
    private Map<String, ComputeData> computes = new HashMap<>();
    private Set<String> handledServiceTemplates = new HashSet<>();
    private Set<String> createdComponents = new HashSet<>();

    public void addHandledServiceTemplates(String handledServiceTemplate) {
        this.handledServiceTemplates.add(handledServiceTemplate);
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

    public void addNic(String nicId, Nic nic) {
        this.nics.put(nicId, nic);
    }

    public void addImage(String imageId, Image image) {
        this.images.put(imageId, image);
    }

    public void addCompute(String computeId, ComputeData computedata) {
        this.computes.put(computeId, computedata);
    }
}
