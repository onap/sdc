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

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.ErrorConfiguration;
import org.openecomp.sdc.be.config.ErrorInfo;
import org.openecomp.sdc.be.config.ErrorInfo.ErrorInfoType;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.exception.OkResponseInfo;
import org.openecomp.sdc.exception.PolicyException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseFormatManager {

	private volatile static ResponseFormatManager instance;
	private static ConfigurationManager configurationManager;
	private static Logger log = LoggerFactory.getLogger(ResponseFormatManager.class.getName());

	public static ResponseFormatManager getInstance() {
		if (instance == null) {

			instance = init();
		}
		return instance;
	}

	private static synchronized ResponseFormatManager init() {
		if (instance == null) {
			instance = new ResponseFormatManager();
			configurationManager = ConfigurationManager.getConfigurationManager();
		}
		return instance;
	}

	public ResponseFormat getResponseFormat(ActionStatus responseEnum, String... variables) {
		ErrorConfiguration errorConfiguration = configurationManager.getErrorConfiguration();
		ErrorInfo errorInfo = errorConfiguration.getErrorInfo(responseEnum.name());
		if (errorInfo == null) {
			log.debug("failed to locate {} in error configuration", responseEnum.name());
			errorInfo = errorConfiguration.getErrorInfo(ActionStatus.GENERAL_ERROR.name());
		}
		ResponseFormat errorResponseWrapper = new ResponseFormat(errorInfo.getCode());
		String errorMessage = errorInfo.getMessage();
		String errorMessageId = errorInfo.getMessageId();
		ErrorInfoType errorInfoType = errorInfo.getErrorInfoType();
		if (errorInfoType.equals(ErrorInfoType.SERVICE_EXCEPTION)) {
			errorResponseWrapper.setServiceException(new ServiceException(errorMessageId, errorMessage, variables));
		} else if (errorInfoType.equals(ErrorInfoType.POLICY_EXCEPTION)) {
			errorResponseWrapper.setPolicyException(new PolicyException(errorMessageId, errorMessage, variables));
		} else if (errorInfoType.equals(ErrorInfoType.OK)) {
			errorResponseWrapper.setOkResponseInfo(new OkResponseInfo(errorMessageId, errorMessage, variables));
		}
		return errorResponseWrapper;
	}
}
