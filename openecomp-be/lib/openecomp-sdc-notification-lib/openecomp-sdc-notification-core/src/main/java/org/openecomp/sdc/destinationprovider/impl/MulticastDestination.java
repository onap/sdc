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
package org.openecomp.sdc.destinationprovider.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.openecomp.sdc.destinationprovider.DestinationProvider;
import org.openecomp.sdc.notification.services.SubscriptionService;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public class MulticastDestination implements DestinationProvider {

    private String entityId;
    private SubscriptionService subscriptionService;
    private String[] excludedSubscribers;

    public MulticastDestination(String entityId, SubscriptionService subscriptionService, String... excludedSubscribers) {
        this.entityId = entityId;
        this.excludedSubscribers = excludedSubscribers;
        this.subscriptionService = subscriptionService;
    }

    public List<String> getSubscribers() {
        ArrayList<String> subscribers = new ArrayList<>(subscriptionService.getSubscribers(entityId));
        if (ArrayUtils.isNotEmpty(excludedSubscribers)) {
            subscribers.removeAll(Arrays.asList(excludedSubscribers));
        }
        return Collections.unmodifiableList(subscribers);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
