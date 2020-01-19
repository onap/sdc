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

package org.openecomp.sdc.be.components.distribution.engine;

import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.getEnumValueByFieldValue;

public interface IDmaapNotificationData {
    String getOperationalEnvironmentId();

    OperationaEnvironmentTypeEnum getOperationalEnvironmentType();

    DmaapActionEnum getAction();

    enum DmaapActionEnum {
        DELETE("Delete"),
        CREATE("Create"),
        UPDATE("Update"),
        UNKONW("UNKONW")

        ;
        private String actionName;

        private DmaapActionEnum(String actionName) {
            this.actionName = actionName;
        }

        public String getActionName() {
            return actionName;
        }

        public static DmaapActionEnum findByName(String actionName){
            return getEnumValueByFieldValue(actionName, DmaapActionEnum.values(), DmaapActionEnum::getActionName, UNKONW, false);
        }
    }

    enum OperationaEnvironmentTypeEnum {
        ECOMP("ECOMP"),
        UNKONW("UNKONW")
        ;
        private String eventTypenName;

        private OperationaEnvironmentTypeEnum(String eventTypenName) {
            this.eventTypenName = eventTypenName;
        }

        public String getEventTypenName() {
            return eventTypenName;
        }

        public static OperationaEnvironmentTypeEnum findByName(String operationalEnvironmentTypeName){
            return getEnumValueByFieldValue(operationalEnvironmentTypeName, OperationaEnvironmentTypeEnum.values(), OperationaEnvironmentTypeEnum::getEventTypenName, UNKONW, false);
        }
    }
}
