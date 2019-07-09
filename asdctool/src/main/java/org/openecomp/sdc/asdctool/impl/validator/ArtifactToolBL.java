/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.impl.validator;

import org.openecomp.sdc.asdctool.impl.validator.executers.IArtifactValidatorExecuter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ArtifactToolBL {
	
	 private static Logger log = Logger.getLogger(ValidationToolBL.class.getName());

	    @Autowired
	    protected List<IArtifactValidatorExecuter> validators;

	    private boolean allValid = true;

	    public boolean validateAll() {
	        for (IArtifactValidatorExecuter validatorExec: validators) {
	            log.debug("ValidatorExecuter "+validatorExec.getName()+" started");
	            if (!validatorExec.executeValidations()) {
	                allValid = false;
	                log.debug("ValidatorExecuter "+validatorExec.getName()+" finished with warnings");
	            }
	            else {
	                log.debug("ValidatorExecuter "+validatorExec.getName()+" finished successfully");
	            }
	        }
	        return allValid;
	    }

}
