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
package org.openecomp.sdc.common.api;

import java.util.List;
import org.junit.Test;

public class ToscaNodeTypeInfoTest {

    private ToscaNodeTypeInfo createTestSubject() {
        return new ToscaNodeTypeInfo();
    }

    @Test
    public void testGetTemplateName() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getTemplateName();
    }

    @Test
    public void testSetTemplateName() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String templateName = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setTemplateName(templateName);
    }

    @Test
    public void testGetNodeName() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getNodeName();
    }

    @Test
    public void testSetNodeName() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String nodeName = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setNodeName(nodeName);
    }

    @Test
    public void testGetTemplateVersion() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getTemplateVersion();
    }

    @Test
    public void testSetTemplateVersion() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String templateVersion = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setTemplateVersion(templateVersion);
    }

    @Test
    public void testGetInterfaces() throws Exception {
        ToscaNodeTypeInfo testSubject;
        List<ToscaNodeTypeInterface> result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getInterfaces();
    }

    @Test
    public void testSetInterfaces() throws Exception {
        ToscaNodeTypeInfo testSubject;
        List<ToscaNodeTypeInterface> interfaces = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setInterfaces(interfaces);
    }

    @Test
    public void testGetIconPath() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getIconPath();
    }

    @Test
    public void testSetIconPath() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String iconPath = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setIconPath(iconPath);
    }

    @Test
    public void testToString() throws Exception {
        ToscaNodeTypeInfo testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.toString();
    }
}
