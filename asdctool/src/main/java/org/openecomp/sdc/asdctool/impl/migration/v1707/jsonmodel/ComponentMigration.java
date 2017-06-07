package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.Function;
import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.FulfilledCapabilitiesMigrationService;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.FulfilledRequirementsMigrationService;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.RequirementsCapabilitiesMigrationService;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.handleError;

public abstract class ComponentMigration <T extends Component> extends JsonModelMigration<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(ComponentMigration.class);

    @Resource(name = "tosca-operation-facade")
    private ToscaOperationFacade toscaOperations;

    @Resource(name = "req-cap-mig-service")
    RequirementsCapabilitiesMigrationService<T> requirementsCapabilitiesMigrationService;

    @Override
    Either<T, StorageOperationStatus> save(T element) {
        LOGGER.debug(String.format("creating component %s in new graph", element.getName()));
        return toscaOperations.createToscaComponent(element).right().map(err -> handleError(err, String.format("failed to create component %s.", element.getName())));

    }

    @Override
    Either<T, StorageOperationStatus> getElementFromNewGraph(T element) {
        LOGGER.debug(String.format("checking if component %s already exists on new graph", element.getName()));
        return toscaOperations.getToscaElement(element.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
    }

    @Override
    public StorageOperationStatus getNotFoundErrorStatus() {
        return StorageOperationStatus.NOT_FOUND;
    }

}
