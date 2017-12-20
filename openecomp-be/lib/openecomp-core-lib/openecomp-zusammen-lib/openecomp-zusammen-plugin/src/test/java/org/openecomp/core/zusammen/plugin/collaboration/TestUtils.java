/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.Relation;

import java.util.Arrays;

public class TestUtils {

  public static SessionContext createSessionContext(UserInfo user, String tenant) {
    SessionContext context = new SessionContext();
    context.setUser(user);
    context.setTenant(tenant);
    return context;
  }

  public static ElementContext createElementContext(Id itemId, Id versionId) {
    ElementContext elementContext = new ElementContext();
    elementContext.setItemId(itemId);
    elementContext.setVersionId(versionId);
    return elementContext;
  }

  public static Info createInfo(String value) {
    Info info = new Info();
    info.setName(value);
    info.addProperty("Name", "name_" + value);
    info.addProperty("Desc", "desc_" + value);
    return info;
  }

  public static ItemVersion createItemVersion(Id id, Id baseId, String name, boolean dirty) {
    ItemVersion version = new ItemVersion();
    version.setId(id);
    version.setBaseId(baseId);
    ItemVersionData data = new ItemVersionData();
    data.setInfo(TestUtils.createInfo(name));
    data.setRelations(Arrays.asList(new Relation(), new Relation()));
    version.setData(data);
    return version;
  }
}
