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

package org.openecomp.sdc.action.errors;

import org.openecomp.sdc.action.ActionConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ActionExceptionMapper implements ExceptionMapper<ActionException> {

  @Override
  public Response toResponse(ActionException actionException) {
    Response response;
    String errorCode = actionException.getErrorCode();
    switch (errorCode) {
      case ActionErrorConstants.ACTION_REQUEST_INVALID_GENERIC_CODE:
      case ActionErrorConstants.ACTION_INVALID_INSTANCE_ID_CODE:
      case ActionErrorConstants.ACTION_INVALID_REQUEST_ID_CODE:
      case ActionErrorConstants.ACTION_INVALID_REQUEST_BODY_CODE:
      case ActionErrorConstants.ACTION_INVALID_PARAM_CODE:
      case ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_FOR_NAME:
      case ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY:
      case ActionErrorConstants.ACTION_ENTITY_UNIQUE_VALUE_ERROR:
      case ActionErrorConstants.ACTION_INVALID_SEARCH_CRITERIA:
      case ActionErrorConstants.ACTION_MULT_SEARCH_CRITERIA:
      case ActionErrorConstants.ACTION_UPDATE_ON_UNLOCKED_ENTITY:
      case ActionErrorConstants.ACTION_UPDATE_INVALID_VERSION:
      case ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE:
      case ActionErrorConstants.ACTION_CHECKIN_ON_UNLOCKED_ENTITY:
      case ActionErrorConstants.ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED:
      case ActionErrorConstants.ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED:
      case ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY:
      case ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE_NAME:
      case ActionErrorConstants.ACTION_ARTIFACT_CHECKSUM_ERROR_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_ALREADY_EXISTS_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_INVALID_NAME_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_TOO_BIG_ERROR_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_INVALID_PROTECTION_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY:
      case ActionErrorConstants.ACTION_NOT_LOCKED_CODE:
        response = Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new ActionExceptionResponse(errorCode,
                Response.Status.BAD_REQUEST.getReasonPhrase(), actionException.getDescription()))
            .type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ActionErrorConstants.ACTION_AUTHENTICATION_ERR_CODE:
        response = Response
            .status(Response.Status.UNAUTHORIZED)
            .header(ActionConstants.WWW_AUTHENTICATE_HEADER_PARAM,
                ActionErrorConstants.ACTION_REQUEST_AUTHORIZATION_HEADER_INVALID)
            .entity(new ActionExceptionResponse(errorCode,
                Response.Status.UNAUTHORIZED.getReasonPhrase(), actionException.getDescription()))
            .type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ActionErrorConstants.ACTION_AUTHORIZATION_ERR_CODE:
      case ActionErrorConstants.ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER:
      case ActionErrorConstants.ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER:
      case ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER:
      case ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER:
      case ActionErrorConstants.ACTION_DELETE_ON_LOCKED_ENTITY_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_UPDATE_READ_ONLY:
        response = Response
            .status(Response.Status.FORBIDDEN)
            .entity(
                new ActionExceptionResponse(errorCode, Response.Status.FORBIDDEN.getReasonPhrase(),
                    actionException.getDescription())).type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE:
      case ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE:
        response = Response
            .status(Response.Status.NOT_FOUND)
            .entity(
                new ActionExceptionResponse(errorCode, Response.Status.NOT_FOUND.getReasonPhrase(),
                    actionException.getDescription())).type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE:
      default:
        response = Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ActionExceptionResponse(errorCode,
                Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                actionException.getDescription()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
    return response;
  }
}
