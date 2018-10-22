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

package org.openecomp.sdc.heat.services.tree;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.mockito.Mockito;
import org.onap.config.api.Configuration;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.services.ValidationFactory;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.testng.annotations.Test;

public class ValidationFactoryTest {

    private static final String A = "A";
    private static final String B = "B";

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void validValidatorsFailure() {
        ImplementationConfiguration implementationConfiguration = Mockito.mock(ImplementationConfiguration.class);
        Configuration configuration = Mockito.mock(Configuration.class);
        final ImmutableMap<String, ImplementationConfiguration> mapA = ImmutableMap.of(A, implementationConfiguration);
        Mockito.when(configuration.populateMap(ConfigConstants.Namespace, ConfigConstants.Validator_Impl_Key,
                ImplementationConfiguration.class)).thenReturn(mapA);
        final ImmutableMap<String, ImplementationConfiguration> mapB = ImmutableMap.of(B, implementationConfiguration);
        Mockito.when(configuration.populateMap(ConfigConstants.Mandatory_Namespace, ConfigConstants.Validator_Impl_Key,
                ImplementationConfiguration.class)).thenReturn(mapB);
        List<Validator> validators = ValidationFactory.getValidators(configuration);
    }


}
