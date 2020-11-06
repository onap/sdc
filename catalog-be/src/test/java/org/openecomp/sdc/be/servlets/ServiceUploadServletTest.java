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

package org.openecomp.sdc.be.servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import static org.junit.Assert.assertEquals;

class ServiceUploadServletTest {

    @InjectMocks
    ServiceUploadServlet serviceUploadServlet;

    UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    ComponentInstanceBusinessLogic componentInstanceBL =  Mockito.mock(ComponentInstanceBusinessLogic.class);
    ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
    ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);

    @BeforeEach
    void setup() {
        serviceUploadServlet = new ServiceUploadServlet(userBusinessLogic,componentInstanceBL,
                componentsUtils,servletUtils,resourceImportManager);
    }

    @Test
    void testServiceAuthorityTypeEnum(){
        int index = 2;
        ServiceUploadServlet.ServiceAuthorityTypeEnum[] values = ServiceUploadServlet.ServiceAuthorityTypeEnum.values();
        ServiceUploadServlet.ServiceAuthorityTypeEnum value = values[index];
        String name = value.name();
        assertEquals(ServiceUploadServlet.ServiceAuthorityTypeEnum.USER_TYPE_UI.toString(),name);
    }

    @Test
    void testServiceAuthorityTypeEnumFindByUrlPath(){
        String urlPath = "user-servcie-ui-import";
        ServiceUploadServlet.ServiceAuthorityTypeEnum byUrlPath = ServiceUploadServlet.ServiceAuthorityTypeEnum.findByUrlPath(urlPath);
        assertEquals(ServiceUploadServlet.ServiceAuthorityTypeEnum.USER_TYPE_UI,byUrlPath);
    }
}