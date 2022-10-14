/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.sdc.heat.datatypes;

import java.util.HashSet;
import java.util.Set;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.heat.services.ErrorCodes;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class HeatBoolean {

    private static final Logger LOG = LoggerFactory.getLogger(HeatBoolean.class.getName());
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

    private HeatBoolean() {
        //Utility classes, which are a collection of static members, are not meant to be instantiated
    }

    /**
     * Eval boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public static Boolean eval(Object value) {
        if (value instanceof String) {
            value = ((String) value).toLowerCase();
        }
        if (heatFalse.contains(value)) {
            return false;
        } else if (heatTrue.contains(value)) {
            return true;
        } else {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withId(ErrorCodes.INVALID_BOOLEAN).withCategory(ErrorCategory.APPLICATION)
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
            eval(value);
            return true;
        } catch (CoreException ce) {
            LOG.error("Failed to evaluate value as boolean: {}", value, ce);
            return false;
        }
    }
}
