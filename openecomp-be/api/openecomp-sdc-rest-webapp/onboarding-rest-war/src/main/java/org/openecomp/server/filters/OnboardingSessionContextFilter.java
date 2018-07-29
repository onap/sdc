package org.openecomp.server.filters;

import org.openecomp.sdcrests.filters.SessionContextFilter;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;

public class OnboardingSessionContextFilter extends SessionContextFilter {

  @Override
  public String getUser(ServletRequest servletRequest) {
    return ((HttpServletRequest) servletRequest).getHeader(USER_ID_HEADER_PARAM);
  }

  @Override
  public String getTenant(ServletRequest servletRequest) {
    return "dox";
  }
}
