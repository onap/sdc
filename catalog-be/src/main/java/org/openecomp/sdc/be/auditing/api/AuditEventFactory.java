package org.openecomp.sdc.be.auditing.api;

import java.util.List;

import org.javatuples.Pair;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;

public interface AuditEventFactory {

    String getLogMessage();
    AuditingGenericEvent getDbEvent();
    List<Pair<String, String>> getQueryParams();

    //TODO remove together with ES related code
    String getAuditingEsType();
}
