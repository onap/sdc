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

package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.model.types.ServiceArtifactEntity;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceArtifactCassandraLoader {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final VspServiceArtifactAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(
          VspServiceArtifactAccessor.class);


  public Collection<ServiceArtifact> list() {


    List<ServiceArtifactEntity> entityList = accessor.listAll().all();

    return entityList.stream().map(entity -> entity.getServiceArtifact())
        .collect(Collectors.toList());
  }

  @Accessor
  interface VspServiceArtifactAccessor {

    @Query("SELECT * FROM vsp_service_artifact")
    Result<ServiceArtifactEntity> listAll();


  }

}
