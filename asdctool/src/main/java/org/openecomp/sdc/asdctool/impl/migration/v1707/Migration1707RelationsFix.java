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

package org.openecomp.sdc.asdctool.impl.migration.v1707;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;
@Component("migration1707relationsFix")
public class Migration1707RelationsFix {

	private static Logger LOGGER = LoggerFactory.getLogger(Migration1707RelationsFix.class);
	
	@Autowired
    private TitanDao titanDao;

	public boolean migrate() {
		boolean result = true;

		try{
			Map<GraphPropertyEnum, Object> propsHasNot = new EnumMap<>(GraphPropertyEnum.class);
			propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);
			Either<List<GraphVertex>, TitanOperationStatus> getAllTopologyTemplatesRes = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, propsHasNot, JsonParseFlagEnum.ParseMetadata);
			if (getAllTopologyTemplatesRes.isRight() && getAllTopologyTemplatesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
				LOGGER.debug("Failed to fetch all non marked as deleted topology templates , error {}", getAllTopologyTemplatesRes.right().value());
				result = false;
			}
			if(getAllTopologyTemplatesRes.isLeft()){
				fixComponentsRelations(getAllTopologyTemplatesRes.left().value());
			}
			if(result){
				Either<List<GraphVertex>, TitanOperationStatus> getAllNodeTypesRes = titanDao.getByCriteria(VertexTypeEnum.NODE_TYPE, null, propsHasNot, JsonParseFlagEnum.ParseMetadata);
				if (getAllNodeTypesRes.isRight() && getAllNodeTypesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
					LOGGER.debug("Failed to fetch all non marked as deleted node types , error {}", getAllNodeTypesRes.right().value());
					result = false;
				}
				if(getAllNodeTypesRes.isLeft()){
					fixComponentsRelations(getAllNodeTypesRes.left().value());
				}
			}
		} catch (Exception e){
			LOGGER.debug("The exception {} occured upon migration 1707 relations fixing. ", e.getMessage());
			e.printStackTrace();
			result = false;
		}
		finally{
			if(result){
				titanDao.commit();
			} else {
				titanDao.rollback();
			}
		}
		return result;
	}
	
	private void fixComponentsRelations(List<GraphVertex> notDeletedComponentVerticies) {
		notDeletedComponentVerticies.stream().forEach(this::fixComponentRelations);
	}
	
	private void fixComponentRelations(GraphVertex componentV) {
		fixCreatorComponentRelation(componentV);
		fixLastModifierComponentRelation(componentV);
		fixStateComponentRelation(componentV);
	}
	
	private void fixStateComponentRelation(GraphVertex componentV) {
		boolean relevantEdgeFound = false;
		Iterator<Edge> edges = componentV.getVertex().edges(Direction.IN, EdgeLabelEnum.STATE.name());
		String getState = (String) componentV.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE);
		while(edges.hasNext()){
			Edge edge = edges.next();
			String edgeState = (String) edge.property(GraphPropertyEnum.STATE.getProperty()).orElse(null);
			if(getState.equals(edgeState) && !relevantEdgeFound){
				relevantEdgeFound = true;
			} else {
				removeEdge(edge);
			}
		}
	}

	private void fixCreatorComponentRelation(GraphVertex componentV) {
		boolean relevantUserFound = false;
		Iterator<Edge> edges = componentV.getVertex().edges(Direction.IN, EdgeLabelEnum.CREATOR.name());
		String getCreatorUserId = (String) componentV.getJsonMetadataField(JsonPresentationFields.USER_ID_CREATOR);
		while(edges.hasNext()){
			Edge edge = edges.next();
			String userId = (String) edge.outVertex().property(GraphPropertyEnum.USERID.getProperty()).orElse(null);
			if(getCreatorUserId.equals(userId) && !relevantUserFound){
				relevantUserFound = true;
			} else {
				removeEdge(edge);
			}
		}
	}
	
	private void fixLastModifierComponentRelation(GraphVertex componentV) {
		boolean relevantUserFound = false;
		Iterator<Edge> edges = componentV.getVertex().edges(Direction.IN, EdgeLabelEnum.LAST_MODIFIER.name());
		String getLastUpdaterUserId = (String) componentV.getJsonMetadataField(JsonPresentationFields.USER_ID_LAST_UPDATER);
		while(edges.hasNext()){
			Edge edge = edges.next();
			String updaterId = (String) edge.outVertex().property(GraphPropertyEnum.USERID.getProperty()).orElse(null);
			if(getLastUpdaterUserId.equals(updaterId) && !relevantUserFound){
				relevantUserFound = true;
			} else {
				removeEdge(edge);
			}
		}
	}

	private void removeEdge(Edge edge) {
		LOGGER.debug("Going to remove edge {} upon migration 1707 relations fixing. ", edge.id());
		edge.remove();
		LOGGER.debug("The edge {} has been removed. ", edge.id());
	}
	
}
