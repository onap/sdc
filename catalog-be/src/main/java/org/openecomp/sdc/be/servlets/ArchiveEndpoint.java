package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.ArchiveBusinessLogic;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Archive Endpoint")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ArchiveEndpoint {

    private final ArchiveBusinessLogic archiveBusinessLogic;

    public ArchiveEndpoint(ArchiveBusinessLogic archiveBusinessLogic) {
        this.archiveBusinessLogic = archiveBusinessLogic;
    }

    @POST
    @Path("/resources/{componentId}/archive")
    @ApiOperation(value = "Archive Resource", httpMethod = "POST", notes = "Marks a resource as archived. Can be restored with restore action", response = String.class, responseContainer = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Archive successful"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Resource not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Response archiveResources(@PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        archiveBusinessLogic.archiveComponent(ComponentTypeEnum.RESOURCE_PARAM_NAME, userId, componentId);
        return Response.ok().build();
    }

    @POST
    @Path("/resources/{componentId}/restore")
    @ApiOperation(value = "Restore Resource", httpMethod = "POST", notes = "Restores a resource from archive.", response = String.class, responseContainer = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Restore successful"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Resource not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Response restoreResource(@PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        archiveBusinessLogic.restoreComponent(ComponentTypeEnum.RESOURCE_PARAM_NAME, userId, componentId);
        return Response.ok().build();
    }

    @POST
    @Path("/services/{componentId}/archive")
    @ApiOperation(value = "Archive Service", httpMethod = "POST", notes = "Marks a service as archived. Can be restored with restore action", response = String.class, responseContainer = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Archive successful"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Service not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Response archiveService(@PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        archiveBusinessLogic.archiveComponent(ComponentTypeEnum.SERVICE_PARAM_NAME, userId, componentId);
        return Response.ok().build();
    }


    @POST
    @Path("/services/{componentId}/restore")
    @ApiOperation(value = "Restore Service", httpMethod = "POST", notes = "Restores a service from archive.", response = String.class, responseContainer = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Restore successful"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Service not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Response restoreService(@PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        archiveBusinessLogic.restoreComponent(ComponentTypeEnum.SERVICE_PARAM_NAME, userId, componentId);
        return Response.ok().build();
    }

    @GET
    @Path("/archive")
    @ApiOperation(value = "Get all Archived Components", httpMethod = "GET", notes = "Get all Archived Components", response = String.class, responseContainer = "")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Map<String, List<CatalogComponent>> getArchivedComponents(@HeaderParam(value = Constants.USER_ID_HEADER) String userId){
        return this.archiveBusinessLogic.getArchiveComponents(userId, new LinkedList<>());
    }

    @POST
    @Path("/notif/vsp/archived")
    @ApiOperation(value = "Notify about an archived VSP. All VFs with relation to the given CSAR IDs will be martked as vspArchived=true", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 500, message = "Internal Error. A list of the failed CSAR IDs may be returned.")
    })
    public Response onVspArchived(@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @RequestBody List<String> csarIds){
        List<String> failedCsarIds = this.archiveBusinessLogic.onVspArchive(userId, csarIds);
        if (!failedCsarIds.isEmpty()){
            //There are some failed CSAR IDs, return 500 and the list of failed CSAR IDs
            Map<String, List<String>> entity = new HashMap<>();
            entity.put("failedIds", failedCsarIds);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(entity)
                    .build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/notif/vsp/restored")
    @ApiOperation(value = "Notify about a restored VSP. All VFs with relation to the given CSAR IDs will be martked as vspArchived=false", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 500, message = "Internal Error. A list of the failed CSAR IDs may be returned.")
    })
    public Response onVspRestored(@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @RequestBody List<String> csarIds){
        List<String> failedCsarIds = this.archiveBusinessLogic.onVspRestore(userId, csarIds);
        if (!failedCsarIds.isEmpty()){
            //There are some failed CSAR IDs, return 500 and the list of failed CSAR IDs
            Map<String, List<String>> entity = new HashMap<>();
            entity.put("failedIds", failedCsarIds);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(entity)
                    .build();
        }
        return Response.ok().build();
    }

}
