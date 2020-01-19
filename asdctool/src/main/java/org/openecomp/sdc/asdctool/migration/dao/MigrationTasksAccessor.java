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

package org.openecomp.sdc.asdctool.migration.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface MigrationTasksAccessor {

    @Query("SELECT minor_version FROM sdcrepository.migrationTasks WHERE major_version = :majorVersion order by minor_version desc limit 1")
    ResultSet getLatestMinorVersion(@Param("majorVersion") Long majorVersion);

    @Query("SELECT major_version FROM sdcrepository.migrationTasks")
    ResultSet getLatestMajorVersion();

    @Query("DELETE FROM sdcrepository.migrationTasks WHERE major_version = :majorVersion")
    void deleteTasksForMajorVersion(@Param("majorVersion") Long majorVersion);

}
