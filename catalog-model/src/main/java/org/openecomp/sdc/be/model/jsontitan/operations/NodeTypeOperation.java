package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabiltyProperty;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
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
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

@org.springframework.stereotype.Component("node-type-operation")
public class NodeTypeOperation extends ToscaElementOperation {
	public static Pattern uuidNewVersion = Pattern.compile("^\\d{1,}.1");
	public static Pattern uuidNormativeNewVersion = Pattern.compile("^\\d{1,}.0");

	private static Logger log = LoggerFactory.getLogger(NodeTypeOperation.class.getName());

	private DerivedNodeTypeResolver derivedResourceResolver;

	public NodeTypeOperation(@Qualifier("derived-resource-resolver") DerivedNodeTypeResolver derivedNodeTypeResolver) {
		this.derivedResourceResolver = derivedNodeTypeResolver;
	}

	public Either<NodeType, StorageOperationStatus> createNodeType(NodeType nodeType) {

		Either<NodeType, StorageOperationStatus> result = null;

		nodeType.generateUUID();

		nodeType = getResourceMetaDataFromResource(nodeType);
		String resourceUniqueId = nodeType.getUniqueId();
		if (resourceUniqueId == null) {
			resourceUniqueId = UniqueIdBuilder.buildResourceUniqueId();
			nodeType.setUniqueId(resourceUniqueId);
		}

		// get derived from resources
		List<GraphVertex> derivedResources = null;
		Either<List<GraphVertex>, StorageOperationStatus> derivedResourcesResult = findDerivedResources(nodeType);
		if (derivedResourcesResult.isRight()) {
			result = Either.right(derivedResourcesResult.right().value());
			return result;
		} else {
			derivedResources = derivedResourcesResult.left().value();
		}

		GraphVertex nodeTypeVertex = new GraphVertex(VertexTypeEnum.NODE_TYPE);
		fillToscaElementVertexData(nodeTypeVertex, nodeType, JsonParseFlagEnum.ParseAll);

		Either<GraphVertex, TitanOperationStatus> createdVertex = titanDao.createVertex(nodeTypeVertex);
		if (createdVertex.isRight()) {
			TitanOperationStatus status = createdVertex.right().value();
			log.error("Error returned after creating resource data node {}. status returned is ", nodeTypeVertex, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}
		nodeTypeVertex = createdVertex.left().value();

		StorageOperationStatus assosiateCommon = assosiateCommonForToscaElement(nodeTypeVertex, nodeType, derivedResources);
		if (assosiateCommon != StorageOperationStatus.OK) {
			result = Either.right(assosiateCommon);
			return result;
		}

		StorageOperationStatus associateDerived = assosiateToDerived(nodeTypeVertex, derivedResources);
		if (associateDerived != StorageOperationStatus.OK) {
			result = Either.right(associateDerived);
			return result;
		}
		StorageOperationStatus associateCategory = assosiateResourceMetadataToCategory(nodeTypeVertex, nodeType);
		if (associateCategory != StorageOperationStatus.OK) {
			result = Either.right(associateCategory);
			return result;
		}

		StorageOperationStatus associateAttributes = associateAttributesToResource(nodeTypeVertex, nodeType, derivedResources);
		if (associateAttributes != StorageOperationStatus.OK) {
			result = Either.right(associateAttributes);
			return result;
		}

		StorageOperationStatus associateRequirements = associateRequirementsToResource(nodeTypeVertex, nodeType, derivedResources);
		if (associateRequirements != StorageOperationStatus.OK) {
			result = Either.right(associateRequirements);
			return result;
		}

		StorageOperationStatus associateCapabilities = associateCapabilitiesToResource(nodeTypeVertex, nodeType, derivedResources);
		if (associateCapabilities != StorageOperationStatus.OK) {
			result = Either.right(associateCapabilities);
			return result;
		}
		StorageOperationStatus associateCapabilitiesProps = associateCapabilitiesPropertiesToResource(nodeTypeVertex, nodeType, derivedResources);
		if (associateCapabilitiesProps != StorageOperationStatus.OK) {
			result = Either.right(associateCapabilitiesProps);
			return result;
		}

		StorageOperationStatus associateInterfaces = associateInterfacesToResource(nodeTypeVertex, nodeType, derivedResources);
		if (associateInterfaces != StorageOperationStatus.OK) {
			result = Either.right(associateInterfaces);
			return result;
		}

		StorageOperationStatus addAdditionalInformation = addAdditionalInformationToResource(nodeTypeVertex, nodeType, derivedResources);
		if (addAdditionalInformation != StorageOperationStatus.OK) {
			result = Either.right(addAdditionalInformation);
			return result;
		}
		result = Either.left(nodeType);
		return result;

	}

	private StorageOperationStatus associateInterfacesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
		// Note : currently only one derived supported!!!!
		Either<Map<String, InterfaceDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.INTERFACE_ARTIFACTS);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, InterfaceDataDefinition> interfacArtsAll = dataFromDerived.left().value();

