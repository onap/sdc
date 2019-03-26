package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABQuery.GABQueryType;
import org.openecomp.sdc.be.info.GenericArtifactQueryInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/gab")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Generic Artifact Browser")
@Controller
public class GenericArtifactBrowserServlet extends BeGenericServlet {

    private static final Logger LOGGER = Logger.getLogger(GenericArtifactBrowserServlet.class);

    @POST
    @Path("/searchFor")
    @ApiOperation(value = "Search json paths inside the yaml", httpMethod = "POST", notes = "Returns found entries of json paths", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returned yaml entries"),
        @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response searchFor(
        @ApiParam(value = "Generic Artifact search model", required = true) GenericArtifactQueryInfo query,
        @Context final HttpServletRequest request) {
        try {
            ServletContext context = request.getSession().getServletContext();
            Either<ImmutablePair<String, byte[]>, ResponseFormat> immutablePairResponseFormatEither = getArtifactBL(context)
                .downloadArtifact(query.getParentId(), query.getArtifactUniqueId());
            if (immutablePairResponseFormatEither.isRight()){
                throw new IOException(immutablePairResponseFormatEither.right().value().getFormattedMessage());
            } else {
                byte[] content = immutablePairResponseFormatEither.left().value().getRight();
                GABQuery gabQuery = new GABQuery(query.getFields(),
                    new String(content),
                    GABQueryType.CONTENT);
                return buildOkResponse(getGenericArtifactBrowserBL(context).searchFor(gabQuery));
            }
        } catch (IOException e) {
            LOGGER.error("Cannot search for a given queries in the yaml file", e);
            return buildGeneralErrorResponse();
        }
    }

}
