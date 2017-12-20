package org.openecomp.server.filters;

import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;

public class SessionContextFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    SessionContextProvider contextProvider =
        SessionContextProviderFactory.getInstance().createInterface();

    try {
      if (servletRequest instanceof HttpServletRequest) {
        String userName = ((HttpServletRequest) servletRequest).getHeader(USER_ID_HEADER_PARAM);
        contextProvider.create(userName);
      }

      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      contextProvider.close();
    }
  }

  @Override
  public void destroy() {

  }
}
