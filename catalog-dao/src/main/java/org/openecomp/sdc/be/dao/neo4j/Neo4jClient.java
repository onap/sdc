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

package org.openecomp.sdc.be.dao.neo4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.filters.MatchFilter;
import org.openecomp.sdc.be.dao.neo4j.filters.RecursiveFilter;
import org.openecomp.sdc.be.dao.neo4j.filters.UpdateFilter;
import org.openecomp.sdc.be.dao.utils.DaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

//@Component("neo4j-client")
public class Neo4jClient {
	private CloseableHttpClient httpClient;
	private JSONParser jsonParser;

	private CypherTranslator cypherTranslator;

	private static Logger logger = LoggerFactory.getLogger(Neo4jClient.class.getName());

	private static final String getServiceRoot = "http://$host$:$port$/db/data/";
	// Error's Classification templates
	private static final String ClientError = "ClientError";
	private static final String DatabaseError = "DatabaseError";
	private static final String TransientError = "TransientError";

	// Error's Category templates
	private static final String General = "General";
	private static final String LegacyIndex = "LegacyIndex";
	private static final String Request = "Request";
	private static final String Schema = "Schema";
	private static final String Security = "Security";
	private static final String Statement = "Statement";
	private static final String Transaction = "Transaction";

	// Error's Title templates
	private static final String EntityNotFound = "EntityNotFound";
	private static final String ConstraintViolation = "ConstraintViolation";

	@PostConstruct
	public void init() {

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(100);
		connectionManager.setDefaultMaxPerRoute(20);
		connectionManager.setValidateAfterInactivity(15000);
		this.httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		jsonParser = new JSONParser();
		cypherTranslator = new CypherTranslator();

	}

	@PreDestroy
	public void shutdown() {
		try {
			httpClient.close();
			logger.debug("Http client to Neo4j Graph closed");
		} catch (Exception e) {
			logger.info("Failed to close http client", e);
		}
	}

	/**
	 * 
	 * @param builder
	 * @return
	 */
	public Either<List<List<GraphElement>>, Neo4jOperationStatus> execute(BatchBuilder builder) {

		String json = cypherTranslator.translate(builder);
		logger.debug("Try to execute cypher request [{}]", json);

		Either<String, Neo4jOperationStatus> result = sendPostCypher(json);
		if (result.isRight()) {
			return Either.right(result.right().value());
		}
		List<List<GraphElement>> batchResult;
		try {
			batchResult = parseResult(result.left().value(), false);
		} catch (ParseException e) {
			logger.error("Failed to parse batchresponse", e);
			return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
		}

		return Either.left(batchResult);
	}

	public Either<List<List<GraphElement>>, Neo4jOperationStatus> executeGet(RecursiveFilter filter) {
		String json = cypherTranslator.translateGet(filter);
		logger.debug("Try to execute cypher request [{}]", json);

		Either<String, Neo4jOperationStatus> result = sendPostCypher(json);
		if (result.isRight()) {
			return Either.right(result.right().value());
		}
		List<List<GraphElement>> batchResult;
		try {
			batchResult = parseResult(result.left().value(), true);
		} catch (ParseException e) {
			logger.error("Failed to parse batchresponse", e);
			return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
		}

		return Either.left(batchResult);

	}

	/**
	 * 
	 * @param element
	 * @param ip
	 * @param user
	 * @param password
	 * @return
	 */
	public Neo4jOperationStatus createElement(GraphElement element) {
		Neo4jOperationStatus result = Neo4jOperationStatus.OK;
		switch (element.getElementType()) {
		case Node:
			Either<String, Neo4jOperationStatus> status = createNode(element);
			if (status.isRight()) {
				result = status.right().value();
			}
			break;
		case Relationship:
			// TODO
			break;

		default:
			break;
		}

		return result;
	}

	public Either<GraphElement, Neo4jOperationStatus> createSingleElement(GraphElement element) {
		switch (element.getElementType()) {
		case Node:
			Either<String, Neo4jOperationStatus> status = createNode(element);
			if (status.isRight()) {
				return Either.right(status.right().value());
			}
			// parse response
			String response = status.left().value();
			try {
				List<GraphElement> listElements = parseGetResponse(element.getElementType(),
						((GraphNode) element).getLabel(), response);
				if (listElements == null || listElements.isEmpty()) {
					return Either.right(Neo4jOperationStatus.NOT_FOUND);
				} else {
					return Either.left(listElements.get(0));
				}
			} catch (Exception e) {
				logger.error("Failed to parse fetched data from graph", e);
				return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
			}
		case Relationship:
			// TODO
			break;

		default:
			break;
		}

		return Either.right(Neo4jOperationStatus.NOT_SUPPORTED);
	}

