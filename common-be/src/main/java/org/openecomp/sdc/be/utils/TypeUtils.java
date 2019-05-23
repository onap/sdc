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
package org.openecomp.sdc.be.utils;

import java.util.Map;
import java.util.function.Consumer;

public class TypeUtils {

    public static final String FIRST_CERTIFIED_VERSION_VERSION = "1.0";

    public static <FieldType> void setField(Map<String, Object> toscaJson, ToscaTagNamesEnum tagName, Consumer<FieldType> setter) {
        String fieldName = tagName.getElementName();
        if (toscaJson.containsKey(fieldName)) {
            FieldType fieldValue = (FieldType) toscaJson.get(fieldName);
            setter.accept(fieldValue);
        }
    }

    public enum ToscaTagNamesEnum {
        DERIVED_FROM("derived_from"), IS_PASSWORD("is_password"),
        // Properties
        PROPERTIES("properties"), TYPE("type"), STATUS("status"), ENTRY_SCHEMA("entry_schema"), REQUIRED("required"), DESCRIPTION("description"), DEFAULT_VALUE("default"), VALUE("value"), CONSTRAINTS("constraints"),
        // Group Types
        MEMBERS("members"), METADATA("metadata"),
        // Policy Types
        TARGETS("targets"),
        // Capabilities
        CAPABILITIES("capabilities"), VALID_SOURCE_TYPES("valid_source_types"),
        // Requirements
        REQUIREMENTS("requirements"), NODE("node"), RELATIONSHIP("relationship"), CAPABILITY("capability"), INTERFACES("interfaces"),
        NODE_FILTER("node_filter"), TOSCA_ID("tosca_id"),
        // Heat env Validation
        PARAMETERS("parameters"),
        // Import Validations
        TOSCA_VERSION("tosca_definitions_version"), TOPOLOGY_TEMPLATE("topology_template"), NODE_TYPES("node_types"), OCCURRENCES("occurrences"), NODE_TEMPLATES("node_templates"), GROUPS("groups"), INPUTS("inputs"),
        SUBSTITUTION_MAPPINGS("substitution_mappings"),  NODE_TYPE("node_type"), DIRECTIVES("directives"),
        // Attributes
        ATTRIBUTES("attributes"), LABEL("label"), HIDDEN("hidden"), IMMUTABLE("immutable"), GET_INPUT("get_input"), ANNOTATIONS("annotations");

        private String elementName;

        ToscaTagNamesEnum(String elementName) {
            this.elementName = elementName;
        }

        public String getElementName() {
            return elementName;
        }
    }
}
