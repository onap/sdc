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
package org.openecomp.sdc.be.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.tinkerpop.gremlin.structure.T;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.EqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MaxLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.PatternConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

public interface PropertyConstraint {

    void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException;

    void validate(Object propertyValue) throws ConstraintViolationException;

    void validate(ToscaType toscaType, String propertyTextValue) throws ConstraintViolationException;

    @JsonIgnore
    ConstraintType getConstraintType();

    void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException;

    String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException exception, String propertyName);

    class PropertyConstraintListDeserializer implements JsonDeserializer<List<PropertyConstraint>> {

        @SneakyThrows
        @Override
        public List<PropertyConstraint> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws
            JsonParseException {
            List<PropertyConstraint> propertyConstraints = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            List<JsonElement> asList = mapper.readValue(
                json.getAsString(), new TypeReference<>() {
                });
            asList.forEach(jsonConstElement -> {
                SimpleModule module = new SimpleModule("customDeserializationModule");
                module.addDeserializer(PropertyConstraint.class,
                    new PropertyOperation.PropertyConstraintJacksonDeserializer());
                mapper.registerModule(module);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                try {
                    PropertyConstraint constraint =
                        mapper.readValue(jsonConstElement.getAsString(), PropertyConstraint.class);
                    propertyConstraints.add(constraint);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            return propertyConstraints;
        }
    }
}


