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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands;

public class CommandImplNames {

  private static final String COMMANDS_IMPL_BASE_PACKAGE =
      "org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.impl";

  public static final String COMPUTE_NEW_NODE_TEMPLATE_ID_GENERATOR_IMPL =
      COMMANDS_IMPL_BASE_PACKAGE + ".ComputeNewNodeTemplateIdGenerator";
  public static final String PORT_NEW_NODE_TEMPLATE_ID_GENERATOR_IMPL =
      COMMANDS_IMPL_BASE_PACKAGE + ".PortNewNodeTemplateIdGenerator";
  public static final String SUB_INTERFACE_NEW_NODE_TEMPLATE_ID_GENERATOR_IMPL =
      COMMANDS_IMPL_BASE_PACKAGE + ".SubInterfaceNewNodeTemplateIdGenerator";
}
