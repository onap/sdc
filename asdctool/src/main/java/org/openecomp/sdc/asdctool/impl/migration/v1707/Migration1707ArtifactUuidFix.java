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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

@org.springframework.stereotype.Component("migration1707UuidFix")
public class Migration1707ArtifactUuidFix {

	@Autowired
	private TitanDao titanDao;

	@Autowired
	private ToscaOperationFacade toscaOperationFacade;

	private static Logger log = LoggerFactory.getLogger(Migration1707ArtifactUuidFix.class.getName());

	public boolean migrate(String fixComponent, String runMode) {
		List<Resource> vfLst = new ArrayList<>();
		List<Service> serviceList = new ArrayList<>();

		long time = System.currentTimeMillis();

		if (fixComponent.equals("vf_only")) {
			if (fetchFaultVf(fixComponent, vfLst, time) == false) {
				return false;
			}
		} else {
			if (fetchServices(fixComponent, serviceList, time) == false) {
				return false;
			}
		}
		if (runMode.equals("service_vf") || runMode.equals("fix")) {
			log.info("Mode {}. Find problem VFs", runMode);
			if (fetchVf(serviceList, vfLst, time) == false) {
				log.info("Mode {}. Find problem VFs finished with failure", runMode);
				return false;
			}
			log.info("Mode {}. Find problem VFs finished with success", runMode);
		}
		if (runMode.equals("fix") || runMode.equals("fix_only_services")) {
			log.info("Mode {}. Start fix", runMode);
			if (fix(vfLst, serviceList) == false) {
				log.info("Mode {}. Fix finished withh failure", runMode);
				return false;
			}
			log.info("Mode {}. Fix finished withh success", runMode);
		}

		return true;
	}

