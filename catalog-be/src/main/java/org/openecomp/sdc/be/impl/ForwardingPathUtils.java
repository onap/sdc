/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.impl;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.javatuples.Pair;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.datamodel.NameIdPair;
import org.openecomp.sdc.be.datamodel.NameIdPairWrapper;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ForwardingPathUtils {

    public static final String FORWARDING_PATH_NODE_NAME = "Forwarding Path";
    public static final String FORWARDER_CAPABILITY = "org.openecomp.capabilities.Forwarder";


    public ServiceRelations convertServiceToServiceRelations(Service service) {
        ServiceRelations serviceRelations = new ServiceRelations();
        List<ComponentInstance> componentInstances = service.getComponentInstances();
        if (componentInstances == null || componentInstances.isEmpty()) {
            return serviceRelations;
        }
        Set<NameIdPairWrapper> relations = new HashSet<>();
        //@todo get all capabilities and requirements.
        SetMultimap<NameIdPair, NameIdPair> nodeToCP = HashMultimap.create();
        componentInstances.forEach(ci -> initNodeToCP(ci, nodeToCP));
        handleRelDef(relations, nodeToCP);
        serviceRelations.setRelations(relations);
        return serviceRelations;
    }

    private void initNodeToCP(ComponentInstance ci, SetMultimap<NameIdPair, NameIdPair> nodeToCP) {
        if (ci.getCapabilities() == null){
            return;
        }
        Set<CapabilityDefinition> capabilities = ci.getCapabilities().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        if (!CollectionUtils.isNotEmpty(capabilities)) {
            return;
        }
        Set<CapabilityDefinition> forwarderCapabilities = capabilities.stream().filter(capabilityDefinition -> capabilityDefinition.getType().equals(FORWARDER_CAPABILITY)).collect(Collectors.toSet());
        if (!CollectionUtils.isNotEmpty(forwarderCapabilities)) {
            return;
        }
        NameIdPair node = new NameIdPair(ci.getName(), ci.getName());
        forwarderCapabilities.forEach(fc -> {
            NameIdPair capability = new NameIdPair(fc.getName(), fc.getName(), fc.getOwnerId());
            nodeToCP.put(node, capability);
         });

    }


    private void handleRelDef(Set<NameIdPairWrapper> relations, SetMultimap<NameIdPair, NameIdPair> nodeToCP) {
        nodeToCP.keySet().forEach(fromNode -> {
            NameIdPairWrapper nameIdPairWrapper = new NameIdPairWrapper();
            nameIdPairWrapper.init(fromNode);
            if (!relations.contains(nameIdPairWrapper)) {
                relations.add(nameIdPairWrapper);
                Collection<NameIdPair> fromCps = nodeToCP.get(fromNode);
                fromCps.forEach(fromCP -> handleFromCp(nodeToCP, nameIdPairWrapper));
            }
        });

    }

    private void handleFromCp(SetMultimap<NameIdPair, NameIdPair> nodeToCP, NameIdPairWrapper wrapper) {
        Map<NameIdPair, Set<NameIdPair>> options = toMap(nodeToCP);

        Set<NameIdPair> cpOptions = options.get(wrapper.getNameIdPair());
        List<NameIdPairWrapper> wrappers = cpOptions.stream().map(this::createWrapper).collect(Collectors.toList());
        wrappers.forEach(cpOptionWrapper -> {
            org.openecomp.sdc.be.datamodel.NameIdPair data = wrapper.getData();
            data.addWrappedData(cpOptionWrapper);
        });
    }

    private NameIdPairWrapper createWrapper(NameIdPair cpOption) {
        NameIdPairWrapper nameIdPairWrapper = new NameIdPairWrapper();
        nameIdPairWrapper.init(new NameIdPair(cpOption));
        return nameIdPairWrapper;
    }


    private Map<NameIdPair, Set<NameIdPair>> toMap(SetMultimap<NameIdPair, NameIdPair> nodeToCP) {
        Map<NameIdPair, Set<NameIdPair>> retVal = new HashMap<>();
        nodeToCP.asMap().forEach((nameIdPair, nameIdPairs) -> retVal.put(nameIdPair, new HashSet<>(nameIdPairs)));
        return retVal;
    }


    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }

    public Set<String> findForwardingPathNamesToDeleteOnComponentInstanceDeletion(Service containerService,
        String componentInstanceId) {
        return findForwardingPathToDeleteOnCIDeletion(containerService, componentInstanceId).values().stream()
            .map(ForwardingPathDataDefinition::getName).collect(Collectors.toSet());
    }

    private Map<String, ForwardingPathDataDefinition> findForwardingPathToDeleteOnCIDeletion(Service containerService,
        String componentInstanceId) {
        return containerService.getForwardingPaths().entrySet().stream()
            .filter(entry -> elementContainsCI(entry, componentInstanceId))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean elementContainsCI(Map.Entry<String, ForwardingPathDataDefinition> fpEntry,
        String componentInstanceId) {
        return fpEntry.getValue().getPathElements()
            .getListToscaDataDefinition().stream()
            .anyMatch(element -> elementContainsCI(element, componentInstanceId));
    }

    private boolean elementContainsCI(ForwardingPathElementDataDefinition elementDataDefinitions,
        String componentInstanceId) {
        return elementDataDefinitions.getFromNode().equals(componentInstanceId)
            || elementDataDefinitions.getToNode().equals(componentInstanceId);
    }

    public Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> updateForwardingPathOnVersionChange(
        Service containerService, DataForMergeHolder dataHolder,
        Component updatedContainerComponent, String newInstanceId) {
        Map<String, ForwardingPathDataDefinition> updated = containerService.getForwardingPaths().entrySet().stream()
            .filter(entry -> elementContainsCIAndForwarder(entry.getValue(), dataHolder.getOrigComponentInstId(), updatedContainerComponent))
            .collect(Collectors.toMap(Map.Entry::getKey,
                entry ->  updateCI(entry.getValue(), dataHolder.getOrigComponentInstId(),newInstanceId)));
        Map<String, ForwardingPathDataDefinition> deleted = containerService.getForwardingPaths().entrySet().stream()
            .filter(entry -> elementContainsCIAndDoesNotContainForwarder(entry.getValue(),  dataHolder.getOrigComponentInstId(), updatedContainerComponent))
            .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        return new Pair<>(updated, deleted);
    }

    public Set<String> getForwardingPathsToBeDeletedOnVersionChange(
        Service containerService, DataForMergeHolder dataHolder, Component updatedContainerComponent) {
        return containerService.getForwardingPaths().entrySet().stream()
          .filter(entry -> elementContainsCIAndDoesNotContainForwarder(entry.getValue(),
              dataHolder.getOrigComponentInstId(), updatedContainerComponent))
           .map(entry -> entry.getValue().getUniqueId()).collect( Collectors.toSet());
    }

    private ForwardingPathDataDefinition updateCI(ForwardingPathDataDefinition inFP, String oldCI, String newCI) {
        ForwardingPathDataDefinition retVal = new ForwardingPathDataDefinition(inFP);
        List<ForwardingPathElementDataDefinition> fpList = retVal.getPathElements().getListToscaDataDefinition()
            .stream().map(element -> updateElement(element, oldCI, newCI)).collect(Collectors.toList());
        retVal.setPathElements(new ListDataDefinition<>(fpList));
        return retVal;
    }

    private ForwardingPathElementDataDefinition updateElement(ForwardingPathElementDataDefinition element, String oldCI,
        String newCI) {
        ForwardingPathElementDataDefinition retVal = new ForwardingPathElementDataDefinition(element);
        if (retVal.getFromNode().equals(oldCI)) {
            retVal.setFromNode(newCI);
        }
        if (retVal.getToNode().equals(oldCI)) {
            retVal.setToNode(newCI);
        }
        if (Objects.equals(retVal.getToCPOriginId(),oldCI )) {
            retVal.setToCPOriginId(newCI);
        }
        if (Objects.equals(retVal.getFromCPOriginId(),oldCI)) {
            retVal.setFromCPOriginId(newCI);
        }
        return retVal;
    }

    private boolean elementContainsCIAndForwarder(ForwardingPathDataDefinition forwardingPathDataDefinition,
        String oldCIId, Component newCI) {
        return forwardingPathDataDefinition.getPathElements()
            .getListToscaDataDefinition().stream()
            .anyMatch(element -> elementContainsCIAndForwarder(element, oldCIId, newCI));
    }

    private boolean elementContainsCIAndForwarder(ForwardingPathElementDataDefinition elementDataDefinitions,
        String oldCIId, Component newCI) {
        return (elementDataDefinitions.getFromNode().equals(oldCIId) && ciContainsForwarder(newCI,
            elementDataDefinitions.getFromCP()))
            || (elementDataDefinitions.getToNode().equals(oldCIId) && ciContainsForwarder(newCI,
            elementDataDefinitions.getToCP()));
    }

    private boolean ciContainsForwarder(Component newCI, String capabilityID) {
        if (newCI.getCapabilities() == null){
            return false;
        }
        return newCI.getCapabilities().values()
            .stream()
            .flatMap(List::stream)
            .anyMatch(c -> c.getName().equals(capabilityID));
    }

    private boolean elementContainsCIAndDoesNotContainForwarder(
        ForwardingPathDataDefinition forwardingPathDataDefinition,
        String oldCIId, Component newCI) {
        return forwardingPathDataDefinition.getPathElements()
            .getListToscaDataDefinition().stream()
            .anyMatch(element -> elementContainsCIAndDoesNotContainForwarder(element, oldCIId, newCI));
    }

    private boolean elementContainsCIAndDoesNotContainForwarder(
        ForwardingPathElementDataDefinition elementDataDefinitions,
        String oldCIId, Component newCI) {
        return (elementDataDefinitions.getFromNode().equals(oldCIId) && !ciContainsForwarder(newCI,
            elementDataDefinitions.getFromCP()))
            || (elementDataDefinitions.getToNode().equals(oldCIId) && !ciContainsForwarder(newCI,
            elementDataDefinitions.getToCP()));
    }


    public Set<ForwardingPathDataDefinition> updateComponentInstanceName(Collection<ForwardingPathDataDefinition> forwardingPathDataDefinitions,
        String oldName, String newName){
        return  forwardingPathDataDefinitions.stream().filter(fp -> shouldRenameCI(fp,oldName)).
            map(forwardingPathDataDefinition -> renamePathCI(forwardingPathDataDefinition,oldName,newName))
            .collect(Collectors.toSet());

    }

    public boolean shouldRenameCI(ForwardingPathDataDefinition forwardingPathDataDefinitions,
        String oldName){
        return forwardingPathDataDefinitions.getPathElements().getListToscaDataDefinition().stream()
            .anyMatch(pe -> pe.getToNode().equals(oldName) || pe.getFromNode().equals(oldName));
    }

    public ForwardingPathDataDefinition renamePathCI(ForwardingPathDataDefinition forwardingPathDataDefinitions,
        String oldName, String newName){
        forwardingPathDataDefinitions.getPathElements().getListToscaDataDefinition().stream()
            .forEach(pe -> renamePathCI(pe,oldName, newName));
        return forwardingPathDataDefinitions;
    }

    public void renamePathCI(ForwardingPathElementDataDefinition pathElementDataDefinition,
        String oldName, String newName){
        if (pathElementDataDefinition.getFromNode().equals(oldName)){
            pathElementDataDefinition.setFromNode(newName);
        }
        if(pathElementDataDefinition.getToNode().equals(oldName)){
            pathElementDataDefinition.setToNode(newName);
        }

    }
}
