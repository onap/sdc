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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public class GroupsAlignment {

	private static Logger log = LoggerFactory.getLogger(ServiceMigration.class.getName());

	public static String INITIAL_VERSION = "1.0";
	private static final String DEFAULT_GROUP_VF_MODULE = "org.openecomp.groups.VfModule";
	private static final String MODULE = "::module-";

	@Autowired
	protected TitanGenericDao titanGenericDao;
	@Autowired
	protected IArtifactOperation artifactOperation;
	@Autowired
	protected IGroupOperation groupOperation;
	@Autowired
	protected GroupTypeOperation groupTypeOperation;

	public boolean alignGroups(String appConfigDir) {

		log.debug("Started the align groups procedure ...");
		log.debug("Getting all resources with resources");
		boolean result = false;
		try {

			Map<String, Object> properties = new HashMap<>();
			properties.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());
			Either<List<ResourceMetadataData>, TitanOperationStatus> allVfResources = titanGenericDao
					.getByCriteria(NodeTypeEnum.Resource, properties, ResourceMetadataData.class);

			if (allVfResources.isRight()) {
				log.error("Couldn't get VF resources from DB, error: {}", allVfResources.right().value());
				result = false;
				return result;
			}
			List<ResourceMetadataData> resourcesList = allVfResources.left().value();
			if (resourcesList == null) {
				log.error("Couldn't get VF resources from DB, no resources found");
				result = false;
				return result;
			}
			log.debug("Found {} VF resources", resourcesList.size());
			for (ResourceMetadataData resource : resourcesList) {
				result = createGroupIfContainsArtifacts(resource);
			}
		} finally {
			if (!result) {
				titanGenericDao.rollback();
				log.debug("**********************************************");
				log.debug("The align groups procedure FAILED!!");
				log.debug("**********************************************");
			} else {
				titanGenericDao.commit();
				log.debug("**********************************************");
				log.debug("The align groups procedure ended successfully!");
				log.debug("**********************************************");
			}
		}

		return result;
	}

	private boolean createGroupIfContainsArtifacts(ResourceMetadataData resource) {

		String uniqueId = resource.getMetadataDataDefinition().getUniqueId();
		StorageOperationStatus result = StorageOperationStatus.OK;
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> allArtifactsRes = artifactOperation
				.getArtifacts(uniqueId, NodeTypeEnum.Resource, true);
		if (allArtifactsRes.isRight()) {
			log.error("Couldn't get resource artifacts from DB, error: {}", allArtifactsRes.right().value());
			return false;
		}
		Map<String, ArtifactDefinition> artifactsHM = allArtifactsRes.left().value();
		ArrayList<String> foundArtifactsAL = new ArrayList<String>();
		for (ArtifactDefinition curArtifact : artifactsHM.values()) {
			String atrifactType = curArtifact.getArtifactType();
			if (atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())
					|| atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT_NET.getType())
					|| atrifactType.equalsIgnoreCase(ArtifactTypeEnum.HEAT.getType())) {
				foundArtifactsAL.add(curArtifact.getUniqueId());
			}
		}
		if (foundArtifactsAL.size() > 0) {
			Either<List<GroupDefinition>, TitanOperationStatus> allGroupsRes = groupOperation
					.getAllGroupsFromGraph(uniqueId, NodeTypeEnum.Resource);
			int groupCounter = 1;
			if (allGroupsRes.isRight()) {
				if (allGroupsRes.right().value().name().equals(TitanOperationStatus.OK.name())
						|| allGroupsRes.right().value().name().equals(TitanOperationStatus.NOT_FOUND.name())) {
					log.debug("Not found groups resource related to resource {}, response: {}",
							resource.getMetadataDataDefinition().getName(), allGroupsRes.right().value());
				} else {
					log.error("Not found groups resource related to resource {}, DB error: {}",
							resource.getMetadataDataDefinition().getName(), allGroupsRes.right().value());
					return false;
				}
			} else if (allGroupsRes.left().value() != null && allGroupsRes.left().value().size() > 0) {
				groupCounter += allGroupsRes.left().value().size();
				for (GroupDefinition curGroup : allGroupsRes.left().value()) {
					for (String curGroupArtifact : curGroup.getArtifacts()) {
						if (foundArtifactsAL.contains(curGroupArtifact)) {
							foundArtifactsAL.remove(curGroupArtifact);
						}
					}
				}
			}
			if (foundArtifactsAL.size() > 0) {
				GroupDefinition groupDefinition = new GroupDefinition();
				groupDefinition.setName(resource.getMetadataDataDefinition().getName() + MODULE + groupCounter);
				groupDefinition.setType(DEFAULT_GROUP_VF_MODULE);
				groupDefinition.setArtifacts(foundArtifactsAL);
				log.debug("Creating new group {} for VF resource {}", groupDefinition.getName(),
						resource.getMetadataDataDefinition().getName());
				return createGroup(resource.getUniqueId(), ComponentTypeEnum.RESOURCE, groupDefinition);

			}
		}
		return true;
	}

	private boolean createGroup(Object uniqueId, ComponentTypeEnum componentType, GroupDefinition groupDefinition) {

		NodeTypeEnum nodeTypeEnum = componentType.getNodeType();
		String groupType = groupDefinition.getType();

		Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeRes = groupTypeOperation
				.getLatestGroupTypeByType(groupType, true);
		if (getGroupTypeRes.isRight()) {
			log.error("Couldn't get grouptype by type {} from DB, error: {}", groupType,
					getGroupTypeRes.right().value());
			return false;
		}

		GroupTypeDefinition groupTypeDefinition = getGroupTypeRes.left().value();

		String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
		groupDefinition.setInvariantUUID(invariantUUID);
		groupDefinition.setVersion(INITIAL_VERSION);
		groupDefinition.setTypeUid(groupTypeDefinition.getUniqueId());

		Either<GroupDefinition, StorageOperationStatus> addGroupToGraphRes = groupOperation.addGroup(nodeTypeEnum,
				(String) uniqueId, groupDefinition, true);

		if (addGroupToGraphRes.isRight()) {
			log.error("Couldn't add group {} to graph, error: {}", groupDefinition.getName(),
					addGroupToGraphRes.right().value());
			return false;
		}
		log.debug("The group {} has been created", groupDefinition.getName());
		return true;
	}

}
