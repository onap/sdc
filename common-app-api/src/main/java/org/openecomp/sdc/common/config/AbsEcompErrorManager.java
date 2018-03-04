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

import com.jcabi.aspects.Loggable;

@Loggable(prepend = true, value = Loggable.TRACE, trim = false)
public abstract class AbsEcompErrorManager {

	public static final String PARAM_STR = "%s";

	public abstract IEcompConfigurationManager getConfigurationManager();

	public void processEcompError(String context, EcompErrorEnum ecompErrorEnum, String... descriptionParams) {

		EcompErrorLogUtil.logEcompError(context, ecompErrorEnum, descriptionParams);

	}

}
