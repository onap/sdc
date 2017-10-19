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

public class RequirementDefinitionDeserializer implements JsonDeserializer<RequirementDefinition> {
  @Override
  public RequirementDefinition deserialize(JsonElement jsonElement, Type type,
                                           JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {


    JsonObject jsonObject = jsonElement.getAsJsonObject();

    RequirementDefinition requirementDefinition = new RequirementDefinition();
    setRequirementValues(jsonObject, requirementDefinition);

    Object[] fixedOccurrences = handleOccurences(jsonObject);
    requirementDefinition.setOccurrences(fixedOccurrences);


    return requirementDefinition;

  }


  private void setRequirementValues(JsonObject jsonObject,
                                    RequirementDefinition requirementDefinition) {
    JsonElement capability = jsonObject.get("capability");
    if (!Objects.isNull(capability)) {
      requirementDefinition.setCapability(capability.getAsString());
    }

    JsonElement node = jsonObject.get("node");
    if (!Objects.isNull(node)) {
      requirementDefinition.setNode(node.getAsString());
    }

    JsonElement relationship = jsonObject.get("relationship");
    if (!Objects.isNull(relationship)) {
      requirementDefinition.setRelationship(relationship.getAsString());
    }
  }

  private Object[] handleOccurences(JsonObject jsonObject) {
    Object[] fixedOccurrences = new Object[2];
    JsonArray occurrences = jsonObject.get("occurrences").getAsJsonArray();
    int size = occurrences.size();

    for (int i = 0; i < size; i++) {
      JsonElement currElement = occurrences.get(i);
      if (currElement.isJsonPrimitive()) {
        JsonPrimitive jsonPrimitive = currElement.getAsJsonPrimitive();

        if (jsonPrimitive.isNumber()) {
          fixedOccurrences[i] = jsonPrimitive.getAsNumber().intValue();
        } else {
          fixedOccurrences[i] = jsonPrimitive.getAsString();
        }
      }
    }

    return fixedOccurrences;
  }
}