	private boolean fetchFaultVf(String fixComponent, List<Resource> vfLst, long time) {
		log.info("Find fault VF ");
		Writer writer = null;
		try {
			String fileName = "fault_" + time + ".csv";
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write("vf name, vf id, state, version\n");

			Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
			hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
			hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF.name());

			Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
			hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
			log.info("Try to fetch resources with properties {} and not {}", hasProps, hasNotProps);

			Either<List<GraphVertex>, TitanOperationStatus> servicesByCriteria = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll);
			if (servicesByCriteria.isRight()) {
				log.info("Failed to fetch resources {}", servicesByCriteria.right().value());
				return false;
			}
			List<GraphVertex> resources = servicesByCriteria.left().value();
			for (GraphVertex gv : resources) {
				ComponentParametersView filter = new ComponentParametersView(true);
				filter.setIgnoreComponentInstances(false);
				filter.setIgnoreArtifacts(false);
				filter.setIgnoreGroups(false);

				Either<Resource, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaElement(gv.getUniqueId());
				if (toscaElement.isRight()) {
					log.info("Failed to fetch resources {} {}", gv.getUniqueId(), toscaElement.right().value());
					return false;
				}

				Resource resource = toscaElement.left().value();
				String resourceName = resource.getName();
				Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
				List<GroupDefinition> groups = resource.getGroups();
				if (groups == null || groups.isEmpty()) {
					log.info("No groups for resource {} id {} ", resourceName, gv.getUniqueId());
					continue;
				}
				boolean isProblematic = false;
				for (GroupDefinition gr : groups) {
					if (gr.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
						if (isProblematicGroup(gr, resourceName, deploymentArtifacts)) {
							isProblematic = true;
							break;
						}
					}
				}
				if (isProblematic) {
					vfLst.add(resource);
					writeModuleResultToFile(writer, resource, null);
					writer.flush();
				}
				titanDao.commit();
			}

		} catch (Exception e) {
			log.info("Failed to fetch vf resources ", e);
			return false;
		} finally {
			titanDao.commit();
			try {
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}
		return true;
	}

	private boolean fetchVf(List<Service> serviceList, List<Resource> vfLst, long time) {
		log.info("Find problem VF ");
		if (serviceList.isEmpty()) {
			log.info("No services as input");
			return true;
		}
		Writer writer = null;
		try {
			String fileName = "problemVf_" + time + ".csv";
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write("vf name, vf id, state, version, example service name\n");
			Set<String> vfIds = new HashSet<>();
			for (Service service : serviceList) {
				List<ComponentInstance> componentInstances = service.getComponentInstances();
				for (ComponentInstance ci : componentInstances) {
					if (!vfIds.contains(ci.getComponentUid())) {
						vfIds.add(ci.getComponentUid());
						Either<Resource, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaElement(ci.getComponentUid());
						if (toscaElement.isRight()) {
							log.info("Failed to fetch resource {} {}", ci.getComponentUid(), toscaElement.right().value());
							return false;
						}
						Resource resource = toscaElement.left().value();
						vfLst.add(resource);
						writeModuleResultToFile(writer, resource, service);
						writer.flush();
						titanDao.commit();
					}
				}
			}
			log.info("output file with list of Vf : {}", fileName);
		} catch (Exception e) {
			log.info("Failed to fetch services ", e);
			return false;
		} finally {
			titanDao.commit();
			try {
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}
		return true;
	}

	private boolean fetchServices(String fixServices, List<Service> serviceList, long time) {
		log.info("Find problem Services {}", fixServices);
		Writer writer = null;

		try {
			String fileName = "problemService_" + time + ".csv";
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write("service name, service id, state, version\n");

			Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
			hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
			if (fixServices.equals("distributed_only")) {
				hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
				hasProps.put(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTED.name());
			}

			Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
			hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
			log.info("Try to fetch services with properties {} and not {}", hasProps, hasNotProps);

			Either<List<GraphVertex>, TitanOperationStatus> servicesByCriteria = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll);
			if (servicesByCriteria.isRight()) {
				log.info("Failed to fetch services {}", servicesByCriteria.right().value());
				return false;
			}
			List<GraphVertex> services = servicesByCriteria.left().value();
			for (GraphVertex gv : services) {
				ComponentParametersView filter = new ComponentParametersView(true);
				filter.setIgnoreComponentInstances(false);
				filter.setIgnoreArtifacts(false);
				filter.setIgnoreGroups(false);

				Either<Service, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaElement(gv.getUniqueId());
				if (toscaElement.isRight()) {
					log.info("Failed to fetch service {} {}", gv.getUniqueId(), toscaElement.right().value());
					return false;
				}
				Service service = toscaElement.left().value();
				List<ComponentInstance> componentInstances = service.getComponentInstances();
				boolean isProblematic = false;
				if (componentInstances == null) {
					log.info("No instances for service {} ", gv.getUniqueId());
					continue;
				}
				String serviceName = (String) gv.getMetadataProperty(GraphPropertyEnum.NAME);

				for (ComponentInstance ci : componentInstances) {
					Map<String, ArtifactDefinition> deploymentArtifacts = ci.getDeploymentArtifacts();
					List<GroupInstance> groupInstances = ci.getGroupInstances();
					if (groupInstances == null || groupInstances.isEmpty()) {
						log.info("No instance groups for instance {} in service {} id {} ", ci.getName(), serviceName, gv.getUniqueId());
						continue;
					}

					for (GroupInstance gi : groupInstances) {
						if (gi.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
							if (isProblematicGroupInstance(gi, ci.getName(), serviceName, deploymentArtifacts)) {
								isProblematic = true;
								break;
							}
						}
					}
					if (isProblematic) {
						serviceList.add(service);
						writeModuleResultToFile(writer, service, null);
						writer.flush();
						break;
					}
				}
				titanDao.commit();
			}
			log.info("output file with list of services : {}", fileName);
		} catch (Exception e) {
			log.info("Failed to fetch services ", e);
			return false;
		} finally {
			titanDao.commit();
			try {
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}
		return true;
	}

	private boolean isProblematicGroup(GroupDefinition gr, String resourceName, Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifacts = gr.getArtifacts();
		List<String> artifactsUuid = gr.getArtifactsUuid();

		if ((artifactsUuid == null || artifactsUuid.isEmpty()) && (artifacts == null || artifacts.isEmpty())) {
			log.info("No groups in resource {} ", resourceName);
			return false;
		}
		if (artifacts.size() < artifactsUuid.size()) {
			log.info(" artifacts.size() < artifactsUuid.size() group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}
		if (artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty())) {
			log.info(" artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty() group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}
		if (artifactsUuid.contains(null)) {
			log.info(" artifactsUuid.contains(null) group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}

		for (String artifactId : artifacts) {
			String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactlabel);
			if (artifactDefinition == null) {
				log.info(" artifactDefinition == null label {} group {} in resource {} ", artifactlabel, gr.getName(), resourceName);
				return true;
			}
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				if (!artifactId.equals(artifactDefinition.getUniqueId())) {
					log.info(" !artifactId.equals(artifactDefinition.getUniqueId() artifact {}  artId {} group {} in resource {} ", artifactlabel, artifactId, gr.getName(), resourceName);
					return true;
				}
				if (!artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					log.info(" artifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} group {} in resource {} ", artifactlabel, gr.getName(), resourceName);
					return true;
				}
			}
		}
		return false;
	}

	private boolean isProblematicGroupInstance(GroupInstance gi, String instName, String servicename, Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifacts = gi.getArtifacts();
		List<String> artifactsUuid = gi.getArtifactsUuid();
		List<String> instArtifactsUuid = gi.getGroupInstanceArtifactsUuid();

		if ((artifactsUuid == null || artifactsUuid.isEmpty()) && (artifacts == null || artifacts.isEmpty())) {
			log.info("No instance groups for instance {} in service {} ", instName, servicename);
			return false;
		}
		if (artifacts.size() < artifactsUuid.size()) {
			log.info(" artifacts.size() < artifactsUuid.size() inst {} in service {} ", instName, servicename);
			return true;
		}
		if (artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty())) {
			log.info(" artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty() inst {} in service {} ", instName, servicename);
			return true;
		}
		if (artifactsUuid.contains(null)) {
			log.info(" artifactsUuid.contains(null) inst {} in service {} ", instName, servicename);
			return true;
		}

		for (String artifactId : artifacts) {
			String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactlabel);
			if (artifactDefinition == null) {
				log.info(" artifactDefinition == null label {} inst {} in service {} ", artifactlabel, instName, servicename);
				return true;
			}
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				if (!artifactId.equals(artifactDefinition.getUniqueId())) {
					log.info(" !artifactId.equals(artifactDefinition.getUniqueId() artifact {}  artId {} inst {} in service {} ", artifactlabel, artifactId, instName, servicename);
					return true;
				}
				if (!artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					log.info(" artifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} inst {} in service {} ", artifactlabel, instName, servicename);
					return true;
				}
			} else {
				if (!instArtifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					log.info(" instArtifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} inst {} in service {} ", artifactlabel, instName, servicename);
					return true;
				}
			}
		}
		return false;
	}

	private boolean fix(List<Resource> vfLst, List<Service> serviceList) {
		boolean res = true;

		if (vfLst != null && !vfLst.isEmpty()) {
			res = fixVf(vfLst);
			if (res) {
				for (Component component : vfLst) {
					TopologyTemplate topologyTemplate = ModelConverter.convertToToscaElement(component);
					Map<String, GroupDataDefinition> groups = topologyTemplate.getGroups();
					res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.GROUPS, EdgeLabelEnum.GROUPS, groups);
				}
			}
		}

		if (res == true && serviceList != null && !serviceList.isEmpty()) {
			res = fixServices(serviceList);
			if (res) {
				for (Component component : serviceList) {
					TopologyTemplate topologyTemplate = ModelConverter.convertToToscaElement(component);
					Map<String, MapGroupsDataDefinition> groups = topologyTemplate.getInstGroups();
					res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, groups);
				}
			}
		}

		return res;
	}

	private <T extends ToscaDataDefinition> boolean fixDataOnGraph(String componentId, VertexTypeEnum vertexTypeEnum, EdgeLabelEnum edgeLabelEnum, Map<String, T> groups) {
		boolean res = true;
		Either<GraphVertex, TitanOperationStatus> getResponse = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
		if (getResponse.isRight()) {
			log.debug("Couldn't fetch component  unique id {}, error: {}", componentId, getResponse.right().value());
			res = false;

		}
		if (res) {
			GraphVertex componentVertex = getResponse.left().value();

			GraphVertex toscaDataVertex = null;
			Either<GraphVertex, TitanOperationStatus> groupVertexEither = titanDao.getChildVertex(componentVertex, edgeLabelEnum, JsonParseFlagEnum.ParseJson);
			if (groupVertexEither.isRight()) {
				res = false;
				log.debug("failed to get child {}  vertex for component  unique id {}, error: {}", edgeLabelEnum, componentId, groupVertexEither.right().value());
			}
			if (res) {
				toscaDataVertex = groupVertexEither.left().value();
				toscaDataVertex.setJson(groups);
				Either<GraphVertex, TitanOperationStatus> updatevertexEither = titanDao.updateVertex(toscaDataVertex);
				if (updatevertexEither.isRight()) {
					log.debug("failed to update vertex for component  unique id {}, error: {}", componentId, updatevertexEither.right().value());
					titanDao.rollback();
					return false;
				}
			}
		}

		titanDao.commit();

		return res;
	}

	private boolean fixServices(List<Service> serviceList) {
		for (Service service : serviceList) {
			log.debug("Migration1707ArtifactUuidFix  fix service: id {},  name {} ", service.getUniqueId(), service.getName());
			List<ComponentInstance> instances = service.getComponentInstances();
			for (ComponentInstance instance : instances) {
				Map<String, ArtifactDefinition> artifactsMap = instance.getDeploymentArtifacts();
				List<GroupInstance> groupsList = instance.getGroupInstances();
				if (groupsList != null && artifactsMap != null) {
					for (GroupInstance group : groupsList) {
						if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
							log.debug("Migration1707ArtifactUuidFix  fix group:  resource id {}, group name {} ", service.getUniqueId(), group.getName());
							List<String> groupArtifacts = new ArrayList<String>(group.getArtifacts());

							group.getArtifacts().clear();
							group.getArtifactsUuid().clear();
							group.getGroupInstanceArtifacts().clear();
							group.getGroupInstanceArtifactsUuid().clear();

							for (String artifactId : groupArtifacts) {
								String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
								log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} artifactId for fix {} artifactlabel {} ", group.getName(), artifactId, artifactlabel);
								if (!artifactlabel.isEmpty() && artifactsMap.containsKey(artifactlabel)) {
									ArtifactDefinition artifact = artifactsMap.get(artifactlabel);
									ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
									String correctArtifactId = artifact.getUniqueId();
									String correctArtifactUUID = artifact.getArtifactUUID();
									if (artifactType != ArtifactTypeEnum.HEAT_ENV) {

										log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ", group.getName(), correctArtifactId, correctArtifactUUID);
										group.getArtifacts().add(correctArtifactId);
										if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
											group.getArtifactsUuid().add(correctArtifactUUID);
										}
									} else {
										log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ", group.getName(), correctArtifactId, correctArtifactUUID);
										group.getGroupInstanceArtifacts().add(correctArtifactId);
										if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
											group.getGroupInstanceArtifactsUuid().add(correctArtifactUUID);
										}
									}
								}
							}
						}
					}
				}
			}

		}
		return true;

	}

	private boolean fixVf(List<Resource> vfLst) {
		for (Resource resource : vfLst) {
			log.debug("Migration1707ArtifactUuidFix  fix resource: id {},  name {} ", resource.getUniqueId(), resource.getName());
			Map<String, ArtifactDefinition> artifactsMap = resource.getDeploymentArtifacts();
			List<GroupDefinition> groupsList = resource.getGroups();
			if (groupsList != null && artifactsMap != null) {
				for (GroupDefinition group : groupsList) {
					if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE) && group.getArtifacts() != null) {
						log.debug("Migration1707ArtifactUuidFix  fix group:  resource id {}, group name {} ", resource.getUniqueId(), group.getName());
						List<String> groupArtifacts = new ArrayList<String>(group.getArtifacts());
						group.getArtifacts().clear();
						group.getArtifactsUuid().clear();
						for (String artifactId : groupArtifacts) {
							String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
							log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} artifactId for fix {} artifactlabel {} ", group.getName(), artifactId, artifactlabel);
							if (!artifactlabel.isEmpty() && artifactsMap.containsKey(artifactlabel)) {
								ArtifactDefinition artifact = artifactsMap.get(artifactlabel);
								String correctArtifactId = artifact.getUniqueId();
								String correctArtifactUUID = artifact.getArtifactUUID();
								log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ", group.getName(), correctArtifactId, correctArtifactUUID);
								group.getArtifacts().add(correctArtifactId);
								if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
									group.getArtifactsUuid().add(correctArtifactUUID);
								}

							}
						}
					}
				}
			}

		}

		return true;
	}

	private String findArtifactLabelFromArtifactId(String artifactId) {
		String artifactLabel = "";

		int index = artifactId.lastIndexOf(".");
		if (index > 0 && index + 1 < artifactId.length())
			artifactLabel = artifactId.substring(index + 1);
		return artifactLabel;
	}

	private void writeModuleResultToFile(Writer writer, org.openecomp.sdc.be.model.Component component, Service service) {
		try {
			// "service name, service id, state, version
			StringBuffer sb = new StringBuffer(component.getName());
			sb.append(",").append(component.getUniqueId()).append(",").append(component.getLifecycleState()).append(",").append(component.getVersion());
			if (service != null) {
				sb.append(",").append(service.getName());
			}
			sb.append("\n");
			writer.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
