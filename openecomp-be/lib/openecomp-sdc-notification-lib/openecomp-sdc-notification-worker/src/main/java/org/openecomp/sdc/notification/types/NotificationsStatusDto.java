package org.openecomp.sdc.notification.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author avrahamg
 * @since June 29, 2017
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NotificationsStatusDto {

    private List<NotificationEntityDto> notifications;
    private List<UUID> newEntries = new ArrayList<>();
    private UUID lastScanned;
    private UUID endOfPage;
    private long numOfNotSeenNotifications;
}
