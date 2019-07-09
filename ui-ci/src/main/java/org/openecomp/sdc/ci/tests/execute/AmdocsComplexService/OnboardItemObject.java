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

package org.openecomp.sdc.ci.tests.execute.AmdocsComplexService;


public class OnboardItemObject extends OnboardItemObjectReqDetails {

    private String itemId;
    private String name;
    private String baseId;
    private String status;

    public OnboardItemObject(){super();}

    public OnboardItemObject(String itemId, String name, String baseId, String status) {
        this.itemId = itemId;
        this.name = name;
        this.baseId = baseId;
        this.status = status;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String itemId) {
        this.name = name;
    }

    public String getBaseId() {
        return baseId;
    }

    public void setBaseId(String baseId) {
        this.baseId = baseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OnboardItemObject{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", baseId='" + baseId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
