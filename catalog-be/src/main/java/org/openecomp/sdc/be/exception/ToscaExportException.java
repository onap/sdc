/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.exception;

import lombok.Getter;
import org.openecomp.sdc.be.tosca.ToscaError;

public class ToscaExportException extends Exception {

    @Getter
    private final ToscaError toscaError;

    public ToscaExportException(String message) {
        super(message);
        toscaError = null;
    }

    public ToscaExportException(final String message, final ToscaError toscaError) {
        super(message);
        this.toscaError = toscaError;
    }
}
