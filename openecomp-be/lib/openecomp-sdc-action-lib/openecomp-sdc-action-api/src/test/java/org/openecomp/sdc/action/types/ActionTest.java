/*
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.action.types;

import com.google.common.base.Objects;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.action.dao.types.ActionEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ActionTest {

    Action action;

    @Before
    public void setup() throws Exception {

        Map<String, String> supportedcomponents = new HashMap<>();
        supportedcomponents.put("Id","one");
        List supportedcomponentsList = new ArrayList<Map<String, String>>();
        supportedcomponentsList.add(supportedcomponents);

        Map<String, String> supportedmodels = new HashMap<>();
        supportedmodels.put("versionId","one");
        List supportedmodelsList = new ArrayList<Map<String, String>>();
        supportedmodelsList.add(supportedmodels);

        action= new Action();
        action.setActionUuId("actionuuid");
        action.setActionInvariantUuId("actioninvariantuuid");
        action.setName("NAME");
        action.setTimestamp(new Date());
        action.setUser("User");
        action.setStatus(ActionStatus.Available);
        action.setVersion("11.10");
        action.setVendorList(new ArrayList<String>());
        action.setCategoryList(new ArrayList<String>());
        action.setSupportedComponents( (List<Map<String,String>>) supportedcomponentsList);
        action.setSupportedModels( (List<Map<String,String>>) supportedmodelsList);
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
        assertEquals(action.getName(), "NAME");
    }

    @Test
    public void testToEntity() {
        ActionEntity destination = action.toEntity();
        assertNotNull(destination);
        assertEqualsMultipleAssert(action,destination);
    }

    @Test
    public void testEqual()
    {
        assertEquals(true,action.equals(action));
    }

    @Test
    public void testEashcode()
    {
        assertEquals(action.hashCode(), Objects.hashCode(action.getVersion(), action.getName()));
    }

    private void assertEqualsMultipleAssert(Action source, ActionEntity destination) {
        assertEquals(source.getName().toLowerCase(),destination.getName());
        assertEquals(source.getActionUuId().toUpperCase(),destination.getActionUuId());
        assertEquals(source.getActionInvariantUuId().toUpperCase(),destination.getActionInvariantUuId());
        assertEquals(source.getTimestamp(),destination.getTimestamp());
        assertEquals(source.getUser(),destination.getUser());
        assertEquals(source.getStatus().name(),destination.getStatus());
        assertEquals(source.getVersion(),destination.getVersion().toString());
        assertEquals(source.getVendorList(), new ArrayList<String>(destination.getVendorList()));
        assertEquals(source.getCategoryList(),new ArrayList<String>(destination.getCategoryList()));
        assertEquals(source.getSupportedComponents().get(0).values().stream().collect(Collectors.toList()), new ArrayList<String>(destination.getSupportedComponents()));
        assertEquals(source.getSupportedModels().get(0).values().stream().collect(Collectors.toList()),new ArrayList<String>(destination.getSupportedModels()));
        assertEquals(source.getData(),destination.getData());
    }
}
