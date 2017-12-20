package org.openecomp.sdc.common.session;

public interface SessionContextProvider {

  void create(String user);

  SessionContext get();

  void close();
}
