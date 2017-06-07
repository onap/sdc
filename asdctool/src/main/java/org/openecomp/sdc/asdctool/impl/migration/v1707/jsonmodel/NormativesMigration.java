package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NormativesMigration extends ComponentMigration<Resource> {

    private static Logger LOGGER = LoggerFactory.getLogger(NormativesMigration.class);

    @javax.annotation.Resource(name = "normatives-resolver")
    private NormativesResolver normativesResolver;

    @javax.annotation.Resource(name = "resource-version-migration")
    private VersionMigration<Resource> versionMigration;

    @Override
    public String description() {
        return "migration of node types";
    }

    @Override
    Either<List<Resource>, ?> getElementsToMigrate() {
        return normativesResolver.getAllNodeTypeNormatives();
    }

    @Override
    boolean doPostMigrateOperation(List<Resource> elements) {
        LOGGER.info("migrating node types versions");
        return versionMigration.buildComponentsVersionChain(elements);
    }
}
