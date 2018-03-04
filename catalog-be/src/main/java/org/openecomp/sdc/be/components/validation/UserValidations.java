package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@org.springframework.stereotype.Component
public class UserValidations {

    private static final Logger log = LoggerFactory.getLogger(UserValidations.class);
    private final IUserBusinessLogic userAdmin;
    private final ComponentsUtils componentsUtils;

    public UserValidations(IUserBusinessLogic userAdmin, ComponentsUtils componentsUtils) {
        this.userAdmin = userAdmin;
        this.componentsUtils = componentsUtils;
    }

    public Either<User, ResponseFormat> validateUserExists(String userId, String ecompErrorContext, boolean inTransaction) {
        Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userId, inTransaction);
        if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
            ResponseFormat responseFormat;
            if (eitherCreator.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
                if (log.isDebugEnabled()) {
                    log.debug("validateUserExists - not authorized user, userId {}", userId);
                }
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.AUTH_FAILED);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("validateUserExists - failed to authorize user, userId {}", userId);
                }
                responseFormat = componentsUtils.getResponseFormat(eitherCreator.right().value());
            }
            if (log.isDebugEnabled()) {
                log.debug("User is not listed. userId {}", userId);
            }
            BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, userId);
            return Either.right(responseFormat);
        }
        return Either.left(eitherCreator.left().value());
    }

    public Either<Boolean, ResponseFormat> validateUserRole(User user, List<Role> roles) {
        Role userRole = Role.valueOf(user.getRole());
        if (roles != null) {
            if (!roles.contains(userRole)) {
                if (log.isDebugEnabled()) {
                    log.debug("user is not in appropriate role to perform action");
                }
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
                return Either.right(responseFormat);
            }
            return Either.left(Boolean.TRUE);
        }
        return Either.left(Boolean.FALSE);
    }

    public Either<User, ActionStatus> validateUserExistsActionStatus(String userId, String ecompErrorContext) {
        Either<User, ActionStatus> eitherCreator = userAdmin.getUser(userId, false);
        if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
            if (eitherCreator.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
                log.debug("validateUserExists - not authorized user, userId {}", userId);
                Either.right(ActionStatus.RESTRICTED_OPERATION);
            } else {
                log.debug("validateUserExists - failed to authorize user, userId {}", userId);
            }
            log.debug("User is not listed. userId {}", userId);
            BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, userId);
            return Either.right(eitherCreator.right().value());
        }
        return Either.left(eitherCreator.left().value());
    }

    public Either<User, ResponseFormat> validateUserNotEmpty(User user, String ecompErrorContext) {
        String userId = user.getUserId();

        if (StringUtils.isEmpty(userId)) {
            log.debug("User header is missing ");
            BeEcompErrorManager.getInstance().logBeUserMissingError(ecompErrorContext, user.getUserId());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
            return Either.right(responseFormat);
        }
        return Either.left(user);
    }

    public Either<User, ResponseFormat> validateUserExists(User user, String ecompErrorContext, boolean inTransaction) {
        return validateUserExists(user.getUserId(), ecompErrorContext, inTransaction);
    }

    public void validateUserExist(String userId, String ecompErrorContext, Wrapper<ResponseFormat> errorWrapper) {
        Either<User, ResponseFormat> resp = validateUserExists(userId, ecompErrorContext, false);
        if (resp.isRight()) {
            errorWrapper.setInnerElement(resp.right().value());
        }
    }



}
