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
package org.openecomp.sdc.action.dao.impl;

import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.action.dao.ActionArtifactDao;
import org.openecomp.sdc.action.dao.ActionArtifactDaoFactory;

import com.datastax.oss.driver.api.core.CqlSession;

public class ActionArtifactDaoFactoryImpl extends ActionArtifactDaoFactory {

    private static ActionArtifactDao INSTANCE;

    static{
        CqlSession session = NoSqlDbFactory.getInstance().createInterface().getSession();
        INSTANCE = new ActionArtifactDaoImpl(session);
    }

    @Override
    public ActionArtifactDao createInterface() {
        return INSTANCE;
    }
}