		Map<String, InterfaceDataDefinition> interfacArts = nodeType.getInterfaceArtifacts();
		if (interfacArts != null) {
			interfacArtsAll.putAll(interfacArts);
		}
		if (!interfacArtsAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.INTERFACE_ARTIFACTS, EdgeLabelEnum.INTERFACE_ARTIFACTS, interfacArtsAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<ToscaElement, StorageOperationStatus> getToscaElement(String uniqueId, ComponentParametersView componentParametersView) {

		Either<GraphVertex, StorageOperationStatus> componentByLabelAndId = getComponentByLabelAndId(uniqueId, ToscaElementTypeEnum.NodeType, JsonParseFlagEnum.ParseMetadata);
		if (componentByLabelAndId.isRight()) {
			return Either.right(componentByLabelAndId.right().value());
		}
		GraphVertex componentV = componentByLabelAndId.left().value();

		return getToscaElement(componentV, componentParametersView);

	}

	// -------------------------------------------------------------
	@Override
	public Either<ToscaElement, StorageOperationStatus> getToscaElement(GraphVertex componentV, ComponentParametersView componentParametersView) {
		NodeType toscaElement;
		toscaElement = convertToComponent(componentV);
		TitanOperationStatus status = null;
		if (false == componentParametersView.isIgnoreUsers()) {
			status = setCreatorFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

			status = setLastModifierFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreProperties()) {
			status = setResourcePropertiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK && status != TitanOperationStatus.NOT_FOUND) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreAttributesFrom()) {
			status = setResourceAttributesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreDerivedFrom()) {
			status = setResourceDerivedFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreCategories()) {
			status = setResourceCategoryFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreRequirements()) {
			status = setResourceRequirementsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				log.error("Failed to set requirement of resource {}. status is {}", componentV.getUniqueId(), status);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreCapabilities()) {
			status = setResourceCapabilitiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreArtifacts()) {
			status = setArtifactsFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreAdditionalInformation()) {
			status = setAdditionalInformationFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreInterfaces()) {
			status = setInterfacesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}
		if (false == componentParametersView.isIgnoreAllVersions()) {
			status = setAllVersions(componentV, toscaElement);
			if (status != TitanOperationStatus.OK && status != TitanOperationStatus.NOT_FOUND) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}
		}