	/**
	 * 
	 * @param type
	 * @param label
	 * @param filter
	 * @param ip
	 * @param user
	 * @param password
	 * @return
	 */
	public Either<List<GraphElement>, Neo4jOperationStatus> getByFilter(GraphElementTypeEnum type, String label,
			MatchFilter filter) {

		List<GraphElement> result = null;

		String requestJson;
		// replace return type
		if (type.equals(GraphElementTypeEnum.Node)) {
			requestJson = CypherTemplates.CypherMatchTemplate.replace("$type$", "n");
		} else {
			requestJson = CypherTemplates.CypherMatchTemplate.replace("$type$", "r");
		}
		// replace label
		if (label != null && !label.isEmpty()) {
			requestJson = requestJson.replace("$label$", label);
		} else {
			requestJson = requestJson.replace("$label$", "");
		}

		// replace filter
		if (filter.getProperties().isEmpty()) {
			// get all records by label
			requestJson = requestJson.replace("{$filter$}", "");
		} else {
			String filterStr = CypherTranslator.prepareFilterBody(filter);
			requestJson = requestJson.replace("$filter$", filterStr);
		}
		logger.debug("Try to perform request []", requestJson);

		Either<String, Neo4jOperationStatus> status = sendPostCypher(requestJson);
		if (status.isRight()) {
			return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
		}
		// parse response
		String response = status.left().value();
		try {
			result = parseGetResponse(type, label, response);
		} catch (Exception e) {
			logger.error("Failed to parse fetched data from graph", e);
			Either.right(Neo4jOperationStatus.GENERAL_ERROR);
		}

		return Either.left(result);
	}

	/**
	 * 
	 * @param type
	 * @param label
	 * @param toMatch
	 * @param toUpdate
	 * @param ip
	 * @param user
	 * @param password
	 * @return
	 */
	public Neo4jOperationStatus updateElement(GraphElementTypeEnum type, String label, UpdateFilter toUpdate) {

		String requestJson;
		// replace return type
		if (type.equals(GraphElementTypeEnum.Node)) {
			requestJson = CypherTemplates.CypherUpdateTemplate.replace("$type$", "n");
		} else {
			requestJson = CypherTemplates.CypherUpdateTemplate.replace("$type$", "r");
		}
		// replace label
		if (label != null && !label.isEmpty()) {
			requestJson = requestJson.replace("$label$", label);
		} else {
			requestJson = requestJson.replace("$label$", "");
		}

		// replace filter
		if (toUpdate.getProperties().isEmpty()) {
			// get all records by label
			requestJson = requestJson.replace("{$filter$}", "");
		} else {
			String filterStr = CypherTranslator.prepareFilterBody(toUpdate);
			requestJson = requestJson.replace("$filter$", filterStr);
		}
		String props = preparePropertiesInStatement(toUpdate.getToUpdate());
		requestJson = requestJson.replace("$props$", props);

		logger.debug("Try to perform request [{}]", requestJson);

		Either<String, Neo4jOperationStatus> result = sendPostCypher(requestJson);
		if (result.isRight()) {
			return Neo4jOperationStatus.GENERAL_ERROR;
		}
		return Neo4jOperationStatus.OK;
	}

	/**
	 * 
	 * @param type
	 * @param label
	 * @param response
	 * @return
	 * @throws ParseException
	 */

