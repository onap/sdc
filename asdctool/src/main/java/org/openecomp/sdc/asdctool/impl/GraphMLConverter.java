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

package org.openecomp.sdc.asdctool.impl;

import com.google.gson.Gson;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanVertex;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONReader;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.asdctool.Utils;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GraphMLConverter {

	private static Logger log = Logger.getLogger(GraphMLConverter.class.getName());

	private Gson gson = new Gson();

	public boolean importGraph(String[] args) {

		TitanGraph graph = null;
		try {
			String titanFileLocation = args[1];
			String inputFile = args[2];
			graph = openGraph(titanFileLocation);

			List<ImmutablePair<String, String>> propertiesCriteriaToDelete = new ArrayList<>();
			ImmutablePair<String, String> immutablePair1 = new ImmutablePair<String, String>("healthcheckis", "GOOD");
			ImmutablePair<String, String> immutablePair2 = new ImmutablePair<String, String>("nodeLabel", "user");
			ImmutablePair<String, String> immutablePair3 = new ImmutablePair<String, String>("nodeLabel",
					"resourceCategory");
			ImmutablePair<String, String> immutablePair4 = new ImmutablePair<String, String>("nodeLabel",
					"serviceCategory");

			propertiesCriteriaToDelete.add(immutablePair1);
			propertiesCriteriaToDelete.add(immutablePair2);
			propertiesCriteriaToDelete.add(immutablePair3);
			propertiesCriteriaToDelete.add(immutablePair4);

			boolean result = importJsonGraph(graph, inputFile, propertiesCriteriaToDelete);

			return result;

		} catch (Exception e) {
			log.info("import graph failed - {} " , e);
			return false;
		} finally {
			if (graph != null) {
				// graph.shutdown();
				graph.close();
			}
		}

	}

	public boolean exportGraph(String[] args) {

		TitanGraph graph = null;
		try {
			String titanFileLocation = args[1];
			String outputDirectory = args[2];
			graph = openGraph(titanFileLocation);

			String result = exportJsonGraph(graph, outputDirectory);

			if (result == null) {
				return false;
			}

			System.out.println("Exported file=" + result);
		} catch (Exception e) {
			log.info("export graph failed -{}" , e);
			return false;
		} finally {
			if (graph != null) {
				// graph.shutdown();
				graph.close();
			}
		}

		return true;
	}

	public String exportGraphMl(String[] args) {

		TitanGraph graph = null;
		String result = null;
		try {
			String titanFileLocation = args[1];
			String outputDirectory = args[2];
			graph = openGraph(titanFileLocation);

			result = exportGraphMl(graph, outputDirectory);

			System.out.println("Exported file=" + result);
		} catch (Exception e) {
			log.info("export exportGraphMl failed - {}" , e);
			return null;
		} finally {
			if (graph != null) {
				graph.close();
			}
		}

		return result;
	}

	public boolean findErrorInJsonGraph(String[] args) {

		TitanGraph graph = null;
		try {
			String titanFileLocation = args[1];
			String outputDirectory = args[2];
			graph = openGraph(titanFileLocation);

			String result = findErrorInJsonGraph(graph, outputDirectory);

			if (result == null) {
				return false;
			}

			System.out.println("Exported file=" + result);
		} catch (Exception e) {
			log.info("find Error In Json Graph failed - {}" , e);
			return false;
		} finally {
			if (graph != null) {
				graph.close();
			}
		}

		return true;
	}

	public TitanGraph openGraph(String titanFileLocation) {

		TitanGraph graph = TitanFactory.open(titanFileLocation);

		return graph;

	}

	public String exportJsonGraph(TitanGraph graph, String outputDirectory) {

		String result = null;

		String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".json";

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(outputFile));

			final GraphSONWriter.Builder builder = GraphSONWriter.build();
			final GraphSONMapper mapper = newGraphSONMapper(graph);
			builder.mapper(mapper);
			final GraphSONWriter writer = builder.create();
			writer.writeGraph(out, graph);

			graph.tx().commit();

			result = outputFile;

		} catch (Exception e) {
			log.info("export Json Graph failed - {}" , e);
			graph.tx().rollback();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				log.info("close FileOutputStream failed - {}" , e);
			}
		}
		return result;

	}

	public String exportGraphMl(TitanGraph graph, String outputDirectory) {
		String result = null;
		String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".graphml";
		try {
			try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
				graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(os, graph);
			}
			result = outputFile;
			graph.tx().commit();
		} catch (Exception e) {
			graph.tx().rollback();
			log.info("export Graph Ml failed - {}" , e);
		}
		return result;

	}

	private static GraphSONMapper newGraphSONMapper(final Graph graph) {
		final GraphSONMapper.Builder builder = graph.io(IoCore.graphson()).mapper();
		return builder.create();
	}

	public boolean importJsonGraph(TitanGraph graph, String graphJsonFile,
			List<ImmutablePair<String, String>> propertiesCriteriaToDelete) {

		boolean result = false;

		InputStream is = null;

		try {

			if (propertiesCriteriaToDelete != null) {
				for (Entry<String, String> entry : propertiesCriteriaToDelete

				) {

					String key = entry.getKey();
					String value = entry.getValue();
					Iterator iterator = graph.query().has(key, value).vertices().iterator();
					while (iterator.hasNext()) {
						Vertex vertex = (Vertex) iterator.next();
						vertex.remove();
						System.out.println("Remove vertex of type " + key + " and value " + value);
					}

				}
			}
			File file = new File(graphJsonFile);
			if (false == file.isFile()) {
				System.out.println("File " + graphJsonFile + " cannot be found.");
				return result;
			}

			is = new BufferedInputStream(new FileInputStream(graphJsonFile));
			System.out.println("Before importing file " + graphJsonFile);

			GraphSONReader create = GraphSONReader.build().create();
			create.readGraph(is, graph);

			graph.tx().commit();

			result = true;

		} catch (Exception e) {
			System.out.println("Failed to import graph " + e.getMessage());
			log.info("Failed to import graph - {}" , e);
			// graph.rollback();
			graph.tx().rollback();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				log.info("close FileOutputStream failed - {}" , e);
			}
		}

		return result;

	}

	public String findErrorInJsonGraph(TitanGraph graph, String outputDirectory) {

		boolean runVertexScan = false;
		boolean runEdgeScan = false;

		String result = null;

		String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".json";

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(outputFile));

			if (runEdgeScan) {

				Vertex vertexFrom = null;
				Vertex vertexTo = null;
				Edge edge = null;

				Iterable<TitanEdge> edges = graph.query().edges();
				Iterator<TitanEdge> iterator = edges.iterator();
				while (iterator.hasNext()) {

					try {

						edge = iterator.next();

						vertexFrom = edge.outVertex();
						vertexTo = edge.inVertex();

						BaseConfiguration conf = new BaseConfiguration();
						conf.setProperty("storage.backend", "inmemory");
						TitanGraph openGraph = Utils.openGraph(conf);

						TitanVertex addVertexFrom = openGraph.addVertex();
						Utils.setProperties(addVertexFrom, Utils.getProperties(vertexFrom));

						TitanVertex addVertexTo = openGraph.addVertex();
						Utils.setProperties(addVertexTo, Utils.getProperties(vertexTo));

						Edge addEdge = addVertexFrom.addEdge(edge.label(), addVertexTo);
						Utils.setProperties(addEdge, Utils.getProperties(edge));

						log.info("fromVertex={}", Utils.getProperties(vertexFrom));
						log.info("toVertex={}", Utils.getProperties(vertexTo));
						log.info("edge={} {} ",edge.label(),Utils.getProperties(edge));

						GraphSONWriter create = GraphSONWriter.build().create();
						create.writeGraph(out, openGraph);

						openGraph.tx().rollback();

					} catch (Exception e) {
						log.info("run Edge Scan failed - {}" , e);

						log.error("fromVertex={}", Utils.getProperties(vertexFrom));
						log.error("toVertex={}", Utils.getProperties(vertexTo));
						log.error("edge={} {} ",edge.label(),Utils.getProperties(edge));

						break;

					}
				}

				graph.tx().rollback();

			}

			if (runVertexScan) {

				Vertex vertex = null;
				Iterator<Vertex> iteratorVertex = graph.vertices();
				while (iteratorVertex.hasNext()) {

					try {

						vertex = iteratorVertex.next();
						Iterator<Edge> iterator2 = vertex.edges(Direction.BOTH);
						if (false == iterator2.hasNext()) {

							BaseConfiguration conf = new BaseConfiguration();
							conf.setProperty("storage.backend", "inmemory");
							TitanGraph openGraph = Utils.openGraph(conf);

							TitanVertex addVertexFrom = openGraph.addVertex();
							Utils.setProperties(addVertexFrom, Utils.getProperties(vertex));

							log.info("fromVertex={}", Utils.getProperties(addVertexFrom));

							GraphSONWriter create = GraphSONWriter.build().create();
							create.writeGraph(out, openGraph);

							openGraph.tx().rollback();

						}

					} catch (Exception e) {
						log.info("run Vertex Scan failed - {}" , e);

						Object property1 = vertex.value(GraphPropertiesDictionary.HEALTH_CHECK.getProperty());
						System.out.println(property1);

						Object property2 = vertex.value("healthcheck");
						System.out.println(property2);

						break;

					}
				}

				graph.tx().rollback();

			}

			Iterable<TitanVertex> vertices2 = graph.query()
					.has(GraphPropertiesDictionary.HEALTH_CHECK.getProperty(), "GOOD").vertices();
			;

			BaseConfiguration conf = new BaseConfiguration();
			conf.setProperty("storage.backend", "inmemory");
			for (NodeTypeEnum nodeTypeEnum : NodeTypeEnum.values()) {
				removeNodesByLabel(graph, nodeTypeEnum.getName());
			}


			GraphSONWriter create = GraphSONWriter.build().create();
			create.writeGraph(out, graph);

			graph.tx().rollback();

		} catch (Exception e) {
			log.info("find Error In Json Graph failed - {}" , e);
			graph.tx().rollback();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				log.info("close FileOutputStream failed - {}" , e);
			}
		}
		return result;

	}

	private void removeNodesByLabel(TitanGraph graph, String label) {
		Iterable<TitanVertex> vertices = graph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), label)
				.vertices();
		Iterator<TitanVertex> iterator = vertices.iterator();
		while (iterator.hasNext()) {
			Vertex next2 = iterator.next();
			next2.remove();
		}
	}

	public String exportUsers(TitanGraph graph, String outputDirectory) {

		List<Map<String, Object>> users = new ArrayList<>();
		String result = null;

		String outputFile = outputDirectory + File.separator + "users." + System.currentTimeMillis() + ".json";

		FileWriter fileWriter = null;
		try {

			TitanGraphQuery graphQuery = graph.query().has(GraphPropertiesDictionary.LABEL.getProperty(),
					NodeTypeEnum.User.getName());

			@SuppressWarnings("unchecked")
			Iterable<TitanVertex> vertices = graphQuery.vertices();

			if (vertices != null) {
				for (Vertex v : vertices) {
					Map<String, Object> properties = getProperties(v);
					properties.remove(GraphPropertiesDictionary.LABEL.getProperty());
					users.add(properties);
				}
			}

			graph.tx().commit();

			String jsonUsers = gson.toJson(users);

			fileWriter = new FileWriter(outputFile);
			fileWriter.write(jsonUsers);

			result = outputFile;

		} catch (Exception e) {
			log.info("export Users failed - {}" , e);
			graph.tx().rollback();
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.close();
				}
			} catch (IOException e) {
				log.info("close FileOutputStream failed - {}" , e);
			}
		}
		return result;

	}

	public Map<String, Object> getProperties(Element element) {

		Map<String, Object> result = new HashMap<String, Object>();
		;

		if (element.keys() != null && element.keys().size() > 0) {
			Map<String, Property> propertyMap = ElementHelper.propertyMap(element,
					element.keys().toArray(new String[element.keys().size()]));

			for (Entry<String, Property> entry : propertyMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue().value();

				result.put(key, value);
			}
		}
		return result;
	}

	public boolean exportUsers(String[] args) {

		TitanGraph graph = null;
		try {
			String titanFileLocation = args[1];
			String outputDirectory = args[2];
			graph = openGraph(titanFileLocation);

			String result = exportUsers(graph, outputDirectory);

			if (result == null) {
				return false;
			}

			System.out.println("Exported file=" + result);
		} catch (Exception e) {
			log.info("export Users failed - {}" , e);
			return false;
		} finally {
			if (graph != null) {
				graph.close();
			}
		}

		return true;
	}
}
