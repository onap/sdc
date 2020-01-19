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

package org.openecomp.sdc.fe.config;

import org.openecomp.sdc.common.config.AbsEcompErrorManager;
import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;

public class FeEcompErrorManager extends AbsEcompErrorManager {

	private static volatile FeEcompErrorManager instance;
	private static ConfigurationManager configurationManager;

	private FeEcompErrorManager() {
	};

	public static FeEcompErrorManager getInstance() {
		if (instance == null) {

			instance = init();
		}
		return instance;
	}

	private static synchronized FeEcompErrorManager init() {
		if (instance == null) {
			instance = new FeEcompErrorManager();
			configurationManager = ConfigurationManager.getConfigurationManager();
		}
		return instance;
	}

	@Override
	public IEcompConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void logFeHealthCheckRecovery(String context) {
		processEcompError(context, EcompErrorEnum.FeHealthCheckRecovery);
	}

	public void logFeHealthCheckError(String context) {
		processEcompError(context, EcompErrorEnum.FeHealthCheckError);
	}

	public void logFeHttpLoggingError(String context) {
		processEcompError(context, EcompErrorEnum.FeHttpLoggingError);
	}

	public void logFePortalServletError(String context) {
		processEcompError(context, EcompErrorEnum.FePortalServletError);
	}

	public void logFeHealthCheckGeneralError(String context) {
		processEcompError(context, EcompErrorEnum.FeHealthCheckGeneralError);
	}

}
