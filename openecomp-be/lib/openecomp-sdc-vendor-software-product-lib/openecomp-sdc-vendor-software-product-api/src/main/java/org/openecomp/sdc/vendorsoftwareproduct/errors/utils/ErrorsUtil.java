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

package org.openecomp.sdc.vendorsoftwareproduct.errors.utils;

import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Talio on 11/30/2016.
 */
public class ErrorsUtil {

    public static void addStructureErrorToErrorMap(String fileName, ErrorMessage errorMessage, Map<String, List<ErrorMessage>> errors) {
        List<ErrorMessage> errorList = errors.get(fileName);
        if (errorList == null) {
            errorList = new ArrayList<>();
            errors.put(fileName, errorList);
        }
        errorList.add(errorMessage);
    }
}
