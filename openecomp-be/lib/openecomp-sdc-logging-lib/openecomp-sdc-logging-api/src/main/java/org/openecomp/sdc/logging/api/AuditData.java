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

package org.openecomp.sdc.logging.api;

/**
 * @author KATYR
 * @since February 15, 2018
 */

public interface AuditData {

    long getStartTime();

    long getEndTime();

    StatusCode getStatusCode();

    String getResponseCode();

    String getResponseDescription();

    String getClientIpAddress();

    public enum StatusCode {
        COMPLETE("COMPLETE"), ERROR("ERROR");
        private String value;

        private StatusCode(String value){
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }
}
