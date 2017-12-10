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

package org.openecomp.sdc.be.model.operations.api;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;

import fj.data.Either;

public interface IUserAdminOperation {

	public Either<User, ActionStatus> getUserData(String id, boolean inTransaction);

	public Either<User, StorageOperationStatus> saveUserData(User user);

	public Either<User, StorageOperationStatus> updateUserData(User user);

	public Either<User, StorageOperationStatus> deActivateUser(User user);

	public Either<User, ActionStatus> deleteUserData(String id);

	public Either<List<User>, ActionStatus> getAllUsersWithRole(String role, String status);

	public Either<List<Edge>, StorageOperationStatus> getUserPendingTasksList(User user, Map<String, Object> properties);

}
