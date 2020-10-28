/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nokia Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.validation.impl.validators;

import org.junit.jupiter.api.Test;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalContextUtilTest {
    private static final String TEST_MANIFEST_PATH = "/org/openecomp/validation/validators/global_context_util/";

    @Test
    void shouldReturnOnlyFilesWithPmDictionaryType() {
        // given
        GlobalValidationContext globalContext = new ValidationTestUtil().createGlobalContextFromPath(TEST_MANIFEST_PATH);

        // when
        Set<String> pmDictionaryFiles = GlobalContextUtil.findPmDictionaryFiles(globalContext);

        // then
        assertEquals(1, pmDictionaryFiles.size());
    }

}
