/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.OrchestrationTemplateFileExtensionErrorBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_CONFIG_NAMESPACE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_IMPL_KEY;

public class OrchestrationUploadFactory {
    private static final Map<String, ImplementationConfiguration> fileHanlders;
    private OrchestrationUploadFactory() {

    }
    static {
        Configuration config = ConfigurationManager.lookup();
        fileHanlders = new ConcurrentHashMap<>(config.populateMap(ORCHESTRATION_CONFIG_NAMESPACE,
                ORCHESTRATION_IMPL_KEY, ImplementationConfiguration.class));

    }

    public static OrchestrationTemplateFileHandler createOrchestrationTemplateFileHandler(String fileSuffix) {
        String fileExtension = fileSuffix.toLowerCase();
        ImplementationConfiguration orchestrationTemplateFileHandler = fileHanlders.get(fileExtension);

        if(Objects.isNull(orchestrationTemplateFileHandler)){
            throw new CoreException(new OrchestrationTemplateFileExtensionErrorBuilder
                ().build());
        }

        return  CommonMethods.newInstance(orchestrationTemplateFileHandler.getImplementationClass(),
                        OrchestrationTemplateFileHandler.class);
    }
}
