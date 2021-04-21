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
package org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public final class HelmValidatorResponse {

    private List<String> renderErrors;
    private List<String> lintWarning;
    private List<String> lintError;
    private String versionUsed;
    @SerializedName("valid")
    private Boolean isValid;
    @SerializedName("deployable")
    private Boolean isDeployable;
    private List<String> otherErrors;

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

    /**
     * Get versionUsed
     *
     * @return versionUsed
     **/
    public String getVersionUsed() {
        return versionUsed;
    }

    /**
     * Get valid
     *
     * @return valid
     **/
    public Boolean isValid() {
        return isValid;
    }

    /**
     * Get deployable
     *
     * @return deployable
     **/
    public Boolean isDeployable() {
        return isDeployable;
    }

}
