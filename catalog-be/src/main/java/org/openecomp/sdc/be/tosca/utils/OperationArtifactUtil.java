/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.tosca.utils;

import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.tosca.CsarUtils;

import java.io.File;

public class OperationArtifactUtil {


    /**
     * This method assumes that operation.getImplementation() is not NULL  ( it should be verified by the caller method)
     *
     * @param componentName component's normalized name
     * @param interfaceType the specific interface type
     * @param operation     the specific operation name
     * @return the full path including file name for operation's artifacts
     */

    public static String createOperationArtifactPath(String componentName, String interfaceType,
            OperationDataDefinition operation) {
        return CsarUtils.ARTIFACTS + File.separator + componentName + File.separator + interfaceType + File.separator
                       + CsarUtils.DEPLOYMENT_ARTIFACTS_DIR + CsarUtils.WORKFLOW_ARTIFACT_DIR + operation
                                                                                                        .getImplementation()
                                                                                                        .getArtifactName();
    }
}
