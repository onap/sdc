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

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

public final class HelmValidatorResponse {

    private List<String> renderErrors;
    private List<String> lintWarning;
    private List<String> lintError;
    @Getter
    private String versionUsed;
    @SerializedName("valid")
    @Getter
    private Boolean isValid;
    @SerializedName("deployable")
    @Getter
    private Boolean isDeployable;

    /**
     * Get renderErrors
     *
     * @return renderErrors
     **/
    public List<String> getRenderErrors() {
        return renderErrors != null ? renderErrors : Collections.emptyList();
    }

    /**
     * Get lintWarning
     *
     * @return lintWarning
     **/
    public List<String> getLintWarning() {
        return lintWarning != null ? lintWarning : Collections.emptyList();
    }

    /**
     * Get lintError
     *
     * @return lintError
     **/
    public List<String> getLintError() {
        return lintError != null ? lintError : Collections.emptyList();
    }
}
