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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElement;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.filters.MatchFilter;
import org.openecomp.sdc.be.dao.neo4j.filters.RecursiveByRelationFilter;
import org.openecomp.sdc.be.dao.neo4j.filters.RecursiveFilter;
import org.openecomp.sdc.be.dao.utils.DaoUtils;

public class CypherTranslator {

	public String translate(BatchBuilder builder) {
		String json = null;
		StringBuilder statementList = new StringBuilder();

		List<GraphElement> elements = builder.getElements();
		int statementCounter = 0;
		for (GraphElement element : elements) {
			String singleStatementBody = null;
			switch (element.getElementType()) {
			case Node:
				singleStatementBody = prepareNodeStatement(element);
				break;
			case Relationship:
				singleStatementBody = prepareRelationStatement(element);
				break;
			}
			if (singleStatementBody != null && !singleStatementBody.isEmpty()) {

				String singleStatement = CypherTemplates.RegularStatementTemplate.replace("$statement$",
						singleStatementBody);

				statementList.append(singleStatement);
			}
			++statementCounter;
			if (statementCounter < elements.size() && singleStatementBody != null) {
				statementList.append(",");
			}

		}
		json = CypherTemplates.BatchTemplate.replace("$statementList$", statementList.toString());
		return json;
	}

	private String prepareNodeStatement(GraphElement element) {
		if (element instanceof GraphNode) {
			GraphNode node = (GraphNode) element;

			switch (node.getAction()) {
			case Create:
				return createNodeStatement(node);
			case Update:
				return updateNodeStatement(node);
			case Delete:
				// TODO
				break;
			default:
				break;
			}
		}
		return null;
	}

	private String updateNodeStatement(GraphNode node) {
		String singleStatement = CypherTemplates.UpdateNodeStatementTemplate.replace("$label$", node.getLabel());
		String filter = prepareKeyValueFilter(node);

		singleStatement = singleStatement.replace("$filter$", filter);

		singleStatement = singleStatement.replace("$props$", DaoUtils.convertToJson(node.toGraphMap()));

		return singleStatement;
	}

	private String createNodeStatement(GraphNode node) {
		String singleStatement = CypherTemplates.CreateSingleNodeTemplate.replace("$label$", node.getLabel());

		singleStatement = singleStatement.replace("$props$", DaoUtils.convertToJson(node.toGraphMap()));
		return singleStatement;
	}

	private String prepareRelationStatement(GraphElement element) {
		if (element instanceof GraphRelation) {

			GraphRelation relation = (GraphRelation) element;

			switch (relation.getAction()) {
			case Create:
				return createRelationStatement(relation);
			case Update:
				return updateRelationStatement(relation);
			case Delete:
				// TODO
				break;
			default:
				break;
			}
		}
		return null;
	}

	private String createRelationStatement(GraphRelation relation) {
		RelationEndPoint from = relation.getFrom();
		String singleStatement;

		Map<String, Object> props = relation.toGraphMap();
		if (props == null || props.isEmpty()) {
			singleStatement = CypherTemplates.CreateRelationTemplateNoProps.replace("$labelFrom$",
					from.getLabel().getName());
		} else {
			singleStatement = CypherTemplates.CreateRelationTemplate.replace("$labelFrom$", from.getLabel().getName());
			singleStatement = singleStatement.replace("$props$", DaoUtils.convertToJson(props));
		}

		singleStatement = singleStatement.replace("$idNameFrom$", from.getIdName());
		singleStatement = singleStatement.replace("$idValueFrom$", from.getIdValue().toString());

		RelationEndPoint to = relation.getTo();
		singleStatement = singleStatement.replace("$labelTo$", to.getLabel().getName());
		singleStatement = singleStatement.replace("$idNameTo$", to.getIdName());
		singleStatement = singleStatement.replace("$idvalueTo$", to.getIdValue().toString());

		singleStatement = singleStatement.replace("$type$", relation.getType());
		return singleStatement;
	}

	private String updateRelationStatement(GraphRelation relation) {
		// TODO
		return null;
	}

	private String prepareKeyValueFilter(GraphNode node) {
		StringBuilder sb = new StringBuilder();

		ImmutablePair<String, Object> keyValueId = node.getKeyValueId();

		sb.append(keyValueId.getKey()).append(":");
		if (keyValueId.getValue() instanceof String) {
			sb.append("'");
		}
		sb.append(keyValueId.getValue());

		if (keyValueId.getValue() instanceof String) {
			sb.append("'");
		}

		return sb.toString();
	}

	public String translateGet(RecursiveFilter filter) {
		String requestJson = null;
		String statement;

		if (filter instanceof RecursiveByRelationFilter) {
			RecursiveByRelationFilter byRelationFilter = (RecursiveByRelationFilter) filter;

			statement = CypherTemplates.GetByRelationNodeRecursiveTemplate.replace("$labelNode$",
					byRelationFilter.getNode().getLabel());
			String keyValueId = prepareKeyValueFilter(byRelationFilter.getNode());

			statement = statement.replace("$propsNode$", keyValueId);

			statement = statement.replace("$type$", byRelationFilter.getRelationType());

			String relationProps = prepareFilterBody(filter);
			statement = statement.replace("$propsRel$", relationProps);
			statement = statement.replace("$labelSrc$", filter.getNodeType().getName());

		} else {

			statement = CypherTemplates.GetNodeRecursiveTemplate.replace("$label$", filter.getNodeType().getName());

			// replace filter
			if (filter.getProperties().isEmpty()) {
				// get all records by label
				statement = statement.replace("{$filter$}", "");
			} else {
				String filterStr = prepareFilterBody(filter);
				statement = statement.replace("$filter$", filterStr);
			}
		}

		if (filter.getChildRelationTypes() == null || filter.getChildRelationTypes().isEmpty()) {
			statement = statement.replace("$typesList$", "");

		} else {
			StringBuilder typesList = new StringBuilder();
			int count = 0;
			for (String type : filter.getChildRelationTypes()) {
				typesList.append(":").append(type);
				++count;
				if (count < filter.getChildRelationTypes().size()) {
					typesList.append("|");
				}
			}
			statement = statement.replace("$typesList$", typesList.toString());
		}
		String singleStatement = CypherTemplates.RegularStatementTemplate.replace("$statement$", statement);
		requestJson = CypherTemplates.BatchTemplate.replace("$statementList$", singleStatement);

		return requestJson;
	}

	public static String prepareFilterBody(MatchFilter filter) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		int size = filter.getProperties().entrySet().size();
		for (Map.Entry<String, Object> entry : filter.getProperties().entrySet()) {
			sb.append(entry.getKey()).append(":");
			if (entry.getValue() instanceof String) {
				sb.append("'");
			}
			sb.append(entry.getValue());
			if (entry.getValue() instanceof String) {
				sb.append("'");
			}
			++count;
			if (count < size) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

}
