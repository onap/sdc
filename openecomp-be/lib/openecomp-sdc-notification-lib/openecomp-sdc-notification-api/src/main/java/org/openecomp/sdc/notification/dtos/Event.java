package org.openecomp.sdc.notification.dtos;

import java.util.Map;

public interface Event {

    String getEventType();

    String getOriginatorId();

    Map<String, Object> getAttributes();

    String getEntityId();
}
