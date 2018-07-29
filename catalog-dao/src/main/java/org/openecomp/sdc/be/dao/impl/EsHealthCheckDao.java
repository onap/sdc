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

package org.openecomp.sdc.be.dao.impl;

import org.openecomp.sdc.be.dao.api.IEsHealthCheckDao;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("esHealthCheckDao")
public class EsHealthCheckDao implements IEsHealthCheckDao {

	private static Logger logger = Logger.getLogger(EsHealthCheckDao.class.getName());

	@Resource(name = "elasticsearch-client")
	private ElasticSearchClient esClient;

	@Resource
	private ESCatalogDAO esCatalogDao;

	public EsHealthCheckDao() {
	}

	public HealthCheckStatus getClusterHealthStatus() {
		return this.esCatalogDao.getHealth();
	}

}
