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
package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

public class MonitoringUploadStatus {

    private String snmpTrap;
    private String snmpPoll;
    private String vesEvent;

    //todo 1802 tech debt story : refactor this to be a map of type-filename
    public MonitoringUploadStatus(String snmpTrap, String snmpPoll) {
        this.snmpTrap = snmpTrap;
        this.snmpPoll = snmpPoll;
    }

    public MonitoringUploadStatus() {
    }

    public String getSnmpTrap() {
        return snmpTrap;
    }

    public void setSnmpTrap(String snmpTrap) {
        this.snmpTrap = snmpTrap;
    }

    public String getSnmpPoll() {
        return snmpPoll;
    }

    public void setSnmpPoll(String snmpPoll) {
        this.snmpPoll = snmpPoll;
    }

    public String getVesEvent() {
        return vesEvent;
    }

    public void setVesEvent(String vesEvent) {
        this.vesEvent = vesEvent;
    }
}
