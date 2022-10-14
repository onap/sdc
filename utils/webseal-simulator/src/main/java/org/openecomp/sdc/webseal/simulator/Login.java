/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.webseal.simulator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.webseal.simulator.conf.Conf;

public class Login extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        if (null != request.getParameter("userId")) {
            doPost(request, response);
            return;
        }
        logger.info("about to build login page");
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

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

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        request.setAttribute("message", "OK");

        logger.info("Login -> doPost userId={}", userId);
        User user = getUser(userId, password);
        if (user == null) {
            response.sendError(500, "ERROR: userId or password incorrect");
        } else {
            logger.info("Login -> doPost redirect to /sdc1 (to proxy)");
            response.addCookie(new Cookie("HTTP_IV_USER", user.getUserId()));
            response.addCookie(new Cookie("USER_ID", user.getUserId()));
            response.addCookie(new Cookie("HTTP_CSP_FIRSTNAME", user.getFirstName()));
            response.addCookie(new Cookie("HTTP_CSP_EMAIL", user.getEmail()));
            response.addCookie(new Cookie("HTTP_CSP_LASTNAME", user.getLastName()));
            response.addCookie(new Cookie("HTTP_IV_REMOTE_ADDRESS", "0.0.0.0"));
            response.addCookie(new Cookie("HTTP_CSP_WSTYPE", "Intranet"));
            response.addCookie(new Cookie(Conf.getInstance().getPortalCookieName(), "portal"));
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
