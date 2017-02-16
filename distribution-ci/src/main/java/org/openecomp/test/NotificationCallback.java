package org.openecomp.test;

import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.INotificationData;

public class NotificationCallback extends SimpleCallback{
	INotificationData latestCallbackData;
	public INotificationData getData() {
		return latestCallbackData;
	}
	public NotificationCallback(IDistributionClient client) {
		super(client);
	}
	
	public void activateCallback(INotificationData data) {
		this.latestCallbackData = data;
		super.activateCallback(data);
	}
}
