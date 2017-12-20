package org.openecomp.sdc.itempermissions.servlet;

import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by ayalaben on 6/27/2017.
 */
public class PermissionsFilter implements Filter {

  private final PermissionsServices permissionsServices;
  public static final String IRRELEVANT_REQUEST = "Irrelevant_Request";
  public static final String EDIT_ITEM = "Edit_Item";

  public PermissionsFilter() {
    this.permissionsServices = PermissionsServicesFactory.getInstance().createInterface();
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {

    if (servletRequest instanceof HttpServletRequest) {
      if (((HttpServletRequest) servletRequest).getMethod().equals("POST")
            ||  ((HttpServletRequest) servletRequest).getMethod().equals("PUT")) {

        String userId = ((HttpServletRequest) servletRequest).getHeader("USER_ID");
        String itemId = parseItemIdFromPath(((HttpServletRequest) servletRequest).getPathInfo());
        if ( ! itemId.equals(IRRELEVANT_REQUEST)) {
          if ( !  permissionsServices.isAllowed(itemId,userId,EDIT_ITEM)) {
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.getWriter().print("Permissions Error. The user does not have " +
                "permission to perform" +
                " this action.");
           return;
          }
        }
      }
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  private String parseItemIdFromPath(String pathInfo) {
    String[] tokens = pathInfo.split("/");
    if (tokens.length < 4) {
      return IRRELEVANT_REQUEST;
    } else {
      return tokens[3];
    }
  }

  @Override
  public void destroy() {

  }
}
