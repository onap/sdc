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

package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;

import java.util.List;

@Accessor
public interface ComponentCacheAccessor {

	@Query("SELECT * FROM sdccomponent.componentcache WHERE id IN :ids ALLOW FILTERING")
	Result<ComponentCacheData> getComponents(@Param("ids") List<String> ids);

	@Query("SELECT * FROM sdccomponent.componentcache WHERE id = :id ALLOW FILTERING")
	Result<ComponentCacheData> getComponent(@Param("id") String id);

	@Query("SELECT id,modification_time,type FROM sdccomponent.componentcache ALLOW FILTERING")
	Result<ComponentCacheData> getAllComponentIdTimeAndType();

	// @Query("SELECT * FROM sdcartifact.resources LIMIT 2000")
	// Result<DAOArtifactData> getListOfResources();

	// Result<DAOArtifactData> getListOfResources(List<String> dids);
}
