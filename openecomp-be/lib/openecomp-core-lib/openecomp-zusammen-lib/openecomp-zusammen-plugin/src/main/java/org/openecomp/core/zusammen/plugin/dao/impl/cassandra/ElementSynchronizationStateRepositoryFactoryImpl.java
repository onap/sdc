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

package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.dao.ElementSynchronizationStateRepository;
import org.openecomp.core.zusammen.plugin.dao.ElementSynchronizationStateRepositoryFactory;

public class ElementSynchronizationStateRepositoryFactoryImpl
    extends ElementSynchronizationStateRepositoryFactory {

  private static final ElementSynchronizationStateRepository INSTANCE =
      new ElementSynchronizationStateRepositoryImpl();

  @Override
  public ElementSynchronizationStateRepository createInterface(SessionContext context) {
    return INSTANCE;
  }
}
