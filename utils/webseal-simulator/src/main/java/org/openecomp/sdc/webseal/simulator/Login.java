package org.openecomp.sdc.webseal.simulator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openecomp.sdc.webseal.simulator.conf.Conf;

public class Login extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		if (null != request.getParameter("userId")) {
			doPost(request, response);
			return;
		}
		System.out.println("about to build login page");
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		String message = (String) request.getAttribute("message");
		if (message == null) {
			message = "";
		}

		Collection<User> allUsers = Conf.getInstance().getUsers().values();
		writer.println("<html>");
		
		writer.println("<head>");
		writer.println("<style>");
		writer.println("body {padding: 40px; font-family: Arial; font-size: 14px;}");
		writer.println("h1 {background-color: #DDDDDD; padding: 4px 10px;}");
		writer.println("h2 {margin-top: 20px;}");
		writer.println(".label {width: 100px; float:left;}");
		writer.println(".break {display: block; margin-bottom: 10px;}");
		writer.println("tr {padding: 4px 10px;}");
		writer.println("th {padding: 4px 10px; text-align: left; background-color: #dddddd;}");
		writer.println("td {padding: 4px 10px; text-align: left;}");
		writer.println("</style>");	
		writer.println("</head>");
		
		writer.println("<body>");
		
		writer.println("<h1>Webseal simulator</h1>");
		writer.println("<h2>Login:</h2>");
		
		writer.println("<form action=\"\" method=\"post\">");
		writer.println("  <div class='label'>User id:</div>");
		writer.println("  <input type='text' name='userId'>");
		writer.println("  <div class='break'></div>");
		
		writer.println("  <div class='label'>Password:</div>");
		writer.println("  <input type='password' name='password'>");
		writer.println("  <div class='break'></div>");
		
		writer.println("  <input type='submit' value='Login'>");
		writer.println("  <label name='message'></label>");
		writer.println("</form>");
		
		writer.println("<hr/>");
		writer.println("<h2>Quick links:</h2>");
		writer.println("<table>");
		writer.println("<tr>");
		writer.println("<th>full name</th>");
		writer.println("<th>user id</th>");
		writer.println("<th>role</th>");
		writer.println("<th>action</th>");
		writer.println("</tr>");
		Iterator<User> iterator = allUsers.iterator();
		while (iterator.hasNext()) {
			User user = iterator.next();
			writer.println("<tr>");
			writer.println("<td>" + user.getUserRef() + "</td>");
			writer.println("<td>" + user.getUserId() + "</td>");
			writer.println("<td>" + user.getRole() + "</td>");
			writer.println("<td>" + user.getUserCreateRef() + "</td>");
			writer.println("</tr>");
		}
		writer.println("</table>");	

		writer.println("<a href='create?all=true' target='resultFrame'>Create All</a>");
		writer.println("<hr/><iframe name='resultFrame' width='400' height='300'></iframe>");	
		
		writer.println("</body>");
		writer.println("</html>");
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String userId = request.getParameter("userId");
		String password = request.getParameter("password");
		request.setAttribute("message", "OK");

		System.out.println("Login -> doPOst userId=" + userId);
		User user = getUser(userId, password);
		if (user == null) {
			response.sendError(500, "ERROR: userId or password incorect");
//			doGet(request, response);
		} else {
			System.out.println("Login -> doPOst redirext to /sdc1 (to proxy)");
			Cookie cookieUser = new Cookie("HTTP_IV_USER", user.getUserId());
			Cookie cookieUserId = new Cookie("USER_ID", user.getUserId());
			Cookie cookieFirstName = new Cookie("HTTP_CSP_FIRSTNAME", user.getFirstName());
			Cookie cookieEmail = new Cookie("HTTP_CSP_EMAIL", user.getEmail());
			Cookie cookieLastName = new Cookie("HTTP_CSP_LASTNAME", user.getLastName());
			Cookie cookieRemoteAddress = new Cookie("HTTP_IV_REMOTE_ADDRESS", "0.0.0.0");
			Cookie cookieWsType = new Cookie("HTTP_CSP_WSTYPE", "Intranet");
			response.addCookie(cookieUser);
			response.addCookie(cookieUserId);
			response.addCookie(cookieFirstName);
			response.addCookie(cookieEmail);
			response.addCookie(cookieLastName);
			response.addCookie(cookieRemoteAddress);
			response.addCookie(cookieWsType);
			response.sendRedirect("/sdc1");
		}

	}

	private User getUser(String userId, String password) {
		User user = Conf.getInstance().getUsers().get(userId);
		if (user == null) {
			return null;
		}
		if (!password.equals(user.getPassword())) {
			return null;
		}
		return user;
	}

	@Override
	public String getServletInfo() {
		return "Http Proxy Servlet";
	}
}
