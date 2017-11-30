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

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.FunctionalMenuInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.UserFunctionalMenuData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.MethodActivationStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("user-operation")
public class UserAdminOperation implements IUserAdminOperation {

	private TitanGenericDao titanGenericDao;

	public UserAdminOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao) {
		super();
		this.titanGenericDao = titanGenericDao;

	}

	private static Logger log = LoggerFactory.getLogger(UserAdminOperation.class.getName());

	@Override
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
				Either<User, ActionStatus> result = Either.left(convertToUser(userWrapper.getInnerElement()));
				resultWrapper.setInnerElement(result);
			}

			return resultWrapper.getInnerElement();
		} finally {
			if (!inTransaction) {
				titanGenericDao.commit();
			}
			log.debug("getUserData - end");
		}
	}

	private void validateInActiveUser(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
		User user = convertToUser(userData);
		if (user.getStatus() == UserStatusEnum.ACTIVE) {
			Either<User, ActionStatus> result = Either.right(ActionStatus.USER_NOT_FOUND);
			resultWrapper.setInnerElement(result);
		}
	}

	private void validateActiveUser(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
		User user = convertToUser(userData);
		if (user.getStatus() == UserStatusEnum.INACTIVE) {
			Either<User, ActionStatus> result = Either.right(ActionStatus.USER_INACTIVE);
			resultWrapper.setInnerElement(result);
		}
	}

	private void validateUserData(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData, String id) {
		if (userData == null) {
			log.debug("Problem get User with userId {}. Reason -  either.left().value() = null", id);
			Either<User, ActionStatus> result = Either.right(ActionStatus.GENERAL_ERROR);
			resultWrapper.setInnerElement(result);
		}
	}

	private void validateUserExists(Wrapper<Either<User, ActionStatus>> resultWrapper, Wrapper<UserData> userWrapper, String id) {
		Either<User, ActionStatus> result;
		if (id == null) {
			log.info("User userId  is empty");
			result = Either.right(ActionStatus.MISSING_INFORMATION);
			resultWrapper.setInnerElement(result);
			return;
		}
		id = id.toLowerCase();
		Either<UserData, TitanOperationStatus> either = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), id, UserData.class);

		if (either.isRight()) {
			resultWrapper.setInnerElement(getUserNotFoundError(id, either.right().value()));
		} else {
			userWrapper.setInnerElement(either.left().value());
		}
	}

	@Override
	public Either<User, StorageOperationStatus> saveUserData(User user) {

		Either<UserData, TitanOperationStatus> result = null;
		try {
			UserData userData = convertToUserData(user);
			result = titanGenericDao.createNode(userData, UserData.class);
			if (result.isRight()) {
				log.debug("Problem while saving User  {}. Reason - {}",userData.toString(),result.right().value().name());
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			log.debug("User {} saved successfully",userData.toString());
			return Either.left(convertToUser(result.left().value()));

		} finally {

			if (result == null || result.isRight()) {
				log.error("saveUserData - Failed");
				titanGenericDao.rollback();
			} else {
				log.debug("saveUserData - end");
				titanGenericDao.commit();
			}
		}
	}

	@Override
	public Either<User, StorageOperationStatus> updateUserData(User user) {
		Either<UserData, TitanOperationStatus> result = null;
		try {
			log.debug("updateUserData - start");
			UserData userData = convertToUserData(user);
			result = titanGenericDao.updateNode(userData, UserData.class);
			if (result.isRight()) {
				log.debug("Problem while updating User {}. Reason - {}",userData.toString(),result.right().value().name());
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			log.debug("User {} updated successfully",userData.toString());
			return Either.left(convertToUser(result.left().value()));

		} finally {

			if (result == null || result.isRight()) {
				log.error("updateUserData - Failed");
				titanGenericDao.rollback();
			} else {
				log.debug("updateUserData - end");
				titanGenericDao.commit();
			}

		}
	}

	@Override
	public Either<User, StorageOperationStatus> deActivateUser(User user) {
		Either<User, StorageOperationStatus> result;
		user.setStatus(UserStatusEnum.INACTIVE);
		Either<User, StorageOperationStatus> status = updateUserData(user);
		if (status.isRight()) {
			result = Either.right(status.right().value());
		} else {
			result = Either.left(user);
		}
		return result;
	}

	@Override
	public Either<User, ActionStatus> deleteUserData(String id) {
		Either<User, ActionStatus> result;
		Either<UserData, TitanOperationStatus> eitherGet = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), id, UserData.class);
		if (eitherGet.isRight()) {
			log.debug("Problem while retriving user with userId {}",id);
			if (eitherGet.right().value() == TitanOperationStatus.NOT_FOUND) {
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
			titanGenericDao.commit();
		}

		return resultWrapper.getInnerElement();
	}

	private void deleteUser(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
		Either<UserData, TitanOperationStatus> eitherDelete = titanGenericDao.deleteNode(userData, UserData.class);
		if (eitherDelete.isRight()) {
			log.debug("Problem while deleting User {}. Reason - {}",userData.toString(),eitherDelete.right().value().name());
			Either<User, ActionStatus> result = Either.right(ActionStatus.GENERAL_ERROR);
			resultWrapper.setInnerElement(result);
		} else {
			log.debug("User {} deleted successfully",userData.toString());
			Either<User, ActionStatus> result = Either.left(convertToUser(eitherDelete.left().value()));
			resultWrapper.setInnerElement(result);
		}
	}

	private void validateUserHasNoConnections(Wrapper<Either<User, ActionStatus>> resultWrapper, UserData userData) {
		if (resultWrapper.isEmpty()) {

			Either<List<Edge>, TitanOperationStatus> edgesForNode = titanGenericDao.getEdgesForNode(userData, Direction.BOTH);
			if (edgesForNode.isRight()) {
				log.debug("Problem while deleting User {}. Reason - {}",userData.toString(),edgesForNode.right().value().name());
				Either<User, ActionStatus> result = Either.right(ActionStatus.GENERAL_ERROR);
				resultWrapper.setInnerElement(result);
			} else {
				List<Edge> vertexEdges = edgesForNode.left().value();
				if (vertexEdges.size() > 0) {
					Either<User, ActionStatus> result = Either.right(ActionStatus.USER_HAS_ACTIVE_ELEMENTS);
					resultWrapper.setInnerElement(result);
				}
			}
		}
	}

	public Either<List<Edge>, StorageOperationStatus> getUserPendingTasksList(User user, Map<String, Object> properties) {

		UserData userData = convertToUserData(user);

		Either<TitanVertex, TitanOperationStatus> vertexUser = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), user.getUserId());
		if (vertexUser.isRight()) {
			log.debug("Problem while deleting User {}. Reason - {}",userData.toString(),vertexUser.right().value().name());
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

		List<Edge> pandingTasks = new ArrayList<>();
		Either<List<Edge>, TitanOperationStatus> edges = titanGenericDao.getOutgoingEdgesByCriteria(vertexUser.left().value(), GraphEdgeLabels.STATE, properties);

		if (edges.isRight() || edges.left().value() == null) {
			if (edges.right().value() == TitanOperationStatus.NOT_FOUND) {
				return Either.left(pandingTasks);
			} else {
				log.debug("Problem while deleting User {}. Reason - ",userData.toString(),edges.right().value().name());
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
		}

		for (Edge edge : edges.left().value()) {
			Vertex componentVertex = edge.inVertex();
			VertexProperty<Object> property = componentVertex.property(GraphPropertiesDictionary.IS_DELETED.getProperty());
			if (!property.isPresent()) {
				pandingTasks.add(edge);
			} else {
				Boolean isDeletedValue = (java.lang.Boolean) property.value();
				if (isDeletedValue == null || isDeletedValue == false) {
					pandingTasks.add(edge);
				}
			}
		}
		
		if(log.isDebugEnabled()) {
			for (Edge edge : pandingTasks) {
				Object resourceUuid = edge.inVertex().property(GraphPropertyEnum.UNIQUE_ID.getProperty()).value();
				Object componentName = edge.inVertex().property(GraphPropertyEnum.NAME.getProperty()).value();
				Object componentState = edge.inVertex().property(GraphPropertyEnum.STATE.getProperty()).value();
				log.debug("The user userId = {} is working on the component name = {} uid = {} in state {}", user.getUserId(), componentName, resourceUuid, componentState);					
			}
		}
		
		return Either.left(pandingTasks);
	}

	@Override
	public Either<List<User>, ActionStatus> getAllUsersWithRole(String role, String status) {
		try {
			List<User> result = new ArrayList<>();
			Map<String, Object> propertiesToMatch = new HashMap<>();
			if (role != null && !role.trim().isEmpty()) {
				propertiesToMatch.put(GraphPropertiesDictionary.ROLE.getProperty(), role);
			}
			if (status != null && !status.isEmpty()) {
				propertiesToMatch.put(GraphPropertiesDictionary.USER_STATUS.getProperty(), status);
			}

			Either<List<UserData>, TitanOperationStatus> userNodes = titanGenericDao.getByCriteria(NodeTypeEnum.User, propertiesToMatch, UserData.class);

			titanGenericDao.commit();
			return convertToUsers(role, userNodes);
		} finally {
			titanGenericDao.commit();
		}
	}

	private Either<List<User>, ActionStatus> convertToUsers(String role, Either<List<UserData>, TitanOperationStatus> userNodes) {

		if (userNodes.isRight()) {
            // in case of NOT_FOUND from Titan return empty list
            if (userNodes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
                return Either.left(Collections.emptyList());
            } else {
                log.error("Problem while getting all users with role {}. Reason - {}", role, userNodes.right().value().name());
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

	private Either<User, ActionStatus> getUserNotFoundError(String uid, TitanOperationStatus status) {
		if (status == TitanOperationStatus.NOT_FOUND) {
            log.debug("User with userId {} not found", uid);
            return Either.right(ActionStatus.USER_NOT_FOUND);
        } else {
            log.debug("Problem get User with userId {}. Reason - {}", uid, status.name());
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
