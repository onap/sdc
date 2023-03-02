/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.normatives.ElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/interface-types")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class InterfaceTypeServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(InterfaceTypeServlet.class);
    private final InterfaceLifecycleOperation interfaceLifecycleOperation;

    public InterfaceTypeServlet(final ComponentsUtils componentsUtils,
                                InterfaceLifecycleOperation interfaceLifecycleOperation) {
        super(componentsUtils);
        this.interfaceLifecycleOperation = interfaceLifecycleOperation;
    }

    @DELETE
    @Path("{interfaceTypeId}")
    public Response deleteInterfaceType(@Parameter(in = ParameterIn.PATH, required = true, description = "The interface type id")
                                        @PathParam("interfaceTypeId") final String interfaceTypeId) {
        final Either<InterfaceDefinition, StorageOperationStatus> interfaceTypeEither = interfaceLifecycleOperation.getInterface(interfaceTypeId);
        if (interfaceTypeEither.isRight()) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INTERFACE_LIFECYCLE_TYPES_NOT_FOUND,
                String.format("Failed to find interface type '%s'", interfaceTypeId)));
        }
        final InterfaceDefinition interfaceDefinition = interfaceTypeEither.left().value();
        if (!interfaceDefinition.isUserCreated()) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.CANNOT_DELETE_SYSTEM_DEPLOYED_RESOURCES,
                ElementTypeEnum.INTERFACE_LIFECYCLE_TYPE.getToscaEntryName(),
                interfaceTypeId));
        }
        if (StringUtils.isEmpty(interfaceDefinition.getModel())) {
            interfaceDefinition.setModel(Constants.DEFAULT_MODEL_NAME);
        }
        try {
            interfaceLifecycleOperation.deleteInterfaceTypeById(interfaceTypeId);
            interfaceLifecycleOperation.removeInterfaceTypeFromAdditionalType(interfaceDefinition);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Interface Type");
            log.debug("delete interface type failed with exception ", e);
            throw e;
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
    }
}
