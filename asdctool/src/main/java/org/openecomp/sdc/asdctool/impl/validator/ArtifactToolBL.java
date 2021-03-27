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

import java.util.List;
import org.openecomp.sdc.asdctool.impl.validator.executor.IArtifactValidatorExecutor;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ArtifactToolBL {

    private static final Logger log = Logger.getLogger(ArtifactToolBL.class);
    private final List<IArtifactValidatorExecutor> validators;

    @Autowired
    public ArtifactToolBL(List<IArtifactValidatorExecutor> validators) {
        this.validators = validators;
    }

    public boolean validateAll(String outputFilePath) {
        boolean allValid = true;
        for (IArtifactValidatorExecutor validatorExec : validators) {
            log.debug("ValidatorExecuter " + validatorExec.getName() + " started");
            if (!validatorExec.executeValidations(outputFilePath)) {
                allValid = false;
                log.debug("ValidatorExecuter " + validatorExec.getName() + " finished with warnings");
            } else {
                log.debug("ValidatorExecuter " + validatorExec.getName() + " finished successfully");
            }
        }
        return allValid;
    }
}
