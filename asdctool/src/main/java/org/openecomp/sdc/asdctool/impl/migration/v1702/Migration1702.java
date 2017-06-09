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

package org.openecomp.sdc.asdctool.impl.migration.v1702;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.analysis.solvers.RiddersSolver;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.VfModuleProperty;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.ResourceMetadataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public class Migration1702 {
	private static final String CONFIG_GROUP_TYPES_YML = "/config/groupTypes.yml";

	private static final String CONFIG_DATA_TYPES_YML = "/config/dataTypes.yml";

	private static Logger log = LoggerFactory.getLogger(Migration1702.class.getName());

	@Autowired
	protected TitanGenericDao titanGenericDao;
	@Autowired
	protected ResourceOperation resourceOperation;
	@Autowired
	protected ServiceOperation serviceOperation;
	@Autowired
	private ServiceBusinessLogic serviceBusinessLogic;
	@Autowired
	private GroupTypeOperation groupTypeOperation;
	@Autowired
	private PropertyOperation propertyOperation;
	@Autowired
	private ComponentsUtils componentsUtils;
	@Autowired
	private GroupOperation groupOperation;

	@Autowired
	private ArtifactsBusinessLogic artifactsBusinessLogic;

	@Autowired
	private UserBusinessLogic userAdminManager;

	@Autowired
	private ComponentInstanceOperation componentInstanceOperation;

	public boolean migrate(String appConfigDir) {
		boolean result = true;
		String methodName = "alignCustomizationUUID";

		try {
			if (!alignCustomizationUUID()) {
				log.error("Failed to align customization UUID");
				result = false;
				return result;
			}
			methodName = "alignGroupDataType";
			if (!alignGroupDataType()) {
				log.error("Failed to align Group data type");
				result = false;
				return result;
			}
			methodName = "alignVfModuleProperties";
			if (!alignVfModuleProperties()) {
				log.error("Failed to align Vf Module Properties");
				result = false;
				return result;
			}
			methodName = "alignDataType";
			if (!alignDataType()) {
				log.error("Failed to align data type");
				result = false;
				return result;
			}
			methodName = "alignHeatEnv";
			if (!alignHeatEnv()) {
				log.error("Failed to align heat env on VF level");
				result = false;
				return result;
			}
			methodName = "alignModuleInstances";
			if (!alignModuleInstances()) {
				log.error("Failed to align module instances");
				result = false;
				return result;
			}

		} catch (Exception e) {
			log.error("Failed {} with exception: ", methodName, e);
			result = false;
		}
		return result;
	}

	private boolean alignModuleInstances() {
		log.info(" Align Module Instances");
		boolean result = true;
		boolean statusToReturn = true;

		Writer writer = null;

		try {
			long time = System.currentTimeMillis();
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("runstatusModules_" + time + ".csv"), "utf-8"));

			writer.write("resource id, instance id, group id, status\n");

			Either<List<ServiceMetadataData>, TitanOperationStatus> allServices = titanGenericDao.getByCriteria(NodeTypeEnum.Service, null, ServiceMetadataData.class);
			if (allServices.isRight()) {
				if (allServices.right().value() != TitanOperationStatus.NOT_FOUND) {
					log.error("Align heat env on Vf  - Failed to fetch services {}", allServices.right().value());
					result = false;
					statusToReturn = false;
					return statusToReturn;
				} else {
					log.debug("No Services. ");
					return statusToReturn;
				}
			}
			log.info("Need to handle {} services", allServices.left().value().size());
			long handledServices = 0;
			for (ServiceMetadataData metadata : allServices.left().value()) {
				String serviceId = metadata.getMetadataDataDefinition().getUniqueId();
				Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> riRes = componentInstanceOperation.getComponentInstancesOfComponent(serviceId, NodeTypeEnum.Service, NodeTypeEnum.Resource);
				if (riRes.isRight()) {
					if (riRes.right().value() == TitanOperationStatus.NOT_FOUND) {
						log.info("No instancces for service {}", serviceId);
					} else {
						log.info("Align vf modules - failed to fetch component instances for service {} error {}", riRes.right().value());
						writeModuleResultToFile(writer, serviceId, null, null, riRes.right().value());
						statusToReturn = false;
					}
					++handledServices;
					continue;
				}
				List<ComponentInstance> componentInstances = riRes.left().value().left;
				for (ComponentInstance ci : componentInstances) {
					Either<TitanVertex, TitanOperationStatus> ciVertexRes = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), ci.getUniqueId());
					if (ciVertexRes.isRight()) {
						log.info("Failed to fetch vertex for component instance {}, error {}", ci.getUniqueId(), ciVertexRes.right().value());
						writeModuleResultToFile(writer, serviceId, ci.getUniqueId(), null, ciVertexRes.right().value());
						statusToReturn = false;
						continue;
					}
					TitanVertex ciVertex = ciVertexRes.left().value();
					if (createGroupInstancesOnComponentInstance(writer, ci, ciVertex, serviceId) == false) {
						statusToReturn = false;
						continue;
					}
				}
				writer.flush();
				++handledServices;
			}

			log.info("Handled {} services", handledServices);
		} catch (Exception e) {
			log.error("Failed {} with exception: ", "alignModuleInstances", e);
			result = false;
			statusToReturn = false;
		} finally {

			log.info(" Align Module Instances finished");
			if (!result) {
				log.info("Doing rollback");
				titanGenericDao.rollback();
			} else {
				log.info("Doing commit");
				titanGenericDao.commit();
			}
			try {
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}
		return statusToReturn;
	}

	private boolean createGroupInstancesOnComponentInstance(Writer writer, ComponentInstance ci, TitanVertex ciVertex, String serviceId) {
		boolean statusToReturn = true;

		Map<String, Object> properties = titanGenericDao.getProperties(ciVertex);
		ComponentInstanceData createdComponentInstance = GraphElementFactory.createElement(NodeTypeEnum.ResourceInstance.getName(), GraphElementTypeEnum.Node, properties, ComponentInstanceData.class);

		Either<List<GroupDefinition>, TitanOperationStatus> groupEither = groupOperation.getAllGroupsFromGraph(ci.getComponentUid(), NodeTypeEnum.Resource);
		if (groupEither.isRight()) {
			if (groupEither.right().value() != TitanOperationStatus.OK && groupEither.right().value() != TitanOperationStatus.NOT_FOUND) {
				TitanOperationStatus status = groupEither.right().value();
				log.error("Failed to associate group instances to component instance {}. Status is {}", ci.getUniqueId(), status);
				writeModuleResultToFile(writer, serviceId, ci.getUniqueId(), null, status);
				return false;
			} else {
				log.debug("No groups for component instance {}. ", ci.getUniqueId());

				writeModuleResultToFile(writer, serviceId, ci.getUniqueId(), null, "No groups");
				return true;
			}
		}
		List<GroupDefinition> groupsIמResource = groupEither.left().value();
		if (groupsIמResource != null && !groupsIמResource.isEmpty()) {
			List<GroupDefinition> vfGroupsListInResource = groupsIמResource.stream().filter(p -> p.getType().equals("org.openecomp.groups.VfModule")).collect(Collectors.toList());

			for (GroupDefinition groupInResource : vfGroupsListInResource) {
				Iterator<Edge> edgesToInstances = ciVertex.edges(Direction.OUT, GraphEdgeLabels.GROUP_INST.getProperty());
				boolean exist = false;
				String normalizedName = ValidationUtils.normalizeComponentInstanceName(ci.getNormalizedName() + ".." + groupInResource.getName());
				String grInstId = UniqueIdBuilder.buildResourceInstanceUniuqeId(ci.getUniqueId(), groupInResource.getUniqueId(), normalizedName);
				

				while (edgesToInstances.hasNext()) {
					Edge edgeToInst = edgesToInstances.next();
					Vertex grInstVertex = edgeToInst.inVertex();
					String grId = (String) titanGenericDao.getProperty((TitanVertex) grInstVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					if (grId.equals(grInstId)) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					Either<GroupInstance, StorageOperationStatus> status = componentInstanceOperation.createGroupInstance(ciVertex, groupInResource, ci);
					if (status.isRight()) {
						log.error("Failed to create group instance {} in component instance {}. Status is {}", grInstId, ci.getUniqueId(), status.right().value());
						statusToReturn = false;
						writeModuleResultToFile(writer, serviceId, ci.getUniqueId(), grInstId, status.right().value());
					} else {
						writeModuleResultToFile(writer, serviceId, ci.getUniqueId(), grInstId, "OK");
					}
				} else {
					writeModuleResultToFile(writer, serviceId, ci.getUniqueId(), grInstId, "Exist");
				}

			}
		}
		return statusToReturn;
	}

	@SuppressWarnings("resource")
	private boolean alignHeatEnv() {
		Writer writer = null;
		log.info(" Align heat env on Vf level");
		boolean statusToReturn = true;

		boolean result = true;
		try {
			long time = System.currentTimeMillis();
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("runstatusEnv_" + time + ".csv"), "utf-8"));

			writer.write("resource id, operation, artifact id, status\n");
			User user = buildDummyUser();

			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());

			Either<List<ResourceMetadataData>, TitanOperationStatus> allResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
			if (allResources.isRight()) {
				if (allResources.right().value() != TitanOperationStatus.NOT_FOUND) {
					log.error("Align heat env on Vf  - Failed to fetch resources {}", allResources.right().value());
					statusToReturn = false;
					result = false;
					return statusToReturn;
				} else {
					log.debug("No VF resources. ");
					return result;
				}
			}
			List<ResourceMetadataData> resources = allResources.left().value();
			log.debug("Need to handle {} resources", resources.size());

			long totalHandledArtifacts = 0;
			for (ResourceMetadataData metadata : resources) {
				Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> artifactNodesRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) metadata.getUniqueId(),
						GraphEdgeLabels.ARTIFACT_REF, NodeTypeEnum.ArtifactRef, ArtifactData.class);
				if (artifactNodesRes.isRight()) {
					if (artifactNodesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
						log.error("Align heat env on Vf  - Failed to fetch artifacts for resources {}", metadata.getUniqueId(), artifactNodesRes.right().value());
						writer.write(metadata.getUniqueId() + ",get artifacts, ,Failed to fetch artifacts " + artifactNodesRes.right().value() + "\n");
						statusToReturn = false;
						continue;
					} else {
						log.debug("No artifact for resource {} . ", metadata.getUniqueId());
						writer.write(metadata.getUniqueId() + ",get artifacts, ,No artfacts\n");
						continue;
					}
				}
				List<ImmutablePair<ArtifactData, GraphEdge>> artifacts = artifactNodesRes.left().value();

				for (ImmutablePair<ArtifactData, GraphEdge> pair : artifacts) {
					ArtifactData artifactData = pair.left;
					if (isNeedCreatePlaceHolder(artifactData)) {
						// check if exist heat env - if not -> create
						String heatEnvId = (String) artifactData.getUniqueId() + "env";
						if (validateOrCreateHeatEnv(user, metadata, artifactData, heatEnvId, writer) == false) {
							statusToReturn = false;
						}
						// check if connected to group - if not -> connect
						if (validateOrAssociateHeatAnv(metadata, artifactData, heatEnvId, writer) == false) {
							statusToReturn = false;
						}
						++totalHandledArtifacts;
						writer.flush();
					}

				}
			}
			log.debug("Total handled {}  artifacts", totalHandledArtifacts);
		} catch (Exception e) {
			log.error("Failed {} with exception: ", "alignHeatEnv", e);
			result = false;
		} finally {

			log.info("Aling heat env on VF level finished ");
			if (!result) {
				log.info("Doing rollback");
				titanGenericDao.rollback();
			} else {
				log.info("Doing commit");
				titanGenericDao.commit();
			}
			try {
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}
		return statusToReturn;
	}

	private boolean validateOrAssociateHeatAnv(ResourceMetadataData metadata, ArtifactData artifactData, String heatEnvId, Writer writer) {
		boolean statusToReturn = true;

		String resourceId = (String) metadata.getUniqueId();
		Either<ArtifactData, TitanOperationStatus> heatEnvArtifactRes = titanGenericDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatEnvId, ArtifactData.class);
		if (heatEnvArtifactRes.isRight()) {
			log.error("Align heat env on Vf  - Failed to fetch heat env node for id {}  {}", heatEnvId, heatEnvArtifactRes.right().value());
			writeResultToFile(writer, "get artifact node for relation", resourceId, heatEnvId, heatEnvArtifactRes.right().value());
			return false;
		}
		ArtifactData heatEnvArtifact = heatEnvArtifactRes.left().value();

		Either<List<ImmutablePair<GroupData, GraphEdge>>, TitanOperationStatus> groupsForHeatRes = titanGenericDao.getParentNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) artifactData.getUniqueId(),
				GraphEdgeLabels.GROUP_ARTIFACT_REF, NodeTypeEnum.Group, GroupData.class);
		if (groupsForHeatRes.isRight()) {
			writeResultToFile(writer, "getChildrenNodes groups for heat", resourceId, (String) artifactData.getUniqueId(), groupsForHeatRes.right().value());
			if (groupsForHeatRes.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.error("Align heat env on Vf  - Failed to fetch groups for heat artifact {} in resources {} : {}", artifactData.getUniqueId(), metadata.getUniqueId(), groupsForHeatRes.right().value());
				return false;
			} else {
				log.debug("Align heat env on Vf  - No groups for heat artifact {} in resources {} : {}", artifactData.getUniqueId(), metadata.getUniqueId(), groupsForHeatRes.right().value());
				return true;
			}
		}
		List<ImmutablePair<GroupData, GraphEdge>> groupsForHeat = groupsForHeatRes.left().value();
		Either<List<ImmutablePair<GroupData, GraphEdge>>, TitanOperationStatus> groupsForHeatEnvRes = titanGenericDao.getParentNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatEnvId, GraphEdgeLabels.GROUP_ARTIFACT_REF, NodeTypeEnum.Group,
				GroupData.class);
		List<ImmutablePair<GroupData, GraphEdge>> groupsForHeatEnv;
		if (groupsForHeatEnvRes.isRight()) {
			if (groupsForHeatEnvRes.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.error("Align heat env on Vf  - Failed to fetch groups for heat env artifact {} in resources {} : ", artifactData.getUniqueId(), metadata.getUniqueId(), groupsForHeatEnvRes.right().value());
				writeResultToFile(writer, "getChildrenNodes groups for heat env", resourceId, heatEnvId, groupsForHeatEnvRes.right().value());
				return false;
			} else {
				groupsForHeatEnv = new ArrayList<>();
			}
		} else {
			groupsForHeatEnv = groupsForHeatEnvRes.left().value();
		}

		for (ImmutablePair<GroupData, GraphEdge> heatGroup : groupsForHeat) {
			// check if exist
			boolean exist = false;
			GroupDataDefinition groupDataDefinition = heatGroup.left.getGroupDataDefinition();
			for (ImmutablePair<GroupData, GraphEdge> heatEnvGroup : groupsForHeatEnv) {
				if (groupDataDefinition.getName().equals(heatEnvGroup.left.getGroupDataDefinition().getName())) {
					exist = true;
					break;
				}
			}
			String groupId = (String) heatGroup.left.getUniqueId();
			if (!exist) {
				// need associate

				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put(GraphPropertiesDictionary.NAME.getProperty(), heatEnvArtifact.getLabel());
				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(heatGroup.left, heatEnvArtifact, GraphEdgeLabels.GROUP_ARTIFACT_REF, properties);
				log.trace("After associate group {} to artifact {}", groupDataDefinition.getName(), heatEnvArtifact.getUniqueIdKey());
				if (createRelation.isRight()) {
					log.error("Align heat env on Vf  - Failed to associate heat env artifact {} to group {} : {}", artifactData.getUniqueId(), groupDataDefinition.getUniqueId(), createRelation.right().value());

					writeResultToFile(writer, "associate to group- relation" + groupId, resourceId, heatEnvId, groupsForHeatRes.right().value());
					statusToReturn = false;
				} else {
					writeResultToFile(writer, "associate to group " + groupId, resourceId, heatEnvId, "OK");
				}
			} else {
				writeResultToFile(writer, "associate group " + groupId, resourceId, heatEnvId, "Exist");
			}
		}
		return statusToReturn;
	}

	private boolean validateOrCreateHeatEnv(User user, ResourceMetadataData metadata, ArtifactData artifactData, String heatEnvId, Writer writer) {
		String resourceId = metadata.getMetadataDataDefinition().getUniqueId();
		boolean statusToReturn = true;
		Either<ArtifactData, TitanOperationStatus> node = titanGenericDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatEnvId, ArtifactData.class);
		boolean isContinue = true;
		if (node.isRight()) {
			if (TitanOperationStatus.NOT_FOUND == node.right().value()) {
				// create
				ArtifactDefinition heatArtifact = new ArtifactDefinition(artifactData.getArtifactDataDefinition());
				ResourceMetadataDefinition resourceMetadataDataDefinition = new ResourceMetadataDefinition((ResourceMetadataDataDefinition) metadata.getMetadataDataDefinition());

				Resource resource = new Resource(resourceMetadataDataDefinition);

				String heatUpdater = heatArtifact.getUserIdLastUpdater();
				Either<User, ActionStatus> userHeat = userAdminManager.getUser(heatUpdater, true);

				Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic.createHeatEnvPlaceHolder(heatArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME, (String) metadata.getUniqueId(), NodeTypeEnum.Resource,
						metadata.getMetadataDataDefinition().getName(), userHeat.left().value(), resource, null, false);
				if (createHeatEnvPlaceHolder.isRight()) {
					log.error("Align heat env on Vf  - Failed to create  heat env {} for heat {} : {}", heatEnvId, heatArtifact.getUniqueId(), createHeatEnvPlaceHolder.right().value().getText());
					writeResultToFile(writer, "create placeholder", resourceId, heatEnvId, createHeatEnvPlaceHolder.right().value().getText());
					isContinue = false;
					statusToReturn = false;
				} else {
					writeResultToFile(writer, "create placeholder", resourceId, heatEnvId, "OK");
				}
			} else {
				log.error("Align heat env on Vf  - Failed to fetch heat env node for id {}  {}", heatEnvId, node.right().value());
				writeResultToFile(writer, "create placeholder - get", resourceId, heatEnvId, node.right().value());
				isContinue = false;
				statusToReturn = false;
			}
		} else {
			writeResultToFile(writer, "create placeholder - get", resourceId, heatEnvId, "Exist");
		}
		if (isContinue) {
			log.debug("associate heat env artifact to all resources ");
			String heatUniqueId = (String) artifactData.getUniqueId();
			Either<TitanVertex, TitanOperationStatus> heatVertexRes = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatUniqueId);
			if (heatVertexRes.isRight()) {
				log.debug("Failed to fetch vertex for heat {} error {}", heatUniqueId, heatVertexRes.right().value());
				writeResultToFile(writer, "create placeholder - get heat vertex", resourceId, heatEnvId, heatVertexRes.right().value());
				statusToReturn = false;
				return statusToReturn;
			}
			TitanVertex heatVertex = heatVertexRes.left().value();
			Either<TitanVertex, TitanOperationStatus> heatEnvVertexRes = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatEnvId);
			if (heatEnvVertexRes.isRight()) {
				log.debug("Failed to fetch vertex for heat env {} error {}", heatEnvId, heatEnvVertexRes.right().value());
				writeResultToFile(writer, "create placeholder - get heat env vertex", resourceId, heatEnvId, heatEnvVertexRes.right().value());
				statusToReturn = false;
				return statusToReturn;
			}

			Vertex heatEnvVertex = heatEnvVertexRes.left().value();
			Iterator<Edge> edgesToHeat = heatVertex.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.name());
			while (edgesToHeat.hasNext()) {
				Edge edgeToHeat = edgesToHeat.next();
				boolean exist = false;
				Vertex outVertexHeat = edgeToHeat.outVertex();
				Map<String, Object> outVertexProps = titanGenericDao.getProperties(outVertexHeat);

				String resIdToHeat = (String) outVertexProps.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty());

				Iterator<Edge> edgesToEnv = heatEnvVertex.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.name());
				while (edgesToEnv.hasNext()) {
					Edge edgeToEnv = edgesToEnv.next();
					Vertex outVertexEnv = edgeToEnv.outVertex();
					String resIdToEnv = (String) titanGenericDao.getProperty((TitanVertex) outVertexEnv, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					if (resIdToHeat.equals(resIdToEnv)) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					Map<String, Object> properties = titanGenericDao.getProperties(edgeToHeat);
					// need to associate additional resource to heat env
					// update artifact label on edge
					String heatEnvLabel = (String) titanGenericDao.getProperty((TitanVertex) heatEnvVertex, GraphPropertiesDictionary.ARTIFACT_LABEL.getProperty());
					properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), heatEnvLabel);

					TitanOperationStatus createEdge = titanGenericDao.createEdge(outVertexHeat, heatEnvVertex, GraphEdgeLabels.ARTIFACT_REF, properties);
					if (createEdge == TitanOperationStatus.OK) {
						writeResultToFile(writer, "associate to resource " + resIdToHeat, resourceId, heatEnvId, "OK");
					} else {
						writeResultToFile(writer, "associate to resource " + resIdToHeat, resourceId, heatEnvId, createEdge);
						statusToReturn = false;
					}
				} else {
					writeResultToFile(writer, "associate to resource " + resIdToHeat, resourceId, heatEnvId, "Exist");
				}
			}
		}
		return statusToReturn;
	}

	private void writeResultToFile(Writer writer, String op, String resourceId, String artifactD, Object status) {
		try {
			StringBuffer sb = new StringBuffer(resourceId);
			sb.append(",").append(op).append(",").append(artifactD).append(",").append(status).append("\n");
			writer.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeModuleResultToFile(Writer writer, String resourceId, String instanceId, String groupId, Object status) {
		try {
			StringBuffer sb = new StringBuffer(resourceId);
			sb.append(",").append(instanceId).append(",").append(groupId).append(",").append(status).append("\n");
			writer.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isNeedCreatePlaceHolder(ArtifactData artifactData) {
		String artifactType = artifactData.getArtifactDataDefinition().getArtifactType();
		ArtifactTypeEnum type = ArtifactTypeEnum.findType(artifactType);
		if (ArtifactGroupTypeEnum.DEPLOYMENT == artifactData.getArtifactDataDefinition().getArtifactGroupType() && (ArtifactTypeEnum.HEAT == type || ArtifactTypeEnum.HEAT_NET == type || ArtifactTypeEnum.HEAT_VOL == type)) {
			return true;
		}
		return false;
	}

	private boolean alignVfModuleProperties() {
		boolean result = true;
		try {
			log.info(" Align Vf module properties");

			final Pattern pattern = Pattern.compile("\\..(.*?)\\..");
			final String LABEL_NAME = "vf_module_label";
			final String VOLUME_GROUP_NAME = "volume_group";

			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.error("Align Vf module properties - Failed to get graph {}", graph.right().value());
				result = false;
				return result;
			}

			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());

			Either<List<ResourceMetadataData>, TitanOperationStatus> allResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);

			if (allResources.isRight()) {
				if (allResources.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					log.debug("Align Vf module properties - no VF resources");
					result = true;
					return result;
				} else {
					log.error("Align Vf module properties - generateTosca failed fetch all resources,error {}", allResources.right().value());
					result = false;
					return result;
				}
			}

			List<ResourceMetadataData> listAllVFs = allResources.left().value();

			ComponentParametersView componentParametersView = new ComponentParametersView(true);
			componentParametersView.setIgnoreGroups(false);
			componentParametersView.setIgnoreArtifacts(false);

			log.info("Align Vf module properties - Starting to update the VF's");
			Map<String, VfModuleProperty> vfModuleProperties = ConfigurationManager.getConfigurationManager().getConfiguration().getVfModuleProperties();
			for (ResourceMetadataData resourceMetadataData : listAllVFs) {
				String uniqueId = (String) resourceMetadataData.getUniqueId();

				Either<Resource, StorageOperationStatus> resourceResponse = resourceOperation.getResource(uniqueId, componentParametersView, true);

				if (resourceResponse.isRight()) {
					log.error("Align Vf module properties - failed resource with UniqueID: {} , error {}", uniqueId, resourceResponse.right().value());
					result = false;
					return result;
				}

				Resource resource = resourceResponse.left().value();
				List<GroupDefinition> groups = resource.getGroups();

				if (groups == null || groups.isEmpty()) {
					log.debug("Align Vf module properties - resource UniqueID: {} does not contain groups", resource.getUniqueId());
					continue;
				} else {

					for (GroupDefinition groupDefinition : groups) {

						if (groupDefinition.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
							log.info("update vf module proerties for group {} ", groupDefinition.getUniqueId());

							List<GroupProperty> properties = groupDefinition.convertToGroupProperties();
							if (properties == null) {
								properties = new ArrayList<>();
							}
							Boolean isBase = false;
							List<String> artifacts = groupDefinition.getArtifacts();
							if (artifacts == null) {
								artifacts = new ArrayList<>();
							}
							Boolean isVolumeGroup = false;
							for (String artifactId : artifacts) {
								ArtifactDefinition artifactDef = null;
								Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
								artifactDef = findArtifactInList(deploymentArtifacts, artifactId);
								if (artifactDef != null && artifactDef.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
									isVolumeGroup = true;
									break;
								}
							}
							for (GroupProperty groupProperty : properties) {
								if (groupProperty.getName().equals(Constants.IS_BASE)) {
									isBase = Boolean.valueOf(groupProperty.getValue());
									break;
								}
							}

							if (null == isBase) {
								log.error("Align Vf module properties - isBase not found in DEFAULT_GROUP_VF_MODULE");
								result = false;
								return result;
							}

							String vfModuleLabel = null;
							String moduleName = groupDefinition.getName();
							Matcher matcher = pattern.matcher(moduleName);

							if (matcher.find()) {
								vfModuleLabel = matcher.group(1);
							} else {
								vfModuleLabel = moduleName;
							}

							boolean isBasePrimitive = isBase;
							boolean isVolumeGroupPrimitive = isVolumeGroup;
							String vfModuleLabelFinal = vfModuleLabel;
							List<GroupProperty> propertiesToAdd = new ArrayList<>();
							properties.stream().forEach(p -> {
								if (p.getValueUniqueUid() == null) {
									if (vfModuleProperties.containsKey(p.getName())) {
										if (isBasePrimitive) {
											p.setValue(vfModuleProperties.get(p.getName()).getForBaseModule());
										} else {
											p.setValue(vfModuleProperties.get(p.getName()).getForNonBaseModule());
										}
									} else if (p.getName().equals(VOLUME_GROUP_NAME)) {
										p.setValue(String.valueOf(isVolumeGroupPrimitive));
									} else if (p.getName().equals(LABEL_NAME)) {
										p.setValue(vfModuleLabelFinal);
									}
									propertiesToAdd.add(p);
								}

							});

							List<GroupProperty> propertiesAlreadyExistOnGraph = properties.stream().filter(p -> !(p.getValueUniqueUid() == null || p.getValueUniqueUid().isEmpty())).collect(Collectors.toList());
							int numOfPropertiesAlreadyExist = propertiesAlreadyExistOnGraph.size();

							log.debug("Need to update default values vfModule {} properties {} ", properties.size(), properties);

							Either<GroupTypeDefinition, TitanOperationStatus> groupTypeRes = groupTypeOperation.getGroupTypeByUid(groupDefinition.getTypeUid());
							if (groupTypeRes.isRight()) {
								TitanOperationStatus operationStatus = groupTypeRes.right().value();
								log.debug("Failed to find group type {}",groupDefinition.getTypeUid());
								if (operationStatus == TitanOperationStatus.NOT_FOUND) {
									result = false;
									return result;
								}
							}

							GroupTypeDefinition groupTypeDefinition = groupTypeRes.left().value();
							List<PropertyDefinition> groupTypeProperties = groupTypeDefinition.getProperties();
							Map<String, PropertyDefinition> groupTypePropertiesMap = groupTypeProperties.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));

							int i = numOfPropertiesAlreadyExist + 1;
							for (GroupProperty prop : propertiesToAdd) {
								if (prop.getUniqueId() == null || prop.getUniqueId().isEmpty()) {
									continue;
								}
								GroupData groupData = new GroupData(groupDefinition);

								Either<PropertyValueData, TitanOperationStatus> addPropertyToGroup = groupOperation.addPropertyToGroup(groupData, prop, groupTypePropertiesMap.get(prop.getName()), i);
								if (addPropertyToGroup.isRight()) {
									log.info("Failed to add properties {}  to group type :{} error {} ", prop.getName(), groupData.getUniqueId(), addPropertyToGroup.right().value());
									result = false;
									return result;
								}
								++i;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed {} with exception: ", "alignVfModuleProperties", e);
			result = false;
		} finally {
			log.info(" Align Vf module properties finished");
			if (!result) {
				log.info("Doing rollback");
				titanGenericDao.rollback();
			} else {
				log.info("Doing commit");
				titanGenericDao.commit();
			}
		}
		return true;
	}

	private ArtifactDefinition findArtifactInList(Map<String, ArtifactDefinition> deploymentArtifacts, String artifactId) {
		Optional<ArtifactDefinition> op = deploymentArtifacts.values().stream().filter(p -> p.getUniqueId().equals(artifactId)).findAny();
		if (op.isPresent())
			return op.get();
		return null;
	}

	private boolean generateTosca() {
		log.info("Regenerate  Tosca and CSAR for VFs and Services");
		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			log.error("Failed to get graph {}", graph.right().value());
			return false;
		}
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());

		User user = buildDummyUser();

		Map<String, Object> propsHasNot = new HashMap<String, Object>();
		propsHasNot.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		propsHasNot.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

		Either<List<ResourceMetadataData>, TitanOperationStatus> allResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, propsHasNot, ResourceMetadataData.class);
		if (allResources.isRight()) {
			if (allResources.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("generateTosca - no VF resources");
			} else {
				log.info("generateTosca failed fetch all resources,error {}", allResources.right().value());
				return false;
			}
		} else {
			if (!handleComponents(user, allResources.left().value(), resourceOperation)) {
				log.info("generateTosca failed generate tosca artifacts for resources");
				return false;

			}
		}
		Either<List<ServiceMetadataData>, TitanOperationStatus> allServices = titanGenericDao.getByCriteria(NodeTypeEnum.Service, null, propsHasNot, ServiceMetadataData.class);
		if (allServices.isRight()) {
			if (allServices.right().value() == TitanOperationStatus.NOT_FOUND) {
				log.debug("generateTosca - no services");

			} else {
				log.debug("generateTosca failed fetch all services,error {}",allServices.right().value());
				return false;
			}
		} else {
			if (!handleComponents(user, allServices.left().value(), serviceOperation)) {
				log.info("generateTosca failed generate tosca artifacts for services");
				return false;

			}
		}
		log.info("Regenerate  Tosca and CSAR for VFs and Services finished");
		return true;
	}

	private <T extends ComponentMetadataData> boolean handleComponents(User user, List<T> allResources, ComponentOperation operation) {
		for (ComponentMetadataData resource : allResources) {
			if (resource.getMetadataDataDefinition().isDeleted() == null || !resource.getMetadataDataDefinition().isDeleted()) {
				Either<Component, StorageOperationStatus> component = operation.getComponent((String) resource.getUniqueId(), true);
				if (component.isRight()) {
					log.info("generateTosca failed fetch component with id {} , error {}", (String) resource.getUniqueId(), component.right().value());
					return false;
				}
				if (populateToscaArtifactsWithLog(component.left().value(), user) != ActionStatus.OK) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean alignCustomizationUUID() {
		boolean result = true;
		try {
			log.info("Update customization UUID for all component instances on graph");
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			if (graph.isRight()) {
				log.error("Failed to get graph {}", graph.right().value());
				return result;
			}
			TitanGraph tGraph = graph.left().value();
			TitanGraphQuery<? extends TitanGraphQuery> query = tGraph.query();
			query = query.has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.ResourceInstance.getName());
			Iterable<TitanVertex> vertices = query.vertices();
			if (vertices == null) {
				log.info("No component instances on graph");
				return result;
			}
			Iterator<TitanVertex> iterator = vertices.iterator();
			if (!iterator.hasNext()) {
				log.info("No component instances on graph");
			}
			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				String property = (String) titanGenericDao.getProperty(vertex, GraphPropertiesDictionary.CUSTOMIZATION_UUID.getProperty());
				if (!ValidationUtils.validateStringNotEmpty(property)) {
					UUID uuid = UUID.randomUUID();
					vertex.property(GraphPropertiesDictionary.CUSTOMIZATION_UUID.getProperty(), uuid.toString());
				}
			}
		} catch (Exception e) {
			log.error("Failed {} with exception: ", "alignCustomizationUUID", e);
			result = false;
		} finally {
			log.info("Update customization UUID finished ");
			if (!result) {
				log.info("Doing rollback");
				titanGenericDao.rollback();
			} else {
				log.info("Doing commit");
				titanGenericDao.commit();
			}
		}
		return result;
	}

	private ActionStatus populateToscaArtifactsWithLog(Component component, User user) {
		ActionStatus ret = ActionStatus.OK;
		LifecycleStateEnum lifecycleState = component.getLifecycleState();
		if (!needRegenarateTosca(lifecycleState)) {
			log.debug("Component {} is in state {}, don't generatate Tosca", component.getUniqueId(), lifecycleState);
			return ret;
		}

		try {
			Either<Either<ArtifactDefinition, Operation>, ResponseFormat> populateToscaArtifacts = serviceBusinessLogic.populateToscaArtifacts(component, user, true, false, true, true);
			if (populateToscaArtifacts.isLeft()) {
				log.debug("Added payload to tosca artifacts of component {} of type:{} with uniqueId:{}", component.getName(), component.getComponentType().getValue(), component.getUniqueId());
			} else {
				log.error("Failed to generate TOSCA artifacts for component {} of type:{} with uniqueId:{}", component.getName(), component.getComponentType().name(), component.getUniqueId());
				return ActionStatus.GENERAL_ERROR;
			}
			return ret;
		} catch (Exception e) {
			log.error("Exception Occured When filling tosca artifact payload for component {} of type:{} with uniqueId:{}", component.getName(), component.getComponentType().name(), component.getUniqueId(), e);
			return ActionStatus.GENERAL_ERROR;
		}
	}

	private boolean needRegenarateTosca(LifecycleStateEnum lifecycleState) {
		if (lifecycleState == LifecycleStateEnum.READY_FOR_CERTIFICATION || lifecycleState == LifecycleStateEnum.CERTIFICATION_IN_PROGRESS || lifecycleState == LifecycleStateEnum.CERTIFIED) {
			return true;
		}
		return false;
	}

	private User buildDummyUser() {
		User user = new User();
		user.setUserId("migrationTask");
		return user;
	}

	private boolean alignGroupDataType() {
		boolean result = true;
		try {
			log.info(" Align group data type properties");
			String categoryMigrationFile = CONFIG_GROUP_TYPES_YML;
			String yamlAsString;
			try {

				InputStream inputStream = getClass().getResourceAsStream(categoryMigrationFile);
				if (inputStream == null) {
					log.info("Failed to load input file : {}", categoryMigrationFile);
					result = false;
					return result;
				}
				yamlAsString = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

			} catch (Exception e) {
				log.info("Failed to load group types file exception : ", e);
				result = false;
				return result;
			}

			log.debug("received yaml: {}", yamlAsString);

			Map<String, Object> toscaJson = (Map<String, Object>) new Yaml().load(yamlAsString);

			if (toscaJson == null || toscaJson.isEmpty()) {
				log.info("group types file is empty");
				result = false;
				return result;
			}

			Map<String, Object> vfModule = (Map<String, Object>) toscaJson.get("org.openecomp.groups.VfModule");
			if (vfModule == null || vfModule.isEmpty()) {
				log.info("No vfModule in group types file");
				result = false;
				return result;
			}
			Map<String, Object> properties = (Map<String, Object>) vfModule.get("properties");
			if (properties == null || properties.isEmpty()) {
				log.info("No properties for vfModule in group types file");
				result = false;
				return result;
			}
			Either<GroupTypeDefinition, StorageOperationStatus> latestGroupTypeByType = groupTypeOperation.getLatestGroupTypeByType("org.openecomp.groups.VfModule", true);
			if (latestGroupTypeByType.isRight()) {
				log.info("Failed to fetch org.openecomp.groups.VfModule group type, error :{}", latestGroupTypeByType.right().value());
				result = false;
				return result;
			}
			GroupTypeDefinition groupTypeInGraph = latestGroupTypeByType.left().value();
			List<PropertyDefinition> propertiesInGraph = groupTypeInGraph.getProperties();

			List<PropertyDefinition> propertiesToAdd = new ArrayList<>();

			properties.entrySet().stream().filter(e -> !ifExistOnGraph(e.getKey(), propertiesInGraph)).forEach(fe -> {
				PropertyDefinition property = new PropertyDefinition();
				property.setName(fe.getKey());
				Map<String, Object> definitionInYaml = (Map<String, Object>) fe.getValue();
				property.setType((String) definitionInYaml.get("type"));
				// Fix by Tal G
				property.setRequired((Boolean) definitionInYaml.get("required"));
				property.setDescription((String) definitionInYaml.get("description"));
				// Fix by Tal G
				String defaultValue = definitionInYaml.get("default") == null ? null : definitionInYaml.get("default").toString();
				if (defaultValue != null) {
					property.setDefaultValue(defaultValue);
				}
				propertiesToAdd.add(property);
			});

			if (!propertiesToAdd.isEmpty()) {
				log.debug("Need to add to vfModule {} properties {} ", propertiesToAdd.size(), propertiesToAdd);

				Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToCapablityType = propertyOperation.addPropertiesToElementType(groupTypeInGraph.getUniqueId(), NodeTypeEnum.GroupType, propertiesToAdd);
				if (addPropertiesToCapablityType.isRight()) {
					log.info("Failed to add properties to group type :{}", addPropertiesToCapablityType.right().value());
					result = false;
					return result;
				}
			} else {
				log.debug("No properties to add to vfModule");
			}

		} catch (Exception e) {
			log.error("Failed {} with exception: ", "alignGroupDataType", e);
			result = false;
		} finally {
			log.info(" Align group data type properties finished");
			if (!result) {
				log.info("Doing rollback");
				titanGenericDao.rollback();
			} else {
				log.info("Doing commit");
				titanGenericDao.commit();
			}
		}
		return result;
	}

	private boolean ifExistOnGraph(String name, List<PropertyDefinition> propertiesInGraph) {
		for (PropertyDefinition pd : propertiesInGraph) {
			if (pd.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean alignDataType() {

		log.info(" Align data type properties");

		boolean isSuccessful = true;
		List<DataTypeDefinition> dataTypes = extractDataTypesFromYaml();

		if (CollectionUtils.isEmpty(dataTypes)) {
			isSuccessful = false;
		}

		List<ImmutablePair<DataTypeDefinition, Boolean>> createdElementTypes = new ArrayList<>();

		Iterator<DataTypeDefinition> elementTypeItr = dataTypes.iterator();
		if (isSuccessful) {
			try {
				while (elementTypeItr.hasNext()) {
					DataTypeDefinition elementType = elementTypeItr.next();
					String elementName = elementType.getName();
					Either<ActionStatus, ResponseFormat> validateElementType = validateDataType(elementType);
					if (validateElementType.isRight()) {
						log.debug("Failed to validate data type {}. Status is {}. ", elementName, validateElementType.right().value());
						isSuccessful = false;
						break;
					}
					log.debug("Going to get data type by name {}. ", elementName);
					Either<DataTypeDefinition, StorageOperationStatus> findElementType = propertyOperation.getDataTypeByNameWithoutDerived(elementName);
					if (findElementType.isRight()) {
						StorageOperationStatus status = findElementType.right().value();
						if (status != StorageOperationStatus.NOT_FOUND) {
							log.debug("Failed to fetch data type {}. Status is {}. ", elementName, validateElementType.right().value());
							isSuccessful = false;
							break;
						} else {
							log.debug("Going to add data type with name {}. ", elementName);
							Either<DataTypeDefinition, StorageOperationStatus> dataModelResponse = propertyOperation.addDataType(elementType);

							if (dataModelResponse.isRight()) {
								if (dataModelResponse.right().value() != StorageOperationStatus.SCHEMA_VIOLATION) {
									log.debug("Failed to add data type {}. Status is {}. ", elementName, dataModelResponse.right().value());
									isSuccessful = false;
									break;
								} else {
									createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(elementType, false));
								}
							} else {
								createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(dataModelResponse.left().value(), true));
							}

						}
					} else {
						DataTypeDefinition dataTypeDefinition = findElementType.left().value();
						log.debug("Going to update data type with name {}. ", elementName);
						Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteDataTypeRes = propertyOperation.deleteAllPropertiesAssociatedToNode(NodeTypeEnum.DataType, dataTypeDefinition.getUniqueId());
						if (deleteDataTypeRes.isRight()) {
							StorageOperationStatus status = deleteDataTypeRes.right().value();
							if (status != StorageOperationStatus.OK) {

								log.debug("Failed to update data type {}. Status is {}. ", elementName, deleteDataTypeRes.right().value());
								isSuccessful = false;
								break;
							}
						}

						Either<Map<String, PropertyData>, TitanOperationStatus> updateDataTypeRes = propertyOperation.addPropertiesToElementType(dataTypeDefinition.getUniqueId(), NodeTypeEnum.DataType, elementType.getProperties());

						if (updateDataTypeRes.isRight()) {
							TitanOperationStatus status = updateDataTypeRes.right().value();

							log.debug("Failed to update data type {}. Status is {}. ", elementName, updateDataTypeRes.right().value());
							isSuccessful = false;
							break;

						} else {
							createdElementTypes.add(new ImmutablePair<DataTypeDefinition, Boolean>(elementType, true));
						}

						DataTypeData dataTypeData = new DataTypeData();
						dataTypeData.setDataTypeDataDefinition(elementType);
						dataTypeData.getDataTypeDataDefinition().setUniqueId(dataTypeDefinition.getUniqueId());
						long modificationTime = System.currentTimeMillis();
						dataTypeData.getDataTypeDataDefinition().setModificationTime(modificationTime);

						Either<DataTypeData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(dataTypeData, DataTypeData.class);
						if (updateNode.isRight()) {
							TitanOperationStatus operationStatus = updateNode.right().value();
							log.debug("Failed to update modification time data type {} from graph. status is {}",
									dataTypeDefinition.getUniqueId() ,operationStatus);
							BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToDataType", "Failed to fetch data type. Status is " + operationStatus, ErrorSeverity.ERROR);
							isSuccessful = false;
							break;
						} else {
							log.debug("Update data type uid {}. Set modification time to {}", dataTypeDefinition.getUniqueId(), modificationTime);
							isSuccessful = true;
						}
					}
				}
			} finally {
				log.info(" Finish to align data type properties");
				if (isSuccessful) {
					propertyOperation.getTitanGenericDao().commit();
				} else {
					propertyOperation.getTitanGenericDao().rollback();
				}
			}
		}
		return isSuccessful;
	}

	@SuppressWarnings("unchecked")
	private List<DataTypeDefinition> extractDataTypesFromYaml() {
		String dataTypeYmlFilePath = CONFIG_DATA_TYPES_YML;
		String yamlAsString;
		try {

			InputStream inputStream = getClass().getResourceAsStream(dataTypeYmlFilePath);
			if (inputStream == null) {
				log.info("Failed to load input file : {}", dataTypeYmlFilePath);
				return null;
			}
			yamlAsString = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

		} catch (Exception e) {
			log.info("Failed to load group types file exception : ", e);
			return null;
		}

		log.debug("received yaml: {}", yamlAsString);

		String dataTypeName;
		List<DataTypeDefinition> dataTypes = new ArrayList<>();

		Map<String, Object> toscaJson = (Map<String, Object>) new Yaml().load(yamlAsString);
		Iterator<Entry<String, Object>> elementTypesEntryItr = toscaJson.entrySet().iterator();
		while (elementTypesEntryItr.hasNext()) {
			Entry<String, Object> elementTypeNameDataEntry = elementTypesEntryItr.next();
			dataTypeName = elementTypeNameDataEntry.getKey();
			Map<String, Object> elementTypeJsonData = (Map<String, Object>) elementTypeNameDataEntry.getValue();

			DataTypeDefinition dataType = new DataTypeDefinition();
			dataType.setName(dataTypeName);

			if (elementTypeJsonData != null) {

				if (elementTypeJsonData.containsKey(ToscaTagNamesEnum.DESCRIPTION.getElementName())) {
					dataType.setDescription((String) elementTypeJsonData.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
				}
				if (elementTypeJsonData.containsKey(ToscaTagNamesEnum.DERIVED_FROM.getElementName())) {
					dataType.setDerivedFromName((String) elementTypeJsonData.get(ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
				}
				List<PropertyDefinition> properties = getProperties(elementTypeJsonData);
				if (elementTypeJsonData.containsKey(ToscaTagNamesEnum.PROPERTIES.getElementName())) {
					dataType.setProperties(properties);
				}
			}
			dataTypes.add(dataType);
		}

		return dataTypes;
	}

	private List<PropertyDefinition> getProperties(Map<String, Object> toscaJson) {
		List<PropertyDefinition> values = null;
		Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = ImportUtils.getProperties(toscaJson);

		if (properties.isLeft()) {
			values = new ArrayList<>();
			Map<String, PropertyDefinition> propertiesMap = properties.left().value();
			if (propertiesMap != null && propertiesMap.isEmpty() == false) {

				for (Entry<String, PropertyDefinition> entry : propertiesMap.entrySet()) {
					String propName = entry.getKey();
					PropertyDefinition propertyDefinition = entry.getValue();
					PropertyDefinition newPropertyDefinition = new PropertyDefinition(propertyDefinition);
					newPropertyDefinition.setName(propName);
					values.add(newPropertyDefinition);
				}
			}
		}

		return values;
	}

	private Either<ActionStatus, ResponseFormat> validateDataType(DataTypeDefinition dataType) {

		String dataTypeName = dataType.getName();
		List<PropertyDefinition> properties = dataType.getProperties();
		if (properties == null) {
			// At least one parameter should be defined either in the properties
			// section or at one of the parents
			String derivedDataType = dataType.getDerivedFromName();
			// If there are no properties, then we can create a data type if it
			// is an abstract one or it derives from non abstract data type
			if ((derivedDataType == null || derivedDataType.isEmpty())) {
				if (false == isAbstract(dataType.getName())) {
					if (false == ToscaPropertyType.isScalarType(dataTypeName)) {
						log.debug("Data type {} must have properties unless it derives from non abstract data type",dataType.getName());
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM, dataType, null);

						return Either.right(responseFormat);
					}
				}
			} else {
				// if it is not a scalar data type and it derives from abstract
				// data type, we should reject the request.
				if (false == ToscaPropertyType.isScalarType(dataTypeName) && true == isAbstract(derivedDataType)) {
					log.debug("Data type {} which derived from abstract data type must have at least one property",dataType.getName());
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_NOR_PROPERTIES_NEITHER_DERIVED_FROM, dataType, null);

					return Either.right(responseFormat);
				}
			}
		} else {
			// properties tag cannot be empty
			if (properties.isEmpty()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROPERTIES_CANNOT_BE_EMPTY, dataType, null);

				return Either.right(responseFormat);
			}

			// check no duplicates
			Set<String> collect = properties.stream().map(p -> p.getName()).collect(Collectors.toSet());
			if (collect != null) {
				if (properties.size() != collect.size()) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_DUPLICATE_PROPERTY, dataType, null);

					return Either.right(responseFormat);
				}
			}

			List<String> propertiesWithSameTypeAsDataType = properties.stream().filter(p -> p.getType().equals(dataType.getName())).map(p -> p.getName()).collect(Collectors.toList());
			if (propertiesWithSameTypeAsDataType != null && propertiesWithSameTypeAsDataType.isEmpty() == false) {
				log.debug("The data type contains properties with the type {}",dataType.getName(),dataType.getName());
				ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROEPRTY_CANNOT_HAVE_SAME_TYPE_OF_DATA_TYPE, dataType, propertiesWithSameTypeAsDataType);

				return Either.right(responseFormat);
			}
		}

		String derivedDataType = dataType.getDerivedFromName();
		if (derivedDataType != null) {
			Either<DataTypeDefinition, StorageOperationStatus> derivedDataTypeByName = propertyOperation.getDataTypeByName(derivedDataType, true);
			if (derivedDataTypeByName.isRight()) {
				StorageOperationStatus status = derivedDataTypeByName.right().value();
				if (status == StorageOperationStatus.NOT_FOUND) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_DERIVED_IS_MISSING, dataType, null);

					return Either.right(responseFormat);
				} else {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.GENERAL_ERROR, dataType, null);

					return Either.right(responseFormat);

				}
			} else {

				DataTypeDefinition derivedDataTypeDef = derivedDataTypeByName.left().value();
				if (properties != null && properties.isEmpty() == false) {

					if (true == isScalarType(derivedDataTypeDef)) {
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_CANNOT_HAVE_PROPERTIES, dataType, null);

						return Either.right(responseFormat);
					}

					Set<String> allParentsProps = new HashSet<>();
					do {
						List<PropertyDefinition> currentParentsProps = derivedDataTypeDef.getProperties();
						if (currentParentsProps != null) {
							for (PropertyDefinition propertyDefinition : currentParentsProps) {
								allParentsProps.add(propertyDefinition.getName());
							}
						}
						derivedDataTypeDef = derivedDataTypeDef.getDerivedFrom();
					} while (derivedDataTypeDef != null);

					// Check that no property is already defined in one of the
					// ancestors
					Set<String> alreadyExistPropsCollection = properties.stream().filter(p -> allParentsProps.contains(p.getName())).map(p -> p.getName()).collect(Collectors.toSet());
					if (alreadyExistPropsCollection != null && alreadyExistPropsCollection.isEmpty() == false) {
						List<String> duplicateProps = new ArrayList<>();
						duplicateProps.addAll(alreadyExistPropsCollection);
						ResponseFormat responseFormat = componentsUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_PROPERTY_ALREADY_DEFINED_IN_ANCESTOR, dataType, duplicateProps);

						return Either.right(responseFormat);
					}

				}
			}
		}
		return Either.left(ActionStatus.OK);
	}

	private boolean isAbstract(String dataTypeName) {

		ToscaPropertyType isPrimitiveToscaType = ToscaPropertyType.isValidType(dataTypeName);

		return isPrimitiveToscaType != null && isPrimitiveToscaType.isAbstract() == true;

	}

	private boolean isScalarType(DataTypeDefinition dataTypeDef) {

		boolean isScalar = false;
		DataTypeDefinition dataType = dataTypeDef;

		while (dataType != null) {

			String name = dataType.getName();
			if (ToscaPropertyType.isScalarType(name)) {
				isScalar = true;
				break;
			}

			dataType = dataType.getDerivedFrom();
		}

		return isScalar;
	}

}
