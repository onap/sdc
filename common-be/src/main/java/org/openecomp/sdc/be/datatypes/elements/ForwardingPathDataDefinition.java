package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.*;

public class ForwardingPathDataDefinition extends ToscaDataDefinition {

    @JsonCreator
    public ForwardingPathDataDefinition() {
        super();
    }

    public ForwardingPathDataDefinition(String name) {
        super();
        setName(name);

    }

    public ForwardingPathDataDefinition(ForwardingPathDataDefinition path) {
        super();
        setName(path.getName());
        setDestinationPortNumber(path.getDestinationPortNumber());
        setUniqueId(path.getUniqueId());
        setPathElements(path.getPathElements());
        setProtocol(path.getProtocol());
        setDescription(path.getDescription());
        setToscaResourceName(path.getToscaResourceName());
    }

    public String getName() {
        return (String) getToscaPresentationValue(PATH_NAME);
    }

    public void setName(String name) {
        setToscaPresentationValue(PATH_NAME, name);
    }

    public ListDataDefinition<ForwardingPathElementDataDefinition> getPathElements() {
        return (ListDataDefinition<ForwardingPathElementDataDefinition>) getToscaPresentationValue(PATH_ELEMENT_LIST);
    }

    public void setPathElements(ListDataDefinition<ForwardingPathElementDataDefinition> pathElements) {
        setToscaPresentationValue(PATH_ELEMENT_LIST, pathElements);
    }

    public String getUniqueId() {
        return (String) getToscaPresentationValue(UNIQUE_ID);
    }

    public void setUniqueId(String uid) {
        setToscaPresentationValue(UNIQUE_ID, uid);
    }

    public String getProtocol() {
        return (String) getToscaPresentationValue(PATH_PROTOCOL);
    }

    public void setProtocol(String protocol) {
        setToscaPresentationValue(PATH_PROTOCOL, protocol);
    }

    public String getDestinationPortNumber() {
        return (String) getToscaPresentationValue(PATH_DESTINATION_PORT_NUMBER);
    }

    public void setDestinationPortNumber(String destinationPortNumber) {
        setToscaPresentationValue(PATH_DESTINATION_PORT_NUMBER, destinationPortNumber);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(DESCRIPTION, description);
    }

    public String getToscaResourceName() {
           return (String) getToscaPresentationValue(TOSCA_RESOURCE_NAME);
    }

    public void setToscaResourceName(String toscaResourceName) {
        setToscaPresentationValue(TOSCA_RESOURCE_NAME, toscaResourceName);
    }
}
