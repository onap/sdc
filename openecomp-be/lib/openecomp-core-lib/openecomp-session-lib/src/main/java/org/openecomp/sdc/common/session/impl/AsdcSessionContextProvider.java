package org.openecomp.sdc.common.session.impl;

import org.openecomp.sdc.common.session.SessionContext;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.User;

public class AsdcSessionContextProvider implements SessionContextProvider {

  private static final ThreadLocal<String> threadUserId = new ThreadLocal<>();

  @Override
  public void create(String userId) {
    threadUserId.set(userId);
  }

  @Override
  public SessionContext get() {
    if (threadUserId.get() == null) {
      throw new RuntimeException("UserId was not set for this thread");
    }

    return new AsdcSessionContext(new User(threadUserId.get()), "dox");
  }

  @Override
  public void close() {
    threadUserId.remove();
  }

  private static class AsdcSessionContext implements SessionContext {

    private final User user;
    private final String tenant;

    private AsdcSessionContext(User user, String tenant) {
      this.user = user;
      this.tenant = tenant;
    }

    @Override
    public User getUser() {
      return user;
    }

    @Override
    public String getTenant() {
      return tenant;
    }
  }
}
