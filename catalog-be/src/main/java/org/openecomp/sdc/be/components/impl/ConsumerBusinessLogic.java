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

package org.openecomp.sdc.be.components.impl;

import java.util.Date;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ConsumerOperation;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("ConsumerBusinessLogic")
public class ConsumerBusinessLogic extends BaseBusinessLogic {

	private static final String CONSUMER_NAME = "Consumer name";
	private static final String CONSUMER_SALT = "Consumer salt";
	private static final String CONSUMER_PW = "Consumer password";

	@javax.annotation.Resource
	private IUserBusinessLogic userAdmin;

	@javax.annotation.Resource
	private ComponentsUtils componentsUtils;

	@javax.annotation.Resource
	private ConsumerOperation consumerOperation;

	@javax.annotation.Resource
	private IGraphLockOperation graphLockOperation;

	private static Logger log = LoggerFactory.getLogger(ConsumerBusinessLogic.class.getName());

	public Either<ConsumerDefinition, ResponseFormat> createConsumer(User user, ConsumerDefinition consumer) {

		Either<User, ResponseFormat> userValidation = validateUser(user, consumer, AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS);

		if (userValidation.isRight()) {
			return Either.right(userValidation.right().value());
		}
		checkFieldsForOverrideAttempt(consumer);
		user = userValidation.left().value();
		consumer.setLastModfierAtuid(user.getUserId());

		Either<ConsumerDefinition, ResponseFormat> consumerValidationResponse = validateConsumer(consumer, user, AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS);
		if (consumerValidationResponse.isRight()) {
			ResponseFormat responseFormat = consumerValidationResponse.right().value();
			componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS, consumer, responseFormat, user);
			return Either.right(responseFormat);
		}
		String consumerName = consumer.getConsumerName();
		StorageOperationStatus lockResult = graphLockOperation.lockComponent(consumerName, NodeTypeEnum.ConsumerCredentials);
		if (!lockResult.equals(StorageOperationStatus.OK)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedLockObjectError, "createConsumer");
			BeEcompErrorManager.getInstance().logBeFailedLockObjectError("createConsumer", NodeTypeEnum.ConsumerCredentials.getName(), consumerName);
			log.debug("Failed to lock consumer: {} error - {}", consumerName, lockResult);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);

			componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS, consumer, responseFormat, user);
			return Either.right(responseFormat);
		}
		try {
			Either<ConsumerData, StorageOperationStatus> getResponse = consumerOperation.getCredentials(consumerName);
			if (getResponse.isLeft() && getResponse.left().value() != null) {
				return updateConsumer(consumer, user, true);
			}

			Date date = new Date();
			consumer.setConsumerDetailsLastupdatedtime(date.getTime());
			consumer.setConsumerLastAuthenticationTime(Long.valueOf(0));

			Either<ConsumerData, StorageOperationStatus> createResponse = consumerOperation.createCredentials(new ConsumerData(consumer));

			if (createResponse.isRight()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForConsumer(createResponse.right().value()));
				componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS, consumer, responseFormat, user);
				return Either.right(responseFormat);
			}
			log.debug("Consumer created successfully!!!");
			consumer = new ConsumerDefinition(createResponse.left().value().getConsumerDataDefinition());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
			componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS, consumer, responseFormat, user);
			return Either.left(consumer);
		} finally {
			graphLockOperation.unlockComponent(consumerName, NodeTypeEnum.ConsumerCredentials);
		}
	}

	private Either<User, ResponseFormat> validateUser(User user, ConsumerDefinition consumer, AuditingActionEnum auditAction) {

		if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
			log.debug("createEcompUser method - user is missing. userId= {}", user.getUserId());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			log.debug("audit before sending response");
			componentsUtils.auditConsumerCredentialsEvent(auditAction, consumer, responseFormat, user);
			return Either.right(responseFormat);
		}
		log.debug("get user from DB");
		Either<User, ActionStatus> eitherCreator = userAdmin.getUser(user.getUserId(), false);
		if (eitherCreator.isRight() || eitherCreator.left().value() == null) {
			log.debug("createEcompUser method - user is not listed. userId= {}", user.getUserId());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_ACCESS);
			log.debug("audit before sending response");
			componentsUtils.auditConsumerCredentialsEvent(auditAction, consumer, responseFormat, user);
			return Either.right(responseFormat);
		}

		user = eitherCreator.left().value();
		// validate user role
		log.debug("validate user role");
		if (!user.getRole().equals(Role.ADMIN.name())) {
			log.info("role {} is not allowed to perform this action", user.getRole());
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			log.debug("audit before sending response");
			componentsUtils.auditConsumerCredentialsEvent(auditAction, consumer, responseFormat, user);
			return Either.right(responseFormat);
		}
		return Either.left(user);
	}

	private Either<ConsumerDefinition, ResponseFormat> validateConsumer(ConsumerDefinition consumer, User user, AuditingActionEnum audatingAction) {
		Either<ConsumerDefinition, ResponseFormat> validateConsumerName = validateConsumerName(consumer);
		if (validateConsumerName.isRight()) {
			return Either.right(validateConsumerName.right().value());
		}
		Either<ConsumerDefinition, ResponseFormat> validateConsumerPassword = validateConsumerPassword(consumer);
		if (validateConsumerPassword.isRight()) {
			return Either.right(validateConsumerPassword.right().value());
		}
		consumer = validateConsumerPassword.left().value();
		Either<ConsumerDefinition, ResponseFormat> validateEcompUserSalt = validateConsumerSalt(consumer);
		if (validateEcompUserSalt.isRight()) {
			return Either.right(validateEcompUserSalt.right().value());
		}
		return Either.left(consumer);

	}

	private Either<ConsumerDefinition, ResponseFormat> validateConsumerName(ConsumerDefinition consumer) {
		String name = consumer.getConsumerName();
		if (!ValidationUtils.validateStringNotEmpty(name)) {
			log.debug("Consumer name cannot be empty.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, CONSUMER_NAME));
		}
		if (!ValidationUtils.validateConsumerName(name)) {
			log.debug("Consumer name is invalid.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, CONSUMER_NAME));
		}
		if (!ValidationUtils.validateLength(name, ValidationUtils.CONSUMER_NAME_MAX_LENGTH)) {
			log.debug("Consumer name exceeds limit.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, CONSUMER_NAME, String.valueOf(ValidationUtils.CONSUMER_NAME_MAX_LENGTH)));
		}
		if (!ValidationUtils.isUTF8Str(name)) {
			log.debug("Consumer name includes non UTF 8 characters.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, CONSUMER_NAME));
		}

		return Either.left(consumer);
	}

	private Either<ConsumerDefinition, ResponseFormat> validateConsumerPassword(ConsumerDefinition consumer) {
		String password = consumer.getConsumerPassword();
		if (!ValidationUtils.validateStringNotEmpty(password)) {
			log.debug("Consumer password cannot be empty.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, CONSUMER_PW));
		}
		if (password.length() != ValidationUtils.CONSUMER_PASSWORD_LENGTH) {
			log.debug("Consumer password length is not valid.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_LENGTH, CONSUMER_PW));
		}
		consumer.setConsumerPassword(password.toLowerCase());
		if (!ValidationUtils.validateConsumerPassSalt(consumer.getConsumerPassword())) {
			log.debug("Consumer password is invalid.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, CONSUMER_PW));
		}

		return Either.left(consumer);
	}

	private Either<ConsumerDefinition, ResponseFormat> validateConsumerSalt(ConsumerDefinition consumer) {
		String salt = consumer.getConsumerSalt();
		if (!ValidationUtils.validateStringNotEmpty(salt)) {
			log.debug("Consumer salt cannot be empty.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_DATA, CONSUMER_SALT));
		}
		if (salt.length() != ValidationUtils.CONSUMER_SALT_LENGTH) {
			log.debug("Consumer salt length is not valid.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_LENGTH, CONSUMER_SALT));
		}
		if (!ValidationUtils.validateConsumerPassSalt(salt)) {
			log.debug("Consumer salt is invalid.");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, CONSUMER_SALT));
		}

		return Either.left(consumer);
	}

	public Either<ConsumerDefinition, ResponseFormat> getConsumer(String consumerId, User user) {
		ConsumerDefinition tmpConsumer = new ConsumerDefinition();
		tmpConsumer.setConsumerName(consumerId);
		// In case of filter (southbound) call
		if (user != null) {
			Either<User, ResponseFormat> userValidation = validateUser(user, tmpConsumer, AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS);
			if (userValidation.isRight()) {
				return Either.right(userValidation.right().value());
			}
			user = userValidation.left().value();
		}
		Either<ConsumerData, StorageOperationStatus> getResult = consumerOperation.getCredentials(consumerId);
		if (getResult.isRight()) {
			ActionStatus action = componentsUtils.convertFromStorageResponseForConsumer(getResult.right().value());
			ResponseFormat responseFormat;
			if (action == ActionStatus.ECOMP_USER_NOT_FOUND) {
				responseFormat = componentsUtils.getResponseFormat(action, consumerId);
			} else {
				responseFormat = componentsUtils.getResponseFormat(action);
			}
			componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS, tmpConsumer, responseFormat, user);
			return Either.right(responseFormat);
		}
		ConsumerDefinition consumer = new ConsumerDefinition(getResult.left().value().getConsumerDataDefinition());
		ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.GET_ECOMP_USER_CREDENTIALS, consumer, responseFormat, user);
		return Either.left(consumer);
	}

	public Either<ConsumerDefinition, ResponseFormat> getConsumer(String consumerId) {
		return getConsumer(consumerId, null);
	}

	public Either<ConsumerDefinition, ResponseFormat> deleteConsumer(String consumerId, User user) {
		ConsumerDefinition tmpConsumer = new ConsumerDefinition();
		tmpConsumer.setConsumerName(consumerId);
		Either<User, ResponseFormat> userValidation = validateUser(user, tmpConsumer, AuditingActionEnum.DELETE_ECOMP_USER_CREDENTIALS);
		if (userValidation.isRight()) {
			return Either.right(userValidation.right().value());
		}
		user = userValidation.left().value();
		Either<ConsumerData, StorageOperationStatus> deleteResult = consumerOperation.deleteCredentials(consumerId);
		if (deleteResult.isRight()) {
			ActionStatus action = componentsUtils.convertFromStorageResponseForConsumer(deleteResult.right().value());
			ResponseFormat responseFormat;
			if (action == ActionStatus.ECOMP_USER_NOT_FOUND) {
				responseFormat = componentsUtils.getResponseFormat(action, consumerId);
			} else {
				responseFormat = componentsUtils.getResponseFormat(action);
			}
			componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.DELETE_ECOMP_USER_CREDENTIALS, tmpConsumer, responseFormat, user);
			return Either.right(responseFormat);
		}
		ConsumerDefinition consumer = new ConsumerDefinition(deleteResult.left().value().getConsumerDataDefinition());
		ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		componentsUtils.auditConsumerCredentialsEvent(AuditingActionEnum.DELETE_ECOMP_USER_CREDENTIALS, consumer, responseFormat, user);
		return Either.left(consumer);
	}

	public Either<ConsumerDefinition, ResponseFormat> updateConsumer(ConsumerDefinition consumer, User modifier, boolean isCreateRequest) {
		Either<ConsumerData, StorageOperationStatus> updateResult = consumerOperation.updateCredentials(new ConsumerData(consumer));
		if (updateResult.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForConsumer(updateResult.right().value()));
			return Either.right(responseFormat);
		}
		consumer = new ConsumerDefinition(updateResult.left().value().getConsumerDataDefinition());
		return Either.left(consumer);
	}

	private void checkFieldsForOverrideAttempt(ConsumerDefinition consumer) {
		if (consumer.getConsumerDetailsLastupdatedtime() != null) {
			log.info("Consumer Details Last updated time cannot be defined by user. This field will be overridden by the application");
		}
		if (consumer.getConsumerLastAuthenticationTime() != null) {
			log.info("Consumer Last Authentication time cannot be defined by user. This field will be overridden by the application");
		}
		if (consumer.getLastModfierAtuid() != null) {
			log.info("Consumer Last Modifier USER_ID cannot be defined by user. This field will be overridden by the application");
		}
	}

}
