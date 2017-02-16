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

package org.openecomp.sdc.tosca.datatypes;


public enum ToscaCapabilityType {

  ROOT("tosca.capabilities.Root"),
  NODE("tosca.capabilities.Node"),
  CONTAINER("tosca.capabilities.Container"),
  NETWORK_BINDABLE("tosca.capabilities.network.Bindable"),
  SCALABLE("tosca.capabilities.Scalable"),
  OPERATING_SYSTEM("tosca.capabilities.OperatingSystem"),
  ENDPOINT_ADMIN("tosca.capabilities.Endpoint.Admin"),
  ATTACHMENT("tosca.capabilities.Attachment"),
  NETWORK_LINKABLE("tosca.capabilities.network.Linkable"),
  METRIC("org.openecomp.capabilities.Metric"),
  NFV_METRIC("tosca.capabilities.nfv.Metric"),
  METRIC_CEILOMETER("org.openecomp.capabilities.metric.Ceilometer"),
  METRIC_SNMP_TRAP("org.openecomp.capabilities.metric.SnmpTrap"),
  METRIC_SNMP_POLLING("org.openecomp.capabilities.metric.SnmpPolling"),;

  private String displayName;

  ToscaCapabilityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }


}
