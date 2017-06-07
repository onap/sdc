package org.openecomp.sdc.asdctool.impl.migration.v1707;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.impl.migration.Migration;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.resources.data.InputValueData;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.be.resources.data.PolicyTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.RelationshipInstData;
import org.openecomp.sdc.be.resources.data.RelationshipTypeData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("toscaNamesUpdate")
public class ToscaNamesUpdate implements Migration {
	private static Logger log = LoggerFactory.getLogger(ToscaNamesUpdate.class.getName());

	@Override
	public String description() {
		return "toscaNamesUpdate";
	}

	@Autowired
	protected TitanGenericDao titanGenericDao;

	@Override
	public boolean migrate() {
		boolean result = true;
		List<ImmutableTriple<NodeTypeEnum, Class<GraphNode>, Function<GraphNode, ImmutablePair<String, GraphNode>>>> updateInfoList = new ArrayList<>();
		for (NodeTypeEnum nodeType : NodeTypeEnum.values()){
			ImmutableTriple<NodeTypeEnum, Class<GraphNode>, Function<GraphNode, ImmutablePair<String, GraphNode>>> updateInfo = getInfo(nodeType);
			if(null == updateInfo)
				continue;
			updateInfoList.add(updateInfo);
		}
		
		for(ImmutableTriple<NodeTypeEnum, Class<GraphNode>, Function<GraphNode, ImmutablePair<String, GraphNode>>> nodeTypeData : updateInfoList){
			log.debug("before updating namespace on nodeType {}", nodeTypeData.left.getName());
			result = updateNamespaceByNodeType(nodeTypeData);
			if(!result){
				log.debug("alignNamespace procedure failed during execution of updating namespace on nodeType {}", nodeTypeData.left.getName());
				return false;
			}
		}
		return true;
	}

	private <T extends GraphNode> ImmutableTriple<NodeTypeEnum, Class<T>, Function<T, ImmutablePair<String, T>>> getInfo(NodeTypeEnum nodeType) {
		switch (nodeType) {
		case Resource:
			Function<ResourceMetadataData, ImmutablePair<String, ResourceMetadataData>> resourceFunc = r -> updateResource(r);
			return new ImmutableTriple(nodeType, ResourceMetadataData.class, resourceFunc);
		case GroupType:
			Function<GroupTypeData, ImmutablePair<String, GroupTypeData>> groupTypeFunc = g -> updateGroupType(g);
			return new ImmutableTriple(nodeType, GroupTypeData.class, groupTypeFunc);
		case Group:
			Function<GroupData, ImmutablePair<String, GroupData>> groupFunc = g -> updateGroupNode(g);
			return new ImmutableTriple(nodeType, GroupData.class, groupFunc);
		case PolicyType:
			Function<PolicyTypeData, ImmutablePair<String , PolicyTypeData>> policyFunc = p -> updatePolicyType(p);
			return new ImmutableTriple(nodeType, PolicyTypeData.class, policyFunc);
		case RelationshipType:
			Function<RelationshipTypeData, ImmutablePair<String, RelationshipTypeData>> relTypeFunc = r -> updateRelationshipType(r);
			return new ImmutableTriple(nodeType, RelationshipTypeData.class, relTypeFunc);
		case RelationshipInst:
			Function<RelationshipInstData, ImmutablePair<String, RelationshipInstData>> relFunc = r -> updateRelationshipNode(r);
			return new ImmutableTriple(nodeType, RelationshipInstData.class, relFunc);
		case Requirement:
			Function<RequirementData, ImmutablePair<String, RequirementData>> reqFunc = r -> updateRequirementType(r);
			return new ImmutableTriple(nodeType, RequirementData.class, reqFunc);
		case CapabilityType:
			Function<CapabilityTypeData, ImmutablePair<String, CapabilityTypeData>> capTypeFunc = c -> updateCapabilityType(c);
			return new ImmutableTriple(nodeType, CapabilityTypeData.class, capTypeFunc);
		case Capability:
			Function<CapabilityData, ImmutablePair<String, CapabilityData>> capFunc = c -> updateCapabilityNode(c);
			return new ImmutableTriple(nodeType, CapabilityData.class, capFunc);
		case Property:
			Function<PropertyData, ImmutablePair<String, PropertyData>> propFunc = p -> updatePropNode(p);
			return new ImmutableTriple(nodeType, PropertyData.class, propFunc);
		case PropertyValue:
			Function<PropertyValueData, ImmutablePair<String, PropertyValueData>> propValueFunc = p -> updatePropValueNode(p);
			return new ImmutableTriple(nodeType, PropertyValueData.class, propValueFunc);
		case Attribute:	
			Function<AttributeData, ImmutablePair<String, AttributeData>> attrFunc = a -> updateAttributeNode(a);
			return new ImmutableTriple(nodeType, AttributeData.class, attrFunc);
		case AttributeValue:
			Function<AttributeValueData, ImmutablePair<String, AttributeValueData>> attrValueFunc = a -> updateAttrValueNode(a);
			return new ImmutableTriple(nodeType, AttributeValueData.class, attrValueFunc);
		case Input:
			Function<InputsData, ImmutablePair<String, InputsData>> inputFunc = i -> updateInputNode(i);
			return new ImmutableTriple(nodeType, InputsData.class, inputFunc);
		case InputValue:
			Function<InputValueData, ImmutablePair<String, InputValueData>> inputValueFunc = i -> updateInputValueNode(i);
			return new ImmutableTriple(nodeType, InputValueData.class, inputValueFunc);
		case DataType:
			Function<DataTypeData, ImmutablePair<String, DataTypeData>> dataTypeFunc = d -> updateDataType(d);
			return new ImmutableTriple(nodeType, DataTypeData.class, dataTypeFunc);
		default:
			return null;
		}

	}
	
	

