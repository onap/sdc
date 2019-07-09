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

package org.openecomp.sdc.be.components.path.beans;

import fj.data.Either;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@org.springframework.stereotype.Component("tosca-operation-facade")
public class ForwardingPathToscaOperationFacade extends ToscaOperationFacade {
    protected static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";
    @Override
    public Either<Resource, StorageOperationStatus> getLatestCertifiedNodeTypeByToscaResourceName(String toscaResourceName) {

        return Either.left(setupGenericServiceMock());
    }

    protected Resource setupGenericServiceMock(){
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }

    @Override
    public <T extends Component> Either<T, StorageOperationStatus> getLatestByName(String resourceName) {
        if(resourceName.equals(ForwardingPathUtils.FORWARDING_PATH_NODE_NAME) || resourceName.equals(ForwardingPathUtils.FORWARDER_CAPABILITY)){
            Resource component = new Resource();
            component.setToscaResourceName(GENERIC_SERVICE_NAME);
            return Either.left((T)component);
        }
        return super.getLatestByName(resourceName);
    }

    @Override
    public <T extends Component> Either<T, StorageOperationStatus> getLatestByToscaResourceName(String toscaResourceName) {
        if(toscaResourceName.equals(ForwardingPathUtils.FORWARDING_PATH_NODE_NAME) || toscaResourceName.equals(ForwardingPathUtils.FORWARDER_CAPABILITY)){
            Resource component = new Resource();
            component.setToscaResourceName(GENERIC_SERVICE_NAME);
            return Either.left((T)component);
        }
        return super.getLatestByToscaResourceName(toscaResourceName);
    }
}
