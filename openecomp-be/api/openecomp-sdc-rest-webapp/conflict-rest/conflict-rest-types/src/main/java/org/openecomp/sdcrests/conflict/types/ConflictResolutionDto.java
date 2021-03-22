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
package org.openecomp.sdcrests.conflict.types;

import java.util.Map;
import org.openecomp.conflicts.types.Resolution;

public class ConflictResolutionDto {

    private Resolution resolution;
    // sits in lower level...
    private Map<String, Object> otherResolution;

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public Map<String, Object> getOtherResolution() {
        return otherResolution;
    }

    public void setOtherResolution(Map<String, Object> otherResolution) {
        this.otherResolution = otherResolution;
    }
}
