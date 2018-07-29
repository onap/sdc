package org.openecomp.sdc.be.servlets.exception;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class StorageExceptionMapper implements ExceptionMapper<StorageException>  {

    private static final Logger log = Logger.getLogger(DefaultExceptionMapper.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentsUtils;

    public StorageExceptionMapper(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public Response toResponse(StorageException exception) {
        log.debug("#toResponse - An error occurred: ", exception);
        ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(exception.getStorageOperationStatus());
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(actionStatus, exception.getParams());
        return Response.status(responseFormat.getStatus())
                .entity(gson.toJson(responseFormat.getRequestError()))
                .build();
    }

}
