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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.openecomp.sdc.action.ActionConstants.WWW_AUTHENTICATE_HEADER_PARAM;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ALREADY_EXISTS_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_CHECKSUM_ERROR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_INVALID_NAME_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_INVALID_PROTECTION_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_TOO_BIG_ERROR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_UPDATE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_AUTHENTICATION_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_AUTHORIZATION_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKIN_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_DELETE_ON_LOCKED_ENTITY_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_UNIQUE_VALUE_ERROR;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_INSTANCE_ID_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_PARAM_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_REQUEST_BODY_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_REQUEST_ID_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INVALID_SEARCH_CRITERIA;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_MULT_SEARCH_CRITERIA;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_NOT_LOCKED_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_REQUEST_AUTHORIZATION_HEADER_INVALID;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_REQUEST_INVALID_GENERIC_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_INVALID_VERSION;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE_NAME;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_FOR_NAME;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_ON_UNLOCKED_ENTITY;

/**
 * Mapper class to map Action Library exceptions to corresponding HTTP Response objects.
 */
public class ActionExceptionMapper implements ExceptionMapper<ActionException> {

  @Override
  public Response toResponse(ActionException exception) {
    Response response;
    String errorCode = exception.getErrorCode();
    switch (errorCode) {
      case ACTION_REQUEST_INVALID_GENERIC_CODE:
      case ACTION_INVALID_INSTANCE_ID_CODE:
      case ACTION_INVALID_REQUEST_ID_CODE:
      case ACTION_INVALID_REQUEST_BODY_CODE:
      case ACTION_INVALID_PARAM_CODE:
      case ACTION_UPDATE_NOT_ALLOWED_FOR_NAME:
      case ACTION_CHECKOUT_ON_LOCKED_ENTITY:
      case ACTION_ENTITY_UNIQUE_VALUE_ERROR:
      case ACTION_INVALID_SEARCH_CRITERIA:
      case ACTION_MULT_SEARCH_CRITERIA:
      case ACTION_UPDATE_ON_UNLOCKED_ENTITY:
      case ACTION_UPDATE_INVALID_VERSION:
      case ACTION_UPDATE_NOT_ALLOWED_CODE:
      case ACTION_CHECKIN_ON_UNLOCKED_ENTITY:
      case ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED:
      case ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED:
      case ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY:
      case ACTION_UPDATE_NOT_ALLOWED_CODE_NAME:
      case ACTION_ARTIFACT_CHECKSUM_ERROR_CODE:
      case ACTION_ARTIFACT_ALREADY_EXISTS_CODE:
      case ACTION_ARTIFACT_INVALID_NAME_CODE:
      case ACTION_ARTIFACT_TOO_BIG_ERROR_CODE:
      case ACTION_ARTIFACT_INVALID_PROTECTION_CODE:
      case ACTION_ARTIFACT_DELETE_READ_ONLY:
      case ACTION_NOT_LOCKED_CODE:
        response = Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new ActionExceptionResponse(errorCode,
                Response.Status.BAD_REQUEST.getReasonPhrase(), exception.getDescription()))
            .type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ACTION_AUTHENTICATION_ERR_CODE:
        response = Response
            .status(Response.Status.UNAUTHORIZED)
            .header(WWW_AUTHENTICATE_HEADER_PARAM, ACTION_REQUEST_AUTHORIZATION_HEADER_INVALID)
            .entity(new ActionExceptionResponse(errorCode,
                Response.Status.UNAUTHORIZED.getReasonPhrase(), exception.getDescription()))
            .type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ACTION_AUTHORIZATION_ERR_CODE:
      case ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER:
      case ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER:
      case ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER:
      case ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER:
      case ACTION_DELETE_ON_LOCKED_ENTITY_CODE:
      case ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE:
      case ACTION_ARTIFACT_UPDATE_READ_ONLY:
        response = Response
            .status(Response.Status.FORBIDDEN)
            .entity(
                new ActionExceptionResponse(errorCode, Response.Status.FORBIDDEN.getReasonPhrase(),
                    exception.getDescription())).type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ACTION_ENTITY_NOT_EXIST_CODE:
      case ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE:
        response = Response
            .status(Response.Status.NOT_FOUND)
            .entity(
                new ActionExceptionResponse(errorCode, Response.Status.NOT_FOUND.getReasonPhrase(),
                    exception.getDescription())).type(MediaType.APPLICATION_JSON)
            .build();
        break;
      case ACTION_INTERNAL_SERVER_ERR_CODE:
      default:
        response = Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ActionExceptionResponse(errorCode,
                Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                exception.getDescription()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
    return response;
  }
}
