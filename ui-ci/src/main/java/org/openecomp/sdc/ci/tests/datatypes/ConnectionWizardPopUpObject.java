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

package org.openecomp.sdc.ci.tests.datatypes;

public class ConnectionWizardPopUpObject {

    private String capabilityTypeFirstItem;
    private String capabilityTypeSecondItem;
    private String capabilityNameFirstItem;
    private String capabilityNameSecondItem;

    public ConnectionWizardPopUpObject(String capabilityTypeFirstItem, String capabilityNameFirstItem, String capabilityTypeSecondItem, String capabilityNameSecondItem) {
        this.capabilityTypeFirstItem = capabilityTypeFirstItem;
        this.capabilityTypeSecondItem = capabilityTypeSecondItem;
        this.capabilityNameFirstItem = capabilityNameFirstItem;
        this.capabilityNameSecondItem = capabilityNameSecondItem;
    }

    public String getCapabilityTypeFirstItem() {
        return capabilityTypeFirstItem;
    }

    public void setCapabilityTypeFirstItem(String capabilityTypeFirstItem) {
        this.capabilityTypeFirstItem = capabilityTypeFirstItem;
    }

    public String getCapabilityTypeSecondItem() {
        return capabilityTypeSecondItem;
    }

    public void setCapabilityTypeSecondItem(String capabilityTypeSecondItem) {
        this.capabilityTypeSecondItem = capabilityTypeSecondItem;
    }

    public String getCapabilityNameFirstItem() {
        return capabilityNameFirstItem;
    }

    public void setCapabilityNameFirstItem(String capabilityNameFirstItem) {
        this.capabilityNameFirstItem = capabilityNameFirstItem;
    }

    public String getCapabilityNameSecondItem() {
        return capabilityNameSecondItem;
    }

    public void setCapabilityNameSecondItem(String capabilityNameSecondItem) {
        this.capabilityNameSecondItem = capabilityNameSecondItem;
    }

    @Override
    public String toString() {
        return "ConnectionWizardPopUpObject{"
                + "capabilityTypeFirstItem='" + capabilityTypeFirstItem + '\''
                + ", capabilityTypeSecondItem='" + capabilityTypeSecondItem + '\''
                + ", capabilityNameFirstItem='" + capabilityNameFirstItem + '\''
                + ", capabilityNameSecondItem='" + capabilityNameSecondItem + '\''
                + '}';
    }
}
