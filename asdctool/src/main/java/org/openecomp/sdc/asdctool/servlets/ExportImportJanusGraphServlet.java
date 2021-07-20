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
package org.openecomp.sdc.asdctool.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.janusgraph.core.JanusGraph;
import org.openecomp.sdc.asdctool.Utils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

@Path("/janusgraph")
public class ExportImportJanusGraphServlet {

    private static final Logger log = LoggerFactory.getLogger(ExportImportJanusGraphServlet.class);

    @GET
    @Path("export")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response export(@FormDataParam("janusGraphProperties") final File janusGraphPropertiesFile,
                           @FormDataParam("metadata") final String exportGraphMetadata) {
        printJanusGraphConfigFile(janusGraphPropertiesFile);
        printMetadata(exportGraphMetadata);
        final Properties janusGraphProperties = convertFileToProperties(janusGraphPropertiesFile);
        if (janusGraphProperties == null) {
            return Utils.buildOkResponse(400, "cannot parse janusgraph properties file", null);
        }
        final Configuration conf = new BaseConfiguration();
        for (final Entry<Object, Object> entry : janusGraphProperties.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();
            conf.setProperty(key, value);
        }
        conf.setProperty("storage.machine-id-appendix", System.currentTimeMillis() % 1000);
        final Optional<JanusGraph> openGraph = Utils.openGraph(conf);
        if (openGraph.isPresent()) {
            try {
                return Utils.buildOkResponse(200, "ok man", null);
            } finally {
                openGraph.get().close();
            }
        } else {
            return Utils.buildOkResponse(500, "failed to open graph", null);
        }
    }

    private Properties convertFileToProperties(final File janusGraphPropertiesFile) {
        final var properties = new Properties();
        try (final var fileReader = new FileReader(janusGraphPropertiesFile)) {
            properties.load(fileReader);
        } catch (final Exception e) {
            log.error("Failed to convert file to properties", e);
            return null;
        }
        return properties;
    }

    private void printJanusGraphConfigFile(final File janusGraphPropertiesFile) {
        if (log.isDebugEnabled()) {
            final var builder = new StringBuilder();
            try (final var br = new BufferedReader(new FileReader(janusGraphPropertiesFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line + Utils.NEW_LINE);
                }
                log.debug(builder.toString());
            } catch (IOException e) {
                log.error("Cannot print janusgraph properties file", e);
            }
        }
    }

    private void printMetadata(final String exportGraphMetadata) {
        log.debug(exportGraphMetadata);
    }

}
