package org.openecomp.sdc.asdctool.impl.migration.v1707;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("distributionStatusUpdate")
public class DistributionStatusUpdate {
	private static Logger LOGGER = LoggerFactory.getLogger(DistributionStatusUpdate.class);

	@Autowired
    private ToscaOperationFacade toscaOperationFacade;
	@Autowired
    private TitanDao titanDao;
	
    
	public boolean migrate() {
		boolean result = true;
		Either<Map<GraphVertex, org.openecomp.sdc.be.model.Service>, StorageOperationStatus> getAllServiceComponentsRes = getAllServiceComponents();
		if(getAllServiceComponentsRes.isRight()){
			result = false;
		}
		if(result && MapUtils.isNotEmpty(getAllServiceComponentsRes.left().value())){
			updateDistributionStatusFromMetadata(getAllServiceComponentsRes.left().value());
			updateDistributionStatusToNotDistributed(getAllServiceComponentsRes.left().value());
		}
		
		toscaOperationFacade.commit();
		
		return result;
	}

	
	private void updateDistributionStatusToNotDistributed(Map<GraphVertex, org.openecomp.sdc.be.model.Service> components) {
		
		Map<GraphVertex, org.openecomp.sdc.be.model.Service> filteredComponents = components.entrySet()
				.stream()
				.filter(e -> e.getValue().getLifecycleState() != LifecycleStateEnum.CERTIFIED)
				.collect(Collectors.toMap(e -> e.getKey(), e -> (Service)e.getValue()));
		
		Service service;
		Either<GraphVertex, TitanOperationStatus> updateResponse;
		GraphVertex metadataV;
		
		for(Entry<GraphVertex, Service> currComponent : filteredComponents.entrySet()){
			metadataV = currComponent.getKey();
			service = currComponent.getValue();
			try {
				metadataV.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
				updateResponse = titanDao.updateVertex(metadataV);
				
				if (updateResponse.isRight()) {
					LOGGER.debug("failed to updateDistributionStatusToNotDistributed service  {} error {}", service.getUniqueId(), updateResponse.right().value());
				}
				
			} catch (Exception e) {
				LOGGER.debug("failed to updateDistributionStatusToNotDistributed service  {} error {}", service.getUniqueId(), e.toString());
			}
		}
	}
	
	private void updateDistributionStatusFromMetadata(Map<GraphVertex, org.openecomp.sdc.be.model.Service> components) {
		Service service;
		String statusFromMetadata;
		Either<GraphVertex, TitanOperationStatus> updateResponse;
		GraphVertex metadataV;
		
		for(Entry<GraphVertex, Service> currComponent : components.entrySet()){
			metadataV = currComponent.getKey();
			service = currComponent.getValue();
			try {
				statusFromMetadata = (String) metadataV.getJsonMetadataField(JsonPresentationFields.DISTRIBUTION_STATUS);
				metadataV.addMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS, statusFromMetadata);
				updateResponse = titanDao.updateVertex(metadataV);
				
				if (updateResponse.isRight()) {
					LOGGER.debug("failed to updateDistributionStatusFromMetadata service  {} error {}", service.getUniqueId(), updateResponse.right().value());
				}
				
			} catch (Exception e) {
				LOGGER.debug("failed to read distribution status of service {} error {}", service.getUniqueId(), e.toString());
			}
			
		}
	}
	
	
	public Either<Map<GraphVertex, org.openecomp.sdc.be.model.Service>, StorageOperationStatus> getAllServiceComponents() {

		Map<GraphVertex, org.openecomp.sdc.be.model.Service> components = new HashMap<>();
		Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
		Map<GraphPropertyEnum, Object> propertiesNotMatch = new EnumMap<>(GraphPropertyEnum.class);
		propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
		propertiesNotMatch.put(GraphPropertyEnum.IS_DELETED, true);
		Either<List<GraphVertex>, TitanOperationStatus> getVerticiesRes = toscaOperationFacade.getTitanDao().getByCriteria(null, propertiesToMatch, propertiesNotMatch, JsonParseFlagEnum.ParseAll);

		if (getVerticiesRes.isRight() && getVerticiesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
			LOGGER.debug("Failed to fetch all service components. Status is {}", getVerticiesRes.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticiesRes.right().value()));
		}
		if(getVerticiesRes.isLeft()){
			List<GraphVertex> componentVerticies = getVerticiesRes.left().value();
			for (GraphVertex componentV : componentVerticies) {
				ComponentParametersView filters = new ComponentParametersView(true);
				Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentsRes = toscaOperationFacade.getToscaElement(componentV.getUniqueId(), filters);
				if (getComponentsRes.isRight()) {
					return Either.right(getComponentsRes.right().value());
				}
				components.put(componentV, (Service) getComponentsRes.left().value());
			}
		}
		return Either.left(components);
	}
	
}
