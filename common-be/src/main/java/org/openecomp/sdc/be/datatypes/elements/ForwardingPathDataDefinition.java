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
package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DESCRIPTION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PATH_DESTINATION_PORT_NUMBER;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PATH_ELEMENT_LIST;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PATH_NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PATH_PROTOCOL;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TOSCA_RESOURCE_NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.UNIQUE_ID;

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
