package org.openecomp.sdcrests.filters;

import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public abstract class SessionContextFilter implements Filter {
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

        contextProvider.create(getUser(servletRequest),getTenant(servletRequest));
      }

      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      contextProvider.close();
    }
  }

  @Override
  public void destroy() {

  }

  public abstract String getUser(ServletRequest servletRequest);

  public abstract String getTenant(ServletRequest servletRequest);
}
