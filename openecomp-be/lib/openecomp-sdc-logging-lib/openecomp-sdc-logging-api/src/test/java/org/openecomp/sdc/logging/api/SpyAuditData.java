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

import java.util.HashSet;
import java.util.Set;

public class SpyAuditData implements  AuditData{
    private final Set<String> calledMethods = new HashSet<>();

    @Override
    public long getStartTime() {
        calledMethods.add("getStartTime");
        return 0;
    }

    @Override
    public long getEndTime() {
        calledMethods.add("getEndTime");
        return 0;
    }

    @Override
    public AuditData.StatusCode getStatusCode() {
        calledMethods.add("getEndTime");
        return null;
    }

    @Override
    public String getResponseCode() {
        calledMethods.add("getResponseCode");
        return null;
    }

    @Override
    public String getResponseDescription() {
        calledMethods.add("getResponseDescription");
        return null;
    }

    @Override
    public String getClientIpAddress() {
        calledMethods.add("getClientIpAddress");
        return null;
    }

    public boolean wasCalled(String method) {
        return calledMethods.contains(method);
    }
}
