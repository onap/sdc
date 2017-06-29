package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;


import fj.data.Either;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class NodeTemplateMissingDataResolver <T extends Component> {
	
	private static Logger LOGGER = LoggerFactory.getLogger(NodeTemplateMissingDataResolver.class);
	
	@Resource(name = "tosca-element-lifecycle-operation")
    private ToscaElementLifecycleOperation lifecycleOperation;
	
	@Resource(name = "topology-template-operation")
    private TopologyTemplateOperation topologyTemplateOperation;
	
	public void resolveNodeTemplateInfo(ComponentInstanceDataDefinition vfInst, Map<String, ToscaElement> origCompMap, T component) {
		lifecycleOperation.resolveToscaComponentName(vfInst, origCompMap);
		if(OriginTypeEnum.VF == vfInst.getOriginType()) {
			Map<String, List<ComponentInstanceInput>> componentInstancesInputs = Optional.ofNullable(component.getComponentInstancesInputs()).orElse(new HashMap<>());
			collectVFInstanceInputs(componentInstancesInputs, origCompMap, vfInst);
		}
	}
	
	private void collectVFInstanceInputs(Map<String, List<ComponentInstanceInput>> instInputs, Map<String, ToscaElement> origCompMap, ComponentInstanceDataDefinition vfInst) {
		String ciUid = vfInst.getUniqueId();
		String origCompUid = vfInst.getComponentUid();
		Either<ToscaElement, StorageOperationStatus> origComp = fetchToscaElement(origCompMap, vfInst, origCompUid);
        if(origComp.isRight())
        	return;
		Map<String, PropertyDataDefinition> origVFInputs = ((TopologyTemplate)origComp.left().value()).getInputs();
		if (origVFInputs != null && !origVFInputs.isEmpty()) {
			Map<String, ComponentInstanceInput> collectedVFInputs = origVFInputs.values().stream()
					                                                                       .collect(Collectors.toMap(PropertyDataDefinition::getName, ComponentInstanceInput::new));
			List<ComponentInstanceInput> instInputList = instInputs.get(ciUid);
			Map<String, ComponentInstanceInput> existingInstInputs = ToscaDataDefinition.listToMapByName(instInputList);
			collectedVFInputs.putAll(existingInstInputs);
			List<ComponentInstanceInput> mergedList = new ArrayList<>(collectedVFInputs.values());
			instInputs.put(ciUid, mergedList);	
		}
	}

	private Either<ToscaElement, StorageOperationStatus> fetchToscaElement(Map<String, ToscaElement> origCompMap, ComponentInstanceDataDefinition vfInst, String origCompUid) {
		Either<ToscaElement, StorageOperationStatus> origCompEither;
		if (!origCompMap.containsKey(origCompUid)) {
			origCompEither = topologyTemplateOperation.getToscaElement(origCompUid);
			if (origCompEither.isRight()) {
				  LOGGER.error("failed to fetch Tosca element {} with id {}", vfInst.getComponentName(), origCompUid);
				  return origCompEither;		
			}
			origCompMap.put(origCompUid, origCompEither.left().value());
		}
		return Either.left(origCompMap.get(origCompUid));
	}
	
	protected boolean isProblematicGroup(GroupDefinition gr, String resourceName, Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifacts = gr.getArtifacts();
		List<String> artifactsUuid = gr.getArtifactsUuid();

		if ((artifactsUuid == null || artifactsUuid.isEmpty()) && (artifacts == null || artifacts.isEmpty())) {
			LOGGER.debug("No groups in resource {} ", resourceName);
			return false;
		}
		if (artifacts.size() < artifactsUuid.size()) {
			LOGGER.debug(" artifacts.size() < artifactsUuid.size() group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}
		if (artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty())) {
			LOGGER.debug(" artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty() group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}
		if (artifactsUuid.contains(null)) {
			LOGGER.debug(" artifactsUuid.contains(null) group {} in resource {} ", gr.getName(), resourceName);
			return true;
		}

		for (String artifactId : artifacts) {
			String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactlabel);
			if (artifactDefinition == null) {
				LOGGER.debug(" artifactDefinition == null label {} group {} in resource {} ", artifactlabel, gr.getName(), resourceName);
				return true;
			}
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				if (!artifactId.equals(artifactDefinition.getUniqueId())) {
					LOGGER.debug(" !artifactId.equals(artifactDefinition.getUniqueId() artifact {}  artId {} group {} in resource {} ", artifactlabel, artifactId, gr.getName(), resourceName);
					return true;
				}
				if (!artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					LOGGER.debug(" artifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} group {} in resource {} ", artifactlabel, gr.getName(), resourceName);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isProblematicGroupInstance(GroupInstance gi, String instName, String servicename, Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifacts = gi.getArtifacts();
		List<String> artifactsUuid = gi.getArtifactsUuid();
		List<String> instArtifactsUuid = gi.getGroupInstanceArtifactsUuid();

		if ((artifactsUuid == null || artifactsUuid.isEmpty()) && (artifacts == null || artifacts.isEmpty())) {
			LOGGER.debug("No instance groups for instance {} in service {} ", instName, servicename);
			return false;
		}
		if (artifacts.size() < artifactsUuid.size()) {
			LOGGER.debug(" artifacts.size() < artifactsUuid.size() inst {} in service {} ", instName, servicename);
			return true;
		}
		if (artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty())) {
			LOGGER.debug(" artifacts.size() > 0 && (artifactsUuid == null || artifactsUuid.isEmpty() inst {} in service {} ", instName, servicename);
			return true;
		}
		if (artifactsUuid.contains(null)) {
			LOGGER.debug(" artifactsUuid.contains(null) inst {} in service {} ", instName, servicename);
			return true;
		}

		for (String artifactId : artifacts) {
			String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactlabel);
			if (artifactDefinition == null) {
				LOGGER.debug(" artifactDefinition == null label {} inst {} in service {} ", artifactlabel, instName, servicename);
				return true;
			}
			ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactDefinition.getArtifactType());
			if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
				if (!artifactId.equals(artifactDefinition.getUniqueId())) {
					LOGGER.debug(" !artifactId.equals(artifactDefinition.getUniqueId() artifact {}  artId {} inst {} in service {} ", artifactlabel, artifactId, instName, servicename);
					return true;
				}
				if (!artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					LOGGER.debug(" artifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} inst {} in service {} ", artifactlabel, instName, servicename);
					return true;
				}
			} else {
				if (!instArtifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
					LOGGER.debug(" instArtifactsUuid.contains(artifactDefinition.getArtifactUUID() label {} inst {} in service {} ", artifactlabel, instName, servicename);
					return true;
				}
			}
		}
		return false;
	}
	
	private String findArtifactLabelFromArtifactId(String artifactId) {
		String artifactLabel = "";

		int index = artifactId.lastIndexOf(".");
		if (index > 0 && index + 1 < artifactId.length())
			artifactLabel = artifactId.substring(index + 1);
		return artifactLabel;
	}
	
	protected boolean fixVFGroups(Component component){
		boolean res = true;
		
		Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
		List<GroupDefinition> groups = component.getGroups();
		if (groups == null || groups.isEmpty()) {
			LOGGER.debug("No  groups  in component {} id {} ",  component.getName(), component.getUniqueId());
			return res;
		}	
				
		for (GroupDefinition group : groups) {
			if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE) && deploymentArtifacts != null) {
				if (isProblematicGroup(group, component.getName(), deploymentArtifacts)) {
					List<String> groupArtifacts = new ArrayList<String>(group.getArtifacts());
					group.getArtifacts().clear();
					group.getArtifactsUuid().clear();
					for (String artifactId : groupArtifacts) {
						String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
						LOGGER.debug("fix group:  group name {} artifactId for fix {} artifactlabel {} ", group.getName(), artifactId, artifactlabel);
						if (!artifactlabel.isEmpty() && deploymentArtifacts.containsKey(artifactlabel)) {
							ArtifactDefinition artifact = deploymentArtifacts.get(artifactlabel);
							String correctArtifactId = artifact.getUniqueId();
							String correctArtifactUUID = artifact.getArtifactUUID();
							LOGGER.debug(" fix group:  group name {} correct artifactId {} artifactUUID {} ", group.getName(), correctArtifactId, correctArtifactUUID);
							group.getArtifacts().add(correctArtifactId);
							if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
								group.getArtifactsUuid().add(correctArtifactUUID);
							}

						}
					}
				}
			}
			
		}		
		
		return res;
	}
	
	protected boolean fixVFGroupInstances(Component component, ComponentInstance instance){
		boolean res = true;
		
		Map<String, ArtifactDefinition> deploymentArtifacts = instance.getDeploymentArtifacts();
		List<GroupInstance> groupInstances = instance.getGroupInstances();
		if (groupInstances == null || groupInstances.isEmpty()) {
			LOGGER.debug("No instance groups for instance {} in service {} id {} ", instance.getName(), component.getName(), component.getUniqueId());
			return res;
		}		
		for (GroupInstance group : groupInstances) {
			if (group.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)) {
				if (isProblematicGroupInstance(group, instance.getName(), component.getName(), deploymentArtifacts)) {

					LOGGER.debug("Migration1707ArtifactUuidFix  fix group:  resource id {}, group name {} ", component.getUniqueId(), group.getName());
					List<String> groupArtifacts = Optional.ofNullable(group.getArtifacts()).orElse(new ArrayList<>());

					group.setArtifacts(new ArrayList<>());
					group.setArtifactsUuid(new ArrayList<>());
					group.setGroupInstanceArtifacts(new ArrayList<>());
					group.setGroupInstanceArtifactsUuid(new ArrayList<>());

					for (String artifactId : groupArtifacts) {
						String artifactlabel = findArtifactLabelFromArtifactId(artifactId);
						LOGGER.debug("Migration1707ArtifactUuidFix  fix group:  group name {} artifactId for fix {} artifactlabel {} ", group.getName(), artifactId, artifactlabel);
						if (!artifactlabel.isEmpty() && deploymentArtifacts.containsKey(artifactlabel)) {
							ArtifactDefinition artifact = deploymentArtifacts.get(artifactlabel);
							ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
							String correctArtifactId = artifact.getUniqueId();
							String correctArtifactUUID = artifact.getArtifactUUID();
							if (artifactType != ArtifactTypeEnum.HEAT_ENV) {

								LOGGER.debug("Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ", group.getName(), correctArtifactId, correctArtifactUUID);
								group.getArtifacts().add(correctArtifactId);
								if (correctArtifactUUID != null && !correctArtifactUUID.isEmpty()) {
									group.getArtifactsUuid().add(correctArtifactUUID);
								}
							} else {
								LOGGER.debug("Migration1707ArtifactUuidFix  fix group:  group name {} correct artifactId {} artifactUUID {} ", group.getName(), correctArtifactId, correctArtifactUUID);
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
		
		return res;
	}

}
