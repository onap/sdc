package org.openecomp.sdc.webseal.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.openecomp.sdc.webseal.simulator.conf.Conf;

public class SdcProxy extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private URL url;
	private HttpClient proxy;
	private Conf conf;
	
	private final String SDC1 = "/sdc1";
	private final String ONBOARDING = "/onboarding/";
	private final String SCRIPTS = "/scripts";
	private final String STYLES = "/styles";
	private final String LANGUAGES = "/languages";
	

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		conf = Conf.getInstance();
		try {
			String feHost = conf.getFeHost();
			this.url = new URL(feHost);
		} catch (MalformedURLException me) {
			throw new ServletException("Proxy URL is invalid", me);
		}
		this.proxy = new HttpClient();
		this.proxy.getHostConfiguration().setHost(this.url.getHost());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		proxy(request, response, MethodEnum.GET);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String userId = request.getParameter("userId");
		String password = request.getParameter("password");

		// Already sign-in
		if (userId == null){
			userId = request.getHeader("USER_ID");
		}

		System.out.println("SdcProxy -> doPost userId=" + userId);
		request.setAttribute("message", "OK");
		if (password != null && getUser(userId, password) == null) {
			MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
			RequestDispatcher view = request.getRequestDispatcher("login");
			request.setAttribute("message", "ERROR: userid or password incorect");
			view.forward(mutableRequest, response);
		} else {
			System.out.println("SdcProxy -> doPost going to doGet");
			request.setAttribute("HTTP_IV_USER", userId);
			proxy(request, response, MethodEnum.POST);
		}
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		proxy(request, response, MethodEnum.PUT);
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		proxy(request, response, MethodEnum.DELETE);
	}

	private synchronized void proxy(HttpServletRequest request, HttpServletResponse response, MethodEnum methodEnum) throws IOException, UnsupportedEncodingException, HttpException {
		Map<String, String[]> requestParameters = request.getParameterMap();
		System.out.print(request.getRequestURI() + " -> ");

		String userIdHeader = getUseridFromRequest(request);
		//System.out.print(" (userIdHeader=" + userIdHeader + ") ");
		User user = getUser(userIdHeader);

		// new request - forward to login page
		if (userIdHeader == null) {
			System.out.print("Going to login");
			response.sendRedirect("/login");
			return;
		}

		String uri = getUri(request, requestParameters);
		HttpMethodBase proxyMethod = getMethod(request, methodEnum, uri);
		System.out.println(uri);
		addHeaders(user, proxyMethod);
		addHeaders(request, proxyMethod);
		this.proxy.executeMethod(proxyMethod);
		response.setStatus(proxyMethod.getStatusCode());
		
		if (request.getRequestURI().indexOf(".svg") > -1) {
			response.setContentType("image/svg+xml");
		}
		
		InputStream responseBodyStream = proxyMethod.getResponseBodyAsStream();
		Header contentEncodingHeader = proxyMethod.getResponseHeader("Content-Encoding");
		if (contentEncodingHeader != null && contentEncodingHeader.getValue().equalsIgnoreCase("gzip")) {
			responseBodyStream = new GZIPInputStream(responseBodyStream);
		}
		write(responseBodyStream, response.getOutputStream());
	}

	private void addHeaders(HttpServletRequest request, HttpMethodBase proxyMethod) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			Enumeration<String> headers = request.getHeaders(headerName);
			while (headers.hasMoreElements()) {
				String headerValue = headers.nextElement();
				proxyMethod.addRequestHeader(headerName, headerValue);
			}
		}
	}

	private User getUser(String userId, String password) {
		User user = getUser(userId);
		if (user.getPassword().equals(password)) {
			return user;
		}
		return null;
	}

	private User getUser(String userId) {
		return conf.getUsers().get(userId);

	}
	
	private List<String> getContextPaths(){
		 List<String> contextPaths = new ArrayList<>();
		 contextPaths.add(SDC1);
		 contextPaths.add(ONBOARDING);
		 contextPaths.add(STYLES);
		 contextPaths.add(SCRIPTS);
		 contextPaths.add(LANGUAGES);
		 return contextPaths;
	}

	private String getUri(HttpServletRequest request, Map<String, String[]> requestParameters) throws UnsupportedEncodingException {
		String suffix = request.getRequestURI();
		if (getContextPaths().stream().anyMatch(request.getRequestURI()::contains))	{
			suffix = alignUrlProxy(suffix);
		}
		StringBuilder query = alignUrlParameters(requestParameters);
		String uri = String.format("%s%s", new Object[] {this.url.toString() + suffix, query.toString() });
		return uri;
	}

	private HttpMethodBase getMethod(HttpServletRequest request, MethodEnum methodEnum, String uri) throws IOException {
		HttpMethodBase proxyMethod = null;
		switch (methodEnum) {
		case GET:
			proxyMethod = new GetMethod(uri);
			break;
		case POST:
			proxyMethod = new PostMethod(uri);
			((PostMethod) proxyMethod).setRequestEntity(new InputStreamRequestEntity(request.getInputStream()));
			break;
		case PUT:
			proxyMethod = new PutMethod(uri);
			((PutMethod) proxyMethod).setRequestBody(getBody(request));
			break;
		case DELETE:
			proxyMethod = new DeleteMethod(uri);
			break;
		}
		return proxyMethod;
	}

	private String getUseridFromRequest(HttpServletRequest request) {

		String userIdHeader = request.getHeader("USER_ID");
		if (userIdHeader != null){
			return userIdHeader;
		}
		Object o = request.getAttribute("HTTP_IV_USER");
		if (o != null) {
			return o.toString();
		}
		Cookie[] cookies = request.getCookies();

		if (cookies != null){
			for (int i=0; i<cookies.length; ++i){
				if (cookies[i].getName().equals("USER_ID")){
					userIdHeader = cookies[i].getValue(); 
				}
			}
		}
		return userIdHeader;
	}
	
	private void addHeaders(User user, HttpMethodBase proxyMethod) {
		proxyMethod.addRequestHeader("HTTP_IV_USER", user.getUserId());
		proxyMethod.addRequestHeader("USER_ID", user.getUserId());
		proxyMethod.addRequestHeader("HTTP_CSP_FIRSTNAME", user.getFirstName());
		proxyMethod.addRequestHeader("HTTP_CSP_EMAIL", user.getEmail());
		proxyMethod.addRequestHeader("HTTP_CSP_LASTNAME", user.getLastName());
		proxyMethod.addRequestHeader("HTTP_IV_REMOTE_ADDRESS", "0.0.0.0");
		proxyMethod.addRequestHeader("HTTP_CSP_WSTYPE", "Intranet");
		proxyMethod.addRequestHeader("HTTP_CSP_EMAIL", "me@mail.com");
	}

	private String alignUrlProxy(String requestURI) {
		
		int i = requestURI.indexOf(ONBOARDING);
		if (-1 != i){
			return requestURI.substring(i);
		}
		
		i = requestURI.indexOf(SDC1+SDC1);
		if (-1 != i){
			return requestURI.substring(SDC1.length());
		}
		
		i = requestURI.indexOf(SDC1);
		if (-1 != i){
			return requestURI;
		}
		
		return SDC1+requestURI;
	}

	private StringBuilder alignUrlParameters(Map<String, String[]> requestParameters) throws UnsupportedEncodingException {
		StringBuilder query = new StringBuilder();
		for (String name : requestParameters.keySet()) {
			for (String value : (String[]) requestParameters.get(name)) {
				if (query.length() == 0) {
					query.append("?");
				} else {
					query.append("&");
				}
				name = URLEncoder.encode(name, "UTF-8");
				value = URLEncoder.encode(value, "UTF-8");

				query.append(String.format("&%s=%s", new Object[] { name, value }));
			}
		}
		return query;
	}

	private void write(InputStream inputStream, OutputStream outputStream) throws IOException {
		int b;
		while (inputStream != null && (b = inputStream.read()) != -1) {
			outputStream.write(b);
		}
		outputStream.flush();
	}

	public String getServletInfo() {
		return "Http Proxy Servlet";
	}
	
	
	public String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}
}
