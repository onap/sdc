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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public class DbUtils {

	private static String titanConfigFilePath;
	private static TitanGraph titanGraph;

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
		TitanGraph titanGraph = getTitanGraph();
		Either<Vertex, Boolean> result = Either.right(false);
		// Iterator<Vertex> vertexItr = titanGraph.getVertices().iterator();

		Iterator<TitanVertex> vertexItr = titanGraph.query().vertices().iterator();
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

	public static TitanState getCurrentTitanState() {
		TitanGraph titanGraph = getTitanGraph();
		List<Vertex> vertices = new ArrayList<>();
		List<Edge> edges = new ArrayList<>();
		// Iterator<Edge> edgesItr = titanGraph.getEdges().iterator();
		Iterator<TitanEdge> edgesItr = titanGraph.query().edges().iterator();
		// Iterator<Vertex> verticesItr = titanGraph.getVertices().iterator();
		Iterator<TitanVertex> verticesItr = titanGraph.query().vertices().iterator();
		while (edgesItr.hasNext()) {
			edges.add(edgesItr.next());
		}
		while (verticesItr.hasNext()) {
			vertices.add(verticesItr.next());
		}

		TitanState currState = new TitanState(edges, vertices);
		return currState;

	}

	private static TitanGraph getTitanGraph() {
		if (titanGraph == null) {
			titanGraph = TitanFactory.open(titanConfigFilePath);
		}
		return titanGraph;
	}

	public void restoreToTitanState(TitanState titanStateToRestoreTo) {
		List<Vertex> verticesToRemove = new ArrayList<>(), verticesToAdd = new ArrayList<>();
		List<Edge> edgesToRemove = new ArrayList<>(), edgesToAdd = new ArrayList<>();

		TitanState currentTitanState = getCurrentTitanState();

		List<Edge> joinedEdges = new ArrayList<>();
		joinedEdges.addAll(titanStateToRestoreTo.edges);
		joinedEdges.retainAll(currentTitanState.edges);

		List<Vertex> joinedVertices = new ArrayList<>();
		joinedVertices.addAll(titanStateToRestoreTo.vertices);
		joinedVertices.retainAll(currentTitanState.vertices);

		edgesToRemove.addAll(currentTitanState.edges);
		edgesToRemove.removeAll(joinedEdges);

		verticesToRemove.addAll(currentTitanState.vertices);
		verticesToRemove.removeAll(joinedVertices);

		edgesToAdd.addAll(titanStateToRestoreTo.edges);
		edgesToAdd.removeAll(joinedEdges);

		verticesToAdd.addAll(titanStateToRestoreTo.vertices);
		verticesToAdd.removeAll(joinedVertices);

		modifyGraphAccordingToDelta(verticesToRemove, verticesToAdd, edgesToRemove, edgesToAdd);

	}

	private void modifyGraphAccordingToDelta(List<Vertex> verticesToRemove, List<Vertex> verticesToAdd,
			List<Edge> edgesToRemove, List<Edge> edgesToAdd) {

		TitanGraph titanGraph = getTitanGraph();

		for (Vertex vertex : verticesToRemove) {
			// titanGraph.removeVertex(vertex);
			vertex.remove();
		}
		for (Vertex vertex : verticesToAdd) {
			TitanVertex titanVertex = titanGraph.addVertex();
			copyProperties(vertex, titanVertex);
		}

		for (Edge edge : edgesToRemove) {
			// titanGraph.removeEdge(edge);
			edge.remove();
		}

		for (Edge edge : edgesToAdd) {
			// Element addedEdge = titanGraph.addEdge(edge.getId(),
			// edge.getVertex(Direction.OUT), edge.getVertex(Direction.IN),
			// edge.getLabel());

			// Edge edge = tGraph.addEdge(null, fromV.left().value(),
			// toV.left().value(), type);

			Element addedEdge = edge.outVertex().addEdge(edge.label(), edge.inVertex());

			copyProperties(edge, addedEdge);

		}

		// titanGraph.commit();
		titanGraph.tx().commit();

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

	public static class TitanState {
		private List<Edge> edges;
		private List<Vertex> vertices;

		private TitanState(List<Edge> edges, List<Vertex> vertices) {
			this.edges = edges;
			this.vertices = vertices;
		}

		@Override
		public String toString() {
			return "TitanState [edges=" + edges.size() + ", vertices=" + vertices.size() + "]";
		}

	}

	public void shutDowntitan() {
		if (titanGraph != null) {
			// titanGraph.shutdown();
			titanGraph.close();
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
