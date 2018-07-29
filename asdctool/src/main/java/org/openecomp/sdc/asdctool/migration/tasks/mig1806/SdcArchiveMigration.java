package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import fj.data.Either;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class SdcArchiveMigration implements Migration {
    private static final Logger logger = Logger.getLogger(SdcArchiveMigration.class);

    private TitanDao titanDao;

    public SdcArchiveMigration(TitanDao titanDao) {
        this.titanDao = titanDao;
    }

    @Override
    public String description() {
        return "add archive node for archiving/restoring components ";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        TitanOperationStatus status = null;
        try {
            status = getOrCreateArchiveRoot();
            return status == TitanOperationStatus.OK ? MigrationResult.success() : MigrationResult.error("failed to create archive root node. error: " + status);
        } finally {
            commitOrRollBack(status);
        }
    }

    private void commitOrRollBack(TitanOperationStatus status) {
        if (status == TitanOperationStatus.OK) {
            titanDao.commit();
        } else {
            titanDao.rollback();
        }
    }

    private TitanOperationStatus getOrCreateArchiveRoot() {
        logger.info("creating or getting catalog archive vertex");
        return titanDao.getVertexByLabel(VertexTypeEnum.ARCHIVE_ROOT)
                .either(v -> TitanOperationStatus.OK, s -> this.createRootArchiveVertex());
    }

    private TitanOperationStatus createRootArchiveVertex() {
        GraphVertex archiveRootVertex = new GraphVertex(VertexTypeEnum.ARCHIVE_ROOT);
        archiveRootVertex.setUniqueId(IdBuilderUtils.generateUniqueId());
        archiveRootVertex.addMetadataProperty(GraphPropertyEnum.LABEL, VertexTypeEnum.ARCHIVE_ROOT);
        archiveRootVertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, archiveRootVertex.getUniqueId());

        logger.info("Creating root archive vertex {}", archiveRootVertex.getUniqueId());

        final Either<GraphVertex, TitanOperationStatus> vertexE = titanDao.createVertex(archiveRootVertex);

        return vertexE.isLeft() ? TitanOperationStatus.OK : vertexE.right().value();
    }

}
