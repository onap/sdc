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

package org.openecomp.sdc.asdctool.impl.migration.v1604;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public class DerivedFromAlignment {
	private static Logger log = LoggerFactory.getLogger(VfcNamingAlignment.class.getName());
	private Map<String, String> newDerivedFromValuesHM = new HashMap<String, String>();
	@Autowired
	protected TitanGenericDao titanGenericDao;

	public boolean alignDerivedFrom1604(String appConfigDir, String dataInputFileDir) {
		log.debug("Started alignDerivedFrom1604 procedure..");
		boolean result = false;
		try {
			if (!getDerivedFromValuesFromFile(dataInputFileDir)) {
				log.error("Started alignDerivedFrom1604 procedure was failed. Missing data in the input data file.");
				return result;
			}
			result = changeDerivedFrom();
		} finally {
			if (!result) {
				titanGenericDao.rollback();
				log.debug("**********************************************");
				log.debug("alignDerivedFrom1604 procedure FAILED!!");
				log.debug("**********************************************");
			} else {
				titanGenericDao.commit();
				log.debug("**********************************************");
				log.debug("alignDerivedFrom1604 procedure ended successfully!");
				log.debug("**********************************************");
			}
		}
		return result;
	}

	private boolean changeDerivedFrom() {
		Map<String, ResourceMetadataData> resourcesHM = getLatestVersionsOfResources();
		if (resourcesHM == null)
			return false;
		Map<String, ResourceMetadataData> derivedFromResourcesHM = getLatestCertifiedVersionsOfDerivedFromResources();
		if (derivedFromResourcesHM == null)
			return false;
		return updateEdges(resourcesHM, derivedFromResourcesHM);
	}

	private boolean updateEdges(Map<String, ResourceMetadataData> resourcesHM,
			Map<String, ResourceMetadataData> derivedFromResourcesHM) {
		log.debug("Updating of Edges has been started..");
		for (Entry<String, ResourceMetadataData> pair : resourcesHM.entrySet()) {
			ResourceMetadataData curResource = pair.getValue();
			String uniqeID = (String) curResource.getUniqueId();
			Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentResourceRes = titanGenericDao
					.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), uniqeID,
							GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource, ResourceMetadataData.class);
			if (parentResourceRes.isLeft()) {
				ImmutablePair<ResourceMetadataData, GraphEdge> value = parentResourceRes.left().value();
				ResourceMetadataData parentResourceData = value.getKey();
				log.debug("Deleting old relation..");
				Either<GraphRelation, TitanOperationStatus> deletingRelationRes = titanGenericDao
						.deleteRelation(curResource, parentResourceData, GraphEdgeLabels.DERIVED_FROM);
				if (deletingRelationRes.isRight()) {
					log.error("Couldn't delete relation from resource {} to resource {}, error: {}",
							curResource.getMetadataDataDefinition().getName(),
							parentResourceData.getMetadataDataDefinition().getName(),
							deletingRelationRes.right().value());
					return false;
				}
				ResourceMetadataData newDerivedFromResource = derivedFromResourcesHM.get(pair.getKey());
				Either<GraphRelation, TitanOperationStatus> creatingRelationRes = titanGenericDao
						.createRelation(curResource, newDerivedFromResource, GraphEdgeLabels.DERIVED_FROM, null);
				if (creatingRelationRes.isRight()) {
					log.error("Couldn't create relation from resource {} to resource {}, error: {}",
							curResource.getMetadataDataDefinition().getName(),
							newDerivedFromResource.getMetadataDataDefinition().getName(),
							creatingRelationRes.right().value());
					return false;
				}
			} else {
				log.error("Couldn't get derived from resource for child resource {}, error: {}", pair.getKey(),
						parentResourceRes.right().value());
				return false;
			}
		}
		return true;
	}

	private Map<String, ResourceMetadataData> getLatestCertifiedVersionsOfDerivedFromResources() {
		log.debug("Getting latest certified versions of derived from resources according input file");
		Map<String, ResourceMetadataData> resourcesHM = new HashMap<String, ResourceMetadataData>();
		Map<String, Object> props = null;
		for (Entry<String, String> pair : newDerivedFromValuesHM.entrySet()) {
			props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty(), pair.getValue());
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
			Either<List<ResourceMetadataData>, TitanOperationStatus> highestVersionResource = titanGenericDao
					.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
			if (highestVersionResource.isRight()) {
				log.error("Couldn't get resource {} from DB, error: {}", pair.getValue(),
						highestVersionResource.right().value());
				return null;
			}
			List<ResourceMetadataData> highestVersionResourceAL = highestVersionResource.left().value();
			if (highestVersionResourceAL == null) {
				log.error("Couldn't get resource {}. No resource found", pair.getValue());
				return null;
			}
			ResourceMetadataData resource = highestVersionResourceAL.get(0);
			String state = resource.getMetadataDataDefinition().getState();
			if (!state.equals(LifecycleStateEnum.CERTIFIED.name())) {
				log.error(
						"alignDerivedFrom1604 procedure FAILED!! Derived from resource {} is not certified. Please certify manually and repeat the procedure.",
						pair.getValue());
				return null;
			}
			resourcesHM.put(pair.getKey(), resource);
		}
		return resourcesHM;
	}

	private Map<String, ResourceMetadataData> getLatestVersionsOfResources() {
		log.debug("Getting latest versions of resources according input file");
		Map<String, ResourceMetadataData> resourcesHM = new HashMap<String, ResourceMetadataData>();
		ResourceMetadataData foundResource = null;
		Map<String, Object> props = null;
		for (Entry<String, String> pair : newDerivedFromValuesHM.entrySet()) {// filter
			props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NAME.getProperty(), pair.getKey());
			props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

			Either<List<ResourceMetadataData>, TitanOperationStatus> highestVersionResource = titanGenericDao
					.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
			if (highestVersionResource.isRight()) {
				log.error("Couldn't get resource {} from DB, error: {}", pair.getKey(),
						highestVersionResource.right().value());
				return null;
			}
			List<ResourceMetadataData> highestVersionResourceAL = highestVersionResource.left().value();
			if (highestVersionResourceAL == null) {
				log.error("Couldn't get resource {}. No resource found", pair.getKey());
				return null;
			}
			if (highestVersionResourceAL.size() > 2) {
				log.error("Invalid response. Found more than two highest version resources with name {}.",
						pair.getKey());
				return null;
			}
			foundResource = highestVersionResourceAL.get(0);
			if (highestVersionResourceAL.size() == 2) {
				foundResource = foundResource.getMetadataDataDefinition().getState()
						.equals(LifecycleStateEnum.CERTIFIED.name()) ? highestVersionResourceAL.get(1) : foundResource;
			}
			resourcesHM.put(pair.getKey(), foundResource);
		}
		return resourcesHM;
	}

	private boolean getDerivedFromValuesFromFile(String dataInputFileDir) {
		BufferedReader br = null;
		String curPair = null;
		try {
			br = new BufferedReader(new FileReader(dataInputFileDir));
			while ((curPair = br.readLine()) != null) {
				String[] pair = curPair.split(" ");
				if (pair.length < 2) {
					log.error(
							"Expected at least two tokens in every line. Usage: <resource_name> <new_derived_from_name>");
					return false;
				}
				String derivedFrom = pair[pair.length - 1];
				String name = curPair.substring(0, curPair.length() - derivedFrom.length() - 1);
				newDerivedFromValuesHM.put(name, derivedFrom);
			}
			return true;
		} catch (FileNotFoundException e) {
			log.error("Started alignDerivedFrom1604 procedure was failed. Missing input data file.", e);
		} catch (IOException e) {
			log.error("Started alignDerivedFrom1604 procedure was failed. The input data file is empty.", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.debug("failed to close file reader", e);
				}
			}
		}
		return false;
	}
}
