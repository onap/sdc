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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspQuestionnaireDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

public class VspQuestionnaireDaoCassandraImpl implements VspQuestionnaireDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<VspQuestionnaireEntity> mapper =
      noSqlDb.getMappingManager().mapper(VspQuestionnaireEntity.class);
  private static final VspQuestionnaireAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(VspQuestionnaireAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, new VersionableEntityMetadata(
            mapper.getTableMetadata().getName(),
            mapper.getTableMetadata().getPartitionKey().get(0).getName(),
            mapper.getTableMetadata().getPartitionKey().get(1).getName()));
  }

  @Override
  public VspQuestionnaireEntity get(VspQuestionnaireEntity entity) {
    return mapper.get(entity.getId(), versionMapper.toUDT(entity.getVersion()));
  }

  @Override
  public void updateQuestionnaireData(String id, Version version, String questionnaireData) {
    accessor.updateQuestionnaireData(questionnaireData, id, versionMapper.toUDT(version));
  }

  @Accessor
  interface VspQuestionnaireAccessor {

    @Query("update vsp_information set questionnaire_data=? where vsp_id=? and version=?")
    ResultSet updateQuestionnaireData(String questionnaireData, String id, UDTValue version);
  }
}
