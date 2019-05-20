package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.javatuples.Pair;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ComponentInstanceForwardingPathMerge implements ComponentInstanceMergeInterface {

    private static Logger log = Logger.getLogger(ComponentInstanceForwardingPathMerge.class);

    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    @Autowired
    private ComponentsUtils componentsUtils;

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent,
        ComponentInstance currentResourceInstance, Component originComponent) {
        dataHolder.setOrigInstanceCapabilities(getAllInstanceCapabilities(currentResourceInstance));
        dataHolder.setOrigInstanceNode(originComponent);
        dataHolder.setOrigComponentInstId(currentResourceInstance.getName());
    }

    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder,
        Component updatedContainerComponent, String newInstanceId) {
        if (!(updatedContainerComponent instanceof Service)) {
            // no need to handle forwarding paths
            return Either.left(updatedContainerComponent);
        }
        Service service = (Service) updatedContainerComponent;
        ComponentInstance ci = service.getComponentInstanceById(newInstanceId).orElse(null);
        if (ci == null){
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, newInstanceId);
            return Either.right(responseFormat);
        }
        Either<Component, StorageOperationStatus> resourceEither = toscaOperationFacade.getToscaFullElement(ci.getComponentUid());
        if (resourceEither.isRight() ) {
            log.debug("Failed to fetch resource with id {} for instance {}",ci.getComponentUid() ,ci.getUniqueId());
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(resourceEither.right().value()));
            return Either.right(responseFormat);
        }

        Component fetchedComponent = resourceEither.left().value();

        Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> pair = new ForwardingPathUtils()
            .updateForwardingPathOnVersionChange(service, dataHolder, fetchedComponent, newInstanceId);
        Map<String, ForwardingPathDataDefinition> updated = pair.getValue0();
        Map<String, ForwardingPathDataDefinition> deleted = pair.getValue1();
        if (deleted != null && !deleted.isEmpty()) {
            Either<Set<String>, ResponseFormat> deleteEither = serviceBusinessLogic
                .deleteForwardingPaths(service.getUniqueId(), new HashSet<>(deleted.keySet()), user, false);
            if (deleteEither.isRight()) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to delete forwarding paths : {}", deleted.values().stream()
                        .map(ForwardingPathDataDefinition::getName).collect(Collectors.joining(", ", "( ", " )")));
                }
                return Either.right(deleteEither.right().value());
            }
            deleted.keySet().forEach(key -> service.getForwardingPaths().remove(key));
        }
        if (updated != null && !updated.isEmpty()) {
            Service updateFPService = new Service();
            updateFPService.setForwardingPaths(updated);
            Either<Service, ResponseFormat> updateFPEither = serviceBusinessLogic
                .updateForwardingPath(service.getUniqueId(), updateFPService, user, false);
            if (updateFPEither.isRight()) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to update forwarding paths : {}", updated.values().stream()
                        .map(ForwardingPathDataDefinition::getName).collect(Collectors.joining(", ", "( ", " )")));
                }
                return Either.right(updateFPEither.right().value());
            }
            updated.forEach((key, forwardingPathDataDefinition) -> service.getForwardingPaths().put(key,forwardingPathDataDefinition));
        }
        return Either.left(updatedContainerComponent);
    }


    private List<CapabilityDefinition> getAllInstanceCapabilities(ComponentInstance currentResourceInstance) {
        if(currentResourceInstance.getCapabilities() == null || currentResourceInstance.getCapabilities().isEmpty()){
            return Collections.EMPTY_LIST;
        }
        return currentResourceInstance.getCapabilities().values().stream().flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
