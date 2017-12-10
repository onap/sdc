package org.openecomp.sdc.asdctool.impl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.components.distribution.engine.VfModuleArtifactPayload;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
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
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

@org.springframework.stereotype.Component("artifactUuidFix")
public class ArtifactUuidFix {

	@Autowired
	private TitanDao titanDao;

	@Autowired
	private ToscaOperationFacade toscaOperationFacade;
	@Autowired
	private ToscaExportHandler toscaExportUtils;
	@Autowired
	private ArtifactCassandraDao artifactCassandraDao;

	@Autowired
	private CsarUtils csarUtils;

	private static Logger log = LoggerFactory.getLogger(ArtifactUuidFix.class.getName());

	public boolean doFix(String fixComponent, String runMode) {
		List<Resource> vfLst = new ArrayList<>();
		List<Service> serviceList = new ArrayList<>();
		Map<String, List<Component>> nodeToFixTosca = new HashMap<>();
		Map<String, List<Component>> vfToFixTosca = new HashMap<>();
		Map<String, List<Component>> serviceToFixTosca = new HashMap<>();

		long time = System.currentTimeMillis();

		doFixTosca(nodeToFixTosca, vfToFixTosca, serviceToFixTosca);

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
			if (fix(vfLst, serviceList, nodeToFixTosca, vfToFixTosca, serviceToFixTosca) == false) {
				log.info("Mode {}. Fix finished with failure", runMode);
				return false;
			}
			log.info("Mode {}. Fix finished with success", runMode);
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

			Either<List<GraphVertex>, TitanOperationStatus> servicesByCriteria = titanDao
					.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll);
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

