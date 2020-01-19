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

package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.SerializeExceptFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepresentationUtils {

    private static final Logger log = Logger.getLogger(RepresentationUtils.class);

    public static ArtifactDefinition convertJsonToArtifactDefinitionForUpdate(String content, Class<ArtifactDefinition> clazz) {

        JsonObject jsonElement = new JsonObject();
        ArtifactDefinition resourceInfo = null;

        try {
            Gson gson = new Gson();
            jsonElement = gson.fromJson(content, jsonElement.getClass());
            String payload = null;
            jsonElement.remove(Constants.ARTIFACT_GROUP_TYPE);
            //in update the group type is ignored but this spagheti code makes it too complex to remove this field.
            jsonElement.addProperty(Constants.ARTIFACT_GROUP_TYPE, ArtifactGroupTypeEnum.INFORMATIONAL.getType());
            JsonElement artifactPayload = jsonElement.get(Constants.ARTIFACT_PAYLOAD_DATA);
            if (artifactPayload != null && !artifactPayload.isJsonNull()) {
                payload = artifactPayload.getAsString();
            }
            jsonElement.remove(Constants.ARTIFACT_PAYLOAD_DATA);
            String json = gson.toJson(jsonElement);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            resourceInfo = mapper.readValue(json, clazz);
            resourceInfo.setPayloadData(payload);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeArtifactInformationInvalidError("Artifact Upload / Update");
            log.debug("Failed to convert the content {} to object.", content.substring(0, Math.min(50, content.length())), e);
        }

        return resourceInfo;
    }


    public static class ResourceRep {

    }

    /**
     * Build Representation of given Object
     *
     * @param elementToRepresent
     * @return
     * @throws IOException
     */
    public static <T> Object toRepresentation(T elementToRepresent) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(elementToRepresent);
    }

    public static <T> T fromRepresentation(String json, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        T object = null;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            object = mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error when parsing JSON of object of type {}", clazz.getSimpleName(), e);
        } // return null in case of exception

        return object;
    }

    public static ArtifactDefinition convertJsonToArtifactDefinition(String content, Class<ArtifactDefinition> clazz, boolean validateTimeout) {

        JsonObject jsonElement = new JsonObject();
        ArtifactDefinition resourceInfo = null;

        if (StringUtils.isEmpty(content)) {
            throw new ByActionStatusComponentException(ActionStatus.MISSING_BODY);
        }

        try {
            Gson gson = new Gson();
            jsonElement = gson.fromJson(content, jsonElement.getClass());
            JsonElement artifactGroupValue = jsonElement.get(Constants.ARTIFACT_GROUP_TYPE);
            HashMap<String, JsonElement> elementsToValidate = new HashMap<>();
            elementsToValidate.put(Constants.ARTIFACT_GROUP_TYPE, artifactGroupValue);
            elementsToValidate.put(Constants.ARTIFACT_TYPE, jsonElement.get(Constants.ARTIFACT_TYPE));
            elementsToValidate.put(Constants.ARTIFACT_LABEL, (jsonElement.get(Constants.ARTIFACT_LABEL)));
            if (validateTimeout) {
                elementsToValidate.put(Constants.ARTIFACT_TIMEOUT, jsonElement.get(Constants.ARTIFACT_TIMEOUT));
            }
            validateMandatoryProperties(elementsToValidate);

            if (artifactGroupValue != null && !artifactGroupValue.isJsonNull()) {
                String groupValueUpper = artifactGroupValue.getAsString().toUpperCase();
                if (!ArtifactGroupTypeEnum.getAllTypes().contains(groupValueUpper)) {
                    StringBuilder sb = new StringBuilder();
                    for (String value : ArtifactGroupTypeEnum.getAllTypes()) {
                        sb.append(value).append(", ");
                    }
                    log.debug("artifactGroupType is {}. valid values are: {}", groupValueUpper, sb);
                    return null;
                } else {
                    jsonElement.remove(Constants.ARTIFACT_GROUP_TYPE);
                    jsonElement.addProperty(Constants.ARTIFACT_GROUP_TYPE, groupValueUpper);
                }
            }
            String payload = null;
            JsonElement artifactPayload = jsonElement.get(Constants.ARTIFACT_PAYLOAD_DATA);
            if (artifactPayload != null && !artifactPayload.isJsonNull()) {
                payload = artifactPayload.getAsString();
            }
            jsonElement.remove(Constants.ARTIFACT_PAYLOAD_DATA);
            String json = gson.toJson(jsonElement);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            resourceInfo = mapper.readValue(json, clazz);
            resourceInfo.setPayloadData(payload);

        } catch (ComponentException ce) {
            BeEcompErrorManager.getInstance().logBeArtifactInformationInvalidError("Artifact Upload / Update");
            log.debug("Failed to convert the content {} to object.", content.substring(0, Math.min(50, content.length())), ce);
            throw ce;
        }
        catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeArtifactInformationInvalidError("Artifact Upload / Update");
            log.debug("Failed to convert the content {} to object.", content.substring(0, Math.min(50, content.length())), e);
        }

        return resourceInfo;
    }

    private static void validateMandatoryProperties(HashMap<String, JsonElement> elementsByName) {
        elementsByName.forEach((name, element) -> {
            if (element == null) {
                throw new ByActionStatusComponentException(ActionStatus.MISSING_MANDATORY_PROPERTY, name);
            }
            if (element.isJsonNull()) {
                throw new ByActionStatusComponentException(ActionStatus.MANDATORY_PROPERTY_MISSING_VALUE, name);
            }
        });
    }

    public static <T> Object toFilteredRepresentation(T elementToRepresent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setMixIns(IS_EMPTY_FILTER_MIXIN);
        return mapper.writer(new SimpleFilterProvider().addFilter(REMOVE_IS_EMPTY_FROM_COLLECTIONS_FILTER,
                SerializeExceptFilter.serializeAllExcept(EMPTY))).writeValueAsString(elementToRepresent);
    }

    @JsonFilter(REMOVE_IS_EMPTY_FROM_COLLECTIONS_FILTER)
    private static class IsEmptyFilterMixIn {}

    private static final String EMPTY = "empty";
    private static final String REMOVE_IS_EMPTY_FROM_COLLECTIONS_FILTER = "removeIsEmptyFromCollections";
    private static final ImmutableMap<Class<?>,Class<?>> IS_EMPTY_FILTER_MIXIN =
            ImmutableMap.<Class<?>,Class<?>>builder()
                    .put(Collection.class,IsEmptyFilterMixIn.class)
                    .put(List.class,IsEmptyFilterMixIn.class)
                    .put(Set.class,IsEmptyFilterMixIn.class)
                    .put(HashMap.class,IsEmptyFilterMixIn.class)
                    .put(ArrayList.class,IsEmptyFilterMixIn.class)
                    .put(HashSet.class,IsEmptyFilterMixIn.class)
                    .put(InterfaceDefinition.class,IsEmptyFilterMixIn.class)
                    .put(Resource.class,IsEmptyFilterMixIn.class)
                    .put(ToscaDataDefinition.class,IsEmptyFilterMixIn.class)
                    .build();

}
