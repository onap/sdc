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

package org.openecomp.sdc.ci.tests.utils;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaAnnotationsTypesDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaParameterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.junit.Assert.assertNotNull;
import static org.openecomp.sdc.ci.tests.utils.CsarParserUtils.downloadComponentCsar;
import static org.openecomp.sdc.ci.tests.utils.ToscaParserUtils.getDataFromZipFileByBytes;
import static org.testng.AssertJUnit.fail;

public class ToscaTypesDefinitionUtils {

    private ToscaTypesDefinitionUtils() {
    }

    private static final String ANNOTATIONS_TYPE_PATH = ToscaParameterConstants.TOSCA_DEFINITION_PATH + "/annotations.yml";
    private static Logger log = LoggerFactory.getLogger(ToscaTypesDefinitionUtils.class);

    public static ToscaAnnotationsTypesDefinition getToscaAnnotationsFromCsar(Component csarOwner, User user) throws Exception {
        byte[] decodeBase64 = downloadComponentCsar(csarOwner, user);
        String dataFromZipFileByBytes = getDataFromZipFileByBytes(ANNOTATIONS_TYPE_PATH, decodeBase64);
        assertNotNull(dataFromZipFileByBytes);
        return parseToscaAnnotationsYml(dataFromZipFileByBytes);
    }

    private static ToscaAnnotationsTypesDefinition parseToscaAnnotationsYml(String payload){
        Constructor constructor = initToscaAnnotationDefObject();
        return (ToscaAnnotationsTypesDefinition) parseToscaYamlPayload(payload, constructor);
    }

    private static Object parseToscaYamlPayload(String payload, Constructor constructor) {
        Yaml yaml = new Yaml(constructor);
        try {
            return yaml.load(payload);
        } catch (Exception e) {
            log.debug("Failed to parse tosca yaml file", e);
            e.printStackTrace();
            fail("Failed to parse tosca yaml file");
        }
        return null;
    }

    private static Constructor initToscaAnnotationDefObject() {
        Constructor toscaStructure = new Constructor(ToscaAnnotationsTypesDefinition.class);
        toscaStructure.addTypeDescription(ToscaAnnotationsTypesDefinition.getTypeDescription());
        return toscaStructure;
    }
}
