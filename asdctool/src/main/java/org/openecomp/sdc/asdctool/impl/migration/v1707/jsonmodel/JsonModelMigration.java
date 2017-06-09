package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.MigrationMsg;
import org.openecomp.sdc.asdctool.impl.migration.Migration;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;

import javax.annotation.Resource;
import java.util.List;

public abstract class JsonModelMigration<T> implements Migration {

    private final boolean COMPLETED_OK = true;

    @Resource(name = "titan-dao")
    TitanDao titanDao;

    @Override
    public boolean migrate() {
        Either<List<T>, ?> elementsToMigrate = getElementsToMigrate();
        return elementsToMigrate.either(this::migrateElementsToNewGraph,
                                        errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_NODES.getMessage(errorStatus.toString())));
    }

    boolean doPostSaveOperation(T element) {
        return true;
    }

    boolean doPostMigrateOperation(List<T> elements) {
        return true;
    }

    private boolean migrateElementsToNewGraph(List<T> elementsToMigrate) {
        for (T node : elementsToMigrate) {
            boolean migratedSuccessfully = migrateElement(node);
            if (!migratedSuccessfully) {
                titanDao.rollback();
                return false;
            }
            titanDao.commit();
        }
        return postMigrate(elementsToMigrate);
    }

    private boolean migrateElement(T node) {
        boolean savedSuccessfully = saveElementIfNotExists(node);
        return savedSuccessfully && doPostSaveOperation(node);
    }

    private boolean postMigrate(List<T> elements) {
        boolean postMigrateSuccessfully = doPostMigrateOperation(elements);
        if (!postMigrateSuccessfully) {
            titanDao.rollback();
            return false;
        }
        titanDao.commit();
        return true;
    }

    private boolean saveElementIfNotExists(T element) {
        return isExists(element).either(isExist -> isExist || createElement(element),
                                        status -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_GET_NODE_FROM_GRAPH.getMessage(status.toString())));
    }

    private boolean createElement(T element) {
        return save(element).either(savedNode -> COMPLETED_OK,
                                 errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_CREATE_NODE.getMessage(element.getClass().getName(), errorStatus.toString())));
    }

    private Either<Boolean, ?> isExists(T element) {
        Either<T, ?> byId = getElementFromNewGraph(element);
        return byId.either(existingVal -> Either.left(true),
                           this::getEitherNotExistOrErrorStatus);
    }

    private <S> Either<Boolean, S> getEitherNotExistOrErrorStatus(S status) {
        return status == getNotFoundErrorStatus() ? Either.left(false) : Either.right(status);
    }

    abstract Either<List<T>, ?> getElementsToMigrate();

    abstract Either<T, ?> getElementFromNewGraph(T element);

    abstract Either<T, ?> save(T element);

    abstract <S extends Enum> S getNotFoundErrorStatus();


}
