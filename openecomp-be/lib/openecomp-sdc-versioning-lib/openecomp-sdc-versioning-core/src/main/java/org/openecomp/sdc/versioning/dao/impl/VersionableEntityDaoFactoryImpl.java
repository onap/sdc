/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.versioning.dao.impl;

import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.versioning.dao.VersionableEntityDao;
import org.openecomp.sdc.versioning.dao.VersionableEntityDaoFactory;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

public class VersionableEntityDaoFactoryImpl extends VersionableEntityDaoFactory {
  private static VersionableEntityDao CASSANDRA_INSTANCE = new VersionableEntityDaoCassandraImpl();
  private static VersionableEntityDao ZUSAMMEN_INSTANCE =
      new VersionableEntityDaoZusammenImpl(ZusammenAdaptorFactory.getInstance().createInterface());

  @Override
  public VersionableEntityDao createInterface() {
    throw new UnsupportedOperationException
        ("Please use createInterface api with VersionableEntityStoreType argument.");
  }

  @Override
  public VersionableEntityDao createInterface(VersionableEntityStoreType storeType) {
    switch (storeType) {
      case Cassandra:
        return CASSANDRA_INSTANCE;
      case Zusammen:
        return ZUSAMMEN_INSTANCE;
      default:
        throw new IllegalArgumentException("Unssported state store");
    }
  }
}
