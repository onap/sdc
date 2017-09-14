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


import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.sdc.tosca.services.ConfigConstants;

public class ToscaCapabilityType {

  private static Configuration config = ConfigurationManager.lookup();

  public static final String CAPABILITY_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_CAPABILITY_TYPE);

  //TOSCA native types
  public static final String NATIVE_ROOT = "tosca.capabilities.Root";
  public static final String NATIVE_NODE = "tosca.capabilities.Node";
  public static final String NATIVE_CONTAINER = "tosca.capabilities.Container";
  public static final String NATIVE_COMPUTE = "tosca.capabilities.Compute";
  public static final String NATIVE_NETWORK_BINDABLE = "tosca.capabilities.network.Bindable";
  public static final String NATIVE_SCALABLE = "tosca.capabilities.Scalable";
  public static final String NATIVE_OPERATING_SYSTEM = "tosca.capabilities.OperatingSystem";
  public static final String NATIVE_ENDPOINT_ADMIN = "tosca.capabilities.Endpoint.Admin";
  public static final String NATIVE_ATTACHMENT = "tosca.capabilities.Attachment";
  public static final String NATIVE_NETWORK_LINKABLE = "tosca.capabilities.network.Linkable";
  public static final String NATIVE_NFV_METRIC = "tosca.capabilities.nfv.Metric";

  //Additional types
  public static final String METRIC = CAPABILITY_PREFIX + "Metric";
  public static final String METRIC_CEILOMETER = CAPABILITY_PREFIX + "metric.Ceilometer";
  public static final String METRIC_SNMP_TRAP = CAPABILITY_PREFIX + "metric.SnmpTrap";
  public static final String METRIC_SNMP_POLLING = CAPABILITY_PREFIX + "metric.SnmpPolling";

}
