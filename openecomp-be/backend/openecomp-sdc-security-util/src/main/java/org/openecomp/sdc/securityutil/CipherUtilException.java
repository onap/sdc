/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.securityutil;


/* ============LICENSE_START==========================================
         * ONAP Portal SDK
         * ===================================================================
         * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
         * ===================================================================
         *
         * Unless otherwise specified, all software contained herein is licensed
         * under the Apache License, Version 2.0 (the "License");
         * you may not use this software except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *             http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         *
         * Unless otherwise specified, all documentation contained herein is licensed
         * under the Creative Commons License, Attribution 4.0 Intl. (the "License");
         * you may not use this documentation except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *             https://creativecommons.org/licenses/by/4.0/
         *
         * Unless required by applicable law or agreed to in writing, documentation
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         *
         * ============LICENSE_END============================================
         *
         *
         */


public class CipherUtilException extends Exception {

    private static final long serialVersionUID = -4163367786939629691L;

    public CipherUtilException() {
        super();
    }

    public CipherUtilException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CipherUtilException(String message, Throwable cause) {
        super(message, cause);
    }

    public CipherUtilException(String message) {
        super(message);
    }

    public CipherUtilException(Throwable cause) {
        super(cause);
    }

}
