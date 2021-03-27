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
package org.openecomp.sdc.asdctool.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;

public class ReportWriter {

    FileWriter file;

    public ReportWriter(String reportName) {
        StringBuilder sb = new StringBuilder();
        Path path = Paths.get("/var/tmp/");
        if (path.toFile().exists()) {
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
