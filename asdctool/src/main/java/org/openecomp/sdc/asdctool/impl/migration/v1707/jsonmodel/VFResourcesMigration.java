package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VFResourcesMigration extends ComponentMigration<Resource> {

    private static Logger LOGGER = LoggerFactory.getLogger(VFResourcesMigration.class);

    @javax.annotation.Resource(name = "resource-operation")
    private IResourceOperation resourceOperation;

    @javax.annotation.Resource(name = "resource-version-migration")
    private VersionMigration<Resource> versionMigration;

    @Override
    public String description() {
        return "migrate VFs";
    }

    @Override
    Either<List<Resource>, ?> getElementsToMigrate() {
        return resourceOperation.getVFResources();
    }

    @Override
    Either<Resource, StorageOperationStatus> save(Resource element) {
        requirementsCapabilitiesMigrationService.overrideInstanceCapabilitiesRequirements(element);
        return super.save(element);
    }

    @Override
    boolean doPostSaveOperation(Resource element) {
        return element.getComponentInstances() == null ||
                (requirementsCapabilitiesMigrationService.associateFulfilledRequirements(element, NodeTypeEnum.Resource) &&
                 requirementsCapabilitiesMigrationService.associateFulfilledCapabilities(element, NodeTypeEnum.Resource));
    }

    @Override
    boolean doPostMigrateOperation(List<Resource> elements) {
        LOGGER.info("migrating VFs versions");
        return versionMigration.buildComponentsVersionChain(elements);
    }


}
