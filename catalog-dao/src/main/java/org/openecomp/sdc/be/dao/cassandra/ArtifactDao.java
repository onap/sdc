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
package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;

import org.openecomp.sdc.be.resources.data.DAOArtifactData;

import com.datastax.oss.driver.api.core.cql.ResultSet;

/**
 * Created by chaya on 7/5/2017.
 */

@Dao
public interface ArtifactDao {

       // Save or update
    @Insert
    void save(DAOArtifactData artifact);

    // Get by primary key
    @Select
    DAOArtifactData findById(String id);

    // Delete by entity
    @Delete(entityClass = DAOArtifactData.class)
    void delete(DAOArtifactData artifact);

    @Query("SELECT COUNT(*) FROM sdcartifact.resources WHERE id = :uniqueId")
    ResultSet getNumOfArtifactsById(String uniqueId);
}
