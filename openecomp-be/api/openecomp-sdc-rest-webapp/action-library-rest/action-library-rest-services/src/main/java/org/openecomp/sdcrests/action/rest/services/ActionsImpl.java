/*-
 *
 * Copyright © 2016-2017 European Support Limited *
 *
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

package org.openecomp.sdcrests.action.rest.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.ActionConstants;
import org.openecomp.sdc.action.ActionManager;
import org.openecomp.sdc.action.errors.ActionErrorConstants;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.logging.StatusCode;
import org.openecomp.sdc.action.types.*;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.action.rest.Actions;
import org.openecomp.sdcrests.action.rest.mapping.MapActionToActionResponseDto;
import org.openecomp.sdcrests.action.types.ActionResponseDto;
import org.openecomp.sdcrests.action.types.ActionVersionDto;
import org.openecomp.sdcrests.action.types.ListResponseWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.openecomp.sdc.action.ActionConstants.*;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.*;
import static org.openecomp.sdc.action.util.ActionUtil.*;

/**
 * Implements various CRUD API that can be performed on Action
 */
@SuppressWarnings("ALL")
@Named
@Service("actions")
@Scope(value = "prototype")
@Validated
public class ActionsImpl implements Actions {

  private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ActionsImpl.class);
  @Autowired
  private ActionManager actionManager;
  private String whitespaceCharacters = "\\s"       /* dummy empty string for homogeneity */
      + "\\u0009" // CHARACTER TABULATION
      + "\\u000A" // LINE FEED (LF)
      + "\\u000B" // LINE TABULATION
      + "\\u000C" // FORM FEED (FF)
      + "\\u000D" // CARRIAGE RETURN (CR)
      + "\\u0020" // SPACE
      + "\\u0085" // NEXT LINE (NEL)
      + "\\u00A0" // NO-BREAK SPACE
      + "\\u1680" // OGHAM SPACE MARK
      + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
      + "\\u2000" // EN QUAD
      + "\\u2001" // EM QUAD
      + "\\u2002" // EN SPACE
      + "\\u2003" // EM SPACE
      + "\\u2004" // THREE-PER-EM SPACE
      + "\\u2005" // FOUR-PER-EM SPACE
      + "\\u2006" // SIX-PER-EM SPACE
      + "\\u2007" // FIGURE SPACE
      + "\\u2008" // PUNCTUATION SPACE
      + "\\u2009" // THIN SPACE
      + "\\u200A" // HAIR SPACE
      + "\\u2028" // LINE SEPARATOR
      + "\\u2029" // PARAGRAPH SEPARATOR
      + "\\u202F" // NARROW NO-BREAK SPACE
      + "\\u205F" // MEDIUM MATHEMATICAL SPACE
      + "\\u3000" // IDEOGRAPHIC SPACE
      ;
  private String invalidFilenameChars = "#<>$+%!`&*'|{}?\"=/:@\\\\";
  private String whitespaceRegex = ".*[" + whitespaceCharacters + "].*";
  private String invalidFilenameRegex = ".*[" + whitespaceCharacters + invalidFilenameChars + "].*";

  /**
   * Calculate the checksum for a given input
   *
   * @param input Byte array for which the checksum has to be calculated
   * @return Calculated checksum of the input byte array
   */
  private static String calculateCheckSum(byte[] input) {
    String checksum = null;
    if (input != null) {
      checksum = DigestUtils.md5Hex(input);
    }
    return checksum;
  }

  @Override
  public Response getActionsByActionInvariantUuId(String invariantID, String actionUUID,
                                                  HttpServletRequest servletRequest) {
    ListResponseWrapper responseList = new ListResponseWrapper();

    try {
      LOGGER.debug(" entering getActionsByActionInvariantUuId ");
      initializeRequestMDC(servletRequest, invariantID, ActionRequest.GET_ACTIONS_INVARIANT_ID);
      MDC.put(SERVICE_INSTANCE_ID, invariantID);

      if (StringUtils.isEmpty(servletRequest.getQueryString())) {
        responseList = getActionsByInvId(servletRequest, invariantID);
      } else {
        Response response = getActionByUUID(servletRequest, invariantID, actionUUID);
        actionLogPostProcessor(StatusCode.COMPLETE, true);
        return response;
      }
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error("");
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error("");
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.GET_ACTIONS_INVARIANT_ID.name());
    }

    LOGGER.debug(" exit getActionsByActionInvariantUuId ");
    actionLogPostProcessor(StatusCode.COMPLETE, true);
    return Response.ok(responseList).build();
  }

  private ListResponseWrapper getActionsByInvId(HttpServletRequest servletRequest,
                                                String invariantID) {
    LOGGER.debug(" entering getActionsByInvId with invariantID= " + invariantID);
    ListResponseWrapper responseList = new ListResponseWrapper();
    if (StringUtils.isEmpty(servletRequest.getQueryString())) {
      Map<String, String> errorMap = validateRequestHeaders(servletRequest);
      Map<String, String> queryParamErrors = validateQueryParam(invariantID);
      errorMap.putAll(queryParamErrors);
      if (errorMap.isEmpty()) {
        List<Action> actions = actionManager.getActionsByActionInvariantUuId(invariantID);
        List<ActionResponseDto> versionList = new ArrayList<>();
        for (Action action : actions) {
          ActionResponseDto responseDTO = createResponseDTO(action);
          versionList.add(responseDTO);
        }
        responseList.setVersions(versionList);
        responseList.setActionList(null);

      } else {
        checkAndThrowError(errorMap);
      }
    }
    LOGGER.debug(" exit getActionsByInvId with invariantID= " + invariantID);
    return responseList;
  }

  private Response getActionByUUID(HttpServletRequest servletRequest, String invariantID,
                                   String actionUUID) throws ActionException {
    int noOfFilterParams = 0;
    Response response = null;
    LOGGER.debug(" entering getActionByUUID with invariantID= " + invariantID + " and actionUUID= " +
        actionUUID);
    if (!StringUtils.isEmpty(actionUUID)) {
      noOfFilterParams++;
      response = getActionsByUniqueID(actionUUID, servletRequest, invariantID);
    }
    if (noOfFilterParams == 0) {
      throw new ActionException(ACTION_INVALID_SEARCH_CRITERIA,
          ACTION_REQUEST_FILTER_PARAM_INVALID);
    }

    LOGGER.debug(" exit getActionByUUID with invariantID= " + invariantID + " and actionUUID= " +
        actionUUID);
    return response;
  }

  @Override
  public Response getOpenEcompComponents(HttpServletRequest servletRequest) {
    try {
      LOGGER.debug(" entering getEcompComponents ");
      initializeRequestMDC(servletRequest, "", ActionRequest.GET_OPEN_ECOMP_COMPONENTS);
      //Validate request syntax before passing to the manager
      Map<String, String> errorMap = validateRequestHeaders(servletRequest);
      checkAndThrowError(errorMap);
      ListResponseWrapper response = new ListResponseWrapper();
      List<OpenEcompComponent> openEcompComponents = actionManager.getOpenEcompComponents();
      response.setActionList(null);
      response.setComponentList(openEcompComponents);
      LOGGER.debug(" exit getEcompComponents ");
      actionLogPostProcessor(StatusCode.COMPLETE, true);
      return Response.ok(response).build();
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error("");
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error("");
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.GET_OPEN_ECOMP_COMPONENTS.name());
    }
  }

  @Override
  public Response getFilteredActions(String vendor, String category, String name, String modelID,
                                     String componentID, HttpServletRequest servletRequest) {
    try {
      LOGGER.debug(" entering getFilteredActions ");
      Response response;
      initializeRequestMDC(servletRequest, "", ActionRequest.GET_FILTERED_ACTIONS);
      int noOfFilterParams = getNoOfFilterParams(vendor, category, name, modelID, componentID);
      if (StringUtils.isEmpty(servletRequest.getQueryString())) {
        response = getAllActions(servletRequest);
        LOGGER.debug(" exit getFilteredActions ");
        actionLogPostProcessor(StatusCode.COMPLETE, true);
        return response;
      }
      validateNoOfFilterParamsExactly1(noOfFilterParams);
      if (!StringUtils.isEmpty(vendor)) {
        response = getActionsByVendor(vendor, servletRequest);
      } else if (!StringUtils.isEmpty(category)) {
        response = getActionsByCategory(category, servletRequest);
      } else if (!StringUtils.isEmpty(name)) {
        response = getActionsByName(name, servletRequest);
      } else if (!StringUtils.isEmpty(modelID)) {
        response = getActionsByModel(modelID, servletRequest);
      } else if (!StringUtils.isEmpty(componentID)) {
        response = getActionsByOpenEcompComponents(componentID, servletRequest);
      } else {
        throw new ActionException(ACTION_INVALID_PARAM_CODE, ACTION_REQUEST_FILTER_PARAM_INVALID);
      }

      LOGGER.debug(" exit getFilteredActions ");
      actionLogPostProcessor(StatusCode.COMPLETE, true);
      return response;
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error("");
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error("");
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.GET_FILTERED_ACTIONS.name());
    }
  }

  private void validateNoOfFilterParamsExactly1(int noOfFilterParams) {
    if (noOfFilterParams > 1) {
      throw new ActionException(ACTION_MULT_SEARCH_CRITERIA,
          ACTION_FILTER_MULTIPLE_QUERY_PARAM_NOT_SUPPORTED);
    }
    if (noOfFilterParams == 0) {
      throw new ActionException(ACTION_INVALID_SEARCH_CRITERIA,
          ACTION_REQUEST_FILTER_PARAM_INVALID);
    }
  }

  private int getNoOfFilterParams(String vendor, String category, String name, String modelID, String componentID) {
    int noOfFilterParams = 0;
    if (!StringUtils.isEmpty(vendor)) {
      noOfFilterParams++;
    }
    if (!StringUtils.isEmpty(category)) {
      noOfFilterParams++;
    }
    if (!StringUtils.isEmpty(name)) {
      noOfFilterParams++;
    }
    if (!StringUtils.isEmpty(modelID)) {
      noOfFilterParams++;
    }
    if (!StringUtils.isEmpty(componentID)) {
      noOfFilterParams++;
    }
    return noOfFilterParams;
  }

  @Override
  public Response createAction(String requestJSON, HttpServletRequest servletRequest) {
    try {
      initializeRequestMDC(servletRequest, null, ActionRequest.CREATE_ACTION);
      LOGGER.debug(" entering API createAction ");
      Map<String, String> errorMap = validateRequestHeaders(servletRequest);
      Map<String, String> requestBodyErrors =
          validateRequestBody(REQUEST_TYPE_CREATE_ACTION, requestJSON);
      errorMap.putAll(requestBodyErrors);
      ActionResponseDto actionResponseDTO = new ActionResponseDto();
      if (errorMap.isEmpty()) {
        String user = servletRequest.getRemoteUser();
        Action action = JsonUtil.json2Object(requestJSON, Action.class);
        action.setData(requestJSON);
        Action responseAction = actionManager.createAction(action, user);
        MDC.put(SERVICE_INSTANCE_ID, responseAction.getActionInvariantUuId());
        new MapActionToActionResponseDto().doMapping(responseAction, actionResponseDTO);
      } else {
        checkAndThrowError(errorMap);
      }
      actionLogPostProcessor(StatusCode.COMPLETE, true);
      LOGGER.debug(" exit API createAction with ActionInvariantUUID= " + MDC.get(SERVICE_INSTANCE_ID));
      return Response.ok(actionResponseDTO).build();
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error("");
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.CREATE_ACTION.name());
    }

  }

  @Override
  public Response updateAction(String invariantUUID, String requestJSON,
                               HttpServletRequest servletRequest) {
    ActionResponseDto actionResponseDTO = null;
    try {
      initializeRequestMDC(servletRequest, invariantUUID, ActionRequest.UPDATE_ACTION);
      Map<String, String> errorMap = validateRequestHeaders(servletRequest);
      Map<String, String> requestBodyErrors =
          validateRequestBody(REQUEST_TYPE_UPDATE_ACTION, requestJSON);
      errorMap.putAll(requestBodyErrors);
      actionResponseDTO = new ActionResponseDto();
      if (errorMap.isEmpty()) {
        String user = servletRequest.getRemoteUser();
        Action action = JsonUtil.json2Object(requestJSON, Action.class);
        action.setActionInvariantUuId(invariantUUID);
        action.setData(requestJSON);
        Action updatedAction = actionManager.updateAction(action, user);
        new MapActionToActionResponseDto().doMapping(updatedAction, actionResponseDTO);
      } else {
        checkAndThrowError(errorMap);
      }
      actionLogPostProcessor(StatusCode.COMPLETE, true);
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error("");
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.UPDATE_ACTION.name());
    }

    return Response.ok(actionResponseDTO).build();
  }

  @Override
  public Response deleteAction(String actionInvariantUUID, HttpServletRequest servletRequest) {
    try {
      initializeRequestMDC(servletRequest, actionInvariantUUID, ActionRequest.DELETE_ACTION);
      Map<String, String> errorMap = validateRequestHeaders(servletRequest);
      if (errorMap.isEmpty()) {
        String user = servletRequest.getRemoteUser();
        actionManager.deleteAction(actionInvariantUUID, user);
      } else {
        checkAndThrowError(errorMap);
      }

      actionLogPostProcessor(StatusCode.COMPLETE, true);
      return Response.ok(new ActionResponseDto()).build();
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error(MDC.get(ERROR_DESCRIPTION));
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.DELETE_ACTION.name());
    }
  }

  @Override
  public Response actOnAction(String invariantUUID, String requestJSON,
                              HttpServletRequest servletRequest) {
    Response response = null;
    try {
      initializeRequestMDC(servletRequest, invariantUUID, ActionRequest.ACTION_VERSIONING);
      LOGGER.debug("entering actOnAction with invariantUUID= " + invariantUUID + " and requestJSON= " +
          requestJSON);
      Map<String, String> errorMap = validateRequestHeaders(servletRequest);
      Map<String, String> requestBodyErrors =
          validateRequestBody(REQUEST_TYPE_VERSION_ACTION, requestJSON);
      errorMap.putAll(requestBodyErrors);

      ActionVersionDto versionDTO = JsonUtil.json2Object(requestJSON, ActionVersionDto.class);
      checkAndThrowError(errorMap);

      String status = versionDTO.getStatus();
      Action action = new Action();
      String user = servletRequest.getRemoteUser();
      switch (status) {
        case "Checkout":
          action = actionManager.checkout(invariantUUID, user);
          break;
        case "Undo_Checkout":
          actionManager.undoCheckout(invariantUUID, user);
          StringWrapperResponse responseText = new StringWrapperResponse();
          responseText.setValue(ActionConstants.UNDO_CHECKOUT_RESPONSE_TEXT);
          response = Response
              .status(Response.Status.OK)
              .entity(responseText)
              .build();
          return response;
        case "Checkin":
          action = actionManager.checkin(invariantUUID, user);
          break;
        case "Submit":
          action = actionManager.submit(invariantUUID, user);
          break;
        default:
          throw new ActionException(ACTION_INVALID_PARAM_CODE,
              String.format(ACTION_UNSUPPORTED_OPERATION, status));
      }

      ActionResponseDto actionResponseDTO = new ActionResponseDto();
      new MapActionToActionResponseDto().doMapping(action, actionResponseDTO);
      response = Response.ok(actionResponseDTO).build();
      actionLogPostProcessor(StatusCode.COMPLETE, true);
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error(MDC.get(ERROR_DESCRIPTION));
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.ACTION_VERSIONING.name());
      LOGGER.debug("exit actOnAction with invariantUUID= " + invariantUUID + " and requestJSON= " +
          requestJSON);
    }
    return response;
  }

  @Override
  public Response uploadArtifact(String actionInvariantUUID,
                                 String artifactName,
                                 String artifactLabel,
                                 String artifactCategory,
                                 String artifactDescription,
                                 String artifactProtection,
                                 String checksum,
                                 Attachment artifactToUpload,
                                 HttpServletRequest servletRequest) {
    Response response = null;
    try {
      initializeRequestMDC(servletRequest, actionInvariantUUID, ActionRequest.UPLOAD_ARTIFACT);
      LOGGER.debug("entering uploadArtifact with actionInvariantUuId= " + actionInvariantUUID +
          "artifactName= " + artifactName);
      response =
          uploadArtifactInternal(actionInvariantUUID, artifactName, artifactLabel, artifactCategory,
              artifactDescription, artifactProtection, checksum, artifactToUpload, servletRequest);
      actionLogPostProcessor(StatusCode.COMPLETE, true);
      LOGGER.debug("exiting uploadArtifact with actionInvariantUuId= " + actionInvariantUUID +
          "artifactName= " + artifactName);
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error(MDC.get(ERROR_DESCRIPTION));
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.UPLOAD_ARTIFACT.name());
    }
    LOGGER.debug("exiting uploadArtifact with actionInvariantUuId= " + actionInvariantUUID +
        "artifactName= " + artifactName);
    return response;
  }

  private Response uploadArtifactInternal(String actionInvariantUUID, String artifactName,
                                          String artifactLabel, String artifactCategory,
                                          String artifactDescription, String artifactProtection,
                                          String checksum, Attachment artifactToUpload,
                                          HttpServletRequest servletRequest) {
    ListResponseWrapper responseList = null;
    byte[] payload = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    //Artifact name empty validation
    if (StringUtils.isEmpty(artifactName)) {
      errorMap.put(ACTION_REQUEST_INVALID_GENERIC_CODE,
          ACTION_REQUEST_MISSING_MANDATORY_PARAM + ARTIFACT_NAME);
    } else {
      //Artifact name syntax check for whitespaces and invalid characters
      if (artifactName.matches(invalidFilenameRegex)) {
        errorMap.put(ACTION_ARTIFACT_INVALID_NAME_CODE, ACTION_ARTIFACT_INVALID_NAME);
      }
    }

    //Content-Type Header Validation
    String contentType = servletRequest.getContentType();
    if (StringUtils.isEmpty(contentType)) {
      errorMap.put(ACTION_REQUEST_INVALID_GENERIC_CODE, ACTION_REQUEST_CONTENT_TYPE_INVALID);
    }

    if (artifactToUpload == null) {
      throw new ActionException(ACTION_REQUEST_INVALID_GENERIC_CODE,
          ACTION_REQUEST_MISSING_MANDATORY_PARAM + ARTIFACT_FILE);
    }

    try (InputStream artifactInputStream = artifactToUpload.getDataHandler().getInputStream()) {
      payload = FileUtils.toByteArray(artifactInputStream);
    } catch (IOException exception) {
      LOGGER.error(ACTION_ARTIFACT_READ_FILE_ERROR, exception);
      throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ARTIFACT_READ_FILE_ERROR);
    }

    //Validate Artifact size
    if (payload != null && payload.length > MAX_ACTION_ARTIFACT_SIZE) {
      throw new ActionException(ACTION_ARTIFACT_TOO_BIG_ERROR_CODE, ACTION_ARTIFACT_TOO_BIG_ERROR);
    }

    //Validate Checksum
    if (StringUtils.isEmpty(checksum) || !checksum.equalsIgnoreCase(calculateCheckSum(payload))) {
      errorMap.put(ACTION_ARTIFACT_CHECKSUM_ERROR_CODE, ACTION_REQUEST_ARTIFACT_CHECKSUM_ERROR);
    }

    //Validate artifact protection values
    if (StringUtils.isEmpty(artifactProtection)) {
      artifactProtection = ActionArtifactProtection.readWrite.name();
    }

    if (!artifactProtection.equals(ActionArtifactProtection.readOnly.name()) &&
        !artifactProtection.equals(ActionArtifactProtection.readWrite.name())) {
      errorMap.put(ACTION_ARTIFACT_INVALID_PROTECTION_CODE,
          ACTION_REQUEST_ARTIFACT_INVALID_PROTECTION_VALUE);
    }

    ActionArtifact uploadedArtifact = new ActionArtifact();
    if (errorMap.isEmpty()) {
      String user = servletRequest.getRemoteUser();
      ActionArtifact upload = new ActionArtifact();
      upload.setArtifactName(artifactName);
      upload.setArtifactLabel(artifactLabel);
      upload.setArtifactDescription(artifactDescription);
      upload.setArtifact(payload);
      upload.setArtifactCategory(artifactCategory);
      upload.setArtifactProtection(artifactProtection);
      uploadedArtifact = actionManager.uploadArtifact(upload, actionInvariantUUID, user);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok(uploadedArtifact).build();
  }

  @Override
  public Response downloadArtifact(String actionUUID, String artifactUUID,
                                   HttpServletRequest servletRequest) {
    Response response = null;
    try {
      initializeRequestMDC(servletRequest, "", ActionRequest.DOWNLOAD_ARTIFACT);
      LOGGER.debug(
          " entering downloadArtifact with actionUUID= " + actionUUID + " and artifactUUID= " +
              artifactUUID);
      response = downloadArtifactInternal(actionUUID, artifactUUID, servletRequest);
      actionLogPostProcessor(StatusCode.COMPLETE, true);
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error(MDC.get(ERROR_DESCRIPTION));
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.DOWNLOAD_ARTIFACT.name());
    }
    LOGGER.debug(" exit downloadArtifact with actionUUID= " + actionUUID + " and artifactUUID= " +
        artifactUUID);
    return response;
  }

  private Response downloadArtifactInternal(String actionUUID, String artifactUUID,
                                            HttpServletRequest servletRequest) {
    Response response;
    ActionArtifact actionartifact = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(actionUUID);
    errorMap.putAll(queryParamErrors);
    queryParamErrors = validateQueryParam(artifactUUID);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      actionartifact = actionManager.downloadArtifact(actionUUID, artifactUUID);
    } else {
      checkAndThrowError(errorMap);
    }
    response = createArtifactDownloadResponse(actionartifact);
    return response;
  }

  @Override
  public Response deleteArtifact(String actionInvariantUUID, String artifactUUID,
                                 HttpServletRequest servletRequest) {
    Response response = null;
    try {
      initializeRequestMDC(servletRequest, actionInvariantUUID, ActionRequest.DELETE_ARTIFACT);
      LOGGER.debug(" entering deleteArtifact with actionInvariantUuId= " + actionInvariantUUID +
          " and artifactUUID= " + artifactUUID);
      response = deleteArtifactInternal(actionInvariantUUID, artifactUUID, servletRequest);
      LOGGER.debug(" exit deleteArtifact with actionInvariantUuId= " + actionInvariantUUID +
          " and artifactUUID= " + artifactUUID);
      actionLogPostProcessor(StatusCode.COMPLETE, true);
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error(MDC.get(ERROR_DESCRIPTION));
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.DELETE_ARTIFACT.name());
    }
    return response;
  }

  private Response deleteArtifactInternal(String actionInvariantUUID, String artifactUUID,
                                          HttpServletRequest servletRequest) {
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(actionInvariantUUID);
    errorMap.putAll(queryParamErrors);
    queryParamErrors = validateQueryParam(artifactUUID);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      actionManager
          .deleteArtifact(actionInvariantUUID, artifactUUID, servletRequest.getRemoteUser());
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok().build();
  }

  @Override
  public Response updateArtifact(String actionInvariantUUID, String artifactUUID,
                                 String artifactName, String artifactLabel, String artifactCategory,
                                 String artifactDescription, String artifactProtection,
                                 String checksum, Attachment artifactToUpdate,
                                 HttpServletRequest servletRequest) {
    Response response = null;
    LOGGER.debug(" entering updateArtifact with actionInvariantUuId= " + actionInvariantUUID +
        " and artifactUUID= " + artifactUUID + " and artifactName= " + artifactName +
        " and artifactLabel= " + artifactLabel + " and artifactCategory= " + artifactCategory +
        " and artifactDescription= " + artifactDescription + " and artifactProtection= " +
        artifactProtection + " and checksum= " + checksum);
    try {
      initializeRequestMDC(servletRequest, actionInvariantUUID, ActionRequest.UPDATE_ARTIFACT);
      response =
          updateArtifactInternal(actionInvariantUUID, artifactUUID, artifactName, artifactLabel,
              artifactCategory, artifactDescription, artifactProtection, checksum, artifactToUpdate,
              servletRequest);
      actionLogPostProcessor(StatusCode.COMPLETE, true);
    } catch (ActionException exception) {
      actionLogPostProcessor(StatusCode.ERROR, exception.getErrorCode(), exception.getDescription(), true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, exception.getErrorCode(), exception.getDescription());
      LOGGER.error(MDC.get(ERROR_DESCRIPTION));
      throw exception;
    } catch (Exception exception) {
      actionLogPostProcessor(StatusCode.ERROR, true);
      actionErrorLogProcessor(CategoryLogLevel.ERROR, ACTION_INTERNAL_SERVER_ERR_CODE,
          ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      LOGGER.error(exception.getMessage());
      throw exception;
    } finally {
      finalAuditMetricsLogProcessor(ActionRequest.UPDATE_ARTIFACT.name());
    }
    LOGGER.debug(" exit updateArtifact with actionInvariantUuId= " + actionInvariantUUID +
        " and artifactUUID= " + artifactUUID + " and artifactName= " + artifactName +
        " and artifactLabel= " + artifactLabel + " and artifactCategory= " + artifactCategory +
        " and artifactDescription= " + artifactDescription + " and artifactProtection= " +
        artifactProtection + " and checksum= " + checksum);
    return response;
  }

  private void finalAuditMetricsLogProcessor(String targetServiceName) {
    MDC.put(TARGET_SERVICE_NAME, targetServiceName);
    MDC.put(TARGET_ENTITY, TARGET_ENTITY_API);
    LOGGER.metrics("");
    LOGGER.audit("");
  }

  private Response updateArtifactInternal(String actionInvariantUUID, String artifactUUID,
                                          String artifactName, String artifactLabel,
                                          String artifactCategory, String artifactDescription,
                                          String artifactProtection, String checksum,
                                          Attachment artifactToUpdate,
                                          HttpServletRequest servletRequest) {
    byte[] payload = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);

    //Content-Type Header Validation
    String contentType = servletRequest.getContentType();
    if (StringUtils.isEmpty(contentType)) {
      errorMap.put(ACTION_REQUEST_INVALID_GENERIC_CODE, ACTION_REQUEST_CONTENT_TYPE_INVALID);
    }

    if (artifactToUpdate != null) {

      try (InputStream artifactInputStream = artifactToUpdate.getDataHandler().getInputStream()) {
        payload = FileUtils.toByteArray(artifactInputStream);
      } catch (IOException exception) {
        LOGGER.error(ACTION_ARTIFACT_READ_FILE_ERROR, exception);
        throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ARTIFACT_READ_FILE_ERROR);
      }

      //Validate Artifact size
      if (payload != null && payload.length > MAX_ACTION_ARTIFACT_SIZE) {
        throw new ActionException(ACTION_ARTIFACT_TOO_BIG_ERROR_CODE,
            ACTION_ARTIFACT_TOO_BIG_ERROR);
      }

      //Validate Checksum
      if (StringUtils.isEmpty(checksum) || !checksum.equalsIgnoreCase(calculateCheckSum(payload))) {
        errorMap.put(ACTION_ARTIFACT_CHECKSUM_ERROR_CODE, ACTION_REQUEST_ARTIFACT_CHECKSUM_ERROR);
      }
    }

    if (artifactProtection != null && (artifactProtection.isEmpty() ||
        (!artifactProtection.equals(ActionArtifactProtection.readOnly.name()) &&
            !artifactProtection.equals(ActionArtifactProtection.readWrite.name())))) {
      errorMap.put(ACTION_ARTIFACT_INVALID_PROTECTION_CODE,
          ACTION_REQUEST_ARTIFACT_INVALID_PROTECTION_VALUE);
    }

    ActionArtifact updateArtifact = new ActionArtifact();
    if (errorMap.isEmpty()) {
      String user = servletRequest.getRemoteUser();
      ActionArtifact update = new ActionArtifact();
      update.setArtifactUuId(artifactUUID);
      update.setArtifactName(artifactName);
      update.setArtifactLabel(artifactLabel);
      update.setArtifactDescription(artifactDescription);
      update.setArtifact(payload);
      update.setArtifactCategory(artifactCategory);
      update.setArtifactProtection(artifactProtection);
      actionManager.updateArtifact(update, actionInvariantUUID, user);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok().build();
  }

  /**
   * Get List of all actions
   */
  private Response getAllActions(HttpServletRequest servletRequest) {
    ListResponseWrapper responseList = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    if (errorMap.isEmpty()) {
      List<Action> actions = actionManager.getFilteredActions(FILTER_TYPE_NONE, null);
      responseList = createResponse(actions);
    } else {
      checkAndThrowError(errorMap);
    }

    return Response.ok(responseList).build();
  }

  /**
   * Get Actions by OPENECOMP component ID
   */
  private Response getActionsByOpenEcompComponents(String componentID,
                                                   HttpServletRequest servletRequest) {
    ListResponseWrapper responseList = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(componentID);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      List<Action> actions =
          actionManager.getFilteredActions(FILTER_TYPE_OPEN_ECOMP_COMPONENT, componentID);
      responseList = createResponse(actions);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok(responseList).build();
  }

  /**
   * Get Actions by Model ID
   */
  private Response getActionsByModel(String modelId, HttpServletRequest servletRequest) {
    ListResponseWrapper responseList = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(modelId);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      List<Action> actions = actionManager.getFilteredActions(FILTER_TYPE_MODEL, modelId);
      responseList = createResponse(actions);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok(responseList).build();
  }

  /**
   * Get all actions with given action name
   */
  private Response getActionsByName(String name, HttpServletRequest servletRequest) {
    ListResponseWrapper responseList = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(name);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      List<Action> actions = actionManager.getFilteredActions(FILTER_TYPE_NAME, name);
      responseList = createResponse(actions);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok(responseList).build();
  }

  /**
   * Get an action with given ActionUUID
   */
  private Response getActionsByUniqueID(String actionUUID, HttpServletRequest servletRequest,
                                        String actionInvariantUUID) {
    LOGGER.debug(
        " entering getActionByUUID with invariantID= " + actionInvariantUUID + " and actionUUID= " +
            actionUUID);
    Map<String, Object> responseDTO = new LinkedHashMap<>();
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(actionUUID);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      Action action = actionManager.getActionsByActionUuId(actionUUID);
      if (action.getActionInvariantUuId() != null &&
          action.getActionInvariantUuId().equalsIgnoreCase(actionInvariantUUID)) {
        responseDTO = JsonUtil.json2Object(action.getData(), LinkedHashMap.class);
        responseDTO.put(STATUS, action.getStatus().name());
        responseDTO.put(TIMESTAMP, getUtcDateStringFromTimestamp(action.getTimestamp()));
        responseDTO.put(UPDATED_BY, action.getUser());
      } else {
        throw new ActionException(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_ENTITY_NOT_EXIST);
      }
    } else {
      checkAndThrowError(errorMap);
    }
    LOGGER.debug(
        " exit getActionByUUID with invariantID= " + actionInvariantUUID + " and actionUUID= " +
            actionUUID);
    return Response.ok(responseDTO).build();
  }

  /**
   * Get all actions with given Vendor Name
   */
  private Response getActionsByVendor(String vendor, HttpServletRequest servletRequest) {
    //Validate request syntax before passing to the manager
    ListResponseWrapper responseList = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(vendor);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      List<Action> actions = actionManager.getFilteredActions(FILTER_TYPE_VENDOR, vendor);
      responseList = createResponse(actions);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok(responseList).build();
  }

  /**
   * Get all actions with given Category Name
   */
  private Response getActionsByCategory(String category, HttpServletRequest servletRequest) {
    //Validate request syntax before passing to the manager
    ListResponseWrapper responseList = null;
    Map<String, String> errorMap = validateRequestHeaders(servletRequest);
    Map<String, String> queryParamErrors = validateQueryParam(category);
    errorMap.putAll(queryParamErrors);
    if (errorMap.isEmpty()) {
      List<Action> actions = actionManager.getFilteredActions(FILTER_TYPE_CATEGORY, category);
      responseList = createResponse(actions);
    } else {
      checkAndThrowError(errorMap);
    }
    return Response.ok(responseList).build();
  }

  /**
   * Validates mandatory headers in the request
   *
   * @param servletRequest Servlet Request object
   * @return Map of error codes and description found in the request headers
   */
  private Map<String, String> validateRequestHeaders(HttpServletRequest servletRequest) {
    Map<String, String> errorMap = new LinkedHashMap<>();
    //Syntactic generic request parameter validations
    String openEcompRequestId = servletRequest.getHeader(X_OPEN_ECOMP_REQUEST_ID_HEADER_PARAM);
    if (StringUtils.isEmpty(openEcompRequestId)) {
      errorMap.put(ACTION_INVALID_REQUEST_ID_CODE, ACTION_REQUEST_OPEN_ECOMP_REQUEST_ID_INVALID);
    }

    String opemnEcompInstanceId = servletRequest.getHeader(X_OPEN_ECOMP_INSTANCE_ID_HEADER_PARAM);
    if (StringUtils.isEmpty(opemnEcompInstanceId)) {
      errorMap.put(ACTION_INVALID_INSTANCE_ID_CODE, ACTION_REQUEST_OPEN_ECOMP_INSTANCE_ID_INVALID);
    }
    return errorMap;
  }

  /**
   * Validates query parameter in the request
   *
   * @param queryParam Query Parameter to be validated
   * @return Map of error codes and description found in the query parameter
   */
  private Map<String, String> validateQueryParam(String queryParam) {
    Map<String, String> queryParamErrors = new LinkedHashMap<>();
    if (StringUtils.isEmpty(queryParam)) {
      queryParamErrors
          .put(ACTION_INVALID_PARAM_CODE, ACTION_REQUEST_MISSING_MANDATORY_PARAM + queryParam);
    }
    return queryParamErrors;
  }

  /**
   * Validate request body based on request type
   *
   * @param requestJSON Raw request json body as string
   * @return Map of error codes and description found in the request body
   */
  private Map<String, String> validateRequestBody(String requestType, String requestJSON) {
    Map<String, String> requestBodyErrorMap = new LinkedHashMap<>();
    if (StringUtils.isEmpty(requestJSON) || requestJSON.equals(REQUEST_EMPTY_BODY)) {
      requestBodyErrorMap.put(ACTION_INVALID_REQUEST_BODY_CODE, ACTION_REQUEST_BODY_EMPTY);
    } else {
      if(requestType == ActionConstants.REQUEST_TYPE_CREATE_ACTION){
        //placeholder for future implementation
      }
      if(requestType == ActionConstants.REQUEST_TYPE_UPDATE_ACTION){
        //Semantic request specific validations
        Action action = JsonUtil.json2Object(requestJSON, Action.class);
        if(StringUtils.isEmpty(action.getName())){
          setErrorValue(ACTION_REQUEST_INVALID_GENERIC_CODE, ACTION_REQUEST_PARAM_NAME,
              requestBodyErrorMap);
        } else {
          //Added check for action names not allowing whitespaces
          if (action.getName().matches(whitespaceRegex)){
            requestBodyErrorMap.put(ACTION_ARTIFACT_INVALID_NAME_CODE, ACTION_REQUEST_INVALID_NAME);
          }
        }

        if(action.getSupportedModels() != null && !isIDPresentInMap(action.getSupportedModels(),
            SUPPORTED_MODELS_VERSION_ID)){
          setErrorValue(ACTION_REQUEST_INVALID_GENERIC_CODE,
              ACTION_REQUEST_PARAM_SUPPORTED_MODELS, requestBodyErrorMap);
        }
        if(action.getSupportedComponents() != null && !isIDPresentInMap(action
            .getSupportedComponents(), SUPPORTED_COMPONENTS_ID)){
          setErrorValue(ACTION_REQUEST_INVALID_GENERIC_CODE,
              ACTION_REQUEST_PARAM_SUPPORTED_MODELS, requestBodyErrorMap);
        }
        if(action.getArtifacts() != null){
          setErrorValue(ACTION_UPDATE_NOT_ALLOWED_CODE,
              ACTION_REQUEST_ARTIFACT_OPERATION_ALLOWED, requestBodyErrorMap);
        }
      }

    }
    return requestBodyErrorMap;
  }

  /**
   * Populates Given Error Map with Given Error Code and Error MEssage
   */
  private void setErrorValue(String key, String message, Map<String, String> errorMap) {
    String errorMessage = errorMap.get(key);
    if (errorMessage != null) {
      message = errorMessage + ", " + message;
    } else {
      if(key == ACTION_REQUEST_INVALID_GENERIC_CODE)
        message = ACTION_REQUEST_MISSING_MANDATORY_PARAM + message;
    }
    errorMap.put(key, message);
  }

  /**
   * Returns true if given key exists in List of HashMap
   */
  private boolean isIDPresentInMap(List<HashMap<String, String>> map, String idName) {
    if (map != null && !map.isEmpty()) {
      for (HashMap<String, String> entry : map) {
        if (StringUtils.isEmpty(entry.get(idName))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * @throws ActionException if given ErrorMap is not empty. All error messages at given time are
   *                         thrown in one single exception
   */
  private void checkAndThrowError(Map<String, String> errorMap) {
    if (errorMap.size() > 1) {
      //Multiple errors detected .. Send the response with a common error code for multiple errors
      throw new ActionException(ACTION_REQUEST_INVALID_GENERIC_CODE,
          StringUtils.join(errorMap.values(), ", "));
    } else if (errorMap.size() == 1) {
      String svcPolicyExceptionCode = errorMap.entrySet().iterator().next().getKey();
      throw new ActionException(svcPolicyExceptionCode,
          errorMap.get(svcPolicyExceptionCode));
    }
  }

  /**
   * Populates ActionResponseDto based on given Action
   */
  private ActionResponseDto createResponseDTO(Action action) {
    String data = action.getData();
    ActionResponseDto responseDTO = JsonUtil.json2Object(data, ActionResponseDto.class);
    responseDTO.setStatus(action.getStatus().name());
    responseDTO.setTimestamp(getUtcDateStringFromTimestamp(action.getTimestamp()));
    //if(!action.getUser().equals(DELETE_ACTION_USER))
    responseDTO.setUpdatedBy(action.getUser());
    return responseDTO;
  }

  /**
   * Creates response based on given list of actions
   */
  private ListResponseWrapper createResponse(List<Action> actions) {
    ListResponseWrapper responseList = new ListResponseWrapper();
    for (Action action : actions) {
      ActionResponseDto responseDTO = createResponseDTO(action);
      responseList.add(responseDTO);
    }
    return responseList;
  }


  private Response createArtifactDownloadResponse(ActionArtifact actionartifact) {
    if (actionartifact != null && actionartifact.getArtifact() != null) {
      byte[] artifactsBytes = actionartifact.getArtifact();
      File artifactFile = new File(actionartifact.getArtifactName());
      try (FileOutputStream fos = new FileOutputStream(artifactFile)) {
        fos.write(artifactsBytes);
        fos.close();
      } catch (IOException exception) {
        LOGGER.error(ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG, exception);
        throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
            ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
      }
      Response.ResponseBuilder responseBuilder = Response.ok(artifactFile);
      responseBuilder.header("Content-Disposition",
          "attachment; filename=" + actionartifact.getArtifactName());
      responseBuilder.header("Content-MD5", CalcMD5CheckSum(artifactsBytes));
      responseBuilder.header("Content-Length", artifactFile.length());
      return responseBuilder.build();
    } else {
      throw new ActionException(ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE,
          ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST);
    }
  }

  /**
   * Initialize MDC for logging the current request
   *
   * @param actionInvariantId Action Invariant Id if available (null otherwise)
   * @param servletRequest    Request Contecxt object
   * @param requestType       Current action request (CRUD of Action, Artifact, Version operations)
   */
  private void initializeRequestMDC(HttpServletRequest servletRequest, String actionInvariantId,
                                    ActionRequest requestType) {
    MDC.put(REQUEST_ID, servletRequest.getHeader(X_OPEN_ECOMP_REQUEST_ID_HEADER_PARAM));
    MDC.put(PARTNER_NAME, servletRequest.getRemoteUser());
    MDC.put(INSTANCE_UUID, MDC_ASDC_INSTANCE_UUID);
    MDC.put(SERVICE_METRIC_BEGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
    MDC.put(STATUS_CODE, StatusCode.COMPLETE.name());
    MDC.put(SERVICE_NAME, requestType.name());
    MDC.put(CLIENT_IP, MDC.get(REMOTE_HOST));
    MDC.put(SERVICE_INSTANCE_ID, actionInvariantId);
    MDC.put(LOCAL_ADDR, MDC.get("ServerIPAddress"));
    MDC.put(BE_FQDN, MDC.get("ServerFQDN"));

    if (LOGGER.isDebugEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.DEBUG.name());
    } else if (LOGGER.isInfoEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.INFO.name());
    } else if (LOGGER.isWarnEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.WARN.name());
    } else if (LOGGER.isErrorEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.ERROR.name());
    }
  }

  private String CalcMD5CheckSum(byte[] input) {
    String checksum = null;
    if (input != null) {
      checksum = DigestUtils.md5Hex(input).toUpperCase();
      System.out.println("checksum : " + checksum);
    }
    return checksum;
  }
}
