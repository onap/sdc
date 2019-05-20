package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import fj.data.Either;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class SdcArchiveMigration implements Migration {
    private static final Logger logger = Logger.getLogger(SdcArchiveMigration.class);

    private JanusGraphDao janusGraphDao;

    public SdcArchiveMigration(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
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
        JanusGraphOperationStatus status = null;
        try {
            status = getOrCreateArchiveRoot();
            return status == JanusGraphOperationStatus.OK ? MigrationResult.success() : MigrationResult.error("failed to create archive root node. error: " + status);
        } finally {
            commitOrRollBack(status);
        }
    }

    private void commitOrRollBack(JanusGraphOperationStatus status) {
        if (status == JanusGraphOperationStatus.OK) {
            janusGraphDao.commit();
        } else {
            janusGraphDao.rollback();
        }
    }

    private JanusGraphOperationStatus getOrCreateArchiveRoot() {
        logger.info("creating or getting catalog archive vertex");
        return janusGraphDao.getVertexByLabel(VertexTypeEnum.ARCHIVE_ROOT)
                .either(v -> JanusGraphOperationStatus.OK, s -> this.createRootArchiveVertex());
    }

    private JanusGraphOperationStatus createRootArchiveVertex() {
        GraphVertex archiveRootVertex = new GraphVertex(VertexTypeEnum.ARCHIVE_ROOT);
        archiveRootVertex.setUniqueId(IdBuilderUtils.generateUniqueId());
        archiveRootVertex.addMetadataProperty(GraphPropertyEnum.LABEL, VertexTypeEnum.ARCHIVE_ROOT);
        archiveRootVertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, archiveRootVertex.getUniqueId());

        logger.info("Creating root archive vertex {}", archiveRootVertex.getUniqueId());

        final Either<GraphVertex, JanusGraphOperationStatus> vertexE = janusGraphDao.createVertex(archiveRootVertex);

        return vertexE.isLeft() ? JanusGraphOperationStatus.OK : vertexE.right().value();
    }

}