	private boolean ifRight(TitanOperationStatus status){
		return TitanOperationStatus.NOT_FOUND == status;
	}
	
	private <T extends GraphNode> boolean ifLeft(List<T> allNodes, ImmutableTriple<NodeTypeEnum, Class<T>, Function<T, ImmutablePair<String, T>>> nodeTypeData){
		boolean result = true;
		try {
			for (T node : allNodes) {
				ImmutablePair<String, T> nodeToUpdate = nodeTypeData.right.apply(node);
				Either<T, TitanOperationStatus> updatedNode = updateNodeIncludingUID(nodeToUpdate.left, nodeToUpdate.right, nodeTypeData.middle);
				if (updatedNode.isRight()) {
					result = false;
					break;
				}
			}
		} finally {
			if (!result) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
		}
		return result;
	}
	
	private <T extends GraphNode> boolean updateNamespaceByNodeType(ImmutableTriple<NodeTypeEnum, Class<T>, Function<T, ImmutablePair<String, T>>> nodeTypeData) {
		Either<List<T>, TitanOperationStatus> getAllNodes = titanGenericDao.getByCriteria(nodeTypeData.left, null, nodeTypeData.middle);
		return getAllNodes.either(list -> ifLeft(list, nodeTypeData), status -> ifRight(status));
	}

