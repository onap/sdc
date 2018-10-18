/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.action.types;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.action.dao.types.ActionEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ActionTest {

    Action action;

    @Before
    public void setup() throws Exception {



        action= new Action();
        action.setActionUuId("actionuuid");                         // Should be displayed as upper case.
        action.setActionInvariantUuId("actioninvariantuuid");       // should be converted to uppercase.
        action.setName("NAME");                                     //should be converted to lower case.
        action.setTimestamp(new Date());
        action.setUser("User");
        action.setStatus(ActionStatus.Available);
        action.setVersion("version");
        action.setVendorList(new ArrayList<String>());
        action.setCategoryList(new ArrayList<String>());
        action.setSupportedComponents(new ArrayList<Map<String,String>>());
        action.setSupportedModels(new ArrayList<Map<String,String>>());
        action.setData("data");
    }

    @Test
    public void testActionUuid() {
        assertEquals(action.getActionUuId(), "actionuuid");
    }

    @Test
    public void testActionInvariantUuid() {
        assertEquals(action.getActionInvariantUuId(), "actioninvariantuuid");
    }

    @Test
    public void testData() {
        assertEquals(action.getData(), "data");
    }

    @Test
    public void testName() {
        assertEquals(action.getName(), "Name");
    }




    @Test
    public void testToEntiry() {
        ActionEntity destination = action.toEntity();
        assertNotNull(destination);
        assertEqualsMultipleAssert(action,destination);
    }



    @Test
    public void testequal()
    {

        assertEquals(true,action.equals(action));
    }

    @Test
    public void testhashcode()
    {
        assertEquals(action.hashCode(),31 * action.getVersion().hashCode() + action.getName().hashCode());
    }

    private void assertEqualsMultipleAssert(Action source, ActionEntity destination) {
        assertEquals(source.getName(),destination.getName());
        assertEquals(source.getActionUuId(),destination.getActionUuId());
        assertEquals(source.getActionInvariantUuId(),destination.getActionInvariantUuId());
        assertEquals(source.getTimestamp(),destination.getTimestamp());
        assertEquals(source.getUser(),destination.getUser());
        assertEquals(source.getStatus(),destination.getStatus());
        assertEquals(source.getVersion(),destination.getVersion());
        assertEquals(source.getVendorList(),destination.getVendorList());
        assertEquals(source.getCategoryList(),destination.getVendorList());
        assertEquals(source.getSupportedComponents(),destination.getSupportedComponents());
        assertEquals(source.getSupportedModels(),destination.getSupportedModels());
        assertEquals(source.getData(),destination.getData());
    }
}