	private List<GraphElement> parseGetResponse(GraphElementTypeEnum type, String label, String response)
			throws ParseException {
		List<GraphElement> result = new ArrayList<GraphElement>();
		JSONObject responseData = (JSONObject) jsonParser.parse(response);
		JSONArray results = (JSONArray) responseData.get("results");
		Iterator<JSONObject> iteratorResults = results.iterator();
		while (iteratorResults.hasNext()) {
			JSONObject elementResult = iteratorResults.next();
			// JSONArray data = (JSONArray) elementResult.get("row");
			JSONArray data = (JSONArray) elementResult.get("data");

			Iterator<JSONObject> iterator = data.iterator();
			JSONObject element;
			while (iterator.hasNext()) {
				element = (JSONObject) iterator.next();
				JSONArray row = (JSONArray) element.get("row");

				Iterator<JSONObject> iteratorRow = row.iterator();
				while (iteratorRow.hasNext()) {
					JSONObject rowElement = iteratorRow.next();

					Map<String, Object> props = new HashMap<String, Object>();

					for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) rowElement.entrySet()) {
						// props.put(entry.getKey(),
						// rowElement.get(entry.getValue()));
						props.put(entry.getKey(), entry.getValue());
					}
					GraphElement newElement = GraphElementFactory.createElement(label, type, props);
					result.add(newElement);
				}
			}
		}
		return result;
	}

	private List<List<GraphElement>> parseResult(String response, boolean storeRelationNode) throws ParseException {

		List<List<GraphElement>> batchList = new ArrayList<List<GraphElement>>();

		JSONObject responseData = (JSONObject) jsonParser.parse(response);
		JSONArray results = (JSONArray) responseData.get("results");
		Iterator<JSONObject> iteratorResults = results.iterator();

		while (iteratorResults.hasNext()) {
			JSONObject elementResult = iteratorResults.next();
			JSONArray data = (JSONArray) elementResult.get("data");
			JSONArray columns = (JSONArray) elementResult.get("columns");
			Iterator<JSONObject> iteratorData = data.iterator();
			List<GraphElement> singleDataList = new ArrayList<GraphElement>();
			while (iteratorData.hasNext()) {

				JSONObject singleData = iteratorData.next();
				JSONArray row = (JSONArray) singleData.get("row");
				if (columns.size() == 2) {
					// node
					JSONArray labelArray = (JSONArray) row.get(1);
					JSONObject node = (JSONObject) row.get(0);

					Map<String, Object> props = jsonObjectToMap(node);
					// get only first label on node. Now single label supported
					GraphElement newElement = GraphElementFactory.createElement((String) labelArray.get(0),
							GraphElementTypeEnum.Node, props);
					singleDataList.add(newElement);
				}
				if (columns.size() == 10) {
					// relation
					JSONObject startNode = (JSONObject) row.get(0);
					JSONArray startNodeArray = (JSONArray) row.get(1);

					JSONObject relationFromStart = (JSONObject) row.get(2);
					String relationFromStartType = (String) row.get(3);

					JSONObject nodeFrom = (JSONObject) row.get(4);
					JSONArray labelFromArray = (JSONArray) row.get(5);

					JSONObject nodeTo = (JSONObject) row.get(6);
					JSONArray labelToArray = (JSONArray) row.get(7);

					JSONObject relation = (JSONObject) row.get(8);
					String type = (String) row.get(9);

					Map<String, Object> propsStartNode = jsonObjectToMap(startNode);
					Map<String, Object> propsRelationStartNode = jsonObjectToMap(relationFromStart);

					Map<String, Object> propsFrom = jsonObjectToMap(nodeFrom);
					Map<String, Object> propsTo = jsonObjectToMap(nodeTo);
					Map<String, Object> propsRelation = jsonObjectToMap(relation);

					GraphNode startN = (GraphNode) GraphElementFactory.createElement((String) startNodeArray.get(0),
							GraphElementTypeEnum.Node, propsStartNode);

					GraphNode from = (GraphNode) GraphElementFactory.createElement((String) labelFromArray.get(0),
							GraphElementTypeEnum.Node, propsFrom);
					GraphNode to = (GraphNode) GraphElementFactory.createElement((String) labelToArray.get(0),
							GraphElementTypeEnum.Node, propsTo);

					singleDataList.add(startN);

					GraphElement relationFromStartNode = GraphElementFactory.createRelation(type,
							propsRelationStartNode, startN, from);
					singleDataList.add(relationFromStartNode);

					singleDataList.add(from);
					singleDataList.add(to);
					// get only first type on relationship. Now single type
					// supported
					GraphElement newElement = GraphElementFactory.createRelation(type, propsRelation, from, to);
					singleDataList.add(newElement);
				}
				if (columns.size() == 8) {

				}
			}
			batchList.add(singleDataList);
		}
		return batchList;
	}

	private Map<String, Object> jsonObjectToMap(JSONObject node) {
		Map<String, Object> props = new HashMap<String, Object>();

		for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) node.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		return props;
	}

	private String preparePropertiesInStatement(Map<String, Object> properties) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		int size = properties.entrySet().size();
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			sb.append("\"").append(entry.getKey()).append("\"").append(":");
			if (entry.getValue() instanceof String) {
				sb.append("\"");
			}
			sb.append(entry.getValue());
			if (entry.getValue() instanceof String) {
				sb.append("\"");
			}
			++count;
			if (count < size) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private Either<String, Neo4jOperationStatus> createNode(GraphElement element) {
		Either<String, Neo4jOperationStatus> status;
		if (element instanceof GraphNode) {
			GraphNode node = (GraphNode) element;
			String json = prepareCreateNodeBody(node);

			logger.debug("Try to save Node [{}] on graph", json);

			status = sendPostCypher(json);

			return status;

		} else {
			return Either.right(Neo4jOperationStatus.WRONG_INPUT);
		}
	}

	private Either<String, Neo4jOperationStatus> sendPostCypher(String json) {
		Map<String, Object> neo4jParams = ConfigurationManager.getConfigurationManager().getConfiguration().getNeo4j();
		String host = (String) neo4jParams.get("host");
		Integer port = (Integer) neo4jParams.get("port");
		String user = (String) neo4jParams.get("user");
		String password = (String) neo4jParams.get("password");

		String uri = CypherTemplates.CypherUrlTemplate.replace("$host$", host);
		uri = uri.replace("$port$", port.toString());

		HttpClientContext context = creatClientContext(host, user, password);
		CloseableHttpResponse response = null;

		HttpPost post = new HttpPost(uri);
		try {
			StringEntity input = new StringEntity(json);
			input.setContentType("application/json");
			post.setEntity(input);

			response = httpClient.execute(post, context);

			int status = response.getStatusLine().getStatusCode();
			String responseString;
			responseString = new BasicResponseHandler().handleResponse(response);
			logger.debug("response [{}]", responseString);

			if (status == 200 || status == 201) {
				logger.debug("cypher request [{}] was succeeded", json);
				Neo4jOperationStatus responseStatus = checkResponse(responseString);
				if (Neo4jOperationStatus.OK.equals(responseStatus)) {
					return Either.left(responseString);
				} else {
					return Either.right(responseStatus);
				}
			} else {
				logger.debug("cypher request [{}] was failed : [{}]", json, responseString);
				return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
			}

		} catch (HttpResponseException e) {
			logger.debug("failed to perform cypher request [{}]", json, e);
			if (e.getStatusCode() == 401) {
				return Either.right(Neo4jOperationStatus.NOT_AUTHORIZED);
			} else {
				return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
			}
		} catch (ClientProtocolException e) {
			logger.debug("failed to perform cypher request [{}]", json, e);
			return Either.right(Neo4jOperationStatus.HTTP_PROTOCOL_ERROR);
		} catch (IOException e) {
			logger.debug("failed to perform cypher request [{}]", json, e);
			return Either.right(Neo4jOperationStatus.NOT_CONNECTED);
		} finally {
			releaseResource(response);
		}
	}

	private Neo4jOperationStatus checkResponse(String responseString) {
		try {
			JSONObject response = (JSONObject) jsonParser.parse(responseString);
			JSONArray errors = (JSONArray) response.get("errors");
			if (errors.size() == 0) {
				return Neo4jOperationStatus.OK;
			} else {
				Iterator<JSONObject> iterator = errors.iterator();
				JSONObject error;
				while (iterator.hasNext()) {
					error = (JSONObject) iterator.next();
					String code = (String) error.get("code");
					String message = (String) error.get("message");

					Neo4jOperationStatus neoError = mapToNeoError(code, message);
					return neoError;
				}
				return Neo4jOperationStatus.GENERAL_ERROR;
			}
		} catch (ParseException e) {
			logger.error("Failed to parse response", e);
			return Neo4jOperationStatus.GENERAL_ERROR;
		}
	}

	private Neo4jOperationStatus mapToNeoError(String code, String message) {
		Neo4jOperationStatus error;

		String[] errorCode = code.split("\\.");
		if (errorCode.length < 4) {
			error = Neo4jOperationStatus.GENERAL_ERROR;
		} else {
			// by Classification
			switch (errorCode[1]) {
			case ClientError:
				// by Category
				switch (errorCode[2]) {
				case General:
					error = Neo4jOperationStatus.DB_READ_ONLY;
					break;
				case LegacyIndex:
					error = Neo4jOperationStatus.LEGACY_INDEX_ERROR;
					break;
				case Request:
					error = Neo4jOperationStatus.BAD_REQUEST;
					break;
				case Schema:
					if (errorCode[3].equals(ConstraintViolation)) {
						error = Neo4jOperationStatus.ENTITY_ALREADY_EXIST;
					} else {
						error = Neo4jOperationStatus.SCHEMA_ERROR;
					}
					break;
				case Security:
					error = Neo4jOperationStatus.NOT_AUTHORIZED;
					break;
				case Statement:
					// by Title
					if (errorCode[3].equals(EntityNotFound)) {
						error = Neo4jOperationStatus.NOT_FOUND;
					} else {
						if (errorCode[3].equals(ConstraintViolation)) {
							error = Neo4jOperationStatus.ENTITY_ALREADY_EXIST;
						} else {
							error = Neo4jOperationStatus.BAD_REQUEST;
						}
					}
					break;
				case Transaction:
					error = Neo4jOperationStatus.TRANSACTION_ERROR;
					break;
				default:
					error = Neo4jOperationStatus.GENERAL_ERROR;
					break;
				}
				break;
			case DatabaseError:
				// by Category
				switch (errorCode[2]) {
				case General:
					error = Neo4jOperationStatus.GENERAL_ERROR;
					break;
				case Schema:
					error = Neo4jOperationStatus.SCHEMA_ERROR;
					break;
				case Statement:
					error = Neo4jOperationStatus.EXECUTION_FAILED;
					break;
				case Transaction:
					error = Neo4jOperationStatus.TRANSACTION_ERROR;
					break;
				default:
					error = Neo4jOperationStatus.GENERAL_ERROR;
					break;
				}
				break;
			case TransientError:
				error = Neo4jOperationStatus.DB_NOT_AVAILABLE;
				break;
			default:
				error = Neo4jOperationStatus.GENERAL_ERROR;
				break;
			}
			error.setOriginError(code).setMessage(message);
			String errorFromCfg = code.replace(".", "_");
			String helpMessage = ConfigurationManager.getConfigurationManager().getNeo4jErrorsConfiguration()
					.getErrorMessage(errorFromCfg);
			if (helpMessage != null && !helpMessage.isEmpty()) {
				error.setHelpErrorMsg(helpMessage);
			}
		}
		return error;
	}

	private String prepareCreateNodeBody(GraphNode node) {

		String body = CypherTemplates.CypherCreateNodeTemplate.replace("$label$", node.getLabel());

		body = body.replace("$props$", DaoUtils.convertToJson(node.toGraphMap()));

		return body;
	}

	/**
	 * the method returns all the indexes for the given label if no label is
	 * supplied ( null or "") all indexes will be returned
	 * 
	 * @param label
	 *            the name of the label
	 * @param ip
	 * @param user
	 * @param password
	 * @return a map of labels and there properties
	 */
	public Either<Map<String, List<String>>, Neo4jOperationStatus> getIndexes(String label) {
		Map<String, Object> neo4jParams = ConfigurationManager.getConfigurationManager().getConfiguration().getNeo4j();
		String host = (String) neo4jParams.get("host");
		Integer port = (Integer) neo4jParams.get("port");
		String user = (String) neo4jParams.get("user");
		String password = (String) neo4jParams.get("password");

		String uri = null;
		if (label == null || "".equals(label)) {
			uri = CypherTemplates.getAllIndexsTemplate.replace("$host$", host);
		} else {
			uri = CypherTemplates.getAllIndexsTemplate.replace("$host$", host) + "/" + label;
		}
		uri = uri.replace("$port$", port.toString());

		HttpClientContext context = creatClientContext(host, user, password);
		CloseableHttpResponse response = null;

		HttpGet get = new HttpGet(uri);
		get.setHeader("Content-Type", "application/json");
		get.setHeader("Accept", "application/json; charset=UTF-8");

		try {

			response = httpClient.execute(get, context);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				logger.error("failed to get indexes requeste returned {}", statusCode);
				return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
			} else {
				Map<String, List<String>> labels = getLeablesFromJson(response);
				return Either.left(labels);
			}
		} catch (Exception e) {
			logger.debug("failed to get indexes ", e);
			return Either.right(Neo4jOperationStatus.GENERAL_ERROR);
		} finally {
			releaseResource(response);

		}

	}

	private Map<String, List<String>> getLeablesFromJson(CloseableHttpResponse response)
			throws HttpResponseException, IOException, ParseException {
		Map<String, List<String>> labels = new HashMap<>();
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONArray results = (JSONArray) jsonParser.parse(responseString);
		Iterator<JSONObject> iteratorResults = results.iterator();
		while (iteratorResults.hasNext()) {
			JSONObject elementResult = iteratorResults.next();
			String label = (String) elementResult.get("label");
			List<String> props = labels.get(label);
			if (props == null) {
				props = new ArrayList<>();
				labels.put(label, props);
			}
			JSONArray properties = (JSONArray) elementResult.get("property_keys");
			Iterator<String> iterator = properties.iterator();
			while (iterator.hasNext()) {
				props.add(iterator.next());
			}
		}
		return labels;
	}

	public Neo4jOperationStatus createIndex(String label, List<String> propertyNames) {

		Neo4jOperationStatus result = Neo4jOperationStatus.OK;
		if (propertyNames != null && !propertyNames.isEmpty()) {

			Map<String, Object> neo4jParams = ConfigurationManager.getConfigurationManager().getConfiguration()
					.getNeo4j();
			String host = (String) neo4jParams.get("host");
			Integer port = (Integer) neo4jParams.get("port");
			String user = (String) neo4jParams.get("user");
			String password = (String) neo4jParams.get("password");

			String uri = CypherTemplates.batchTemplate.replace("$host$", host);
			uri = uri.replace("$port$", port.toString());

			String opertionUri = "/schema/index/" + label;

			HttpClientContext context = creatClientContext(host, user, password);

			CloseableHttpResponse response = null;

			HttpPost post = new HttpPost(uri);

			String json = createBatchJson(HttpMethod.POST, opertionUri, propertyNames);

			try {
				StringEntity input = new StringEntity(json);
				input.setContentType("application/json");
				post.setEntity(input);
				response = httpClient.execute(post, context);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != 200) {
					logger.error("failed to create index for label [{}] with properties:{} requeste returned {}",label,propertyNames,statusCode);
					result = Neo4jOperationStatus.GENERAL_ERROR;
				} else {
					logger.debug("index for label [{}] with properties: {} created", label, propertyNames);
				}
			} catch (Exception e) {
				logger.debug("failed to create index for label [{}] with properties: {}", label, propertyNames);
				result = Neo4jOperationStatus.GENERAL_ERROR;
			} finally {

				releaseResource(response);

			}

		}

		else {
			logger.debug("no index was created for label :{} the recived propertyNames list: {} is invalide",label,propertyNames);
			return Neo4jOperationStatus.WRONG_INPUT;
		}

		return result;
	}

	public Neo4jOperationStatus createUniquenessConstraints(String label, List<String> propertyNames) {
		Neo4jOperationStatus result = Neo4jOperationStatus.OK;
		if (propertyNames != null && !propertyNames.isEmpty()) {

			Map<String, Object> neo4jParams = ConfigurationManager.getConfigurationManager().getConfiguration()
					.getNeo4j();
			String host = (String) neo4jParams.get("host");
			Integer port = (Integer) neo4jParams.get("port");
			String user = (String) neo4jParams.get("user");
			String password = (String) neo4jParams.get("password");

			String uri = CypherTemplates.batchTemplate.replace("$host$", host);
			uri = uri.replace("$port$", port.toString());

			String opertionUri = "/schema/constraint/" + label + "/uniqueness/";

			HttpClientContext context = creatClientContext(host, user, password);

			CloseableHttpResponse response = null;

			HttpPost post = new HttpPost(uri);

			String json = createBatchJson(HttpMethod.POST, opertionUri, propertyNames);

			try {
				StringEntity input = new StringEntity(json);
				input.setContentType("application/json");
				post.setEntity(input);
				response = httpClient.execute(post, context);

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != 200) {
					logger.error("failed to create uniqueness constraint  for label [{}] on properties:{}. request returned ",
							label,propertyNames,statusCode);
					result = Neo4jOperationStatus.GENERAL_ERROR;
				} else {
					logger.debug("uniqueness constraint for label [{}] on properties:{} created",label,propertyNames);
				}
			} catch (Exception e) {
				logger.error("failed to create uniqueness constraint [{}] with properties:{}",label,propertyNames,e);
				result = Neo4jOperationStatus.GENERAL_ERROR;
			} finally {
				releaseResource(response);
			}

		}

		else {
			logger.debug("no index was created for label :{} the recived propertyNames list: {} is invalide",label,propertyNames);
			return Neo4jOperationStatus.WRONG_INPUT;
		}

		return result;
	}

	public Neo4jOperationStatus deleteElement(GraphElementTypeEnum type, String label, MatchFilter filter) {

		String requestJson;
		// replace return type
		if (type.equals(GraphElementTypeEnum.Node)) {
			logger.debug("removing node label: {}", label);
			requestJson = createDeleteNodeStatment(label, filter);

		} else {
			logger.error(" delete on type {} is not yet supported", type);
			throw new RuntimeException(" delete on type " + type + " is not yet supported");
		}

		logger.debug("Try to perform request [{}]", requestJson);

		Either<String, Neo4jOperationStatus> status = sendPostCypher(requestJson);
		if (status.isRight()) {
			logger.error(" delete request failed with: {}", status.right());
			return Neo4jOperationStatus.GENERAL_ERROR;
		} else {
			return Neo4jOperationStatus.OK;
		}
	}

	public String getNeo4jVersion() throws Exception {
		Map<String, Object> neo4jParams = ConfigurationManager.getConfigurationManager().getConfiguration().getNeo4j();
		String host = (String) neo4jParams.get("host");
		Integer port = (Integer) neo4jParams.get("port");
		String user = (String) neo4jParams.get("user");
		String password = (String) neo4jParams.get("password");

		String uri = getServiceRoot.replace("$host$", host).replace("$port$", port.toString());

		HttpClientContext context = creatClientContext(host, user, password);
		CloseableHttpResponse response = null;
		String result = null;

		HttpGet get = new HttpGet(uri);
		get.setHeader("Content-Type", "application/json");
		get.setHeader("Accept", "application/json; charset=UTF-8");

		try {
			response = httpClient.execute(get, context);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				throw new Exception("Couldn't get Neo4j service root, HTTP status " + statusCode);
			} else {
				// Parse response
				String responseString = new BasicResponseHandler().handleResponse(response);
				JSONObject responseData = (JSONObject) jsonParser.parse(responseString);
				Object obj = responseData.get("neo4j_version");
				if (obj != null) {
					result = (String) obj;
				}
				return result;
			}
		} finally {
			releaseResource(response);
		}
	}

	private String createDeleteNodeStatment(String label, MatchFilter filter) {
		String requestJson;
		requestJson = CypherTemplates.CypherDeleteNodeTemplate;

		if (label != null && !label.isEmpty()) {
			requestJson = requestJson.replace("$label$", label);
		} else {
			requestJson = requestJson.replace("$label$", "");
		}

		// replace filter
		if (filter.getProperties().isEmpty()) {
			// get all records by label
			requestJson = requestJson.replace("{$filter$}", "");
		} else {
			String filterStr = CypherTranslator.prepareFilterBody(filter);
			requestJson = requestJson.replace("$filter$", filterStr);
		}
		return requestJson;
	}

	/*
	 * removed do to fortify scan CredentialsProvider cp = new
	 * BasicCredentialsProvider(); cp.setCredentials(AuthScope.ANY, new
	 * UsernamePasswordCredentials(user, password)); AuthCache authCache = new
	 * BasicAuthCache(); BasicScheme basicAuth = new BasicScheme();
	 * authCache.put(new HttpHost(ip, 7474, "http"), basicAuth);
	 * context.setAuthCache(authCache); context.setCredentialsProvider(cp);
	 * 
	 */
	private HttpClientContext creatClientContext(String ip, String user, String password) {
		HttpClientContext context = HttpClientContext.create();

		return context;
	}

	private void releaseResource(CloseableHttpResponse response) {
		if (response != null) {
			try {
				HttpEntity entity = response.getEntity();
				EntityUtils.consume(entity);
				response.close();
			} catch (Exception e) {
				logger.error("failed to close connection exception", e);
			}
		}
	}

	private String createBatchJson(HttpMethod method, String opertionUri, List<String> propertyNames) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int i = 0; i < propertyNames.size(); i++) {
			sb.append("{ \"method\" : \"" + method + "\" , \"to\" : \"" + opertionUri
					+ "\" , \"body\" : { \"property_keys\" : [ \"" + propertyNames.get(i) + "\" ] } }");
			if (i + 1 < propertyNames.size()) {
				sb.append(",");
			}
		}
		sb.append(" ]");
		String json = sb.toString();
		return json;
	}

	enum HttpMethod {
		GET, PUT, POST, DELETE
	}

}
