package org.openecomp.sdc.versioning.types;

/**
 * @author avrahamg
 * @since July 10, 2017
 */
public enum NotificationEventTypes {
    SUBMIT("submit"),
    COMMIT("commit");

    private String eventName;

    NotificationEventTypes(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
