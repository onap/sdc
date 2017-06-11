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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.webseal.simulator.conf.Conf;

public class RequestsClient extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		String hostname = request.getParameter("hostname") != null ? request.getParameter("hostname") : "127.0.0.1";
		String port = request.getParameter("port") != null ? request.getParameter("port") : "8080";
		String adminId = request.getParameter("adminId") != null ? request.getParameter("adminId") : "jh0003";

		String createAll = request.getParameter("all");
		
		PrintWriter writer = response.getWriter();
		
		int resultCode;
		
		if ("true".equals(createAll)) {
			Map<String, User> users = Conf.getInstance().getUsers();
			for (User user : users.values()) {
				resultCode = createUser(response, user.getUserId(), user.getRole().toUpperCase(), user.getFirstName(), user.getLastName(), user.getEmail(), hostname, port, adminId);
				writer.println("User "+ user.getFirstName() + " " + user.getLastName() + getResultMessage(resultCode) + "<br>");
			}
		} else {
			String userId = request.getParameter("userId");
			String role = request.getParameter("role").toUpperCase();
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String email = request.getParameter("email");
			resultCode = createUser(response, userId, role, firstName, lastName, email, hostname, port, adminId);
			writer.println("User "+ firstName + " " + lastName +getResultMessage(resultCode));	
		}

		

	}
	
	private String getResultMessage(int resultCode){
		return 201 == resultCode? " created successfuly":" not created ("+ resultCode +")";
	}

	private int createUser(final HttpServletResponse response, String userId, String role, String firstName, String lastName, String email, String hostname, String port, String adminId) throws IOException {
		response.setContentType("text/html");

		// Fill the data of the request
		String url = "http://" + hostname + ":" + port + "/sdc2/rest/v1/user";
		String body = "{'firstName':'" + firstName + "', 'lastName':'" + lastName + "', 'userId':'" + userId + "', 'email':'" + email + "','role':'" + role + "'}";
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

		StringBuffer response = new StringBuffer();
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
			result = null;
		}
		// logger.debug("Response body: {}", response);

		if (response != null) {
			responseString = response.toString();
		}

		// Map<String, List<String>> headerFields = con.getHeaderFields();
		// String responseMessage = con.getResponseMessage();

		con.disconnect();
		return responseCode;

	}

}
