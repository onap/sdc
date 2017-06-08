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

package org.openecomp.core.zusammen.plugin.main;


import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.plugin.statestore.cassandra.StateStoreImpl;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.state.types.StateElement;
import org.openecomp.core.zusammen.plugin.dao.ElementRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;

public class CassandraStateStorePluginImpl extends StateStoreImpl {

  @Override
  public Response<Void> createElement(SessionContext context, StateElement element) {
    ElementEntity elementEntity = new ElementEntity(element.getId());
    elementEntity.setNamespace(element.getNamespace());

    ElementRepositoryFactory.getInstance().createInterface(context)
        .createNamespace(context,
            new ElementEntityContext(getSpaceName(context, element.getSpace()),
                element.getItemId(), element.getVersionId()),
            elementEntity);
    // create element is done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateElement(SessionContext context, StateElement element) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteElement(SessionContext context, StateElement element) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

}
