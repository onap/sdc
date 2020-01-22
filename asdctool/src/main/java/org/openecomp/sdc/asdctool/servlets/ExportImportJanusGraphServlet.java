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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.janusgraph.core.JanusGraph;
import org.openecomp.sdc.asdctool.Utils;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
//import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

@Path("/janusgraph")
public class ExportImportJanusGraphServlet {

	private static Logger log = Logger.getLogger(ExportImportJanusGraphServlet.class.getName());

	@GET
	@Path("export")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response export(@FormDataParam("janusGraphProperties") File janusGraphPropertiesFile,
			@FormDataParam("metadata") String exportGraphMetadata) {

		printJanusGraphConfigFile(janusGraphPropertiesFile);
		printMetadata(exportGraphMetadata);

		Properties janusGraphProperties = convertFileToProperties(janusGraphPropertiesFile);

		if (janusGraphProperties == null) {
			Response response = Utils.buildOkResponse(400, "cannot parse janusgraph properties file", null);
			return response;
		}

		Configuration conf = new BaseConfiguration();
		for (Entry<Object, Object> entry : janusGraphProperties.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();
			conf.setProperty(key, value);
		}

		conf.setProperty("storage.machine-id-appendix", System.currentTimeMillis() % 1000);

		try(JanusGraph openGraph = Utils.openGraph(conf)){
			
			if (openGraph == null) {
				Response buildErrorResponse = Utils.buildOkResponse(500, "failed to open graph", null);
				return buildErrorResponse;
			}
	
			// Open JanusGraph Graph
	
			Response buildOkResponse = Utils.buildOkResponse(200, "ok man", null);
	
			return buildOkResponse;
		}
	}

	private Properties convertFileToProperties(File janusGraphPropertiesFile) {

		Properties properties = new Properties();

		try (FileReader fileReader = new FileReader(janusGraphPropertiesFile)){
			properties.load(fileReader);
		} catch (Exception e) {
			log.error("Failed to convert file to properties", e);
			return null;
		}

		return properties;
	}

	private void printJanusGraphConfigFile(File janusGraphPropertiesFile) {

		if (log.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new FileReader(janusGraphPropertiesFile))) {
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

	private void printMetadata(String exportGraphMetadata) {

		log.debug(exportGraphMetadata);

	}

	public String exportGraph(JanusGraph graph, String outputDirectory) {

		String result = null;

		// GraphMLWriter graphMLWriter = new GraphMLWriter(graph);
		GraphMLWriter graphMLWriter = GraphMLWriter.build().create();

		String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".ml";

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new ByteArrayOutputStream());

			// graphMLWriter.outputGraph(out);

			graphMLWriter.writeGraph(out, graph);

			// graph.commit();
			graph.tx().commit();

			result = outputFile;

		} catch (Exception e) {
			e.printStackTrace();
			// graph.rollback();
			graph.tx().rollback();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;

	}

}
