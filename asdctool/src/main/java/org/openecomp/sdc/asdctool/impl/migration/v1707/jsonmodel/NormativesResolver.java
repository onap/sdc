package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class NormativesResolver {

    @javax.annotation.Resource(name = "resource-operation")
    private IResourceOperation resourceOperation;

    /**
     *
     * @return list of all normatives sorted by neighboring order
     */
    public Either<List<Resource>, StorageOperationStatus> getAllNodeTypeNormatives() {
        Either<List<Resource>, StorageOperationStatus> rootNormatives = resourceOperation.getRootResources();
        return rootNormatives.either(this::getAllNormatives,
                                     Either::right);

    }

    private Either<List<Resource>, StorageOperationStatus> getAllNormatives(List<Resource> rootResources) {
        List<Resource> allNormatives = new ArrayList<>();
        for (Resource rootResource : rootResources) {
            Either<List<Resource>, StorageOperationStatus> normativesOfRoot = getAllNodeTypeNormatives(rootResource);
            if (normativesOfRoot.isRight()) {
                return Either.right(normativesOfRoot.right().value());
            }
            allNormatives.addAll(normativesOfRoot.left().value());
        }
        return Either.left(allNormatives);
    }

    private Either<List<Resource>, StorageOperationStatus> getAllNodeTypeNormatives(Resource root) {
        List<Resource> normativeResources = new ArrayList<>();
        Queue<Resource> resources = new ArrayDeque<>();
        resources.add(root);
        while (!resources.isEmpty()) {
            Resource currentResource = resources.poll();
            normativeResources.add(currentResource);
            Either<List<Resource>, StorageOperationStatus> allDerivedResources = getAllNonVFDerivedResources(currentResource);
            if (allDerivedResources.isRight()) {
                return Either.right(allDerivedResources.right().value());
            }
            List<Resource> derivedResources = allDerivedResources.left().value();
            replaceDerivedNameWithDerivedUniqueId(currentResource, derivedResources);
            resources.addAll(derivedResources);
        }
        return Either.left(normativeResources);
    }

    private void replaceDerivedNameWithDerivedUniqueId(Resource currentResource, List<Resource> derivedResources) {
        derivedResources.forEach(resource -> resource.setDerivedFrom(Collections.singletonList(currentResource.getUniqueId())));
    }

    private Either<List<Resource>, StorageOperationStatus> getAllNonVFDerivedResources(Resource resource) {
        Either<List<Resource>, StorageOperationStatus> childrenNodes = resourceOperation.getAllDerivedResources(resource);
        return childrenNodes.either(resourceList -> Either.left(filterNonVFResources(resourceList)),
                                    this::resolveEmptyListOrErrorStatus);
    }

    private List<Resource> filterNonVFResources(List<Resource> resources) {
        return resources.stream().filter(resource -> resource.getResourceType() != ResourceTypeEnum.VF).collect(Collectors.toList());
    }

    private Either<List<Resource>, StorageOperationStatus> resolveEmptyListOrErrorStatus(StorageOperationStatus storageOperationStatus) {
        return storageOperationStatus == StorageOperationStatus.NOT_FOUND ? Either.left(Collections.emptyList()) : Either.right(storageOperationStatus);
    }


}
