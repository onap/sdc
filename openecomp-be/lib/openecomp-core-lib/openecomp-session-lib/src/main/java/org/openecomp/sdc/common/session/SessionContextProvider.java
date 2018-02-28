package org.openecomp.sdc.common.session;

public interface SessionContextProvider {

  void create(String user, String tenant);

  SessionContext get();

  void close();
}
