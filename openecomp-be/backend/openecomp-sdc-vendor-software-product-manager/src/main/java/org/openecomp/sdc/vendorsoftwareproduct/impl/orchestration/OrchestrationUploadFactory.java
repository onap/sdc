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

import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_CONFIG_NAMESPACE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_IMPL_KEY;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.OrchestrationTemplateFileExtensionErrorBuilder;

public class OrchestrationUploadFactory {

    private static final Map<String, ImplementationConfiguration> FILE_HANLDERS;

    static {
        Configuration config = ConfigurationManager.lookup();
        FILE_HANLDERS = new ConcurrentHashMap<>(
            config.populateMap(ORCHESTRATION_CONFIG_NAMESPACE, ORCHESTRATION_IMPL_KEY, ImplementationConfiguration.class));
    }

    private OrchestrationUploadFactory() {
    }

    public static OrchestrationTemplateFileHandler createOrchestrationTemplateFileHandler(final OnboardingTypesEnum onboardingType) {
        final ImplementationConfiguration orchestrationTemplateFileHandler = FILE_HANLDERS.get(onboardingType.toString());
        if (Objects.isNull(orchestrationTemplateFileHandler)) {
            throw new CoreException(new OrchestrationTemplateFileExtensionErrorBuilder().build());
        }
        return CommonMethods.newInstance(orchestrationTemplateFileHandler.getImplementationClass(), OrchestrationTemplateFileHandler.class);
    }
}
