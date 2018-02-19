package org.openecomp.sdc.logging.api;

import java.util.HashSet;
import java.util.Set;

public class SpyAuditData implements  AuditData{
    private final Set<String> calledMethods = new HashSet<>();

    @Override
    public long getStartTime() {
        calledMethods.add("getStartTime");
        return 0;
    }

    @Override
    public long getEndTime() {
        calledMethods.add("getEndTime");
        return 0;
    }

    @Override
    public AuditData.StatusCode getStatusCode() {
        calledMethods.add("getEndTime");
        return null;
    }

    @Override
    public String getResponseCode() {
        calledMethods.add("getResponseCode");
        return null;
    }

    @Override
    public String getResponseDescription() {
        calledMethods.add("getResponseDescription");
        return null;
    }

    @Override
    public String getClientIpAddress() {
        calledMethods.add("getClientIpAddress");
        return null;
    }

    public boolean wasCalled(String method) {
        return calledMethods.contains(method);
    }
}
