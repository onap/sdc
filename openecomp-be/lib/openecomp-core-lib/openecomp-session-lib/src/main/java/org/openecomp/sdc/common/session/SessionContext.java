package org.openecomp.sdc.common.session;

public interface SessionContext {

  User getUser();

  String getTenant();
}
