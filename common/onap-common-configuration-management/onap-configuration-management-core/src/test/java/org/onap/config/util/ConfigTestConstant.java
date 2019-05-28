/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.config.util;

public class ConfigTestConstant {

    public static final String ARTIFACT = "artifact";
    public static final String ARTIFACT_NAME_MAXLENGTH = ARTIFACT + ".name.maxlength";
    public static final String ARTIFACT_MAXSIZE = ARTIFACT + ".maxsize";
    public static final String ARTIFACT_EXT = ARTIFACT + ".extension";
    public static final String ARTIFACT_ENC = ARTIFACT + ".supportedEncryption";
    public static final String ARTIFACT_NAME_UPPER = ARTIFACT + ".name.allowedChar";
    public static final String ARTIFACT_NAME_LOWER = ARTIFACT + ".name.allowedchar";
    public static final String ARTIFACT_STATUS = ARTIFACT + ".status";
    public static final String ARTIFACT_LOC = ARTIFACT + ".persistLocation";
    public static final String ARTIFACT_JSON_SCHEMA = ARTIFACT + ".jsonSchema";
    public static final String ARTIFACT_XML_SCHEMA = ARTIFACT + ".xmlSchema";
    public static final String ARTIFACT_CONSUMER_APPC = ARTIFACT + ".consumerAPPC";
    public static final String ARTIFACT_CONSUMER = ARTIFACT + ".consumer";
    public static final String ARTIFACT_MANDATORY_NAME = ARTIFACT + ".mandatory.name";
    public static final String ARTIFACT_NAME_MINLENGTH = ARTIFACT + ".name.minlength";
    public static final String ARTIFACT_ENCODED = ARTIFACT + ".encoded";

    public static final String PATH = "PATH";

    private ConfigTestConstant() {
        // prevent instantiation
    }

}
