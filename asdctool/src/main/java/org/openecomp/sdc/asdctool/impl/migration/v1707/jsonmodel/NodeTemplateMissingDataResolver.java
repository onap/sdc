package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;


public class NodeTemplateMissingDataResolver <T extends Component> {
	
	private static Logger LOGGER = LoggerFactory.getLogger(NodeTemplateMissingDataResolver.class);
	
	@Resource(name = "tosca-element-lifecycle-operation")
    private ToscaElementLifecycleOperation lifecycleOperation;
	
	@Resource(name = "topology-template-operation")
    private TopologyTemplateOperation topologyTemplateOperation;
	
	public void resolveNodeTemplateInfo(ComponentInstanceDataDefinition vfInst, Map<String, ToscaElement> origCompMap, T component) {
		lifecycleOperation.resolveToscaComponentName(vfInst, origCompMap);
		if(OriginTypeEnum.VF == vfInst.getOriginType())
			collectVFInstanceInputs(component.getComponentInstancesInputs(), origCompMap, vfInst);
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
					                                                                       .collect(Collectors.toMap(p -> p.getName(), p -> new ComponentInstanceInput(p)));
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
}
