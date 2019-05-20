package org.openecomp.sdc.be.tosca.utils;

import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.tosca.CapabilityRequirementConverter;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;

import java.util.*;
import java.util.Collection;

/**
 * @author KATYR
 * @since November 19, 2017
 */

public class ForwardingPathToscaUtil {
    public static final String FORWARDS_TO_TOSCA_NAME =
            "org.openecomp.relationships.ForwardsTo";
    public static final String PROTOCOL = "protocol";
    public static final String PORTS_RANGE = "target_range";
    public static final String FORWARDER = "forwarder";

    public static void addForwardingPaths(Service service, Map<String, ToscaNodeTemplate>
            nodeTemplates, CapabilityRequirementConverter capabiltyRequirementConvertor, Map<String, Component> originComponents, ToscaOperationFacade toscaOperationFacade) {
        for (String forwardingPathName : service.getForwardingPaths().keySet()) {
            ToscaNodeTemplate forwardingPathNodeTemplate =
                    new ToscaNodeTemplate();
            final ForwardingPathDataDefinition path =
                    service.getForwardingPaths().get(forwardingPathName);
            forwardingPathNodeTemplate.setType(path.getToscaResourceName());

            if (Objects.nonNull(path.getDescription())) {
                forwardingPathNodeTemplate.setDescription(path
                        .getDescription());
            }
            Map<String, Object> props = new HashMap<>();
            if (Objects.nonNull(path.getDestinationPortNumber())) {
                props.put(PORTS_RANGE, Collections.singletonList(path.getDestinationPortNumber()));
            }
            if (Objects.nonNull(path.getProtocol())) {
                props.put(PROTOCOL, path.getProtocol());
            }
            if (MapUtils.isNotEmpty(props)) {
                forwardingPathNodeTemplate.setProperties(props);
            }

            final List<ForwardingPathElementDataDefinition> pathElements =
                    path.getPathElements()
                            .getListToscaDataDefinition();
            forwardingPathNodeTemplate.setRequirements(convertPathElementsToRequirements(pathElements,
                    service, capabiltyRequirementConvertor, originComponents, toscaOperationFacade));

            nodeTemplates.put(path.getName(), forwardingPathNodeTemplate);
        }

    }

    private static List<Map<String, ToscaTemplateRequirement>> convertPathElementsToRequirements(
            List<ForwardingPathElementDataDefinition> pathElements, Service service, CapabilityRequirementConverter capabiltyRequirementConvertor, Map<String, Component> originComponents, ToscaOperationFacade toscaOperationFacade) {
        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        for (int i = 0; i <= pathElements.size() -1 ; i++) {
                final ForwardingPathElementDataDefinition element = pathElements.get(i);
                toscaRequirements.add(handleSingleReq(fetchCPName(service, element.getFromNode(), element.getFromCP(), capabiltyRequirementConvertor, originComponents, toscaOperationFacade), fetchNodeName(service, element.getFromNode())));
                if ( i == pathElements.size() -1) {
                    toscaRequirements.add(handleSingleReq(fetchCPName(service, element.getToNode(), element.getToCP(), capabiltyRequirementConvertor, originComponents, toscaOperationFacade), fetchNodeName(service, element
                            .getToNode())));
                }
       }
        return toscaRequirements;

    }

    private static String fetchNodeName(Service service, String nodeId) {
        if (service.getComponentInstanceByName(nodeId).isPresent()) {
            return service.getComponentInstanceByName(nodeId).get().getName();
        } else {
            return "";
        }
    }


    private static Map<String, ToscaTemplateRequirement> handleSingleReq(
            String fromCP, String fromNode) {
        Map<String, ToscaTemplateRequirement> toscaReqMap = new HashMap<>();
        ToscaTemplateRequirement firstReq = new ToscaTemplateRequirement();
        firstReq.setRelationship(FORWARDS_TO_TOSCA_NAME); //todo
        firstReq.setCapability(fromCP);
        firstReq.setNode(fromNode);
        toscaReqMap.put(FORWARDER, firstReq);

        return toscaReqMap;
    }

    /**
     * @todo handle errors.
     */
    private static String fetchCPName(Service service, String nodeID, String cpName, CapabilityRequirementConverter capabiltyRequirementConvertor, Map<String, Component> originComponents, ToscaOperationFacade toscaOperationFacade) {
        Optional<ComponentInstance> componentInstance = service.getComponentInstanceByName(nodeID);
        ComponentInstance componentInstanceVal = componentInstance.get();
        String name = componentInstanceVal.getNormalizedName();
        Component component = originComponents.get(componentInstanceVal.getComponentUid());
        if(componentInstanceVal.getIsProxy()){
            component = originComponents.get(componentInstanceVal.getSourceModelUid());
            if (component == null) {
                component = toscaOperationFacade.getToscaFullElement(componentInstanceVal.getSourceModelUid()).left().value();
            }

        }
        CapabilityDefinition capability = componentInstanceVal.getCapabilities().values().stream().flatMap(Collection::stream)
                .filter(capabilityDefinition -> capabilityDefinition.getName().equals(cpName)).findAny().get();
        List<String> path = capability.getPath();
        List<String> reducedPath = new ArrayList<>(path);
        reducedPath.remove(reducedPath.size() - 1);
        Either<String, Boolean> stringBooleanEither = capabiltyRequirementConvertor.buildSubstitutedName(originComponents, component, reducedPath, capability.getName(), null);
        return name + "." + stringBooleanEither.left().value();
    }
}
