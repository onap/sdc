package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations.RequirementsCapabilitiesMigrationService;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.migration.MigrationMalformedDataLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.handleError;

public abstract class ComponentMigration <T extends Component> extends JsonModelMigration<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(ComponentMigration.class);

    @Resource(name = "tosca-operation-facade")
    private ToscaOperationFacade toscaOperations;

    @Resource(name = "req-cap-mig-service")
    RequirementsCapabilitiesMigrationService<T> requirementsCapabilitiesMigrationService;

    @Resource(name = "invariant-uuid-resolver")
    private InvariantUUIDResolver <T> invariantUUIDResolver;

    @Override
    boolean save(T element) {
        LOGGER.debug(String.format("creating component %s in new graph", element.getName()));
        return toscaOperations.createToscaComponent(element)
                .either(savedNode -> true,
                        err -> handleError(String.format("failed to create component %s with id %s. reason: %s", element.getName(), element.getUniqueId(), err.name())));

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

    @Override
    void doPreMigrationOperation(List<T> elements) {
        setMissingInvariantUids(elements);
    }

    //some invariants uids are missing in production
    private void setMissingInvariantUids(List<T> components) {
        List<T> missingInvariantCmpts = getComponentsWithNoInvariantUUIDs(components);
        for (T missingInvariantCmpt : missingInvariantCmpts) {
            missingInvariantCmpt.setInvariantUUID(invariantUUIDResolver.resolveInvariantUUID(components, missingInvariantCmpt));
        }
    }

    private List<T> getComponentsWithNoInvariantUUIDs(List<T> components) {
        List<T> cmptsWithoutInvariant = components.stream().filter(c -> c.getInvariantUUID() == null).collect(Collectors.toList());
        if (!cmptsWithoutInvariant.isEmpty()) {
            cmptsWithoutInvariant.forEach(cmpt -> MigrationMalformedDataLogger.logMalformedDataMsg(String.format("component %s is missing invariant uuid", cmpt.getUniqueId())));
        }
        return cmptsWithoutInvariant;
    }


}
