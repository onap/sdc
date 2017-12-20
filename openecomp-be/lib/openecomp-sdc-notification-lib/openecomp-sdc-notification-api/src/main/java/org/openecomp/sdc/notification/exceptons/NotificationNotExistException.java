package org.openecomp.sdc.notification.exceptons;

/**
 * @author avrahamg
 * @since July 02, 2017
 */
public class NotificationNotExistException extends Exception {
    private String message;


    public NotificationNotExistException(String Message){
        this(Message, null);
    }
    public NotificationNotExistException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }
}
