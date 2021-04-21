/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.config.api.Configuration;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig;

@ExtendWith(MockitoExtension.class)
public class HelmValidatorConfigReaderTest {

    private final static String CONFIG_NAMESPACE = "helmvalidator";
    @Mock
    private Configuration configuration;

    @ParameterizedTest
    @ValueSource(strings = {"v3", "3.4.5"})
    void shouldReadVersionFromConfig(String helmVersion) {
        //given
        when(configuration.getAsString(CONFIG_NAMESPACE, "hValidatorVersion")).thenReturn(helmVersion);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        assertEquals(helmVersion, helmValidatorConfig.getVersion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:3211", "https://test-abc"})
    void shouldReadValidatorUrlFromConfig(String validatorUrl) {
        //given
        when(configuration.getAsString(CONFIG_NAMESPACE, "hValidatorUrl")).thenReturn(validatorUrl);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        assertEquals(validatorUrl, helmValidatorConfig.getValidatorUrl());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReadEnabledValueFromConfig(boolean isEnabled) {
        //given
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorEnabled")).thenReturn(isEnabled);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        Assertions.assertEquals(isEnabled, helmValidatorConfig.isEnabled());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReadDeployableValueFromConfig(boolean isDeployable) {
        //given
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorDeployable")).thenReturn(isDeployable);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        Assertions.assertEquals(isDeployable, helmValidatorConfig.isDeployable());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReadLintableValueFromConfig(boolean isLintable) {
        //given
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorLintable")).thenReturn(isLintable);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        Assertions.assertEquals(isLintable, helmValidatorConfig.isLintable());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReadStrictLintableValueFromConfig(boolean isStrictLintable) {
        //given
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorStrictLintable"))
            .thenReturn(isStrictLintable);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        Assertions.assertEquals(isStrictLintable, helmValidatorConfig.isStrictLintable());
    }
}
