package org.openecomp.config.impl;

import org.openecomp.config.Constants;
import org.openecomp.config.api.Configuration;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/")
public class ConfigurationFilter implements Filter {

  @Override
  public void init(FilterConfig paramFilterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest paramServletRequest, ServletResponse paramServletResponse,
                       FilterChain paramFilterChain) throws IOException, ServletException {
    Configuration.tenant.set(Constants.DEFAULT_TENANT);
    try {
      paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
    } finally {
      Configuration.tenant.remove();
    }
  }

  @Override
  public void destroy() {
  }

}
