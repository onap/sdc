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

package org.openecomp.sdc.heat.datatypes;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.heat.services.ErrorCodes;

import java.util.HashSet;
import java.util.Set;

public class HeatBoolean {

  private static Set<Object> heatFalse;
  private static Set<Object> heatTrue;

  static {


    heatFalse = new HashSet<>();
    heatFalse.add("f");
    heatFalse.add(false);
    heatFalse.add("false");
    heatFalse.add("off");
    heatFalse.add("n");
    heatFalse.add("no");
    heatFalse.add(0);

    heatTrue = new HashSet<>();
    heatTrue.add("t");
    heatTrue.add(true);
    heatTrue.add("true");
    heatTrue.add("on");
    heatTrue.add("y");
    heatTrue.add("yes");
    heatTrue.add(1);

  }

  /**
   * Eval boolean.
   *
   * @param value the value
   * @return the boolean
   */
  public static Boolean eval(Object value) {

    if (value instanceof String) {
      value = (String) ((String) value).toLowerCase();
    }
    if (heatFalse.contains(value)) {
      return false;
    } else if (heatTrue.contains(value)) {
      return true;
    } else {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withId(ErrorCodes.INVALID_BOOLEAN)
          .withCategory(ErrorCategory.APPLICATION)
          .withMessage("Invalid boolean value [" + value + "].").build());
    }

  }

  /**
   * Is value boolean boolean.
   *
   * @param value the value
   * @return the boolean
   */
  public static boolean isValueBoolean(Object value) {
    try {
      Boolean answer = eval(value);
      return true;
    } catch (CoreException ce) {
      return false;
    }
  }
}
