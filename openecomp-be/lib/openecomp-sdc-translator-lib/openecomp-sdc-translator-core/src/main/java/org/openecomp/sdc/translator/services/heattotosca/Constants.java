/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca;

public class Constants {
  //Service Template - Template Names
  public static final String GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME = "GlobalSubstitutionTypes";
  public static final String MAIN_TEMPLATE_NAME = "Main";
  //properties
  public static final String MAX_INSTANCES_PROPERTY_NAME = "max_instances";
  public static final String DESCRIPTION_PROPERTY_NAME = "description";
  public static final String NAME_PROPERTY_NAME = "name";
  public static final String SECURITY_GROUPS_PROPERTY_NAME = "security_groups";
  public static final String PORT_PROPERTY_NAME = "port";
  //General
  public static final String PROP = "properties";
  public static final String ATTR = "attributes";
  public static final String SERVICE_INSTANCE_PORT_PREFIX = "port_";
  public static final String SERVICE_INSTANCE_LINK_PREFIX = "link_";
  //Unified model
  static final String COMPUTE_IDENTICAL_VALUE_PROPERTY_PREFIX = "vm_";
  static final String COMPUTE_IDENTICAL_VALUE_PROPERTY_SUFFIX = "_name";
  static final String PORT_IDENTICAL_VALUE_PROPERTY_PREFIX = "port_";
  static final String SUB_INTERFACE_PROPERTY_VALUE_PREFIX = "subinterface_";
  public static final String ABSTRACT_NODE_TEMPLATE_ID_PREFIX = "abstract_";

  private Constants() {
  }
}
