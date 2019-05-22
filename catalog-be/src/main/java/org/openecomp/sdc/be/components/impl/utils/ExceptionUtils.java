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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl.utils;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component
public class ExceptionUtils {

    private final TitanDao titanDao;

    public ExceptionUtils(TitanDao titanDao) {
        this.titanDao = titanDao;
    }

    public <T> T rollBackAndThrow(ActionStatus actionStatus, String ... params) {
         titanDao.rollback();
         throw new ByActionStatusComponentException(actionStatus, params);
     }

    public <T> T rollBackAndThrow(ResponseFormat responseFormat) {
        titanDao.rollback();
        throw new ByResponseFormatComponentException(responseFormat);
    }

    public <T> T rollBackAndThrow(StorageOperationStatus status, String ... params) {
        titanDao.rollback();
        throw new StorageException(status, params);
    }

    public <T> T rollBackAndThrow(TitanOperationStatus status, String ... params) {
        titanDao.rollback();
        throw new StorageException(status, params);
    }




}
