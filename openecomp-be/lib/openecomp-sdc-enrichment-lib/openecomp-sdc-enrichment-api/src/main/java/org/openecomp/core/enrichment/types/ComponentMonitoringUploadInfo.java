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

package org.openecomp.core.enrichment.types;

import java.util.HashMap;
import java.util.Map;

import static org.openecomp.core.enrichment.types.MonitoringUploadType.SNMP_POLL;
import static org.openecomp.core.enrichment.types.MonitoringUploadType.SNMP_TRAP;
import static org.openecomp.core.enrichment.types.MonitoringUploadType.VES_EVENTS;

public class ComponentMonitoringUploadInfo {


  private Map<MonitoringUploadType, MonitoringArtifactInfo> infoByType = new HashMap<>();
  //todo as part of ATTASDC-4503


  public MonitoringArtifactInfo getSnmpTrap() {
    return infoByType.get(SNMP_TRAP);
  }

  public void setSnmpTrap(MonitoringArtifactInfo snmpTrapInfo) {
    infoByType.put(SNMP_TRAP, snmpTrapInfo);
  }

  public MonitoringArtifactInfo getSnmpPoll() {
    return infoByType.get(SNMP_POLL);
  }

  public void setSnmpPoll(MonitoringArtifactInfo snmpPollInfo) {
    infoByType.put(SNMP_POLL, snmpPollInfo);
  }

  public MonitoringArtifactInfo getVesEvent() {
    return infoByType.get(VES_EVENTS);
  }

  public void setVesEvent(MonitoringArtifactInfo snmpPollInfo) {
    infoByType.put(VES_EVENTS, snmpPollInfo);
  }
}
