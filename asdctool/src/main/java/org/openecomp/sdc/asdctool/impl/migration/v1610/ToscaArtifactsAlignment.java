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

package org.openecomp.sdc.asdctool.impl.migration.v1610;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.AbstractOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.StreamUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

/**
 * This Class holds the logic to add Tosca Artifacts placeholder and payload.<br>
 * This addition is done for old version of Services and Resources (pre 1610) that weren't created with them.<br>
 * 
 * @author mshitrit <br>
 * 
 *
 */
public class ToscaArtifactsAlignment extends AbstractOperation {
	@Autowired
	private IArtifactOperation artifactOperation;

	@Autowired
	private ServiceBusinessLogic serviceBusinessLogic;

	private static Logger log = LoggerFactory.getLogger(ToscaArtifactsAlignment.class.getName());

	private static final String ERROR_PREFIX = "Tosca Artifact Alignment Error: ";

	// API that Fetches Resource
	private final Function<ComponentMetadataData, Resource> resourceFetcher = componentMD -> getComponent(componentMD, ComponentTypeEnum.RESOURCE);
	// API that Fetches Service
	private final Function<ComponentMetadataData, Service> serviceFetcher = componentMD -> getComponent(componentMD, ComponentTypeEnum.SERVICE);
	// Class Getters
	private final Supplier<Class<ResourceMetadataData>> resourceClassGetter = () -> ResourceMetadataData.class;
	private final Supplier<Class<ServiceMetadataData>> serviceClassGetter = () -> ServiceMetadataData.class;

	/**
	 * This method holds the logic to add Tosca Artifacts placeholder and payload.<br>
	 * 
	 * @return true if succeed otherwise returns false
	 */
	public boolean alignToscaArtifacts() {
		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		List<ResourceMetadataData> allResources = new ArrayList<>();
		List<ResourceMetadataData> resourcesWithoutToscaPlaceHolder = new ArrayList<>();
		List<ServiceMetadataData> allServices = new ArrayList<>();
		List<ServiceMetadataData> servicesWithoutToscaPlaceHolder = new ArrayList<>();
		log.debug("alignToscaArtifacts Start");
		try {

			if (errorWrapper.isEmpty()) {
				log.info("Fetching all resources");
				fillAllComponetOfSpecificType(allResources, NodeTypeEnum.Resource, resourceClassGetter, errorWrapper);
			}

			if (errorWrapper.isEmpty()) {
				// Filter Resources Without Tosca Artifacts
				log.info("filtering resources to add tosca placeholder");
				Either<List<ResourceMetadataData>, TitanOperationStatus> eitherRelevantResources = getComponentsWithMissingToscaArtifacts(resourceClassGetter, NodeTypeEnum.Resource, allResources);
				fillListOrWrapper(errorWrapper, resourcesWithoutToscaPlaceHolder, eitherRelevantResources);
			}

			if (errorWrapper.isEmpty()) {
				// Add PlaceHolders To Resources
				log.info("adding tosca placeholders artifacts to resources");
				addToscaArtifactToComponents(resourcesWithoutToscaPlaceHolder, resourceFetcher, NodeTypeEnum.Resource, errorWrapper);
			}
			if (errorWrapper.isEmpty()) {
				// Add payload to Resources
				log.info("generating payload to tosca artifacts on resources");
				fillResourcesPayload(allResources, errorWrapper);
			}

			if (errorWrapper.isEmpty()) {
				log.info("Fetching all services");
				fillAllComponetOfSpecificType(allServices, NodeTypeEnum.Service, serviceClassGetter, errorWrapper);
			}
			if (errorWrapper.isEmpty()) {
				// Filter Services Without Tosca Artifacts
				log.info("filtering services to add tosca placeholder");
				Either<List<ServiceMetadataData>, TitanOperationStatus> eitherRelevantServices = getComponentsWithMissingToscaArtifacts(serviceClassGetter, NodeTypeEnum.Service, allServices);
				fillListOrWrapper(errorWrapper, servicesWithoutToscaPlaceHolder, eitherRelevantServices);
			}

			if (errorWrapper.isEmpty()) {
				// Add PlaceHolders To Services
				log.info("adding tosca placeholders artifacts to services");
				addToscaArtifactToComponents(servicesWithoutToscaPlaceHolder, serviceFetcher, NodeTypeEnum.Service, errorWrapper);
			}

			if (errorWrapper.isEmpty()) {
				// Filter Services for Payload Add
				// Add payload to Services
				log.info("generating payload to tosca artifacts on services");
				fillToscaArtifactPayload(allServices, serviceFetcher, errorWrapper);
			}
		} finally {
			titanGenericDao.commit();
		}
		return errorWrapper.isEmpty();

	}

