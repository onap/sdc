package org.openecomp.sdc.common.session.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.session.SessionContext;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.User;

public class AsdcSessionContextProvider implements SessionContextProvider {

  private static final ThreadLocal<String> threadUserId = new ThreadLocal<>();
  private static final ThreadLocal<String> threadTenant = new ThreadLocal<>();

  @Override
  public void create(String userId, String tenant) {
    threadUserId.set(userId);
    threadTenant.set(tenant);
  }

  @Override
  public SessionContext get() {
    if (threadUserId.get() == null) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage("UserId was not set "
          + "for this thread").build());
    }

    if (threadTenant.get() == null) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage("Tenant was not set "
          + "for this thread").build());
    }

    return new AsdcSessionContext(new User(threadUserId.get()), threadTenant.get());
  }

  @Override
  public void close() {
    threadUserId.remove();
    threadTenant.remove();
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