		if (false == componentParametersView.isIgnoreCapabiltyProperties()) {
			status = setComponentCapPropertiesFromGraph(componentV, toscaElement);
			if (status != TitanOperationStatus.OK) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

			}
		}
		return Either.left(toscaElement);
	}

	private TitanOperationStatus setComponentCapPropertiesFromGraph(GraphVertex componentV, NodeType toscaElement) {
		Either<Map<String, MapPropertiesDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CAPABILITIES_PROPERTIES);
		if (result.isLeft()) {
			toscaElement.setCapabiltiesProperties(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setInterfacesFromGraph(GraphVertex componentV, NodeType toscaElement) {
		Either<Map<String, InterfaceDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.INTERFACE_ARTIFACTS);
		if (result.isLeft()) {
			toscaElement.setInterfaceArtifacts(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	protected <T extends ToscaElement> TitanOperationStatus setCapabilitiesFromGraph(GraphVertex componentV, T toscaElement) {
		return setResourceCapabilitiesFromGraph(componentV, (NodeType) toscaElement);
	}

	private TitanOperationStatus setResourceCapabilitiesFromGraph(GraphVertex componentV, NodeType toscaElement) {
		Either<Map<String, ListCapabilityDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.CAPABILITIES);
		if (result.isLeft()) {
			toscaElement.setCapabilties(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setResourceDerivedFromGraph(GraphVertex componentV, NodeType toscaElement) {
		List<String> derivedFromList = new ArrayList<String>();

		TitanOperationStatus listFromGraphStatus = findResourcesPathRecursively(componentV, derivedFromList);
		if (TitanOperationStatus.OK != listFromGraphStatus) {
			return listFromGraphStatus;
		}

		if (false == derivedFromList.isEmpty()) {
			if (derivedFromList.size() > 1) {
				List<String> lastDerivedFrom = new ArrayList<String>();
				lastDerivedFrom.add(derivedFromList.get(1));
				toscaElement.setDerivedFrom(lastDerivedFrom);
				toscaElement.setDerivedList(derivedFromList);
			} else {
				toscaElement.setDerivedFrom(null);
				toscaElement.setDerivedList(derivedFromList);
			}

		}
		return TitanOperationStatus.OK;
	}

	protected TitanOperationStatus findResourcesPathRecursively(GraphVertex nodeTypeV, List<String> resourcesPathList) {
		Either<GraphVertex, TitanOperationStatus> parentResourceRes = titanDao.getChildVertex(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
		resourcesPathList.add((String) nodeTypeV.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME));
		while (parentResourceRes.isLeft()) {

			GraphVertex parent = parentResourceRes.left().value();
			resourcesPathList.add((String) parent.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME));
			parentResourceRes = titanDao.getChildVertex(parent, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
		}
		TitanOperationStatus operationStatus = parentResourceRes.right().value();

		if (operationStatus != TitanOperationStatus.NOT_FOUND) {
			return operationStatus;
		} else {
			return TitanOperationStatus.OK;
		}

	}

	protected <T extends ToscaElement> TitanOperationStatus setRequirementsFromGraph(GraphVertex componentV, T toscaElement) {
		return setResourceRequirementsFromGraph(componentV, (NodeType) toscaElement);
	}

	private TitanOperationStatus setResourceRequirementsFromGraph(GraphVertex componentV, NodeType toscaElement) {
		Either<Map<String, ListRequirementDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.REQUIREMENTS);
		if (result.isLeft()) {
			toscaElement.setRequirements(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setResourceAttributesFromGraph(GraphVertex componentV, NodeType toscaElement) {
		Either<Map<String, PropertyDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.ATTRIBUTES);
		if (result.isLeft()) {
			toscaElement.setAttributes(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus setResourcePropertiesFromGraph(GraphVertex componentV, NodeType toscaElement) {
		Either<Map<String, PropertyDataDefinition>, TitanOperationStatus> result = getDataFromGraph(componentV, EdgeLabelEnum.PROPERTIES);
		if (result.isLeft()) {
			toscaElement.setProperties(result.left().value());
		} else {
			if (result.right().value() != TitanOperationStatus.NOT_FOUND) {
				return result.right().value();
			}
		}
		return TitanOperationStatus.OK;
	}

	private StorageOperationStatus assosiateToDerived(GraphVertex nodeTypeVertex, List<GraphVertex> derivedResources) {
		for (GraphVertex derivedV : derivedResources) {
			TitanOperationStatus createEdge = titanDao.createEdge(nodeTypeVertex, derivedV, EdgeLabelEnum.DERIVED_FROM, null);
			if (createEdge != TitanOperationStatus.OK) {
				log.trace("Failed to associate resource {} to derived with id {}", nodeTypeVertex.getUniqueId(), derivedV.getUniqueId());
				return DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge);
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus addAdditionalInformationToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
		// Note : currently only one derived supported!!!!
		Either<Map<String, AdditionalInfoParameterDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.ADDITIONAL_INFORMATION);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, AdditionalInfoParameterDataDefinition> addInformationAll = dataFromDerived.left().value();

		Map<String, AdditionalInfoParameterDataDefinition> addInformation = nodeType.getAdditionalInformation();
		if (addInformation != null) {
			ToscaDataDefinition.mergeDataMaps(addInformationAll, addInformation);
		}
		if (!addInformationAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.ADDITIONAL_INFORMATION, EdgeLabelEnum.ADDITIONAL_INFORMATION, addInformationAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateCapabilitiesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
		// Note : currently only one derived supported!!!!
		Either<Map<String, ListCapabilityDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.CAPABILITIES);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, ListCapabilityDataDefinition> capabiltiesAll = dataFromDerived.left().value();

		Map<String, ListCapabilityDataDefinition> capabilties = nodeType.getCapabilties();
		if (capabilties != null) {
			if (capabiltiesAll == null) {
				capabiltiesAll = new HashMap<>();
			}
			capabilties.values().forEach(l -> {
				l.getListToscaDataDefinition().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
					String uid = UniqueIdBuilder.buildCapabilityUid(nodeTypeVertex.getUniqueId(), p.getName());
					p.setUniqueId(uid);
				});
			});

			ToscaDataDefinition.mergeDataMaps(capabiltiesAll, capabilties);
		}
		if (!capabiltiesAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CAPABILTIES, EdgeLabelEnum.CAPABILITIES, capabiltiesAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateRequirementsToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
		// Note : currently only one derived supported!!!!
		Either<Map<String, ListRequirementDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.REQUIREMENTS);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, ListRequirementDataDefinition> requirementsAll = dataFromDerived.left().value();

		Map<String, ListRequirementDataDefinition> requirements = nodeType.getRequirements();
		if (requirements != null) {
			if (requirementsAll == null) {
				requirementsAll = new HashMap<>();
			}
			requirements.values().forEach(l -> {
				l.getListToscaDataDefinition().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
					String uid = UniqueIdBuilder.buildRequirementUid(nodeTypeVertex.getUniqueId(), p.getName());
					p.setUniqueId(uid);
				});
			});
			
			ToscaDataDefinition.mergeDataMaps(requirementsAll, requirements);

		}
		if (!requirementsAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.REQUIREMENTS, EdgeLabelEnum.REQUIREMENTS, requirementsAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus associateAttributesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
		// Note : currently only one derived supported!!!!
		Either<Map<String, PropertyDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.ATTRIBUTES);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, PropertyDataDefinition> attributesAll = dataFromDerived.left().value();

		Map<String, PropertyDataDefinition> attributes = nodeType.getAttributes();
		if (attributes != null) {
			attributes.values().stream().filter(p -> p.getUniqueId() == null).forEach(p -> {
				String uid = UniqueIdBuilder.buildAttributeUid(nodeTypeVertex.getUniqueId(), p.getName());
				p.setUniqueId(uid);
			});
			ToscaDataDefinition.mergeDataMaps(attributesAll, attributes);
		}
		if (!attributesAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.ATTRIBUTES, EdgeLabelEnum.ATTRIBUTES, attributesAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	// TODO get from derived
	private StorageOperationStatus associateCapabilitiesPropertiesToResource(GraphVertex nodeTypeVertex, NodeType nodeType, List<GraphVertex> derivedResources) {
		// // Note : currently only one derived supported!!!!
		Either<Map<String, MapPropertiesDataDefinition>, StorageOperationStatus> dataFromDerived = getDataFromDerived(derivedResources, EdgeLabelEnum.CAPABILITIES_PROPERTIES);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, MapPropertiesDataDefinition> propertiesAll = dataFromDerived.left().value();
		Map<String, MapPropertiesDataDefinition> capabiltiesProps = nodeType.getCapabiltiesProperties();
		if (capabiltiesProps != null) {
			capabiltiesProps.values().forEach(l -> {
				if (l.getMapToscaDataDefinition() != null && l.getMapToscaDataDefinition().values() != null) {
					Collection<PropertyDataDefinition> mapToscaDataDefinition = l.getMapToscaDataDefinition().values();
					mapToscaDataDefinition.stream().filter(p -> p != null && p.getUniqueId() == null).forEach(p -> {
						String uid = UniqueIdBuilder.buildRequirementUid(nodeTypeVertex.getUniqueId(), p.getName());
						p.setUniqueId(uid);
					});
				}
			});
			ToscaDataDefinition.mergeDataMaps(propertiesAll, capabiltiesProps);
		}
		if (!propertiesAll.isEmpty()) {
			Either<GraphVertex, StorageOperationStatus> assosiateElementToData = assosiateElementToData(nodeTypeVertex, VertexTypeEnum.CAPABILITIES_PROPERTIES, EdgeLabelEnum.CAPABILITIES_PROPERTIES, propertiesAll);
			if (assosiateElementToData.isRight()) {
				return assosiateElementToData.right().value();
			}
		}
		return StorageOperationStatus.OK;
	}

	public Either<List<GraphVertex>, StorageOperationStatus> findDerivedResources(NodeType nodeType) {

		List<GraphVertex> derivedResources = new ArrayList<GraphVertex>();
		List<String> derivedFromResources = nodeType.getDerivedFrom();
		if (derivedFromResources != null && false == derivedFromResources.isEmpty()) {

			for (String parentResource : derivedFromResources) {
				Either<List<GraphVertex>, TitanOperationStatus> getParentResources = derivedResourceResolver.findDerivedResources(parentResource);
				List<GraphVertex> resources = null;
				if (getParentResources.isRight()) {
					log.error("Cannot find parent resource by tosca resource name {} in the graph.", parentResource);
					return Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);

				} else {
					resources = getParentResources.left().value();
					if (resources == null || resources.size() == 0) {
						log.error("Cannot find parent resource by tosca name {} in the graph. resources size is empty", parentResource);
						return Either.right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
					} else {
						if (resources.size() > 1) {
							log.error("Multiple parent resources called {} found in the graph.", parentResource);
							return Either.right(StorageOperationStatus.MULTIPLE_PARENT_RESOURCE_FOUND);
						}
						GraphVertex parentResourceData = resources.get(0);
						derivedResources.add(parentResourceData);
					}

				}

			}
		}
		return Either.left(derivedResources);
	}

	private GraphVertex fillMetadata(GraphVertex nodeTypeVertex, NodeType nodeType) {
		nodeTypeVertex.setLabel(VertexTypeEnum.NODE_TYPE);

		fillCommonMetadata(nodeTypeVertex, nodeType);

		return nodeTypeVertex;
	}

	@Override
	public Either<ToscaElement, StorageOperationStatus> deleteToscaElement(GraphVertex toscaElementVertex) {
		Either<ToscaElement, StorageOperationStatus> nodeType = getToscaElement(toscaElementVertex, new ComponentParametersView());
		if (nodeType.isRight()) {
			log.debug("Failed to fetch tosca element {} error {}", toscaElementVertex.getUniqueId(), nodeType.right().value());
			return nodeType;
		}
		TitanOperationStatus status = disassociateAndDeleteCommonElements(toscaElementVertex);
		if (status != TitanOperationStatus.OK) {
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CAPABILITIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate capabilties for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.CAPABILITIES_PROPERTIES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate capabilties properties for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.REQUIREMENTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate requirements for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.ATTRIBUTES);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate attributes for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		status = titanDao.disassociateAndDeleteLast(toscaElementVertex, Direction.OUT, EdgeLabelEnum.INTERFACE_ARTIFACTS);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to disassociate interface artifacts for {} error {}", toscaElementVertex.getUniqueId(), status);
			Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		toscaElementVertex.getVertex().remove();
		log.trace("Tosca element vertex for {} was removed", toscaElementVertex.getUniqueId());

		return nodeType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<NodeType, StorageOperationStatus> createToscaElement(ToscaElement toscaElement) {
		return createNodeType((NodeType) toscaElement);
	}

	@Override
	protected <T extends ToscaElement> TitanOperationStatus setCategoriesFromGraph(GraphVertex vertexComponent, T toscaElement) {
		return setResourceCategoryFromGraph(vertexComponent, toscaElement);
	}

	@Override
	protected <T extends ToscaElement> StorageOperationStatus validateCategories(T toscaElementToUpdate, GraphVertex elementV) {
		return validateResourceCategory(toscaElementToUpdate, elementV);
	}

	@Override
	protected <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate, GraphVertex nodeTypeV) {

		NodeType nodeType = (NodeType) toscaElementToUpdate;
		List<GraphVertex> derivedResources = new ArrayList<>();

		List<String> derivedFromResources = nodeType.getDerivedFrom();

		// now supported only single derived from
		if (derivedFromResources != null && !derivedFromResources.isEmpty() && derivedFromResources.get(0) != null) {
			String firstDerived = derivedFromResources.get(0);
			boolean derivedFromGenericType = null != nodeType.getDerivedFromGenericType();
			Either<GraphVertex, TitanOperationStatus> childVertex = titanDao.getChildVertex(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.NoParse);
			if (childVertex.isRight()) {
				TitanOperationStatus getchieldError = childVertex.right().value();
				log.debug("Failed to fetch derived resource for element {} error {}", nodeTypeV.getUniqueId(), getchieldError);
				return DaoStatusConverter.convertTitanStatusToStorageStatus(getchieldError);
			}
			GraphVertex firstDerivedInChain = childVertex.left().value();

			String firstCurrentDerived = (String) firstDerivedInChain.getMetadataProperty(GraphPropertyEnum.TOSCA_RESOURCE_NAME);
			if (!firstDerived.equals(firstCurrentDerived) || derivedFromGenericType) {

				Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<GraphPropertyEnum, Object>();
				propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

				propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, firstDerived);
				propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

				Either<List<GraphVertex>, TitanOperationStatus> getParentResources = titanDao.getByCriteria(VertexTypeEnum.NODE_TYPE, propertiesToMatch, JsonParseFlagEnum.NoParse);

				if (getParentResources.isRight()) {
					TitanOperationStatus error = getParentResources.right().value();
					CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch derived by criteria {}. error {} ", propertiesToMatch, error);
					return DaoStatusConverter.convertTitanStatusToStorageStatus(error);
				}
				// must be only one
				GraphVertex newDerived = getParentResources.left().value().get(0);
				derivedResources.add(newDerived);
				StorageOperationStatus updateStatus = updateDataFromNewDerived(derivedResources, nodeTypeV, (NodeType)toscaElementToUpdate);
				if (updateStatus != StorageOperationStatus.OK) {
					CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update data for {} from new derived {} ", nodeTypeV.getUniqueId(), newDerived.getUniqueId(), updateStatus);
					return updateStatus;
				}

				Either<Edge, TitanOperationStatus> deleteEdge = titanDao.deleteEdge(nodeTypeV, firstDerivedInChain, EdgeLabelEnum.DERIVED_FROM);
				if (deleteEdge.isRight()) {
					TitanOperationStatus deleteError = deleteEdge.right().value();
					log.debug("Failed to disassociate element {} from derived {} , error {}", nodeTypeV.getUniqueId(), firstDerivedInChain.getUniqueId(), deleteError);
					return DaoStatusConverter.convertTitanStatusToStorageStatus(deleteError);
				}

				titanDao.createEdge(nodeTypeV, newDerived, EdgeLabelEnum.DERIVED_FROM, new HashMap<>());
			}
		}

		return StorageOperationStatus.OK;
	}
	
	private StorageOperationStatus associateDerivedDataByType(EdgeLabelEnum edgeLabel, GraphVertex nodeTypeV, NodeType nodeToUpdate, List<GraphVertex> newDerived) {
		
		switch (edgeLabel) {
		case CAPABILITIES:
			return associateCapabilitiesToResource(nodeTypeV, nodeToUpdate, newDerived);
		case REQUIREMENTS:
			return associateRequirementsToResource(nodeTypeV, nodeToUpdate, newDerived);
		case PROPERTIES:
			return associatePropertiesToResource(nodeTypeV, nodeToUpdate, newDerived);
		case ATTRIBUTES:
			return associateAttributesToResource(nodeTypeV, nodeToUpdate, newDerived);
		case ADDITIONAL_INFORMATION:
			return addAdditionalInformationToResource(nodeTypeV, nodeToUpdate, newDerived);
		case CAPABILITIES_PROPERTIES:
			return associateCapabilitiesPropertiesToResource(nodeTypeV, nodeToUpdate, newDerived);
		default:
			return StorageOperationStatus.OK;
		}

	}

	private StorageOperationStatus updateDataFromNewDerived(List<GraphVertex> newDerived, GraphVertex nodeTypeV, NodeType nodeToUpdate) {
        
		StorageOperationStatus status = updateDataByType(newDerived, nodeTypeV, EdgeLabelEnum.CAPABILITIES, nodeToUpdate);
		if (status != StorageOperationStatus.OK) {
			return status;
		}
		
		status = updateDataByType(newDerived, nodeTypeV, EdgeLabelEnum.REQUIREMENTS, nodeToUpdate);
		if (status != StorageOperationStatus.OK) {
			return status;
		}
	
		status = updateDataByType(newDerived, nodeTypeV, EdgeLabelEnum.PROPERTIES, nodeToUpdate);
		if (status != StorageOperationStatus.OK) {
			return status;
		}
		
		status = updateDataByType(newDerived, nodeTypeV, EdgeLabelEnum.ATTRIBUTES, nodeToUpdate);
		if (status != StorageOperationStatus.OK) {
				return status;
		}
		
		status = updateDataByType(newDerived, nodeTypeV,EdgeLabelEnum.CAPABILITIES_PROPERTIES, nodeToUpdate);
		if (status != StorageOperationStatus.OK) {
			return status;
		}
		status = updateDataByType(newDerived, nodeTypeV, EdgeLabelEnum.ADDITIONAL_INFORMATION, nodeToUpdate);
		return status;
	}

	private <T extends ToscaDataDefinition> StorageOperationStatus updateDataByType(List<GraphVertex> newDerivedList, GraphVertex nodeTypeV, EdgeLabelEnum label, NodeType nodeElement) {
		log.debug("Update data from derived for element {} type {}", nodeTypeV.getUniqueId(), label);
		Either<GraphVertex, TitanOperationStatus> dataFromGraph = getDataVertex(nodeTypeV, label);
		if (dataFromGraph.isRight()) {
			if (TitanOperationStatus.NOT_FOUND == dataFromGraph.right().value())
				return associateDerivedDataByType(label, nodeTypeV, nodeElement, newDerivedList);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(dataFromGraph.right().value());
		}
		GraphVertex dataV = dataFromGraph.left().value();

		Map<String, T> mapFromGraph = (Map<String, T>) dataV.getJson();
		mapFromGraph.entrySet().removeIf(e -> e.getValue().getOwnerId() != null);

		
		Either<Map<String, T>, StorageOperationStatus> dataFromDerived = getDataFromDerived(newDerivedList, label);
		if (dataFromDerived.isRight()) {
			return dataFromDerived.right().value();
		}
		Map<String, T> dataFromDerivedAll = dataFromDerived.left().value();
		
		Either<Map<String, T>, String> merged = ToscaDataDefinition.mergeDataMaps(dataFromDerivedAll, mapFromGraph);
		if(merged.isRight()){
			log.debug("property {} cannot be overriden", merged.right().value());
			return StorageOperationStatus.INVALID_PROPERTY;
		}
		dataV.setJson(dataFromDerivedAll);
		Either<GraphVertex, TitanOperationStatus> updateDataV = updateOrCopyOnUpdate(dataV, nodeTypeV, label);
		if (updateDataV.isRight()) {
			return DaoStatusConverter.convertTitanStatusToStorageStatus(updateDataV.right().value());
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public <T extends ToscaElement> void fillToscaElementVertexData(GraphVertex elementV, T toscaElementToUpdate, JsonParseFlagEnum flag) {
		fillMetadata(elementV, (NodeType) toscaElementToUpdate);
	}

}
