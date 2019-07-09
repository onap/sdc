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

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.Service;

public class ServiceBuilder extends ComponentBuilder<Service, ServiceBuilder> {

    private Service service;

    public ServiceBuilder(Service component) {
        super(component);
    }

    public ServiceBuilder() {
        super();
    }

    @Override
    protected Service component() {
        service = new Service();
        return service;
    }

    @Override
    protected ComponentBuilder<Service, ServiceBuilder> self() {
        return this;
    }


}
