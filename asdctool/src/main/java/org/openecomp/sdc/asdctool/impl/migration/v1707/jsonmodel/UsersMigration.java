package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.Function;
import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.handleError;

public class UsersMigration extends JsonModelMigration<User> {

    private static Logger LOGGER = LoggerFactory.getLogger(UsersMigration.class);

    @Resource(name = "user-operation")
    IUserAdminOperation userAdminOperation;

    @Resource(name = "user-operation-migration")
    IUserAdminOperation userAdminOperationMigration;


    @Override
    Either<List<User>, ActionStatus> getElementsToMigrate() {
        LOGGER.debug("fetching users to migrate from old graph");
        return userAdminOperation.getAllUsers();
    }

    @Override
    Either<User, ActionStatus> getElementFromNewGraph(User user) {
        LOGGER.debug(String.format("trying to load user %s from new graph", user.getUserId()));
        return userAdminOperationMigration.getUserData(user.getUserId(), false);
    }

    @Override
    Either<User, StorageOperationStatus> save(User user) {
        LOGGER.debug(String.format("trying to save user %s to new graph", user.getUserId()));
        return userAdminOperationMigration.saveUserData(user);
    }

    @Override
    public ActionStatus getNotFoundErrorStatus() {
        return ActionStatus.USER_NOT_FOUND;
    }

    @Override
    public String description() {
        return "migrate users";
    }

}
