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
package org.openecomp.sdc.validation.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.type.ConfigConstants;

public class ValidationFactory {

    private static final List<Validator> validators = new ArrayList<>();
    private static Map<String, ImplementationConfiguration> validationImplMap;
    private static Logger logger = (Logger) LoggerFactory.getLogger(ValidationFactory.class);
    private static File file;

    static {
        Configuration config = ConfigurationManager.lookup();
        validationImplMap = config.populateMap(ConfigConstants.Namespace, ConfigConstants.Validator_Impl_Key, ImplementationConfiguration.class);
        validationImplMap
            .putAll(config.populateMap(ConfigConstants.Mandatory_Namespace, ConfigConstants.Validator_Impl_Key, ImplementationConfiguration.class));
    }

    /**
     * Initialize a validator.
     */
    public static List<Validator> getValidators() {
        synchronized (validators) {
            if (CollectionUtils.isEmpty(validators)) {
                validationImplMap.values().stream().filter(implementationConfiguration -> implementationConfiguration.isEnable())
                    .forEachOrdered(implementationConfiguration -> validators.add(cerateValidatorImpl(implementationConfiguration)));
            }
        }
        return validators;
    }

    private static Validator cerateValidatorImpl(ImplementationConfiguration validatorConf) {
        Validator validator = CommonMethods.newInstance(validatorConf.getImplementationClass(), Validator.class);
        validator.init(validatorConf.getProperties());
        logger.debug("created validator" + validatorConf.getImplementationClass());
        return validator;
    }
}
