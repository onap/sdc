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

package org.openecomp.sdc.common.config;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.config.EcompErrorConfiguration.EcompAlarmSeverity;
import org.openecomp.sdc.common.config.EcompErrorConfiguration.EcompErrorSeverity;
import org.openecomp.sdc.common.config.EcompErrorConfiguration.EcompErrorType;

import com.jcabi.aspects.Loggable;

@Loggable(prepend = true, value = Loggable.TRACE, trim = false)
public abstract class AbsEcompErrorManager implements IEcompErrorManager {

	public static final String PARAM_STR = "%s";

	public abstract IEcompConfigurationManager getConfigurationManager();

	@Deprecated
	@Override
	public void processEcompError(EcompErrorName ecompErrorName, String ecompErrorContext,
			String... descriptionParams) {

		/*
		 * //Getting the relevant config manager IEcompConfigurationManager
		 * configurationManager = getConfigurationManager();
		 * 
		 * //Getting the error by name EcompErrorInfo ecompErrorInfo =
		 * configurationManager.getEcompErrorConfiguration().getEcompErrorInfo(
		 * ecompErrorName.name());
		 * 
		 * if (ecompErrorInfo != null){ ecompErrorInfo =
		 * setDescriptionParams(ecompErrorInfo, ecompErrorName.name(),
		 * descriptionParams); EcompErrorLogUtil.logEcompError(ecompErrorName,
		 * ecompErrorInfo, ecompErrorContext); } else {
		 * EcompErrorLogUtil.logEcompError(EcompErrorName.EcompErrorNotFound,
		 * getErrorInfoForUnknownErrorName(ecompErrorName.name()),
		 * ecompErrorContext); }
		 */

	}

	private EcompErrorInfo setDescriptionParams(EcompErrorInfo ecompErrorInfo, String ecompErrorName,
			String... descriptionParams) {
		String description = ecompErrorInfo.getDescription();
		// Counting number of params in description
		int countMatches = StringUtils.countMatches(description, PARAM_STR);
		// Catching cases when there are more params passed than there are in
		// the description (formatter will ignore extra params and won't throw
		// exception)
		if (countMatches != descriptionParams.length) {
			return getErrorInfoForDescriptionParamsMismatch(ecompErrorName);
		}
		// Setting params of the description if any
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		try {
			formatter.format(description, (Object[]) descriptionParams).toString();
			ecompErrorInfo.setDescription(formatter.toString());
		} catch (IllegalFormatException e) {
			// Number of passed params doesn't match number of params in config
			// file
			return getErrorInfoForDescriptionParamsMismatch(ecompErrorName);
		} finally {
			formatter.close();
		}
		return ecompErrorInfo;
	}

	private EcompErrorInfo getErrorInfoForUnknownErrorName(String ecompErrorName) {
		EcompErrorInfo ecompErrorInfo = new EcompErrorInfo();
		ecompErrorInfo.setCode(EcompErrorConfiguration.ECODE_PREFIX + "3001");
		ecompErrorInfo.setType(EcompErrorType.CONFIG_ERROR.name());
		ecompErrorInfo.setSeverity(EcompErrorSeverity.ERROR.name());
		ecompErrorInfo.setAlarmSeverity(EcompAlarmSeverity.MAJOR.name());
		ecompErrorInfo.setDescription(new StringBuilder().append("Ecomp error element  not found in YAML, name: ")
				.append(ecompErrorName).toString());
		return ecompErrorInfo;
	}

	private EcompErrorInfo getErrorInfoForDescriptionParamsMismatch(String ecompErrorName) {
		EcompErrorInfo ecompErrorInfo = new EcompErrorInfo();
		ecompErrorInfo.setCode(EcompErrorConfiguration.ECODE_PREFIX + "3002");
		ecompErrorInfo.setType(EcompErrorType.CONFIG_ERROR.name());
		ecompErrorInfo.setSeverity(EcompErrorSeverity.ERROR.name());
		ecompErrorInfo.setAlarmSeverity(EcompAlarmSeverity.MAJOR.name());
		ecompErrorInfo.setDescription(new StringBuilder()
				.append("Ecomp error description params mismatch between code and YAML or wrong format, name: ")
				.append(ecompErrorName).toString());
		return ecompErrorInfo;
	}

	public void processEcompError(String context, EcompErrorEnum ecompErrorEnum, String... descriptionParams) {

		EcompErrorLogUtil.logEcompError(context, ecompErrorEnum, descriptionParams);

	}

}
