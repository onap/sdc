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

package org.openecomp.sdc.be.components.path;

import fj.data.Either;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("forwardingPathValidator")
public class ForwardingPathValidator {

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    private static final Logger logger = Logger.getLogger(ForwardingPathValidator.class);
    private static final int  PATH_NAME_LENGTH = 200;
    private static final int  PROTOCOL_LENGTH = 200;
    private static final int  DESTINATION_PORT_LENGTH = 200;

    public void validateForwardingPaths(Collection<ForwardingPathDataDefinition> paths,
                                        String serviceId, boolean isUpdate) {
        for (ForwardingPathDataDefinition path : paths) {
            validateForwardingPath(path, serviceId, isUpdate);
        }
    }

    private void validateForwardingPath(ForwardingPathDataDefinition path, String serviceId, boolean isUpdate) {
        ResponseFormatManager responseFormatManager = getResponseFormatManager();
        validateName(path, responseFormatManager, serviceId, isUpdate);
        validateProtocol(path);
        validateDestinationPortNumber(path);
    }

    private void validateDestinationPortNumber(ForwardingPathDataDefinition dataDefinition) {
        if (dataDefinition.getDestinationPortNumber() != null &&
            dataDefinition.getDestinationPortNumber().length() > DESTINATION_PORT_LENGTH ) {
            logger.debug("Forwarding path destination port {} too long, , maximum allowed 200 characters ",
                    dataDefinition.getDestinationPortNumber());
            throw new ByActionStatusComponentException(ActionStatus
                    .FORWARDING_PATH_DESTINATION_PORT_MAXIMUM_LENGTH, dataDefinition.getDestinationPortNumber());
        }
    }

    private void validateProtocol(ForwardingPathDataDefinition dataDefinition) {
        if (dataDefinition.getProtocol() != null && dataDefinition.getProtocol().length() > PROTOCOL_LENGTH) {
            logger.debug("Forwarding path protocol {} too long, , maximum allowed 200 characters ", dataDefinition.getProtocol());
            throw new ByActionStatusComponentException(ActionStatus.FORWARDING_PATH_PROTOCOL_MAXIMUM_LENGTH, dataDefinition.getProtocol());
        }
    }

    private void validateName(ForwardingPathDataDefinition dataDefinition,
                                                         ResponseFormatManager responseFormatManager,
                                                         String serviceId, boolean isUpdate) {
        String pathName = dataDefinition.getName();
        validatePathNameIfEmpty(responseFormatManager, pathName);

        validatePathNameLength(responseFormatManager, pathName);

        Boolean isPathNameUniqueResponse = validatePathIfUnique(dataDefinition, serviceId, isUpdate, responseFormatManager );
        if (!isPathNameUniqueResponse) {
            logger.debug("Forwarding path name {} already in use ", dataDefinition.getName());
            throw new ByActionStatusComponentException(ActionStatus.FORWARDING_PATH_NAME_ALREADY_IN_USE, dataDefinition.getName());
        }
    }

    private void validatePathNameLength(ResponseFormatManager responseFormatManager, String pathName) {
        if (pathName.length() > PATH_NAME_LENGTH) {
            logger.debug("Forwarding path name  {} too long, , maximum allowed 200 characters ", pathName);
            throw new ByActionStatusComponentException(ActionStatus.FORWARDING_PATH_NAME_MAXIMUM_LENGTH, pathName);
        }
    }

    private void validatePathNameIfEmpty(ResponseFormatManager responseFormatManager, String pathName) {
        if (StringUtils.isEmpty(pathName)) {
            logger.debug("Forwarding Path Name can't be empty");
            throw new ByActionStatusComponentException(ActionStatus.FORWARDING_PATH_NAME_EMPTY);
        }
    }


    private Boolean validatePathIfUnique(ForwardingPathDataDefinition dataDefinition, String serviceId,
                                                                 boolean isUpdate, ResponseFormatManager responseFormatManager) {
        boolean isPathNameUnique = false;
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreForwardingPath(false);
        Either<Service, StorageOperationStatus> forwardingPathOrigin = toscaOperationFacade
                .getToscaElement(serviceId, filter);
        if (forwardingPathOrigin.isRight()){
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        Collection<ForwardingPathDataDefinition> allPaths = forwardingPathOrigin.left().value().getForwardingPaths().values();
        Map<String, String> pathNames = new HashMap<>();
        allPaths.forEach( path -> pathNames.put(path.getUniqueId(), path.getName()) );

        if (isUpdate){
            for(Map.Entry<String, String> entry : pathNames.entrySet()){
                if (entry.getKey().equals(dataDefinition.getUniqueId()) && entry.getValue().
                        equals(dataDefinition.getName())) {
                    isPathNameUnique = true;
                }

                if(entry.getKey().equals(dataDefinition.getUniqueId()) && !pathNames.values().contains(dataDefinition.getName())){
                    isPathNameUnique = true;
                }
            }
        }
        else
        if (!pathNames.values().contains(dataDefinition.getName())){
            isPathNameUnique = true;
        }

        return isPathNameUnique;
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }


}
