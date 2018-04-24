package org.openecomp.sdc.be.tosca.utils;

import java.io.File;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.tosca.CsarUtils;

public class OperationArtifactUtil {

    /**
     * This method assumes that operation.getImplementation() is not NULL  ( it should be verified by the caller method)
     *
     * @param componentName component's normalized name
     * @param interfaceType the specific interface name
     * @param operation     teh specific operation name
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
