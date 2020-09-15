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
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;

import java.util.HashMap;
import java.util.Map;

public class ServiceImportParseLogicTest extends ServiceImportBussinessLogicBaseTestSetup {


    @InjectMocks
    private ServiceImportParseLogic serviceImportParseLogic;

    private ServiceImportParseLogic createTestSubject() {
        return new ServiceImportParseLogic();
    }

    @Test
    public void testGetServiceBusinessLogic() {
        ServiceImportParseLogic testSubject;
        ServiceBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceBusinessLogic();
    }

    @Test
    public void testSetServiceBusinessLogic() {
        ServiceImportParseLogic testSubject;
        ServiceBusinessLogic serviceBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceBusinessLogic(serviceBusinessLogic);
    }

    @Test
    public void testGetCapabilityTypeOperation() {
        ServiceImportParseLogic testSubject;
        ICapabilityTypeOperation result;

        testSubject = createTestSubject();
        result = testSubject.getCapabilityTypeOperation();
    }

    @Test
    public void testSetCapabilityTypeOperation() {
        ServiceImportParseLogic testSubject;
        ICapabilityTypeOperation iCapabilityTypeOperation = null;

        testSubject = createTestSubject();
        testSubject.setCapabilityTypeOperation(iCapabilityTypeOperation);
    }

    private CsarInfo createCsarInfo() {
        Map<String, byte[]> csar = new HashMap<>();
        User user = new User();
        CsarInfo csarInfo = new CsarInfo(user, "csar_UUID", csar, "vfResourceName", "mainTemplateName", "mainTemplateContent", true);
        csarInfo.setVfResourceName("vfResourceName");
        csarInfo.setCsar(csar);
        csarInfo.setCsarUUID("csarUUID");
        csarInfo.setModifier(user);
        csarInfo.setUpdate(true);
        return csarInfo;
    }

}