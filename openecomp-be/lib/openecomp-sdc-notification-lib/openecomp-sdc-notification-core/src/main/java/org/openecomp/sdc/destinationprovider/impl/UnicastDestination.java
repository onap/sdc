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

import java.util.Collections;
import java.util.List;
import org.openecomp.sdc.destinationprovider.DestinationProvider;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public class UnicastDestination implements DestinationProvider {

    private String originatorId;

    public UnicastDestination(String originatorId) {
        this.originatorId = originatorId;
    }

    public List<String> getSubscribers() {
        return Collections.unmodifiableList(Collections.singletonList(originatorId));
    }
}
