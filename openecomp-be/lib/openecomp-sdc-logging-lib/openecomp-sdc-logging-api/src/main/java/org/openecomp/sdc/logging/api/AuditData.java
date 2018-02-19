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
 * This interface defines part of the Audit log application is responsible to provide.
 * Fields list is according to ONAP application logging guidelines
 * (https://wiki.onap.org/download/attachments/1015849/ONAP%20application%20logging%20guidelines.pdf?api=v2)
 * StartTime -> BeginTimestamp (Date-time that processing for the activities begins)
 * EndTime-> EndTimestamp (Date-time that processing for the activities being logged ends)
 * StatusCode -> StatusCode (indicate high level success or failure of the operation activities that is invoked)
 * ResponseCode -> ResponseCode(application-specific response code returned by the operation activities)
 * ResponseDescription - > ResponseDescription (human readable description of the response code)
 * ClientIpAddress -> ClientIpAddress (Requesting remote client application’s IP address)
 */

public interface AuditData {

    enum StatusCode {
        COMPLETE, ERROR
    }

    long getStartTime();

    long getEndTime();

    StatusCode getStatusCode();

    String getResponseCode();

    String getResponseDescription();

    String getClientIpAddress();
}
