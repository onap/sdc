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

package org.openecomp.sdc.notification.services.impl;

import org.openecomp.sdc.notification.dao.SubscribersDao;
import org.openecomp.sdc.notification.services.SubscriptionService;

import java.util.Set;

public class SubscriptionServiceImpl implements SubscriptionService {

    private SubscribersDao subscribersDao;

    public SubscriptionServiceImpl(SubscribersDao subscribersDao) {
        this.subscribersDao = subscribersDao;
    }

    @Override
    public void subscribe(String ownerId, String entityId) {
        subscribersDao.subscribe(ownerId, entityId);
    }

    @Override
    public void unsubscribe(String ownerId, String entityId) {
        subscribersDao.unsubscribe(ownerId, entityId);
    }

    @Override
    public Set<String> getSubscribers(String entityId) {
        return subscribersDao.getSubscribers(entityId);
    }
}