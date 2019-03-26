package org.openecomp.sdc.be.components.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import java.lang.reflect.Type;
import org.onap.sdc.gab.GABService;
import org.onap.sdc.gab.GABServiceImpl;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABResult;
import org.onap.sdc.gab.model.GABResults;

@org.springframework.stereotype.Component
public class GenericArtifactBrowserBusinessLogic extends BaseBusinessLogic {

    private GABService gabService;

    public GenericArtifactBrowserBusinessLogic() {
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
