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

package org.openecomp.sdc.logging.context.impl;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcData;
import org.openecomp.sdc.logging.messages.DebugMessages;
import org.openecomp.sdc.logging.types.DebugConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCategory;
import org.openecomp.sdc.logging.util.LoggingUtils;

import java.util.HashMap;
import java.util.Map;

public class MdcDataDebugMessage extends MdcData {

  private Logger logger;
  private static Map<String, String> mapExitOrEntryToMessage;

  static {
    mapExitOrEntryToMessage = new HashMap<>();
    mapExitOrEntryToMessage.put(DebugConstants.ENTER, DebugMessages.ENTER_METHOD);
    mapExitOrEntryToMessage.put(DebugConstants.ENTER_DEFAULT, DebugMessages.DEFAULT_ENTER_METHOD);
    mapExitOrEntryToMessage.put(DebugConstants.EXIT, DebugMessages.EXIT_METHOD);
    mapExitOrEntryToMessage.put(DebugConstants.EXIT_DEFAULT, DebugMessages.DEFAULT_EXIT_METHOD);
  }

  public MdcDataDebugMessage() {
    super(LoggerErrorCategory.DEBUG.name(), null);
  }

  //todo add more explanations as to the first parameter structure in case of multiples and in
  // case of no params in method
  /**
   * Debug entry message.
   *
   * @param entityParameter the entity parameter
   * @param ids             the ids
   */
  public void debugEntryMessage(String entityParameter, String... ids) {
    logDebugMessage(entityParameter, DebugConstants.ENTER, ids);
  }

  /**
   * Debug exit message.
   *
   * @param entityParameter the entity parameter
   * @param ids             the ids
   */
  public void debugExitMessage(String entityParameter, String... ids) {
    logDebugMessage(entityParameter, DebugConstants.EXIT, ids);
  }

  private void logDebugMessage(String entityParameter, String enterOrExit, String... ids) {
    String methodName = LoggingUtils.getCallingMethodNameForDebugging();
    String declaringClass = LoggingUtils.getDeclaringClass();
    logger = LoggerFactory.getLogger(declaringClass);
    String messageToWrite;

    if (entityParameter == null || ids == null) {
      messageToWrite = mapExitOrEntryToMessage.get(enterOrExit + "_" + DebugConstants.DEFAULT);
      logger.debug(String.format(messageToWrite, methodName));
    } else {
      messageToWrite = mapExitOrEntryToMessage.get(enterOrExit);
      logger.debug(String
          .format(messageToWrite, methodName, entityParameter, String.join(",", ids)));
    }
  }
}
