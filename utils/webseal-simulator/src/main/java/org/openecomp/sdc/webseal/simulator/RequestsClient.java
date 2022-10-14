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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.webseal.simulator.conf.Conf;

public class RequestsClient extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        String adminId = request.getParameter("adminId") != null ? request.getParameter("adminId") : "jh0003";
        String createAll = request.getParameter("all");
        String url = Conf.getInstance().getFeHost() + "/sdc1/feProxy/rest/v1/user";

        PrintWriter writer = response.getWriter();

        int resultCode;

        if ("true".equals(createAll)) {
            Map<String, User> users = Conf.getInstance().getUsers();
            for (User user : users.values()) {
                resultCode = createUser(response, user.getUserId(), user.getRole().toUpperCase(), user.getFirstName(), user.getLastName(),
                    user.getEmail(), url, adminId);
                writer.println("User " + user.getFirstName() + " " + user.getLastName() + getResultMessage(resultCode) + "<br>");
            }
        } else {
            String userId = request.getParameter("userId");
            String role = request.getParameter("role").toUpperCase();
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            resultCode = createUser(response, userId, role, firstName, lastName, email, url, adminId);

            writer.println("User " + firstName + " " + lastName + getResultMessage(resultCode));
        }

    }

    private String getResultMessage(int resultCode) {
        return 201 == resultCode ? " created successfuly" : " not created (" + resultCode + ")";
    }

    private int createUser(final HttpServletResponse response, String userId, String role, String firstName, String lastName, String email,
                           String url, String adminId) throws IOException {
        response.setContentType("text/html");

        String body = "{\"firstName\":\"" + firstName + "\", \"lastName\":\"" + lastName + "\", \"userId\":\"" + userId + "\", \"email\":\"" + email
            + "\",\"role\":\"" + role + "\"}";

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("USER_ID", adminId);
        return sendHttpPost(url, body, headers);
    }

    private int sendHttpPost(String url, String body, Map<String, String> headers) throws IOException {

        String responseString = "";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // add request method
        con.setRequestMethod("POST");

        // add request headers
        if (headers != null) {
            for (Entry<String, String> header : headers.entrySet()) {
                String key = header.getKey();
                String value = header.getValue();
                con.setRequestProperty(key, value);
            }
        }

        // Send post request
        if (body != null) {
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();
        }

        int responseCode = con.getResponseCode();
        // logger.debug("Send POST http request, url: {}", url);
        // logger.debug("Response Code: {}", responseCode);

        StringBuilder response = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            // logger.debug("response body is null");
        }

        String result;

        try {
            result = IOUtils.toString(con.getErrorStream());
            response.append(result);
        } catch (Exception e2) {
        }

        con.disconnect();
        return responseCode;

    }

}
