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
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class ForwardingPathElementDataDefinition extends ToscaDataDefinition {

    @JsonCreator
    public ForwardingPathElementDataDefinition() {
    }

    public ForwardingPathElementDataDefinition(String fromNode, String toNode, String fromCPName, String toCPName, String fromCPOriginId,
                                               String toCPOriginId) {
        super();
        setFromNode(fromNode);
        setToNode(toNode);
        setFromCP(fromCPName);
        setToCP(toCPName);
        setFromCPOriginId(fromCPOriginId);
        setToCPOriginId(toCPOriginId);
    }

    public ForwardingPathElementDataDefinition(ForwardingPathElementDataDefinition pathElement) {
        super();
        setFromNode(pathElement.getFromNode());
        setToNode(pathElement.getToNode());
        setFromCP(pathElement.getFromCP());
        setToCP(pathElement.getToCP());
        setFromCPOriginId(pathElement.getFromCPOriginId());
        setToCPOriginId(pathElement.getToCPOriginId());
    }

    public String getFromNode() {
        return (String) getToscaPresentationValue(JsonPresentationFields.FROM_NODE);
    }

    public void setFromNode(String fromNode) {
        setToscaPresentationValue(JsonPresentationFields.FROM_NODE, fromNode);
    }

    public String getToNode() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TO_NODE);
    }

    public void setToNode(String toNode) {
        setToscaPresentationValue(JsonPresentationFields.TO_NODE, toNode);
    }

    public String getFromCP() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PATH_FROM_CP);
    }

    public void setFromCP(String fromCP) {
        setToscaPresentationValue(JsonPresentationFields.PATH_FROM_CP, fromCP);
    }

    public String getToCP() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PATH_TO_CP);
    }

    public void setToCP(String toCP) {
        setToscaPresentationValue(JsonPresentationFields.PATH_TO_CP, toCP);
    }

    public String getToCPOriginId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PATH_TO_CP_ORIGIN);
    }

    public void setToCPOriginId(String toCPOriginId) {
        setToscaPresentationValue(JsonPresentationFields.PATH_TO_CP_ORIGIN, toCPOriginId);
    }

    public String getFromCPOriginId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PATH_FROM_CP_ORIGIN);
    }

    public void setFromCPOriginId(String fromCPOriginId) {
        setToscaPresentationValue(JsonPresentationFields.PATH_FROM_CP_ORIGIN, fromCPOriginId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ForwardingPathElementDataDefinition that = (ForwardingPathElementDataDefinition) o;
        return Objects.equals(getFromNode(), that.getFromNode()) && Objects.equals(getToNode(), that.getToNode())
            && Objects.equals(getFromCPOriginId(), that.getFromCPOriginId()) && Objects.equals(getToCPOriginId(), that.getToCPOriginId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFromNode(), getToNode(), getFromCP(), getToCP(), getFromCPOriginId(), getToCPOriginId());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fromNode", getFromNode()).add("toNode", getToNode())
            .add("fromCPOriginId", getFromCPOriginId()).add("toCPOriginId", getToCPOriginId())
            .add("fromCPName", getFromCP()).add("toCPName", getToCP()).toString();
    }
}
