/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.onap.sdc.gab.GABService;
import org.onap.sdc.gab.GABServiceImpl;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABResult;
import org.onap.sdc.gab.model.GABResults;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Type;

@org.springframework.stereotype.Component
public class GenericArtifactBrowserBusinessLogic extends BaseBusinessLogic {

    private GABService gabService;

    @Autowired
    public GenericArtifactBrowserBusinessLogic(IElementOperation elementDao,
        IGroupOperation groupOperation,
        IGroupInstanceOperation groupInstanceOperation,
        IGroupTypeOperation groupTypeOperation,
        InterfaceOperation interfaceOperation,
        InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
        ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        gabService = new GABServiceImpl();
    }

    public String searchFor(GABQuery gabQuery) throws IOException {
        GABResults gabResults = gabService.searchFor(gabQuery);
        return createGsonForGABResult().toJson(gabResults);
    }

    private Gson createGsonForGABResult(){
        return new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(GABResult.class, new GABResultSerializer())
            .registerTypeAdapter(GABResults.class, new GABResultsSerializer())
            .create();
    }

    private class GABResultsSerializer implements JsonSerializer<GABResults> {
        @Override
        public JsonElement serialize(GABResults gabResults, Type type,
            JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            gabResults.getRows().stream().map(jsonSerializationContext::serialize).forEach(jsonArray::add);
            result.add("data", jsonArray);
            return result;
        }
    }

    private class GABResultSerializer implements JsonSerializer<GABResult> {
        @Override
        public JsonElement serialize(GABResult gabResult, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject result = new JsonObject();
            gabResult.getEntries().forEach(entry -> result.addProperty(entry.getPath(), String.valueOf(entry.getData())));
            return result;
        }
    }

}
