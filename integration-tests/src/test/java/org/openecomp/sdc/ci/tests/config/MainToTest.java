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

package org.openecomp.sdc.ci.tests.config;


import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class MainToTest {

    private static final String CREDENTIALS_FILE = "credentials.yaml";

//    public static void main(String[] args) throws Exception {
//        System.out.println("Hello World!"); // Display the string.
//        System.out.println("user.dir: " + System.getProperty("user.dir"));
//        System.out.println(UserRoleEnum.DESIGNER.getFirstName());
//        String file = readFile();
//        convertToJson(file);
//        Either<Service, RestResponse> createDefaultService1e = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
//
//
//
//    }

    private static String convertToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }

    private static String readFile() {

    File credentialsFileLocal = new File(FileHandling.getSdcVnfsPath() + File.separator + "conf"
            + File.separator + CREDENTIALS_FILE);

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(credentialsFileLocal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                String mystr = IOUtils.toString(inputStream, Charset.forName("UTF-8"));
                inputStream.close();
                return mystr;
            } catch(IOException e) {
            }
        }

        return null;
    }


}
