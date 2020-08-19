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

package org.openecomp.sdc.ci.tests.utils;

import com.google.gson.*;
import org.janusgraph.core.JanusGraphEdge;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.users.UserAuditJavaObject;
import org.openecomp.sdc.ci.tests.utils.cassandra.CassandraUtils;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DbUtils {

	private static String janusGraphConfigFilePath;
	private static JanusGraph janusGraph;

	
	public static void cleanAllAudits() throws IOException {
		CassandraUtils.truncateAllTables("sdcaudit");
	}

	public static RestResponse deleteFromEsDbByPattern(String patternToDelete) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SEARCH_DATA_FROM_ES, config.getEsHost(), config.getEsPort(),
				patternToDelete);
		HttpRequest httpRequest = new HttpRequest();
		RestResponse restResponse = httpRequest.httpSendDelete(url, null);
		restResponse.getErrorCode();
		cleanAllAudits();

		return restResponse;
	}

	public static RestResponse getFromEsByPattern(String patternToGet) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SEARCH_DATA_FROM_ES, config.getEsHost(), config.getEsPort(), patternToGet);
		HttpRequest httpRequest = new HttpRequest();
		RestResponse restResponse = httpRequest.httpSendGet(url, null);
		restResponse.getErrorCode();

		return restResponse;
	}

	public Either<Vertex, Boolean> getVertexByUId(String uid) {
		JanusGraph janusGraph = getJanusGraph();
		Either<Vertex, Boolean> result = Either.right(false);
		// Iterator<Vertex> vertexItr = janusGraph.getVertices().iterator();

		Iterator<JanusGraphVertex> vertexItr = janusGraph.query().vertices().iterator();
		while (vertexItr.hasNext()) {
			Vertex vertex = vertexItr.next();
			// String uidFoundVal = vertex.getProperty("uid");
			String uidFoundVal = vertex.value("uid");
			if (uid.equals(uidFoundVal)) {
				result = Either.left(vertex);
			}
		}
		return result;
	}

	public static JanusGraphState getCurrentJanusGraphState() {
		JanusGraph janusGraph = getJanusGraph();
		List<Vertex> vertices = new ArrayList<>();
		List<Edge> edges = new ArrayList<>();
		// Iterator<Edge> edgesItr = janusGraph.getEdges().iterator();
		Iterator<JanusGraphEdge> edgesItr = janusGraph.query().edges().iterator();
		// Iterator<Vertex> verticesItr = janusGraph.getVertices().iterator();
		Iterator<JanusGraphVertex> verticesItr = janusGraph.query().vertices().iterator();
		while (edgesItr.hasNext()) {
			edges.add(edgesItr.next());
		}
		while (verticesItr.hasNext()) {
			vertices.add(verticesItr.next());
		}

		JanusGraphState currState = new JanusGraphState(edges, vertices);
		return currState;

	}

	//
	private static JanusGraph getJanusGraph() {
		if (janusGraph == null) {
			janusGraph = JanusGraphFactory.open(janusGraphConfigFilePath);
		}
		return janusGraph;
	}

	public void restoreToJanusGraphState(JanusGraphState janusGraphStateToRestoreTo) {
		List<Vertex> verticesToRemove = new ArrayList<>(), verticesToAdd = new ArrayList<>();
		List<Edge> edgesToRemove = new ArrayList<>(), edgesToAdd = new ArrayList<>();

		JanusGraphState currentJanusGraphState = getCurrentJanusGraphState();

		List<Edge> joinedEdges = new ArrayList<>();
		joinedEdges.addAll(janusGraphStateToRestoreTo.edges);
		joinedEdges.retainAll(currentJanusGraphState.edges);

		List<Vertex> joinedVertices = new ArrayList<>();
		joinedVertices.addAll(janusGraphStateToRestoreTo.vertices);
		joinedVertices.retainAll(currentJanusGraphState.vertices);

		edgesToRemove.addAll(currentJanusGraphState.edges);
		edgesToRemove.removeAll(joinedEdges);

		verticesToRemove.addAll(currentJanusGraphState.vertices);
		verticesToRemove.removeAll(joinedVertices);

		edgesToAdd.addAll(janusGraphStateToRestoreTo.edges);
		edgesToAdd.removeAll(joinedEdges);

		verticesToAdd.addAll(janusGraphStateToRestoreTo.vertices);
		verticesToAdd.removeAll(joinedVertices);

		modifyGraphAccordingToDelta(verticesToRemove, verticesToAdd, edgesToRemove, edgesToAdd);

	}

	private void modifyGraphAccordingToDelta(List<Vertex> verticesToRemove, List<Vertex> verticesToAdd,
			List<Edge> edgesToRemove, List<Edge> edgesToAdd) {

		JanusGraph janusGraph = getJanusGraph();

		for (Vertex vertex : verticesToRemove) {
			// janusGraph.removeVertex(vertex);
			vertex.remove();
		}
		for (Vertex vertex : verticesToAdd) {
			JanusGraphVertex janusGraphVertex = janusGraph.addVertex();
			copyProperties(vertex, janusGraphVertex);
		}

		for (Edge edge : edgesToRemove) {
			// janusGraph.removeEdge(edge);
			edge.remove();
		}

		for (Edge edge : edgesToAdd) {
			// Element addedEdge = janusGraph.addEdge(edge.getId(),
			// edge.getVertex(Direction.OUT), edge.getVertex(Direction.IN),
			// edge.getLabel());

			// Edge edge = tGraph.addEdge(null, fromV.left().value(),
			// toV.left().value(), type);

			Element addedEdge = edge.outVertex().addEdge(edge.label(), edge.inVertex());

			copyProperties(edge, addedEdge);

		}

		// janusGraph.commit();
		janusGraph.tx().commit();

	}

	private void copyProperties(Element copyFrom, Element copyTo) {
		// Set<String> properties = copyFrom.getPropertyKeys();
		Set<String> properties = copyFrom.keys();
		for (String propertyKey : properties) {
			// copyTo.setProperty(propertyKey,
			// copyFrom.getProperty(propertyKey));
			copyTo.property(propertyKey, copyFrom.value(propertyKey));
		}

	}

	public static class JanusGraphState {
		private List<Edge> edges;
		private List<Vertex> vertices;

		private JanusGraphState(List<Edge> edges, List<Vertex> vertices) {
			this.edges = edges;
			this.vertices = vertices;
		}

		@Override
		public String toString() {
			return "JanusGraphState [edges=" + edges.size() + ", vertices=" + vertices.size() + "]";
		}

	}

	public void shutDownJanusGraph() {
		if (janusGraph != null) {
			// janusGraph.shutdown();
			janusGraph.close();
		}
	}

	public static void setProperties(Element element, Map<String, Object> properties) {

		if (properties != null && false == properties.isEmpty()) {

			Object[] propertyKeyValues = new Object[properties.size() * 2];
			int i = 0;
			for (Entry<String, Object> entry : properties.entrySet()) {
				propertyKeyValues[i++] = entry.getKey();
				propertyKeyValues[i++] = entry.getValue();
			}

			ElementHelper.attachProperties(element, propertyKeyValues);

		}

	}

	public static UserAuditJavaObject parseAuditRespByAction(String action) throws Exception {

		// String index = "auditingevents*";
		// String type = "useradminevent";
		// String pattern = "/_search?q=action:\""+action+"\"";
		// String auditingMessage = retrieveAuditMessageByIndexType(index, type,
		// pattern);
		UserAuditJavaObject auditParsedResp = new UserAuditJavaObject();
		Gson gson = new Gson();

		String pattern = "/_search?q=ACTION:\"" + action + "\"";
		String auditingMessage = retrieveAuditMessagesByPattern(pattern);
		JsonElement jElement = new JsonParser().parse(auditingMessage);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonObject hitsObject = (JsonObject) jObject.get("hits");
		JsonArray hitsArray = (JsonArray) hitsObject.get("hits");
		// for (int i = 0; i < hitsArray.size();){
		if (hitsArray.size() == 0) {
			return auditParsedResp;
		}
		JsonObject jHitObject = (JsonObject) hitsArray.get(0);
		JsonObject jSourceObject = (JsonObject) jHitObject.get("_source");

		auditParsedResp = gson.fromJson(jSourceObject, UserAuditJavaObject.class);		

		return auditParsedResp;

	}

	public static String retrieveAuditMessagesByPattern(String pattern) throws IOException {

		Config config = Utils.getConfig();
		HttpRequest getAuditingMessage = new HttpRequest();
		String url = String.format(Urls.GET_SEARCH_DATA_FROM_ES, config.getEsHost(), config.getEsPort(), pattern);
		RestResponse restResponse = getAuditingMessage.httpSendGet(url, null);

		return restResponse.getResponse();
	}
}
