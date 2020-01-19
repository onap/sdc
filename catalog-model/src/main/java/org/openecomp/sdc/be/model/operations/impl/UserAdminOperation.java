/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.operations.impl;

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@org.springframework.stereotype.Component
public class UserAdminOperation {

    private final JanusGraphGenericDao janusGraphGenericDao;
    private final ToscaOperationFacade toscaOperationFacade;

    public UserAdminOperation(JanusGraphGenericDao janusGraphGenericDao, ToscaOperationFacade toscaOperationFacade) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.toscaOperationFacade = toscaOperationFacade;
    }

    private static final Logger log = Logger.getLogger(UserAdminOperation.class.getName());

    public Either<User, ActionStatus> getUserData(String id, boolean inTransaction) {
        return getUserData(id, true, inTransaction);
    }

    private Either<User, ActionStatus> getUserData(String id, boolean isActive, boolean inTransaction) {
        log.debug("getUserData - start");
        Wrapper<Either<User, ActionStatus>> resultWrapper = new Wrapper<>();
        Wrapper<UserData> userWrapper = new Wrapper<>();
        try {
            validateUserExists(resultWrapper, userWrapper, id);

            if (resultWrapper.isEmpty()) {
                validateUserData(resultWrapper, userWrapper.getInnerElement(), id);

            }
            if (resultWrapper.isEmpty()) {
                if (isActive) {
                    validateActiveUser(resultWrapper, userWrapper.getInnerElement());
                } else {
                    validateInActiveUser(resultWrapper, userWrapper.getInnerElement());
                }
            }

            if (resultWrapper.isEmpty()) {
                resultWrapper.setInnerElement(Either.left(convertToUser(userWrapper.getInnerElement())));
            }

            return resultWrapper.getInnerElement();
        } finally {
            if (!inTransaction) {
                janusGraphGenericDao.commit();
            }
            log.debug("getUserData - end");
        }
    }

    private void validateInActiveUser(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
        User user = convertToUser(userData);
        if (user.getStatus() == UserStatusEnum.ACTIVE) {
            resultWrapper.setInnerElement(Either.right(ActionStatus.USER_NOT_FOUND));
        }
    }

    private void validateActiveUser(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
        User user = convertToUser(userData);
        if (user.getStatus() == UserStatusEnum.INACTIVE) {
            resultWrapper.setInnerElement(Either.right(ActionStatus.USER_INACTIVE));
        }
    }

    private void validateUserData(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData, String id) {
        if (userData == null) {
            log.debug("Problem get User with userId {}. Reason -  either.left().value() = null", id);
            resultWrapper.setInnerElement(Either.right(ActionStatus.GENERAL_ERROR));
        }
    }

    private void validateUserExists(Wrapper<Either<User, ActionStatus>> resultWrapper, Wrapper<UserData> userWrapper, String id) {
        if (id == null) {
            log.info("User userId  is empty");
            resultWrapper.setInnerElement(Either.right(ActionStatus.MISSING_USER_ID));
            return;
        }
        id = id.toLowerCase();
        Either<UserData, JanusGraphOperationStatus> either = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), id, UserData.class);

        if (either.isRight()) {
            resultWrapper.setInnerElement(getUserNotFoundError(id, either.right().value()));
        } else {
            userWrapper.setInnerElement(either.left().value());
        }
    }

    public User saveUserData(User user) {
        Either<UserData, JanusGraphOperationStatus> result = null;
        try {
            UserData userData = convertToUserData(user);
            result = janusGraphGenericDao.createNode(userData, UserData.class);
            if (result.isRight()) {
                log.error("Problem while saving User  {}. Reason - {}", userData.getUserId(), result.right().value());
                throw new StorageException(StorageOperationStatus.GENERAL_ERROR);
            }
            log.debug("User {} saved successfully", userData.getUserId());
            return convertToUser(result.left().value());

        } finally {
            if (result == null || result.isRight()) {
                log.error("saveUserData - Failed");
                janusGraphGenericDao.rollback();
            } else {
                log.debug("saveUserData - end");
                janusGraphGenericDao.commit();
            }
        }
    }

    public User updateUserData(User user) {
        Either<UserData, JanusGraphOperationStatus> result = null;
        try {
            log.debug("updateUserData - start");
            UserData userData = convertToUserData(user);
            result = janusGraphGenericDao.updateNode(userData, UserData.class);
            if (result.isRight()) {
                log.error("Problem while updating User {}. Reason - {}", userData.toString(), result.right().value());
                throw new StorageException(StorageOperationStatus.GENERAL_ERROR);
            }
            log.debug("User {} updated successfully",userData.getUserId());
            return convertToUser(result.left().value());

        } finally {
            if (result == null || result.isRight()) {
                log.error("updateUserData - Failed");
                janusGraphGenericDao.rollback();
            } else {
                log.debug("updateUserData - end");
                janusGraphGenericDao.commit();
            }

        }
    }

    public User deActivateUser(User user) {
        user.setStatus(UserStatusEnum.INACTIVE);
        return updateUserData(user);
    }

    public Either<User, ActionStatus> deleteUserData(String id) {
        Either<User, ActionStatus> result;
        Either<UserData, JanusGraphOperationStatus> eitherGet = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), id, UserData.class);
        if (eitherGet.isRight()) {
            log.debug("Problem while retriving user with userId {}",id);
            if (eitherGet.right().value() == JanusGraphOperationStatus.NOT_FOUND) {
                result = Either.right(ActionStatus.USER_NOT_FOUND);
            } else {
                result = Either.right(ActionStatus.GENERAL_ERROR);
            }
        } else {
            result = deleteUserLogic(eitherGet.left().value());
        }
        return result;
    }

    private Either<User, ActionStatus> deleteUserLogic(UserData userData) {
        Wrapper<Either<User, ActionStatus>> resultWrapper = new Wrapper<>();
        try {
            validateUserHasNoConnections(resultWrapper, userData);
            if (resultWrapper.isEmpty()) {
                deleteUser(resultWrapper, userData);
            }
        } finally {
            janusGraphGenericDao.commit();
        }
        return resultWrapper.getInnerElement();
    }

    private void deleteUser(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
        Either<UserData, JanusGraphOperationStatus> eitherDelete = janusGraphGenericDao
            .deleteNode(userData, UserData.class);
        if (eitherDelete.isRight()) {
            if (log.isDebugEnabled()) {
                log.debug("Problem while deleting User {}. Reason - {}", userData.toString(), eitherDelete.right().value());
            }
            resultWrapper.setInnerElement(Either.right(ActionStatus.GENERAL_ERROR));
        } else {
            log.debug("User {} deleted successfully",userData.getUserId());
            resultWrapper.setInnerElement(Either.left(convertToUser(eitherDelete.left().value())));
        }
    }

    private void validateUserHasNoConnections(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
        if (resultWrapper.isEmpty()) {

            Either<List<Edge>, JanusGraphOperationStatus> edgesForNode = janusGraphGenericDao
                .getEdgesForNode(userData, Direction.BOTH);
            if (edgesForNode.isRight()) {
                if (log.isDebugEnabled()) {
                    log.debug("Problem while deleting User {}. Reason - {}", userData.getUserId(), edgesForNode.right().value());
                }
                resultWrapper.setInnerElement(Either.right(ActionStatus.GENERAL_ERROR));
            } else {
                List<Edge> vertexEdges = edgesForNode.left().value();
                if (!isEmpty(vertexEdges)) {
                    resultWrapper.setInnerElement(Either.right(ActionStatus.USER_HAS_ACTIVE_ELEMENTS));
                }
            }
        }
    }

    public @NotNull List<Edge> getUserPendingTasksList(User user, List<Object> states) {

        JanusGraphVertex userVertex = janusGraphGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), user.getUserId())
            .left()
            .on(this::handleJanusGraphError);

        List<Edge> pendingTasks = new ArrayList<>();
        for (Object state : states) {
            Map<String, Object> property = new HashMap<>();
            property.put(GraphPropertiesDictionary.STATE.getProperty(), state);
            List<Edge> edges = janusGraphGenericDao.getOutgoingEdgesByCriteria(userVertex, GraphEdgeLabels.STATE, property)
                    .left()
                    .on(this::handleJanusGraphError);
            for (Edge edge : edges) {
                Vertex vertex = edge.inVertex();
                if (!isComponentDeleted(vertex)) {
                    pendingTasks.add(edge);
                }
            }
        }
        logPendingTasks(user, pendingTasks);
        return pendingTasks;
    }

    public @NotNull List<Component> getUserActiveComponents(User user, List<Object> states) {
        List<Component> components = new ArrayList<>();
        List<Edge> edges = getUserPendingTasksList(user, states);
        for (Edge edge : edges) {
            JanusGraphVertex componentVertex = (JanusGraphVertex) edge.inVertex();
            String componentId = (String) janusGraphGenericDao.getProperty(componentVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
            Either<Component, StorageOperationStatus> toscaFullElement = toscaOperationFacade.getToscaFullElement(componentId);
            if (toscaFullElement.isLeft()) {
                components.add(toscaFullElement.left().value());
            } else {
                log.warn(EcompLoggerErrorCode.SCHEMA_ERROR, "", "", "Failed to retrieve component {} from graph db", componentId);
            }
        }
        return components;
    }

    private <T> T handleJanusGraphError(JanusGraphOperationStatus janusGraphOperationStatus) {
        throw new StorageException(janusGraphOperationStatus);
    }

    private boolean isComponentDeleted(Vertex componentVertex) {
        VertexProperty<Object> property = componentVertex.property(GraphPropertiesDictionary.IS_DELETED.getProperty());
        if (property.isPresent()) {
            return BooleanUtils.isTrue((Boolean) property.value());
        }
        return false;
    }

    private void logPendingTasks(User user, List<Edge> pendingTasks) {
        if (log.isDebugEnabled()) {
            for (Edge edge : pendingTasks) {
                Object resourceUuid = edge.inVertex().property(GraphPropertyEnum.UNIQUE_ID.getProperty()).value();
                Object componentName = edge.inVertex().property(GraphPropertyEnum.NAME.getProperty()).value();
                Object componentState = edge.inVertex().property(GraphPropertyEnum.STATE.getProperty()).value();
                log.debug("The user userId = {} is working on the component name = {} uid = {} in state {}", user.getUserId(), componentName, resourceUuid, componentState);
            }
        }
    }

    public Either<List<User>, ActionStatus> getAllUsersWithRole(String role, String status) {
        try {
            Map<String, Object> propertiesToMatch = new HashMap<>();
            if (role != null && !role.trim().isEmpty()) {
                propertiesToMatch.put(GraphPropertiesDictionary.ROLE.getProperty(), role);
            }
            if (status != null && !status.isEmpty()) {
                propertiesToMatch.put(GraphPropertiesDictionary.USER_STATUS.getProperty(), status);
            }

            Either<List<UserData>, JanusGraphOperationStatus> userNodes = janusGraphGenericDao
                .getByCriteria(NodeTypeEnum.User, propertiesToMatch, UserData.class);

            janusGraphGenericDao.commit();
            return convertToUsers(role, userNodes);
        } finally {
            janusGraphGenericDao.commit();
        }
    }

    private Either<List<User>, ActionStatus> convertToUsers(String role, Either<List<UserData>, JanusGraphOperationStatus> userNodes) {

        if (userNodes.isRight()) {
            // in case of NOT_FOUND from JanusGraph return empty list
            JanusGraphOperationStatus tos = userNodes.right().value();
            if (tos.equals(JanusGraphOperationStatus.NOT_FOUND)) {
                return Either.left(Collections.emptyList());
            } else {
                log.error("Problem while getting all users with role {}. Reason - {}", role, tos);
                return Either.right(ActionStatus.GENERAL_ERROR);
            }
        } else {
            List<UserData> userDataList = userNodes.left().value();
            if (userDataList != null) {
                return Either.left(convertToUsers(userDataList));
            }
            log.debug("No users were found with role {}", role);
            return Either.left(Collections.emptyList());
        }
    }

    private List<User> convertToUsers(List<UserData> usersData) {
        List<User> result = new ArrayList<>();
        for (UserData userData : usersData) {
            User user = convertToUser(userData);
            result.add(user);
        }
        return result;
    }

    private Either<User, ActionStatus> getUserNotFoundError(String uid, JanusGraphOperationStatus status) {
        if (status == JanusGraphOperationStatus.NOT_FOUND) {
            log.debug("User with userId {} not found", uid);
            return Either.right(ActionStatus.USER_NOT_FOUND);
        } else {
            log.debug("Problem get User with userId {}. Reason - {}", uid, status);
            return  Either.right(ActionStatus.GENERAL_ERROR);
        }
    }

    protected User convertToUser(UserData userData) {
        User user = new User();
        user.setUserId(userData.getUserId());
        user.setEmail(userData.getEmail());
        user.setFirstName(userData.getFirstName());
        user.setLastName(userData.getLastName());
        user.setRole(userData.getRole());
        user.setLastLoginTime(userData.getLastLoginTime());
        // Support backward compatibility - user status may not exist in old
        // users
        Either<UserStatusEnum, MethodActivationStatusEnum> either = UserStatusEnum.findByName(userData.getStatus());
        user.setStatus(either.isLeft() ? either.left().value() : UserStatusEnum.ACTIVE);
        return user;
    }

    protected UserData convertToUserData(User user) {
        UserData userData = new UserData();
        userData.setUserId(user.getUserId().toLowerCase());
        userData.setEmail(user.getEmail());
        userData.setFirstName(user.getFirstName());
        userData.setLastName(user.getLastName());
        userData.setRole(user.getRole());
        userData.setStatus(user.getStatus().name());
        userData.setLastLoginTime(user.getLastLoginTime());
        return userData;
    }

}
