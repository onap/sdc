package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

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
    boolean save(Resource element) {
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

    @Override
    void doPreMigrationOperation(List<Resource> elements) {
        super.doPreMigrationOperation(elements);
        setMissingTemplateInfo(elements);
    }


}
