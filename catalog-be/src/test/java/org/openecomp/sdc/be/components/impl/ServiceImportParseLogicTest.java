/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.be.components.impl;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.openecomp.sdc.be.components.csar.CsarInfo;
import org.openecomp.sdc.be.model.User;

import java.util.HashMap;
import java.util.Map;

public class ServiceImportParseLogicTest {

    private final static String USER_ID = "jh0003";
    private final static String csar_UUID = "csarUUID";
    private final static String VF_RESOURCE_NAME = "vfResourceName";
    private final static String ORIGIN_COMPONENT_VERSION = "1.0";
    private final static String MAIN_TEMPLATE_NAME = "mainTemplateName";
    private final static String MAIN_TEMPLATE_CONTENT = "mainTemplateContent";
    private final static String COMPONENT_INSTANCE_ID = "componentInstanceId";
    private final static String COMPONENT_INSTANCE_NAME = "componentInstanceName";
    private final static String FROM_INSTANCE_ID = "fromInstanceId";
    private final static String RELATION_ID = "relationId";
    private final static String CAPABILITY_OWNER_ID = "capabilityOwnerId";
    private final static String CAPABILITY_UID = "capabilityUid";
    private final static String CAPABILITY_NAME = "capabilityName";
    private final static String REQUIREMENT_OWNER_ID = "requirementOwnerId";
    private final static String REQUIREMENT_UID = "requirementUid";
    private final static String REQUIREMENT_NAME = "requirementName";
    private final static String RELATIONSHIP_TYPE = "relationshipType";
    private final static String ARTIFACT_1 = "cloudtech_k8s_charts.zip";
    private final static String ARTIFACT_2 = "cloudtech_azure_day0.zip";
    private final static String ARTIFACT_3 = "cloudtech_aws_configtemplate.zip";
    private final static String ARTIFACT_4 = "k8s_charts.zip";
    private final static String ARTIFACT_5 = "cloudtech_openstack_configtemplate.zip";
    private final static String PROP_NAME = "propName";
    private final static String NON_EXIST_NAME = "nonExistName";
    private final static String INPUT_ID = "inputId";
    private final static String ICON_NAME = "icon";

    @InjectMocks
    private ServiceImportParseLogic serviceImportParseLogic;

    @Test
    public void testBuildNodeTypeYaml(){

        Map<String, Object> mapToConvert = new HashMap<>();
        String nodeResourceType = "VFC";
    }

    private CsarInfo createCsarInfo(){
        Map<String, byte[]> csar = new HashMap<>();
        User user = new User();
        CsarInfo csarInfo = new CsarInfo(user,"csar_UUID",csar,"vfResourceName","mainTemplateName","mainTemplateContent",true);
        csarInfo.setVfResourceName("vfResourceName");
        csarInfo.setCsar(csar);
        csarInfo.setCsarUUID("csarUUID");
        csarInfo.setModifier(user);
        csarInfo.setUpdate(true);
        return csarInfo;
    }

}