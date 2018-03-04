package org.openecomp.sdc.be.components.distribution.engine;

public interface INotificationHandler {
    /**
     * Allows to handle received topic message
     * @param notification
     * @return true if finished successfully otherwise false
     */
    public boolean handleMessage(String notification);

}
