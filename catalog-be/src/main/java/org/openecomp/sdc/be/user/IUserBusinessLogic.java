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

package org.openecomp.sdc.be.user;

import java.util.List;

import javax.servlet.ServletContext;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

/**
 * 
 * @author tg851x
 *
 */
public interface IUserBusinessLogic {
	public Either<User, ActionStatus> getUser(String userId, boolean inTransaction);

	public Either<User, ResponseFormat> createUser(User modifier, User newUser);

	public Either<User, ResponseFormat> updateUserRole(User modifier, String userIdToUpdate, String userRole);

	public Either<List<User>, ResponseFormat> getAllAdminUsers(ServletContext context);

	public Either<List<User>, ResponseFormat> getUsersList(String userId, List<String> roles, String rolesStr);

	public Either<User, ResponseFormat> deActivateUser(User modifier, String userUniuqeIdToDeactive);

	public Either<User, ResponseFormat> authorize(User authUser);
}
