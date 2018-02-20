package org.openecomp.activityspec.api.server.filters;

import org.openecomp.sdcrests.filters.SessionContextFilter;

import javax.servlet.ServletRequest;

public class ActivitySpecSessionContextFilter extends SessionContextFilter {

  private static final String TENANT = "activity_spec";
  private static final String USER = "activity_spec_USER";

  @Override
  public String getUser(ServletRequest servletRequest) {
    return USER;
  }

  @Override
  public String getTenant(ServletRequest servletRequest) {
    return TENANT;
  }
}
