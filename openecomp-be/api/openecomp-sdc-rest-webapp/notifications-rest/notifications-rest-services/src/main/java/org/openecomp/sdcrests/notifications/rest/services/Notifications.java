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

package org.openecomp.sdcrests.notifications.rest.services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.notifications.types.NotificationsStatusDto;
import org.openecomp.sdcrests.notifications.types.UpdateNotificationResponseStatus;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.openecomp.sdcrests.common.RestConstants.LAST_DELIVERED_QUERY_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Notifications")
@Validated
public interface Notifications {
  String LIMIT_QUERY_PARAM = "NOTIFICATION_ROWS_LIMIT";
  String END_OF_PAGE_QUERY_PARAM = "END_OF_PAGE_EVENT_ID";

  @GET
  @ApiOperation(value = "Retrive all user notifications",
      response = NotificationsStatusDto.class,
      responseContainer = "List")
  Response getNotifications(
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user,
      @QueryParam(LAST_DELIVERED_QUERY_PARAM) UUID lastDelvered,
      @QueryParam(END_OF_PAGE_QUERY_PARAM) UUID endOfPage);

  @PUT
  @Path("/{notificationId}")
  @ApiOperation(value = "Mark notification as read",
      response = UpdateNotificationResponseStatus.class)
  Response markAsRead(
      @ApiParam(value = "Notification Id") @PathParam("notificationId") String notificationId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws InvocationTargetException, IllegalAccessException;

  @PUT
  @Path("/last-seen/{notificationId}")
  @ApiOperation(value = "Update Last Seen Notification",
      response = UpdateNotificationResponseStatus.class)
  Response updateLastSeenNotification(
      @ApiParam(value = "Notification Id") @PathParam("notificationId") String notificationId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws InvocationTargetException, IllegalAccessException;

  @GET
  @Path("/worker")
  @ApiOperation(value = "Retrive user not delivered notifications",
      response = NotificationsStatusDto.class,
      responseContainer = "List")
  Response getNewNotificationsByOwnerId(
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user,
      @QueryParam(LAST_DELIVERED_QUERY_PARAM) String eventId,
      @QueryParam(LIMIT_QUERY_PARAM) String limit);

}
