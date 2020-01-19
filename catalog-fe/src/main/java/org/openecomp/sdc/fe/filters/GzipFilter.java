package org.openecomp.sdc.fe.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GzipFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException,
            ServletException {

      
        final HttpServletResponse httpResponse = (HttpServletResponse) response;


        httpResponse.setHeader("Content-Encoding", "gzip");
        httpResponse.setHeader("Content-Type", "application/javascript");
        chain.doFilter(request, response);
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}