				Either<Resource, StorageOperationStatus> toscaElement = toscaOperationFacade
						.getToscaElement(gv.getUniqueId());
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
				/* ignore */
			}
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
				List<ComponentInstance> componentInstances = service.getComponentInstances().stream()
						.filter(ci -> ci.getOriginType().equals(OriginTypeEnum.VF)).collect(Collectors.toList());
				for (ComponentInstance ci : componentInstances) {
					if (!vfIds.contains(ci.getComponentUid())) {
						vfIds.add(ci.getComponentUid());
						Either<Resource, StorageOperationStatus> toscaElement = toscaOperationFacade
								.getToscaElement(ci.getComponentUid());
						if (toscaElement.isRight()) {
							log.info("Failed to fetch resource {} {}", ci.getComponentUid(),
									toscaElement.right().value());
							return false;
						}
						Resource resource = toscaElement.left().value();
						if (resource.getResourceType().equals(ResourceTypeEnum.VF)) {
							vfLst.add(resource);
							writeModuleResultToFile(writer, resource, service);
							writer.flush();
							titanDao.commit();
						}
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
				/* ignore */
			}
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

			Either<List<GraphVertex>, TitanOperationStatus> servicesByCriteria = titanDao
					.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll);
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

				Either<Service, StorageOperationStatus> toscaElement = toscaOperationFacade
						.getToscaElement(gv.getUniqueId());
				if (toscaElement.isRight()) {
					log.info("Failed to fetch service {} {}", gv.getUniqueId(), toscaElement.right().value());
					continue;
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
						log.info("No instance groups for instance {} in service {} id {} ", ci.getName(), serviceName,
								gv.getUniqueId());
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
				/* ignore */
			}
		}
		return true;
	}

	private boolean isProblematicGroup(GroupDefinition gr, String resourceName,
			Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifacts = gr.getArtifacts();
		List<String> artifactsUuid = gr.getArtifactsUuid();
		Set<String> artifactsSet = new HashSet<>();

		if ((artifactsUuid == null || artifactsUuid.isEmpty()) && (artifacts == null || artifacts.isEmpty())) {
			log.info("No groups in resource {} ", resourceName);
			return true;
		}
		artifactsSet.addAll(artifacts);
		if (artifactsSet.size() < artifacts.size()) {
			log.info(" artifactsSet.size() < artifacts.size() group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}

		if (artifacts.size() < artifactsUuid.size()) {
			log.info(" artifacts.size() < artifactsUuid.size() group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}
		if (artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty())) {
			log.info(
					" artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty() group {} in resource {} ",
					gr.getName(), resourceName);
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
				log.info(" artifactDefinition == null label {} group {} in resource {} ", artifactlabel, gr.getName(),
						resourceName);
				return true;
			}
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				if (!artifactId.equals(artifactDefinition.getUniqueId())) {
					log.info(
							" !artifactId.equals(artifactDefinition.getUniqueId() artifact {}  artId {} group {} in resource {} ",
							artifactlabel, artifactId, gr.getName(), resourceName);
					return true;
				}
				if (!artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					log.info(
							" artifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} group {} in resource {} ",
							artifactlabel, gr.getName(), resourceName);
					return true;
				}
			}
		}
		for (String artifactUUID : artifactsUuid) {
			String label = findArtifactLabelFromArtifactId(artifactUUID);
			if (label != null && !label.isEmpty() && !label.equals("")) {
				return true;
			}
		}

		return false;
	}

	private boolean isProblematicGroupInstance(GroupInstance gi, String instName, String servicename,
			Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifacts = gi.getArtifacts();
		List<String> artifactsUuid = gi.getArtifactsUuid();
		List<String> instArtifactsUuid = gi.getGroupInstanceArtifactsUuid();
		List<String> instArtifactsId = gi.getGroupInstanceArtifacts();
		Set<String> instArtifatIdSet = new HashSet<>();
		Set<String> artifactsSet = new HashSet<>();

		if ((artifactsUuid == null || artifactsUuid.isEmpty()) && (artifacts == null || artifacts.isEmpty())) {
			log.info("No instance groups for instance {} in service {} ", instName, servicename);
			return true;
		}
		artifactsSet.addAll(artifacts);
		if (artifactsSet.size() < artifacts.size()) {
			log.info(" artifactsSet.size() < artifacts.size() group {} in resource {} ", instName, servicename);
			return true;
		}

		if (instArtifactsId != null && !instArtifactsId.isEmpty()) {
			instArtifatIdSet.addAll(instArtifactsId);
		}

		if (artifacts.size() < artifactsUuid.size()) {
			log.info(" artifacts.size() < artifactsUuid.size() inst {} in service {} ", instName, servicename);
			return true;
		}
		if (!artifacts.isEmpty() && (artifactsUuid == null || artifactsUuid.isEmpty())) {
			log.info(
					" artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty() inst {} in service {} ",
					instName, servicename);
			return true;
		}
		if (artifactsUuid.contains(null)) {
			log.info(" artifactsUuid.contains(null) inst {} in service {} ", instName, servicename);
			return true;
		}
		if (instArtifactsId != null && instArtifatIdSet.size() < instArtifactsId.size()) {
			log.info(" instArtifatIdSet.size() < instArtifactsId.size() inst {} in service {} ", instName, servicename);
			return true;
		}

		if ((instArtifactsId != null && instArtifactsUuid != null)
				&& instArtifactsId.size() != instArtifactsUuid.size()) {
			log.info(" instArtifactsId.size() != instArtifactsUuid.size() inst {} in service {} ", instName,
					servicename);
			return true;
		}

		for (String artifactId : artifacts) {
			String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactlabel);
			if (artifactDefinition == null) {
				log.info(" artifactDefinition == null label {} inst {} in service {} ", artifactlabel, instName,
						servicename);
				return true;
			}
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				if (!artifactId.equals(artifactDefinition.getUniqueId())) {
					log.info(
							" !artifactId.equals(artifactDefinition.getUniqueId() artifact {}  artId {} inst {} in service {} ",
							artifactlabel, artifactId, instName, servicename);
					return true;
				}
				if (!artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					log.info(
							" artifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} inst {} in service {} ",
							artifactlabel, instName, servicename);
					return true;
				}
			} else {
				if (instArtifactsUuid == null || instArtifactsUuid.isEmpty()) {
					log.info(" instArtifactsUuid empty. label {} inst {} in service {} ", artifactlabel, instName,
							servicename);
					return true;
				}
				if (!instArtifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					log.info(
							" instArtifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} inst {} in service {} ",
							artifactlabel, instName, servicename);
					return true;
				}
			}
		}
		for (String artifactUUID : artifactsUuid) {
			String label = findArtifactLabelFromArtifactId(artifactUUID);
			if (label != null && !label.isEmpty() && !label.equals("")) {
				return true;
			}
		}
		return false;
	}

	private boolean fix(List<Resource> vfLst, List<Service> serviceList, Map<String, List<Component>> nodesToFixTosca,
			Map<String, List<Component>> vfToFixTosca, Map<String, List<Component>> servicesToFixTosca) {
		boolean res = true;
		log.info(" Fix started ***** ");
		if (vfLst != null && !vfLst.isEmpty()) {
			res = fixVf(vfLst);

		}

		if (res && serviceList != null && !serviceList.isEmpty()) {
			res = fixServices(serviceList);

		}

		Set<String> fixedIds = new HashSet<>();
		if (res && nodesToFixTosca != null && !nodesToFixTosca.isEmpty()) {

			generateAndSaveToscaArtifacts(nodesToFixTosca, fixedIds, null);

			for (Map.Entry<String, List<Component>> entry : nodesToFixTosca.entrySet()) {
				List<Component> components = entry.getValue();
				for (Component c : components) {

					ToscaElement topologyTemplate = ModelConverter.convertToToscaElement(c);
					Map<String, ArtifactDataDefinition> arifacts = topologyTemplate.getToscaArtifacts();
					res = fixDataOnGraph(c.getUniqueId(), VertexTypeEnum.TOSCA_ARTIFACTS, EdgeLabelEnum.TOSCA_ARTIFACTS,
							arifacts);
					titanDao.commit();
				}
			}

		}
		if (res && vfToFixTosca != null && !vfToFixTosca.isEmpty()) {

			generateAndSaveToscaArtifacts(vfToFixTosca, fixedIds, vfLst);

			for (Map.Entry<String, List<Component>> entry : vfToFixTosca.entrySet()) {
				List<Component> components = entry.getValue();
				for (Component c : components) {
					TopologyTemplate topologyTemplate = ModelConverter.convertToToscaElement(c);
					Map<String, ArtifactDataDefinition> arifacts = topologyTemplate.getToscaArtifacts();
					res = fixDataOnGraph(c.getUniqueId(), VertexTypeEnum.TOSCA_ARTIFACTS, EdgeLabelEnum.TOSCA_ARTIFACTS,
							arifacts);
					titanDao.commit();
				}
			}

		}

		if (res && servicesToFixTosca != null && !servicesToFixTosca.isEmpty()) {
			generateAndSaveToscaArtifacts(servicesToFixTosca, fixedIds, serviceList);

			for (Map.Entry<String, List<Component>> entry : servicesToFixTosca.entrySet()) {
				List<Component> components = entry.getValue();
				for (Component c : components) {
					TopologyTemplate topologyTemplate = ModelConverter.convertToToscaElement(c);
					Map<String, ArtifactDataDefinition> arifacts = topologyTemplate.getToscaArtifacts();
					res = fixDataOnGraph(c.getUniqueId(), VertexTypeEnum.TOSCA_ARTIFACTS, EdgeLabelEnum.TOSCA_ARTIFACTS,
							arifacts);
					titanDao.commit();
				}
			}

		}

		if (res) {

			for (Component component : vfLst) {
				generateToscaPerComponent(fixedIds, component);

				TopologyTemplate topologyTemplate = ModelConverter.convertToToscaElement(component);
				Map<String, GroupDataDefinition> groups = topologyTemplate.getGroups();
				res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.GROUPS, EdgeLabelEnum.GROUPS, groups);
				if (res) {
					Map<String, ArtifactDataDefinition> arifacts = topologyTemplate.getDeploymentArtifacts();
					res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.DEPLOYMENT_ARTIFACTS,
							EdgeLabelEnum.DEPLOYMENT_ARTIFACTS, arifacts);
				}
				if (res) {
					Map<String, ArtifactDataDefinition> arifacts = topologyTemplate.getToscaArtifacts();
					res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.TOSCA_ARTIFACTS,
							EdgeLabelEnum.TOSCA_ARTIFACTS, arifacts);
				}
				titanDao.commit();
			}
		}

		if (res) {

			for (Component component : serviceList) {
				generateToscaPerComponent(fixedIds, component);

				TopologyTemplate topologyTemplate = ModelConverter.convertToToscaElement(component);
				Map<String, MapGroupsDataDefinition> groups = topologyTemplate.getInstGroups();
				res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS,
						groups);

				if (res) {
					Map<String, MapArtifactDataDefinition> artifacts = topologyTemplate.getInstDeploymentArtifacts();
					res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.INST_DEPLOYMENT_ARTIFACTS,
							EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, artifacts);
				}
				if (res) {
					Map<String, ArtifactDataDefinition> arifacts = topologyTemplate.getToscaArtifacts();
					res = fixDataOnGraph(component.getUniqueId(), VertexTypeEnum.TOSCA_ARTIFACTS,
							EdgeLabelEnum.TOSCA_ARTIFACTS, arifacts);
				}
				titanDao.commit();
			}

		}
		log.info(" Fix finished with res {} ***** ", res);
		return res;
	}

	private boolean generateAndSaveToscaArtifacts(Map<String, List<Component>> nodesToFixTosca, Set<String> fixedIds,
			List<? extends Component> componentsWithFailedGroups) {
		boolean res = true;
		log.debug("Migration1707ArtifactUuidFix  generateAndSaveToscaArtifacts started ");
		for (Map.Entry<String, List<Component>> entry : nodesToFixTosca.entrySet()) {

			List<Component> component = entry.getValue();
			for (Component c : component) {
				log.debug("Migration1707ArtifactUuidFix  fix tosca on component : id {},  name {} ", c.getUniqueId(),
						c.getName());
				if (componentsWithFailedGroups != null) {
					Optional<Component> op = (Optional<Component>) componentsWithFailedGroups.stream()
							.filter(cg -> cg.getUniqueId().equals(c.getUniqueId())).findAny();
					if (!op.isPresent())
						res = generateToscaPerComponent(fixedIds, c);
				} else
					res = generateToscaPerComponent(fixedIds, c);
			}
		}
		log.debug("Migration1707ArtifactUuidFix  generateAndSaveToscaArtifacts finished with res {} ", res);
		return res;
	}

	private boolean generateToscaPerComponent(Set<String> fixedIds, Component c) {
		boolean res = true;
		log.debug("Migration1707ArtifactUuidFix  generateToscaPerComponent started component name {} id {}",
				c.getName(), c.getUniqueId());
		try {
			Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade
					.getToscaFullElement(c.getUniqueId());
			if (toscaElement.isRight()) {
				log.info("Failed to fetch resources {} {}", c.getUniqueId(), toscaElement.right().value());
				return false;
			}
			Component toscaElementFull = toscaElement.left().value();
			toscaElementFull.setGroups(c.getGroups());
			List<ComponentInstance> ciListFull = toscaElementFull.getComponentInstances();
			List<ComponentInstance> ciList = c.getComponentInstances();
			if (ciListFull != null && !ciListFull.isEmpty()) {
				ciListFull.forEach(ciFull -> {
					ComponentInstance compInst = ciList.stream()
							.filter(ci -> ci.getUniqueId().equals(ciFull.getUniqueId())).findAny().get();
					ciFull.setGroupInstances(compInst.getGroupInstances());
				});
			}

			Map<String, ArtifactDefinition> toscaArtifacts = c.getToscaArtifacts();
			log.debug("Migration1707ArtifactUuidFix  generateToscaPerComponent tocsa artifacts size {}",
					toscaArtifacts.size());

			Either<ArtifactDefinition, ToscaError> either = Either.right(ToscaError.GENERAL_ERROR);
			ArtifactDefinition toscaArtifact = null;
			Optional<ArtifactDefinition> op = toscaArtifacts.values().stream()
					.filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_TEMPLATE.getType())).findAny();

			if (op.isPresent()) {
				toscaArtifact = op.get();
			}

			if (toscaArtifact != null) {
				log.debug("Migration1707ArtifactUuidFix  generateToscaPerComponent artifact name {} id {} esId {}",
						toscaArtifact.getArtifactName(), toscaArtifact.getUniqueId(), toscaArtifact.getEsId());
				either = generateToscaArtifact(toscaElementFull, toscaArtifact);
				if (either.isRight()) {
					log.error("Couldn't generate and save tosca template component  unique id {}, name {} error: {}",
							toscaElementFull.getUniqueId(), toscaElementFull.getName(), either.right().value());
					res = false;

				}
			}
			if (res) {

				ArtifactDefinition csarArtifact = null;
				op = toscaArtifacts.values().stream()
						.filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())).findAny();

				if (op.isPresent()) {
					csarArtifact = op.get();
				}

				if (csarArtifact != null) {
					log.debug("Migration1707ArtifactUuidFix  generateToscaPerComponent artifact name {} id {} esId {}",
							csarArtifact.getArtifactName(), csarArtifact.getUniqueId(), csarArtifact.getEsId());
					either = generateToscaArtifact(toscaElementFull, csarArtifact);
					if (either.isRight()) {
						log.error("Couldn't generate and save tosca csar for component  uuid {}, id {}, name {}.  error: {}",
								toscaElementFull.getUUID(), toscaElementFull.getUniqueId(), toscaElementFull.getName(), either.right().value());
						res = false;

					}
				}
			}
			c.setToscaArtifacts(toscaArtifacts);

			if (res) {
				fixedIds.add(toscaElementFull.getUniqueId());
			}
		} finally {
			titanDao.commit();
		}
		log.debug("Migration1707ArtifactUuidFix  generateToscaPerComponent finished  component name {} id {} res {}",
				c.getName(), c.getUniqueId(), res);
		return res;
	}

	private <T extends ToscaDataDefinition> boolean fixDataOnGraph(String componentId, VertexTypeEnum vertexTypeEnum,
			EdgeLabelEnum edgeLabelEnum, Map<String, T> groups) {
		log.debug("amount groups to update: VertexTypeEnum {} EdgeLabelEnum {} data size {}", vertexTypeEnum.getName(),
				edgeLabelEnum, groups.size());
		boolean res = true;
		Either<GraphVertex, TitanOperationStatus> getResponse = titanDao.getVertexById(componentId,
				JsonParseFlagEnum.NoParse);
		if (getResponse.isRight()) {
			log.debug("Couldn't fetch component  unique id {}, error: {}", componentId, getResponse.right().value());
			res = false;

		}
		if (res) {
			GraphVertex componentVertex = getResponse.left().value();

			GraphVertex toscaDataVertex = null;
			Either<GraphVertex, TitanOperationStatus> groupVertexEither = titanDao.getChildVertex(componentVertex,
					edgeLabelEnum, JsonParseFlagEnum.ParseJson);
			if (groupVertexEither.isRight() && groupVertexEither.right().value() == TitanOperationStatus.NOT_FOUND) {
				log.debug("no child {}  vertex for component  unique id {}, error: {}", edgeLabelEnum, componentId,
						groupVertexEither.right().value());
				return true;
			}
			if (groupVertexEither.isRight()) {
				res = false;
				log.debug("failed to get child {}  vertex for component  unique id {}, error: {}", edgeLabelEnum,
						componentId, groupVertexEither.right().value());
			}
			if (res) {
				toscaDataVertex = groupVertexEither.left().value();
				toscaDataVertex.setJson(groups);
				Either<GraphVertex, TitanOperationStatus> updatevertexEither = titanDao.updateVertex(toscaDataVertex);
				if (updatevertexEither.isRight()) {
					log.debug("failed to update vertex for component  unique id {}, error: {}", componentId,
							updatevertexEither.right().value());
					titanDao.rollback();
					return false;
				}
			}
		}
		log.debug("Fix data on graph finished: VertexTypeEnum {} EdgeLabelEnum {} res {}", vertexTypeEnum.getName(),
				res);
		return res;
	}

	private boolean fixServices(List<Service> serviceList) {
		for (Service service : serviceList) {
			log.debug("Migration1707ArtifactUuidFix  fix service: id {},  name {} ", service.getUniqueId(),
					service.getName());
			List<ComponentInstance> instances = service.getComponentInstances();
			for (ComponentInstance instance : instances) {
				fixComponentInstances(service, instance);
			}

		}
		return true;

	}

	private void fixComponentInstances(Service service, ComponentInstance instance) {
		Map<String, ArtifactDefinition> artifactsMap = instance.getDeploymentArtifacts();
		List<GroupInstance> groupsList = instance.getGroupInstances();
		if (groupsList != null && artifactsMap != null) {
			List<GroupInstance> groupsToDelete = new ArrayList<>();
			for (GroupInstance group : groupsList) {
				fixGroupInstances(service, artifactsMap, groupsToDelete, group);

			}

			if (!groupsToDelete.isEmpty()) {
				log.debug("Migration1707ArtifactUuidFix  delete group:  resource id {}, group instance to delete {} ",
						service.getUniqueId(), groupsToDelete);
				groupsList.removeAll(groupsToDelete);

			}

			Optional<ArtifactDefinition> optionalVfModuleArtifact = artifactsMap.values().stream()
					.filter(p -> p.getArtifactType().equals(ArtifactTypeEnum.VF_MODULES_METADATA.name())).findAny();
			if (optionalVfModuleArtifact.isPresent()) {
				ArtifactDefinition vfModuleAertifact = optionalVfModuleArtifact.get();
				fillVfModuleInstHeatEnvPayload(groupsList, vfModuleAertifact);
			}
		}
	}

	private void fixGroupInstances(Service service, Map<String, ArtifactDefinition> artifactsMap,
			List<GroupInstance> groupsToDelete, GroupInstance group) {
		if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
			log.debug("Migration1707ArtifactUuidFix  fix group:  resource id {}, group name {} ", service.getUniqueId(),
					group.getName());
			List<String> groupArtifacts = new ArrayList<String>(group.getArtifacts());

			group.getArtifacts().clear();
			group.getArtifactsUuid().clear();
			group.getGroupInstanceArtifacts().clear();
			group.getGroupInstanceArtifactsUuid().clear();

			for (String artifactId : groupArtifacts) {
				fixArtifactUndergroupInstances(artifactsMap, group, groupArtifacts, artifactId);
			}
			if (group.getArtifacts() == null || group.getArtifacts().isEmpty()) {
				log.debug(
						"Migration1707ArtifactUuidFix  fix groupInstance add to delete list:  resource id {} name {} , group name {} ",
						service.getUniqueId(), service.getName(), group.getName());
				groupsToDelete.add(group);
			}
		}
	}

	private void fixArtifactUndergroupInstances(Map<String, ArtifactDefinition> artifactsMap, GroupInstance group,
			List<String> groupArtifacts, String artifactId) {
		String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
		log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} artifactId for fix {} artifactlabel {} ",
				group.getName(), artifactId, artifactlabel);
		if (!artifactlabel.isEmpty() && artifactsMap.containsKey(artifactlabel)) {
			ArtifactDefinition artifact = artifactsMap.get(artifactlabel);
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
			String correctArtifactId = artifact.getUniqueId();
			String correctArtifactUUID = artifact.getArtifactUUID();
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				boolean isAddToGroup = true;
				if (groupArtifacts.size() == 1) {

					if (artifactType == ArtifactTypeEnum.HEAT_ARTIFACT) {
						isAddToGroup = false;
						artifact.setArtifactType(ArtifactTypeEnum.OTHER.getType());
					}
				}
				if (isAddToGroup) {
					log.debug(
							"Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ",
							group.getName(), correctArtifactId, correctArtifactUUID);
					group.getArtifacts().add(correctArtifactId);
					if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
						group.getArtifactsUuid().add(correctArtifactUUID);
					}
				}
			} else {
				log.debug(
						"Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ",
						group.getName(), correctArtifactId, correctArtifactUUID);
				group.getGroupInstanceArtifacts().add(correctArtifactId);
				if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
					group.getGroupInstanceArtifactsUuid().add(correctArtifactUUID);
				}
			}
		}
	}

	private boolean fixVf(List<Resource> vfLst) {
		for (Resource resource : vfLst) {
			log.debug("Migration1707ArtifactUuidFix  fix resource: id {},  name {} ", resource.getUniqueId(),
					resource.getName());
			Map<String, ArtifactDefinition> artifactsMap = resource.getDeploymentArtifacts();
			List<GroupDefinition> groupsList = resource.getGroups();
			List<GroupDefinition> groupsToDelete = new ArrayList<>();
			if (groupsList != null && artifactsMap != null) {
				for (GroupDefinition group : groupsList) {
					if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE) && group.getArtifacts() != null) {
						fixVfGroup(resource, artifactsMap, group);
					}
					if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)
							&& (group.getArtifacts() == null || group.getArtifacts().isEmpty())) {
						log.debug(
								"Migration1707ArtifactUuidFix  add group to delete list fix resource: id {},  name {} ",
								resource.getUniqueId(), resource.getName(), group.getName());
						groupsToDelete.add(group);
					}
				}

				if (!groupsToDelete.isEmpty()) {
					groupsList.removeAll(groupsToDelete);

				}
			}

		}

		return true;
	}

	private void fixVfGroup(Resource resource, Map<String, ArtifactDefinition> artifactsMap, GroupDefinition group) {
		log.debug("Migration1707ArtifactUuidFix  fix group:  resource id {}, group name {} ", resource.getUniqueId(),
				group.getName());
		List<String> groupArtifacts = new ArrayList<>(group.getArtifacts());

		for (String artifactId : groupArtifacts) {
			fixArtifactUnderGroup(artifactsMap, group, groupArtifacts, artifactId);
		}
	}

	private void fixArtifactUnderGroup(Map<String, ArtifactDefinition> artifactsMap, GroupDefinition group,
			List<String> groupArtifacts, String artifactId) {
		group.getArtifacts().clear();
		group.getArtifactsUuid().clear();
		String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
		log.debug("Migration1707ArtifactUuidFix  fix group:  group name {} artifactId for fix {} artifactlabel {} ",
				group.getName(), artifactId, artifactlabel);
		if (!artifactlabel.isEmpty() && artifactsMap.containsKey(artifactlabel)) {
			ArtifactDefinition artifact = artifactsMap.get(artifactlabel);
			String correctArtifactId = artifact.getUniqueId();
			String correctArtifactUUID = artifact.getArtifactUUID();
			boolean isAddToGroup = true;
			if (groupArtifacts.size() == 1) {
				ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
				if (artifactType == ArtifactTypeEnum.HEAT_ARTIFACT) {
					isAddToGroup = false;
					artifact.setArtifactType(ArtifactTypeEnum.OTHER.getType());
				}
			}
			if (isAddToGroup) {
				log.debug(
						"Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ",
						group.getName(), correctArtifactId, correctArtifactUUID);
				group.getArtifacts().add(correctArtifactId);
				if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
					group.getArtifactsUuid().add(correctArtifactUUID);
				}
			}

		}
	}

	private String findArtifactLabelFromArtifactId(String artifactId) {
		String artifactLabel = "";

		int index = artifactId.lastIndexOf('.');
		if (index > 0 && index + 1 < artifactId.length())
			artifactLabel = artifactId.substring(index + 1);
		return artifactLabel;
	}

	private void writeModuleResultToFile(Writer writer, org.openecomp.sdc.be.model.Component component,
			Service service) {
		try {
			// "service name, service id, state, version
			StringBuilder sb = new StringBuilder(component.getName());
			sb.append(",").append(component.getUniqueId()).append(",").append(component.getLifecycleState()).append(",")
					.append(component.getVersion());
			if (service != null) {
				sb.append(",").append(service.getName());
			}
			sb.append("\n");
			writer.write(sb.toString());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void writeModuleResultToFile(Writer writer, List<Component> components) {
		try {
			// "service name, service id, state, version
			for (Component component : components) {
				StringBuilder sb = new StringBuilder(component.getName());
				sb.append(",").append(component.getUniqueId()).append(",").append(component.getInvariantUUID())
						.append(",").append(component.getLifecycleState()).append(",").append(component.getVersion());

				sb.append("\n");
				writer.write(sb.toString());
			}
		} catch (IOException e) {

			log.error(e.getMessage());
		}
	}

	public boolean doFixTosca(Map<String, List<Component>> nodeToFix, Map<String, List<Component>> vfToFix,
			Map<String, List<Component>> serviceToFix) {

		Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

		Map<String, List<Component>> vertices = getVerticesToValidate(VertexTypeEnum.NODE_TYPE, hasProps);
		boolean result = validateTosca(vertices, nodeToFix, "RESOURCE_TOSCA_ARTIFACTS");//

		hasProps.clear();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF);
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

		vertices = getVerticesToValidate(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps);
		result = validateTosca(vertices, vfToFix, "VF_TOSCA_ARTIFACTS");

		hasProps.clear();
		hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		hasProps.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

		vertices = getVerticesToValidate(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps);
		result = validateTosca(vertices, serviceToFix, "SERVICE_TOSCA_ARTIFACTS");

		return result;
	}

	public Map<String, List<Component>> getVerticesToValidate(VertexTypeEnum type,
			Map<GraphPropertyEnum, Object> hasProps) {

		Map<String, List<Component>> result = new HashMap<>();
		try {

			Either<List<GraphVertex>, TitanOperationStatus> resultsEither = titanDao.getByCriteria(type, hasProps);
			if (resultsEither.isRight()) {
				System.out.println("getVerticesToValidate failed " + resultsEither.right().value());
				return result;
			}
			System.out.println("getVerticesToValidate: " + resultsEither.left().value().size() + " vertices to scan");
			List<GraphVertex> componentsList = resultsEither.left().value();
			componentsList.forEach(vertex -> {
				String ivariantUuid = (String) vertex.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID);
				if (!result.containsKey(ivariantUuid)) {
					List<Component> compList = new ArrayList<Component>();
					result.put(ivariantUuid, compList);
				}
				List<Component> compList = result.get(ivariantUuid);

				ComponentParametersView filter = new ComponentParametersView(true);
				filter.setIgnoreArtifacts(false);

				Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade
						.getToscaElement(vertex.getUniqueId(), filter);
				if (toscaElement.isRight()) {
					System.out.println("getVerticesToValidate: failed to find element" + vertex.getUniqueId()
							+ " staus is" + toscaElement.right().value());
				} else {
					compList.add(toscaElement.left().value());
				}
				titanDao.commit();

			});

		} catch (Exception e) {
			log.info("Failed to fetch vf resources ", e);

		} finally {
			titanDao.commit();

		}
		return result;
	}

	public boolean validateTosca(Map<String, List<Component>> vertices, Map<String, List<Component>> compToFix,
			String name) {
		boolean result = true;
		long time = System.currentTimeMillis();
		String fileName = name + "_" + time + ".csv";
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write("name, UUID, invariantUUID, state, version\n");
			for (Map.Entry<String, List<Component>> entry : vertices.entrySet()) {
				List<Component> compList = entry.getValue();
				Set<String> artifactEsId = new HashSet<>();
				for (Component component : compList) {
					Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
					Optional<ArtifactDefinition> op = toscaArtifacts.values().stream()
							.filter(a -> artifactEsId.contains(a.getEsId()) && a.getEsId() != null).findAny();
					if (op.isPresent()) {
						result = false;
						writeModuleResultToFile(writer, compList);
						writer.flush();
						break;
					} else {
						artifactEsId.addAll(toscaArtifacts.values().stream().map(ArtifactDefinition::getEsId)
								.collect(Collectors.toList()));
					}
				}
				if (!result) {
					List<Component> compListfull = new ArrayList<>();
					for (Component c : compList) {
						ComponentParametersView filter = new ComponentParametersView(true);
						filter.setIgnoreComponentInstances(false);
						filter.setIgnoreArtifacts(false);
						filter.setIgnoreGroups(false);

						Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade
								.getToscaElement(c.getUniqueId(), filter);
						if (toscaElement.isRight()) {
							System.out.println("getVerticesToValidate: failed to find element" + c.getUniqueId()
									+ " staus is" + toscaElement.right().value());
						} else {
							compListfull.add(toscaElement.left().value());
						}
						this.titanDao.commit();
					}

					compToFix.put(entry.getKey(), compListfull);
					result = true;
				}

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
		return result;
	}

	private Either<ArtifactDefinition, ToscaError> generateToscaArtifact(Component parent,
			ArtifactDefinition artifactInfo) {
		log.debug("tosca artifact generation");
		try {
			if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.TOSCA_CSAR.getType())) {
				Either<byte[], ResponseFormat> generated = csarUtils.createCsar(parent, true, true);

				if (generated.isRight()) {
					log.debug("Failed to export tosca csar for component {} error {}", parent.getUniqueId(),
							generated.right().value());

					return Either.right(ToscaError.GENERAL_ERROR);
				}
				byte[] value = generated.left().value();
				artifactInfo.setPayload(value);

			} else {
				Either<ToscaRepresentation, ToscaError> exportComponent = toscaExportUtils.exportComponent(parent);
				if (exportComponent.isRight()) {
					log.debug("Failed export tosca yaml for component {} error {}", parent.getUniqueId(),
							exportComponent.right().value());

					return Either.right(exportComponent.right().value());
				}
				log.debug("Tosca yaml exported for component {} ", parent.getUniqueId());
				String payload = exportComponent.left().value().getMainYaml();

				artifactInfo.setPayloadData(payload);
			}

			byte[] decodedPayload = artifactInfo.getPayloadData();
			artifactInfo.setEsId(artifactInfo.getUniqueId());
			artifactInfo.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(decodedPayload));
			ESArtifactData artifactData = new ESArtifactData(artifactInfo.getEsId(), decodedPayload);
			artifactCassandraDao.saveArtifact(artifactData);
			log.debug("Tosca yaml artifact esId  ", artifactInfo.getEsId());
		} catch (Exception ex) {
			log.error("Failed to generate tosca atifact id {} component id {} component name {} error {}",artifactInfo.getUniqueId(),
					parent.getUniqueId(), parent.getName(), ex.getMessage()	);

			return Either.right(ToscaError.GENERAL_ERROR);
		}
		
		return Either.left(artifactInfo);
	}

	private void fillVfModuleInstHeatEnvPayload(List<GroupInstance> groupsForCurrVF,
			ArtifactDefinition vfModuleArtifact) {

		List<VfModuleArtifactPayload> vfModulePayloadForCurrVF = new ArrayList<VfModuleArtifactPayload>();
		if (groupsForCurrVF != null) {
			for (GroupInstance groupInstance : groupsForCurrVF) {
				VfModuleArtifactPayload modulePayload = new VfModuleArtifactPayload(groupInstance);
				vfModulePayloadForCurrVF.add(modulePayload);
			}
			Collections.sort(vfModulePayloadForCurrVF,
					(art1, art2) -> VfModuleArtifactPayload.compareByGroupName(art1, art2));

			final Gson gson = new GsonBuilder().setPrettyPrinting().create();

			String vfModulePayloadString = gson.toJson(vfModulePayloadForCurrVF);
			if (vfModulePayloadString != null) {
				String newCheckSum = GeneralUtility
						.calculateMD5Base64EncodedByByteArray(vfModulePayloadString.getBytes());
				vfModuleArtifact.setArtifactChecksum(newCheckSum);

				ESArtifactData artifactData = new ESArtifactData(vfModuleArtifact.getEsId(),
						vfModulePayloadString.getBytes());
				artifactCassandraDao.saveArtifact(artifactData);

			}

		}

	}
}
