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

package org.openecomp.sdc.be.model.operations.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fj.data.Either;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.annotation.PostConstruct;
import java.util.Map;

@org.springframework.stereotype.Component("csar-operation")
public class CsarOperation {

    private static final Logger log = Logger.getLogger(CsarOperation.class.getName());

    @javax.annotation.Resource
    private OnboardingClient onboardingClient;

    public static void main(String[] args) {

        CsarOperation csarOperation = new CsarOperation();
        csarOperation.init();

        String csarUuid = "70025CF6081B489CA7B1CBA583D5278D";
        Either<Map<String, byte[]>, StorageOperationStatus> csar = csarOperation.getCsar(csarUuid, null);
        System.out.println(csar.left().value());

    }

    @PostConstruct
    public void init() {

    }

    /**
     * get csar from remote repository
     *
     * @param csarUuid
     * @return
     */
    public Either<Map<String, byte[]>, StorageOperationStatus> getCsar(String csarUuid, User user) {

        Either<Map<String, byte[]>, StorageOperationStatus> result = onboardingClient.getCsar(csarUuid, user.getUserId());

        if (result.isRight()) {
            log.debug("Cannot find csar {}. Staus returned is {}", csarUuid, result.right().value());
        } else {
            Map<String, byte[]> values = result.left().value();
            if (values != null) {
                log.debug("The returned files are {}", values.keySet());
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public Either<String, StorageOperationStatus> getCsarLatestVersion(String csarUuid, User user) {

        Either<String, StorageOperationStatus> result = onboardingClient.getPackages(user.getUserId());

        if (result.isRight()) {
            log.debug("Cannot find version for package with Id {}. Status returned is {}", csarUuid, result.right().value());
        } else {
            String latestVersion = null;
            JsonElement root = new JsonParser().parse(result.left().value());
            JsonArray csarsInfo = root.getAsJsonObject().get("results").getAsJsonArray();
            for (JsonElement csarInfo : csarsInfo) {
                Map<String, String> csarInfoMap = new Gson().fromJson(csarInfo, Map.class);
                if(csarInfoMap.get("packageId").equals(csarUuid)){
                    String curVersion = csarInfoMap.get("version");
                    if(latestVersion == null || isGreater(latestVersion, curVersion)){
                        latestVersion = curVersion;
                    }
                }
            }
            if (latestVersion != null) {
                result = Either.left(latestVersion);
            } else {
                log.debug("The returned packages are {}. Failed to find latest version for package with Id {}. ", result.left().value(), csarUuid);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
            }
        }

        return result;
    }

    private boolean isGreater(String latestVersion, String currentVersion) {
        return Double.parseDouble(latestVersion) < Double.parseDouble(currentVersion);
    }

    public OnboardingClient getOnboardingClient() {
        return onboardingClient;
    }

}
