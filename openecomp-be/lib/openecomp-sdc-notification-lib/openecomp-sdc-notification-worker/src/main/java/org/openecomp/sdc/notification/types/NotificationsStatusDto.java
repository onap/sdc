package org.openecomp.sdc.notification.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author avrahamg
 * @since June 29, 2017
 */
public class NotificationsStatusDto {
    private List<NotificationEntityDto> notifications;
    private List<UUID> newEntries = new ArrayList<>();
    private UUID lastScanned;
    private UUID endOfPage;
    private long numOfNotSeenNotifications;

    public NotificationsStatusDto() {
    }

    public List<NotificationEntityDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(
        List<NotificationEntityDto> notifications) {
        this.notifications = notifications;
    }

    public List<UUID> getNewEntries() {
        return newEntries;
    }

    public void setNewEntries(List<UUID> newEntries) {
        this.newEntries = newEntries;
    }

    public UUID getLastScanned() {
        return lastScanned;
    }

    public void setLastScanned(UUID lastScanned) {
        this.lastScanned = lastScanned;
    }

    public UUID getEndOfPage() {
        return endOfPage;
    }

    public void setEndOfPage(UUID endOfPage) {
        this.endOfPage = endOfPage;
    }

    public long getNumOfNotSeenNotifications() {
        return numOfNotSeenNotifications;
    }

    public void setNumOfNotSeenNotifications(long numOfNotSeenNotifications) {
        this.numOfNotSeenNotifications = numOfNotSeenNotifications;
    }

    @Override
    public String toString() {
        return "NotificationsStatusDto{" +
                "notifications=" + notifications +
                ", newEntries=" + newEntries +
                ", lastScanned=" + lastScanned +
                ", endOfPage=" + endOfPage +
                ", numOfNotSeenNotifications=" + numOfNotSeenNotifications +
                '}';
    }
}
