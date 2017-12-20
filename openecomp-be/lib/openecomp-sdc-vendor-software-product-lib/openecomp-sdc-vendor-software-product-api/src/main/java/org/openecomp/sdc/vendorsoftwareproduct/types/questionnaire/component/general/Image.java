/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general;

/**
 * Created by TALIO on 11/22/2016.
 */
public class Image {
    private String format;
    private String providedBy;
    private Number bootDiskSizePerVM;
    private Number ephemeralDiskSizePerVM;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getProvidedBy() {
        return providedBy;
    }

    public void setProvidedBy(String providedBy) {
        this.providedBy = providedBy;
    }

    public Number getBootDiskSizePerVM() {
        return bootDiskSizePerVM;
    }

    public void setBootDiskSizePerVM(Number bootDiskSizePerVM) {
        this.bootDiskSizePerVM = bootDiskSizePerVM;
    }

    public Number getEphemeralDiskSizePerVM() {
        return ephemeralDiskSizePerVM;
    }

    public void setEphemeralDiskSizePerVM(Number ephemeralDiskSizePerVM) {
        this.ephemeralDiskSizePerVM = ephemeralDiskSizePerVM;
    }
}
