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
package org.openecomp.core.utilities.deserializers;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RequirementDefinitionDeserializer implements JsonDeserializer<RequirementDefinition> {

  static final String CAPABILITY = "capability";
  static final String NODE = "node";
  static final String RELATIONSHIP = "relationship";
  private static final String OCCURRENCES = "occurrences";

  @Override
  public RequirementDefinition deserialize(JsonElement jsonElement, Type type,
                                           JsonDeserializationContext jsonDeserializationContext) {

    JsonObject jsonObject = jsonElement.getAsJsonObject();

    RequirementDefinition requirementDefinition = new RequirementDefinition();
    setRequirementValues(jsonObject, requirementDefinition);

    Optional<Object[]> occurrences = handleOccurrences(jsonObject);
    occurrences.ifPresent(requirementDefinition::setOccurrences);

    return requirementDefinition;
  }


  private void setRequirementValues(JsonObject jsonObject,
                                    RequirementDefinition requirementDefinition) {

    JsonElement capabilityElement = jsonObject.get(CAPABILITY);
    if (!Objects.isNull(capabilityElement)) {
      requirementDefinition.setCapability(capabilityElement.getAsString());
    }

    JsonElement nodeElement = jsonObject.get(NODE);
    if (!Objects.isNull(nodeElement)) {
      requirementDefinition.setNode(nodeElement.getAsString());
    }

    JsonElement relationshipElement = jsonObject.get(RELATIONSHIP);
    if (!Objects.isNull(relationshipElement)) {
      requirementDefinition.setRelationship(relationshipElement.getAsString());
    }
  }

  private Optional<Object[]> handleOccurrences(JsonObject jsonObject) {

    JsonElement occurrencesElement = jsonObject.get(OCCURRENCES);

    if(Objects.isNull(occurrencesElement)){
      return Optional.empty();
    }

    JsonArray occurrences = occurrencesElement.getAsJsonArray();
    Object[] fixedOccurrences = new Object[occurrences.size()];

    for (int i = 0; i < occurrences.size(); i++) {
      JsonElement currElement = occurrences.get(i);

      // values inside occurrences array can be either String or Integer
      if (currElement.isJsonPrimitive()) {
        JsonPrimitive jsonPrimitive = currElement.getAsJsonPrimitive();

        if (jsonPrimitive.isNumber()) {
          fixedOccurrences[i] = jsonPrimitive.getAsNumber().intValue();
        } else {
          fixedOccurrences[i] = jsonPrimitive.getAsString();
        }
      }
    }

    return Optional.of(fixedOccurrences);
  }
}
