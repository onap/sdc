/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.common.exception;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCode.ErrorCodeBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataValidatorFilterExceptionSupplier {

    private static final String NOT_PERMITTED_SPECIAL_CHARS = "NOT_PERMITTED_SPECIAL_CHARS";

    public static Supplier<CoreException> notAllowedSpecialCharsError() {
        final String errorMsg = "Error: Special characters not allowed.";
        final ErrorCode errorCode = new ErrorCodeBuilder().withId(NOT_PERMITTED_SPECIAL_CHARS).withMessage(errorMsg).build();
        return () -> new CoreException(errorCode);
    }

}
