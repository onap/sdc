package org.openecomp.sdc.asdctool.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReportWriter {
    FileWriter file;
    public ReportWriter(String reportName) {

        StringBuilder sb = new StringBuilder();
        Path path = Paths.get("/var/tmp/");
        if ( path.toFile().exists() ) {
            sb.append("/var/tmp/");
        }
        sb.append("report_").append(reportName).append("_").append(System.currentTimeMillis()).append(".json");
        String fileName = sb.toString();
        try {
            file = new FileWriter(fileName);
        } catch (IOException e) {
            System.out.println("Failed to create report file. " + e.getMessage());
        }
    }

    public void report(Object objectToWrite) throws IOException {
        if (file != null) {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(JsonParserUtils.toJson(objectToWrite)).getAsJsonObject();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            
            file.write(prettyJson);
            file.flush();
        }
    }

    public void close() throws IOException {
        if (file != null) {
            file.close();
        }
    }
}