	private ImmutablePair<String, ResourceMetadataData> updateResource(ResourceMetadataData resource) {
		String toscaResourceName = updateNamespace(((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).getToscaResourceName());
		((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).setToscaResourceName(toscaResourceName);
		return new ImmutablePair<>((String) resource.getUniqueId(), resource);
	}

	private ImmutablePair<String, GroupTypeData> updateGroupType(GroupTypeData group) {
		String originId = group.getUniqueId();
		group.getGroupTypeDataDefinition().setUniqueId(updateNamespace(originId));
		String type = updateNamespace(group.getGroupTypeDataDefinition().getType());
		group.getGroupTypeDataDefinition().setType(type);
		return new ImmutablePair<>(originId, group);
	}

	private ImmutablePair<String, GroupData> updateGroupNode(GroupData group) {
		String type = updateNamespace(group.getGroupDataDefinition().getType());
		group.getGroupDataDefinition().setType(type);
		return new ImmutablePair<>((String) group.getUniqueId(), group);
	}
	

	private ImmutablePair<String, PolicyTypeData> updatePolicyType(PolicyTypeData policy) {
		String originId = policy.getUniqueId();
		policy.getPolicyTypeDataDefinition().setUniqueId(updateNamespace(originId));
		String type = updateNamespace(policy.getPolicyTypeDataDefinition().getType());
		policy.getPolicyTypeDataDefinition().setType(type);
		return new ImmutablePair<>(originId, policy);
	}

	private ImmutablePair<String, RelationshipTypeData> updateRelationshipType(RelationshipTypeData relation) {
		String type = updateNamespace(relation.getRelationshipTypeDataDefinition().getType());
		relation.getRelationshipTypeDataDefinition().setType(type);
		List<String> validSources = relation.getRelationshipTypeDataDefinition().getValidSourceTypes();
		if(null != validSources){
			List<String> validSourceTypes = new ArrayList<>();
			for (String validSourceType : validSources) {
				validSourceTypes.add(updateNamespace(validSourceType));
			}
			relation.getRelationshipTypeDataDefinition().setValidSourceTypes(validSourceTypes);
		}
		return new ImmutablePair<>(relation.getUniqueId(), relation);
	}

	private ImmutablePair<String, RelationshipInstData> updateRelationshipNode(RelationshipInstData relation) {
		String type = updateNamespace(relation.getType());
		relation.setType(type);
		return new ImmutablePair<>(relation.getUniqueId(), relation);
	}

	private ImmutablePair<String, RequirementData> updateRequirementType(RequirementData req) {
		String node = req.getNode();
		if(null != node)
			req.setNode(updateNamespace(node));
		String type = updateNamespace(req.getRelationshipType());
		req.setRelationshipType(type);
		return new ImmutablePair<>(req.getUniqueId(), req);
	}

	private ImmutablePair<String, CapabilityTypeData> updateCapabilityType(CapabilityTypeData capType) {
		String originId = capType.getUniqueId();
		capType.getCapabilityTypeDataDefinition().setUniqueId(updateNamespace(originId));
		String type = updateNamespace(capType.getCapabilityTypeDataDefinition().getType());
		capType.getCapabilityTypeDataDefinition().setType(type);
		List<String> validSources = capType.getCapabilityTypeDataDefinition().getValidSourceTypes();
		if(null != validSources){
			List<String> validSourceTypes = new ArrayList<>();
			for (String validSourceType : validSources) {
				validSourceTypes.add(updateNamespace(validSourceType));
			}
			capType.getCapabilityTypeDataDefinition().setValidSourceTypes(validSourceTypes);
		}	
		return new ImmutablePair<>(originId, capType);

	}

	private ImmutablePair<String, CapabilityData> updateCapabilityNode(CapabilityData capNode) {
		List<String> validSources = capNode.getValidSourceTypes();
		if(null != validSources){
			List<String> validSourceTypes = new ArrayList<>();
			for (String validSourceType : validSources) {
				validSourceTypes.add(updateNamespace(validSourceType));
			}
			capNode.setValidSourceTypes(validSourceTypes);
		}		
		return new ImmutablePair<>(capNode.getUniqueId(), capNode);
	}


	private ImmutablePair<String, PropertyData> updatePropNode(PropertyData propType) {
		String originId = (String)propType.getUniqueId();
		propType.getPropertyDataDefinition().setUniqueId(updateNamespace(originId));
		String type = updateNamespace(propType.getPropertyDataDefinition().getType());
		propType.getPropertyDataDefinition().setType(type);
		if ("list".equalsIgnoreCase(type) || "map".equalsIgnoreCase(type)){
			SchemaDefinition schema = propType.getPropertyDataDefinition().getSchema();
			if(null != schema && null != schema.getProperty())
				handleSchemaTypeDef(schema.getProperty());
		}
		return new ImmutablePair<>(originId, propType);
	}

	private ImmutablePair<String, PropertyValueData> updatePropValueNode(PropertyValueData prop) {
		String type = updateNamespace(prop.getType());
		prop.setType(type);
		return new ImmutablePair<>(prop.getUniqueId(), prop);
	}
	
	private ImmutablePair<String, AttributeValueData> updateAttrValueNode(AttributeValueData attr) {
		String type = updateNamespace(attr.getType());
		attr.setType(type);
		return new ImmutablePair<>(attr.getUniqueId(), attr);
	}
	
	private ImmutablePair<String, InputValueData> updateInputValueNode(InputValueData input) {
		String type = updateNamespace(input.getType());
		input.setType(type);
		return new ImmutablePair<>(input.getUniqueId(), input);
	}
	
	private ImmutablePair<String, InputsData> updateInputNode(InputsData input){
		String type = updateNamespace(input.getPropertyDataDefinition().getType());
		input.getPropertyDataDefinition().setType(type);
		if ("list".equalsIgnoreCase(type) || "map".equalsIgnoreCase(type)){
			SchemaDefinition schema = input.getPropertyDataDefinition().getSchema();
			if(null != schema && null != schema.getProperty())
				handleSchemaTypeDef(schema.getProperty());
		}
		return new ImmutablePair<>((String)input.getUniqueId(), input);
	}


	private void handleSchemaTypeDef(PropertyDataDefinition schemaProp) {
		String schemaType = updateNamespace(schemaProp.getType());
		schemaProp.setType(schemaType);
	}

	private ImmutablePair<String, DataTypeData> updateDataType(DataTypeData dataType) {
		String originId = dataType.getUniqueId();
		dataType.getDataTypeDataDefinition().setUniqueId(updateNamespace(originId));
		String name = updateNamespace(dataType.getDataTypeDataDefinition().getName());
		dataType.getDataTypeDataDefinition().setName(name);
		String derivedFromName = updateNamespace(dataType.getDataTypeDataDefinition().getDerivedFromName());
		dataType.getDataTypeDataDefinition().setDerivedFromName(derivedFromName);
		return new ImmutablePair<>(originId, dataType);

	}
	
	private ImmutablePair<String, AttributeData> updateAttributeNode(AttributeData attr){
		String type = updateNamespace(attr.getAttributeDataDefinition().getType());
		attr.getAttributeDataDefinition().setType(type);
		if("list".equalsIgnoreCase(type) || "map".equalsIgnoreCase(type)){
			SchemaDefinition schema = attr.getAttributeDataDefinition().getSchema();
			if(null != schema && null != schema.getProperty())
				handleSchemaTypeDef(schema.getProperty());
		}
		return new ImmutablePair<>(attr.getUniqueId(), attr);
	}
	
	

	private String updateNamespace(String oldName) {
		if (oldName == null) {
			return null;
		}
		String name = oldName.replace("com.att.d2.", "org.openecomp.");
		// correcting naming convention
		return name.replace("org.openecomp.resources.", "org.openecomp.resource.");
	}
	
	private <T extends GraphNode> T onSuccess(TitanVertex vertex, GraphNode node, Class<T> clazz){
		Map<String, Object> newProp = titanGenericDao.getProperties(vertex);
		return GraphElementFactory.createElement(node.getLabel(), GraphElementTypeEnum.Node, newProp, clazz);
	}
	
	private <T extends GraphNode> Either<T, TitanOperationStatus> handleNode(Vertex vertex, GraphNode node, Class<T> clazz){
		try {
			
			Map<String, Object> mapProps = node.toGraphMap();

			for (Map.Entry<String, Object> entry : mapProps.entrySet()) {
				vertex.property(entry.getKey(), entry.getValue());
			}

			Either<TitanVertex, TitanOperationStatus> vertexByPropertyAndLabel = titanGenericDao.getVertexByProperty(node.getUniqueIdKey(), node.getUniqueId());
			return vertexByPropertyAndLabel.either(v -> Either.left(onSuccess(v, node, clazz)), status -> Either.right(status));
			
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to update node for {}", node.getKeyValueId(), e);
			}
			return Either.right(TitanGraphClient.handleTitanException(e));
		}
	}
	
	private <T extends GraphNode> Either<T, TitanOperationStatus> updateNodeIncludingUID(String originId, GraphNode node, Class<T> clazz) {
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(node.getUniqueIdKey(), originId);
		return vertexByProperty.either(vertex -> handleNode(vertex, node, clazz), status -> Either.right(status));	
	}
}
