package org.openecomp.core.utilities.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public class RequirementDefinitionDeserializer implements JsonDeserializer<RequirementDefinition> {

  private static String capability = "capability";
  private static String node = "node";
  private static String relationship = "relationship";
  private static String occurrences = "occurrences";


  @Override
  public RequirementDefinition deserialize(JsonElement jsonElement, Type type,
                                           JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {


    JsonObject jsonObject = jsonElement.getAsJsonObject();

    RequirementDefinition requirementDefinition = new RequirementDefinition();
    setRequirementValues(jsonObject, requirementDefinition);

    Optional<Object[]> occurences = handleOccurrences(jsonObject);
    occurences.ifPresent(requirementDefinition::setOccurrences);


    return requirementDefinition;

  }


  private void setRequirementValues(JsonObject jsonObject,
                                    RequirementDefinition requirementDefinition) {
    JsonElement capabilityElement = jsonObject.get(capability);
    if (!Objects.isNull(capabilityElement)) {
      requirementDefinition.setCapability(capabilityElement.getAsString());
    }

    JsonElement nodeElement = jsonObject.get(node);
    if (!Objects.isNull(nodeElement)) {
      requirementDefinition.setNode(nodeElement.getAsString());
    }

    JsonElement relationshipElement = jsonObject.get(relationship);
    if (!Objects.isNull(relationshipElement)) {
      requirementDefinition.setRelationship(relationshipElement.getAsString());
    }
  }

  private Optional<Object[]> handleOccurrences(JsonObject jsonObject) {
    JsonElement occurrencesElement = jsonObject.get(occurrences);

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