	private void fillResourcesPayload(List<ResourceMetadataData> allResources, Wrapper<TitanOperationStatus> errorWrapper) {
		if (errorWrapper.isEmpty()) {
			// First Only Non VF (CP, VL & VFC)
			List<ResourceMetadataData> basicResources = allResources.stream().filter(e -> isBasicResource((ResourceMetadataDataDefinition) e.getMetadataDataDefinition())).collect(Collectors.toList());
			// Filter resources for Payload Add
			// Add payload to resources
			fillToscaArtifactPayload(basicResources, resourceFetcher, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			// VFs
			List<ResourceMetadataData> complexResource = allResources.stream().filter(e -> ((ResourceMetadataDataDefinition) e.getMetadataDataDefinition()).getResourceType() == ResourceTypeEnum.VF).collect(Collectors.toList());
			// Filter resources for Payload Add
			// Add payload to resources
			fillToscaArtifactPayload(complexResource, resourceFetcher, errorWrapper);
		}
	}

	private boolean isBasicResource(ResourceMetadataDataDefinition resourceMetadataDataDefinition) {
		final ResourceTypeEnum resourceType = resourceMetadataDataDefinition.getResourceType();
		boolean isBasicResource = resourceType == ResourceTypeEnum.CP || resourceType == ResourceTypeEnum.VL || resourceType == ResourceTypeEnum.VFC;
		return isBasicResource;
	}

	private <T extends ComponentMetadataData> void fillAllComponetOfSpecificType(List<T> components, NodeTypeEnum nodeType, Supplier<Class<T>> classGetter, Wrapper<TitanOperationStatus> errorWrapper) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.IS_DELETED.getProperty(), true);
		Either<List<T>, TitanOperationStatus> eitherComponentMD = titanGenericDao.getByCriteria(nodeType, null, props, classGetter.get());
		if (eitherComponentMD.isLeft()) {
			components.addAll(eitherComponentMD.left().value());
		} else {
			final TitanOperationStatus errorType = eitherComponentMD.right().value();
			if (errorType != TitanOperationStatus.NOT_FOUND) {
				log.error("{} When fetching all components of type:{} a titan error occured:{}", ERROR_PREFIX, nodeType.getName(), errorType.name());
				errorWrapper.setInnerElement(errorType);
			}
		}

	}

	private <T extends ComponentMetadataData, R extends Component> void addToscaArtifactToComponents(List<T> relevantResources, Function<ComponentMetadataData, R> componentConvertor, NodeTypeEnum nodeType,
			Wrapper<TitanOperationStatus> errorWrapper) {

		// This Stream contains all create tosca placeholder results
		Stream<StorageOperationStatus> addToscaToComponentsResultsStream = relevantResources.stream().map(e -> addToscaArtifacts(e, nodeType, componentConvertor));
		// Execute the stream, and collect error
		Optional<StorageOperationStatus> optionalError = addToscaToComponentsResultsStream.filter(e -> e != StorageOperationStatus.OK).findFirst();

		// Handle error
		if (optionalError.isPresent()) {
			errorWrapper.setInnerElement(TitanOperationStatus.NOT_CREATED);
		}
	}

	private <R extends Component> R getComponent(ComponentMetadataData md, ComponentTypeEnum componentTypeEnum) {
		R result = null;
		Either<R, StorageOperationStatus> eitherComponent = serviceBusinessLogic.getComponent(md.getMetadataDataDefinition().getUniqueId(), componentTypeEnum);
		if (eitherComponent.isRight()) {
			log.error("{} When fetching component {} of type:{} with uniqueId:{}", ERROR_PREFIX, md.getMetadataDataDefinition().getName(), componentTypeEnum.getValue(), md.getMetadataDataDefinition().getUniqueId());
		} else {
			result = eitherComponent.left().value();
		}
		return result;
	}

	private Either<Either<ArtifactDefinition, Operation>, ResponseFormat> populateToscaArtifactsWithLog(Component component, User user, boolean isInCertificationRequest, boolean inTransaction, boolean shouldLock) {
		Either<Either<ArtifactDefinition, Operation>, ResponseFormat> ret;
		try {
			ret = serviceBusinessLogic.populateToscaArtifacts(component, user, isInCertificationRequest, inTransaction, shouldLock);
			if (ret.isLeft()) {
				log.debug("Added payload to tosca artifacts of component {} of type:{} with uniqueId:{}", component.getName(), component.getComponentType().getValue(), component.getUniqueId());
			}
			return ret;
		} catch (Exception e) {
			log.error("{} Exception Occured When filling tosca artifact payload for component {} of type:{} with uniqueId:{}", ERROR_PREFIX, component.getName(), component.getComponentType().name(), component.getUniqueId(), e);
			throw e;
		}
	}

	private <R extends Component, T extends ComponentMetadataData> void fillToscaArtifactPayload(List<T> relevantComponents, Function<ComponentMetadataData, R> componentCreator, Wrapper<TitanOperationStatus> errorWrapper) {

		final User dummyUser = buildDummyUser();
		// Stream for all fill payload results
		Stream<ImmutablePair<Component, Either<Either<ArtifactDefinition, Operation>, ResponseFormat>>>
		// Filter elements that needs generation of tosca payload
		fillToscaPayloadResultsStream = relevantComponents.stream().filter(e -> isGenerateToscaPayload(e))
				// Converts ComponentMetadataData to Component
				.map(e -> componentCreator.apply(e))
				// For each component generate payload for tosca
				// artifacts
				.map(component -> {
					return new ImmutablePair<Component, Either<Either<ArtifactDefinition, Operation>, ResponseFormat>>(component, populateToscaArtifactsWithLog(component, dummyUser, true, true, false));
				});

		try {
			// execute and the stream
			Optional<Component> optionalError = fillToscaPayloadResultsStream.
			// filter in error
					filter(e -> e.getRight().isRight())
					// convert the result to error and execute the stream
					.map(e -> e.getLeft()).findFirst();

			// Check if error occurred
			if (optionalError.isPresent()) {
				Component component = optionalError.get();
				log.error("{} When filling tosca artifact payload for component {} of type:{} with uniqueId:{}", ERROR_PREFIX, component.getName(), component.getComponentType().name(), component.getUniqueId());

				errorWrapper.setInnerElement(TitanOperationStatus.GENERAL_ERROR);
			}
		} catch (Exception e) {
			log.error("{} When filling tosca artifact payload for components : {}", ERROR_PREFIX, e.getMessage(), e);
			errorWrapper.setInnerElement(TitanOperationStatus.GENERAL_ERROR);
		}
	}

	private <R extends Component> StorageOperationStatus addToscaArtifacts(ComponentMetadataData component, NodeTypeEnum nodeType, Function<ComponentMetadataData, R> componentCreator) {

		StorageOperationStatus result = StorageOperationStatus.OK;
		R componentDefinition = componentCreator.apply(component);

		// Fetch artifacts to be Added
		Either<List<ArtifactDefinition>, StorageOperationStatus> eitherToscaArtifacts = getToscaArtifactsToAdd(componentDefinition);
		if (eitherToscaArtifacts.isRight()) {
			result = eitherToscaArtifacts.right().value();
		} else {
			List<ArtifactDefinition> toscaArtifactsToAdd = eitherToscaArtifacts.left().value();
			if (!CollectionUtils.isEmpty(eitherToscaArtifacts.left().value())) {
				final Stream<ImmutablePair<ArtifactDefinition, Either<ArtifactDefinition, StorageOperationStatus>>> createdToscaPlaceHolderStream = toscaArtifactsToAdd.stream()
						// creates the artifact in the graph
						.map(artifactDef -> new ImmutablePair<ArtifactDefinition, Either<ArtifactDefinition, StorageOperationStatus>>(artifactDef,
								artifactOperation.addArifactToComponent(artifactDef, componentDefinition.getUniqueId(), nodeType, false, true)));

				// Execute the stream, and collect error
				Optional<ImmutablePair<ArtifactDefinition, StorageOperationStatus>> optionalError = createdToscaPlaceHolderStream.filter(e -> e.getRight().isRight()).map(e -> new ImmutablePair<>(e.getLeft(), e.getRight().right().value()))
						.findFirst();

				// In case error occurred
				if (optionalError.isPresent()) {
					ArtifactDefinition toscaArtifact = optionalError.get().getLeft();
					StorageOperationStatus storageError = optionalError.get().getRight();
					log.error("{} When adding tosca artifact of type {} to component {} of type:{} " + "with uniqueId:{} a storageError occurred:{}", ERROR_PREFIX, toscaArtifact.getArtifactType(), component.getMetadataDataDefinition().getName(),
							nodeType.getName(), component.getMetadataDataDefinition().getUniqueId(), storageError.name());

					result = storageError;
				} else {
					log.debug("Added tosca artifacts to component {} of type:{} with uniqueId:{}", component.getMetadataDataDefinition().getName(), nodeType.getName(), component.getMetadataDataDefinition().getUniqueId());
				}

			}
		}

		return result;
	}

	private <R extends Component> Either<List<ArtifactDefinition>, StorageOperationStatus> getToscaArtifactsToAdd(R componentDefinition) {

		Either<List<ArtifactDefinition>, StorageOperationStatus> result;
		List<ArtifactDefinition> toscaArtifactsAlreadyExist = new ArrayList<>();
		if (!MapUtils.isEmpty(componentDefinition.getToscaArtifacts())) {
			toscaArtifactsAlreadyExist.addAll(componentDefinition.getToscaArtifacts().values());
		}

		// Set Tosca Artifacts on component
		serviceBusinessLogic.setToscaArtifactsPlaceHolders(componentDefinition, buildDummyUser());

		List<ArtifactDefinition> toscaArtifactsToAdd = new ArrayList<>();
		if (!MapUtils.isEmpty(componentDefinition.getToscaArtifacts())) {
			final Collection<ArtifactDefinition> allToscaArtifacts = componentDefinition.getToscaArtifacts().values();
			Set<String> artifactTypesExist = toscaArtifactsAlreadyExist.stream().map(e -> e.getArtifactType()).collect(Collectors.toSet());
			toscaArtifactsToAdd = allToscaArtifacts.stream().filter(e -> !artifactTypesExist.contains(e.getArtifactType())).collect(Collectors.toList());
			result = Either.left(toscaArtifactsToAdd);
		} else {
			log.error("{} failed to add tosca artifacts in bussiness logic to component {} of type:{} with uniqueId:{}", ERROR_PREFIX, componentDefinition.getName(), componentDefinition.getComponentType().getValue(),
					componentDefinition.getUniqueId());
			result = Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND);
		}
		return result;
	}

	private User buildDummyUser() {
		User user = new User();
		user.setUserId("migrationTask");
		return user;
	}

	private boolean isGenerateToscaPayload(ComponentMetadataData component) {
		final String state = component.getMetadataDataDefinition().getState();
		boolean componentLifeCycleStateIsValid = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name().equals(state) || LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name().equals(state);

		return !componentLifeCycleStateIsValid;

	}

	private <T> void fillListOrWrapper(Wrapper<TitanOperationStatus> wrapper, List<T> listToFill, Either<List<T>, TitanOperationStatus> either) {
		if (either.isRight()) {
			final TitanOperationStatus errorType = either.right().value();
			if (errorType != TitanOperationStatus.NOT_FOUND) {
				wrapper.setInnerElement(errorType);
			}
		} else {
			listToFill.addAll(either.left().value());
		}
	}

	private <T extends ComponentMetadataData> Either<List<T>, TitanOperationStatus> getComponentsWithMissingToscaArtifacts(Supplier<Class<T>> classGetter, NodeTypeEnum nodeType, List<T> allComponents) {

		Either<List<T>, TitanOperationStatus> result;
		Stream<ImmutablePair<T, Either<List<ArtifactData>, TitanOperationStatus>>> componentsWithToscaStream =
				// Create a Stream of pairs : component and its Tosca Artifacts
				allComponents.stream().map(e -> new ImmutablePair<>(e, getToscaArtifatcs(e, nodeType)));

		List<ImmutablePair<T, Either<List<ArtifactData>, TitanOperationStatus>>> componentsWithToscaArtifacts =
				// Collect the stream to list.
				// in case getToscaArtifatcs failed, the first failure is
				// added to the list
				// (the collection stops after first failure)
				StreamUtils.takeWhilePlusOneNoEval(componentsWithToscaStream, e -> e.getRight().isLeft()).collect(Collectors.toList());

		// retrieve the failure optional (it may or may not exist)
		Optional<TitanOperationStatus> isErrorOccured = componentsWithToscaArtifacts.stream()
				// convert to the right side of the pair of type Either
				.map(e -> e.getRight())
				// Filter in only the errors
				.filter(e -> e.isRight()).
				// map the error from Either to TitanOperationStatus
				map(e -> e.right().value()).findFirst();

		// In case failure occurred
		if (isErrorOccured.isPresent()) {
			result = Either.right(isErrorOccured.get());
			// In case NO failure occurred
		} else {
			List<T> filteredComponents = componentsWithToscaArtifacts.stream()
					// Filter in only elements that does NOT have tosca
					// artifacts
					.filter(e -> isNotContainAllToscaArtifacts(e))
					// Convert back to Components List & collect
					.map(e -> e.getLeft()).collect(Collectors.toList());

			result = Either.left(filteredComponents);
		}

		return result;
	}

	private <T extends ComponentMetadataData> boolean isNotContainAllToscaArtifacts(ImmutablePair<T, Either<List<ArtifactData>, TitanOperationStatus>> pair) {

		final List<ArtifactData> artifactList = pair.getRight().left().value();

		Set<ArtifactTypeEnum> filteredToscaList = artifactList.stream().
		// Convert to ArtifactDataDefinition
				map(e -> e.getArtifactDataDefinition()).
				// Filter in Only Tosca Artifacts
				filter(e -> e.getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA).
				// Convert To ArtifactTypeEnum
				map(e -> ArtifactTypeEnum.findType(e.getArtifactType())).
				// Filter Out nulls in case of Type not found
				filter(e -> e != null).collect(Collectors.toSet());

		boolean toscaArifactContained = filteredToscaList.contains(ArtifactTypeEnum.TOSCA_CSAR) && filteredToscaList.contains(ArtifactTypeEnum.TOSCA_TEMPLATE);
		return !toscaArifactContained;
	}

	private <T extends ComponentMetadataData> Either<List<ArtifactData>, TitanOperationStatus> getToscaArtifatcs(T component, NodeTypeEnum nodeType) {

		Either<List<ArtifactData>, TitanOperationStatus> result;
		// All The Artifacts of the Component
		Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> eitherComponentArtifacts = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), component.getMetadataDataDefinition().getUniqueId(),
				GraphEdgeLabels.ARTIFACT_REF, NodeTypeEnum.ArtifactRef, ArtifactData.class);

		if (eitherComponentArtifacts.isLeft()) {
			// Convert Artifact Edge Pair to Artifact
			List<ArtifactData> toscaArtifacts = eitherComponentArtifacts.left().value().stream()
					// map ImmutablePair<ArtifactData, GraphEdge> to
					// ArtifactData
					.map(e -> e.getLeft())
					// Filter in only Tosca Artifacts
					.filter(artifact -> artifact.getArtifactDataDefinition().getArtifactGroupType() == ArtifactGroupTypeEnum.TOSCA)
					// Collect
					.collect(Collectors.toList());
			result = Either.left(toscaArtifacts);
		} else if (eitherComponentArtifacts.right().value() == TitanOperationStatus.NOT_FOUND) {
			result = Either.left(new ArrayList<>());
		} else {
			final TitanOperationStatus titanError = eitherComponentArtifacts.right().value();
			log.error("{} When fetching artifacts for component {} of type:{} with uniqueId:{} a titanError occurred:{}", ERROR_PREFIX, component.getMetadataDataDefinition().getName(), nodeType.getName(),
					component.getMetadataDataDefinition().getUniqueId(), titanError.name());

			result = Either.right(titanError);
		}

		return result;
	}

}
