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

package org.onap.sdc.backend.ci.tests.config;

import org.onap.sdc.backend.ci.tests.datatypes.UserCredentials;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

public class UserCredentialsFromFile {

    private static final String CREDENTIALS_FILE = "src/test/resources/ci/conf/credentials.yaml";
    private static Map<String, UserCredentials> credentials;
    private static Yaml yaml = new Yaml();

    private static final UserCredentialsFromFile instance = new UserCredentialsFromFile();

    public static UserCredentialsFromFile getInstance() {
        return instance;
    }

    private UserCredentialsFromFile() {

        credentials = null;

        File credentialsFile = new File(System.getProperty("user.dir")+ System.getProperty("file.separator")+CREDENTIALS_FILE);
        if (credentialsFile.exists()) {
            try {
                credentials = (Map<String, UserCredentials>) FileHandling.parseYamlFile(credentialsFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public UserCredentials getUserCredentialsByRole(String userRole) {
        Map<String, String> credentialsMap = (Map<String, String>) credentials.get(userRole);
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserId(credentialsMap.get("username"));
        userCredentials.setFirstName(credentialsMap.get("firstname"));
        userCredentials.setLastName(credentialsMap.get("lastname"));
        userCredentials.setPassword(credentialsMap.get("password"));
        return userCredentials;
    }

}
