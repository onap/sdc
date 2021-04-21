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
package org.openecomp.sdc.validation.type.helmvalidator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HelmValidatorConfig {
    private final String validatorUrl;
    private final String version;
    private final boolean isEnabled;
    private final boolean isDeployable;
    private final boolean isLintable;
    private final boolean isStrictLintable;

    public static class HelmValidationConfigBuilder {

        private String validatorUrl;
        private String version;
        private boolean enabled;
        private boolean deployable;
        private boolean lintable;
        private boolean strictLintable;

        public HelmValidationConfigBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public HelmValidationConfigBuilder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public HelmValidationConfigBuilder setDeployable(boolean deployable) {
            this.deployable = deployable;
            return this;
        }

        public HelmValidationConfigBuilder setLintable(boolean lintable) {
            this.lintable = lintable;
            return this;
        }

        public HelmValidationConfigBuilder setStrictLintable(boolean strictLintable) {
            this.strictLintable = strictLintable;
            return this;
        }

        public HelmValidationConfigBuilder setValidatorUrl(String validatorUrl) {
            this.validatorUrl = validatorUrl;
            return this;
        }

        public HelmValidatorConfig build() {
            return new HelmValidatorConfig(validatorUrl, version, enabled, deployable, lintable, strictLintable);
        }
    }

}
