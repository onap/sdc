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

import static org.openecomp.sdcrests.common.RestConstants.LAST_DELIVERED_QUERY_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import javax.validation.constraints.NotNull;

import org.openecomp.sdcrests.notifications.types.NotificationsStatusDto;
import org.openecomp.sdcrests.notifications.types.UpdateNotificationResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1.0/notifications")
@Validated
@Tag(name = "SDCE-1 APIs")
@Tag(name = "Notifications")
public interface Notifications {

    String LIMIT_QUERY_PARAM = "NOTIFICATION_ROWS_LIMIT";
    String END_OF_PAGE_QUERY_PARAM = "END_OF_PAGE_EVENT_ID";

    @GetMapping({ "", "/" })
    @Operation(description = "Retrieve all user notifications", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationsStatusDto.class)))))
    ResponseEntity getNotifications(@NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user,
                                    @RequestParam(name = LAST_DELIVERED_QUERY_PARAM, required = false) UUID lastDelvered, @RequestParam(name = END_OF_PAGE_QUERY_PARAM, required = false) UUID endOfPage);

    @PutMapping("/{notificationId}")
    @Operation(description = "Mark notification as read", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = UpdateNotificationResponseStatus.class))))
    ResponseEntity markAsRead(@Parameter(description = "Notification Id") @PathVariable("notificationId") String notificationId,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user)
        throws InvocationTargetException, IllegalAccessException;

    @PutMapping("/last-seen/{notificationId}")
    @Operation(description = "Update Last Seen Notification", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = UpdateNotificationResponseStatus.class))))
    ResponseEntity updateLastSeenNotification(@Parameter(description = "Notification Id") @PathVariable("notificationId") String notificationId,
                                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user)
        throws InvocationTargetException, IllegalAccessException;

    @GetMapping("/worker")
    @Operation(description = "Retrive user not delivered notifications", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationsStatusDto.class)))))
    ResponseEntity getNewNotificationsByOwnerId(@NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user,
                                          @RequestParam(name = LAST_DELIVERED_QUERY_PARAM, required = false) String eventId, @RequestParam(LIMIT_QUERY_PARAM) String limit);
}
