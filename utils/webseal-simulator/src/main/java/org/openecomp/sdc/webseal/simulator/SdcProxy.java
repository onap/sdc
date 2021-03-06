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

import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.webseal.simulator.conf.Conf;

import javax.net.ssl.SSLContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class SdcProxy extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static URL url;
    private CloseableHttpClient httpClient;
    private Conf conf;
    private final String SDC1 = "/sdc1";
    private final String ONBOARDING = "/onboarding/";
    private final String SCRIPTS = "/scripts";
    private final String STYLES = "/styles";
    private final String LANGUAGES = "/languages";
    private final String CONFIGURATIONS = "/configurations";
    private static final Set<String> RESERVED_HEADERS = Arrays.stream(ReservedHeaders.values()).map(h -> h.getValue()).collect(Collectors.toSet());

    private static final Logger logger = LoggerFactory.getLogger(SdcProxy.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        conf = Conf.getInstance();
        try {
            String feHost = conf.getFeHost();
            url = new URL(feHost);
        } catch (MalformedURLException me) {
            throw new ServletException("Proxy URL is invalid", me);
        }

        try {
            httpClient = buildRestClient();
        } catch (Exception e) {
            throw new ServletException("Build rest client failed", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        proxy(request, response, MethodEnum.GET);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        // Already sign-in
        if (userId == null) {
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

    private synchronized void proxy(HttpServletRequest request, HttpServletResponse response, MethodEnum methodEnum) throws IOException, UnsupportedEncodingException {

        Map<String, String[]> requestParameters = request.getParameterMap();
        String userIdHeader = getUseridFromRequest(request);
        User user = getUser(userIdHeader);

        // new request - forward to login page
        if (userIdHeader == null) {
            System.out.print("Going to login");
            response.sendRedirect("/login");
            return;
        }

        String uri = getUri(request, requestParameters);
        HttpRequestBase httpMethod = createHttpMethod(request, methodEnum, uri);
        addHeadersToMethod(httpMethod, user, request);

        try (CloseableHttpResponse closeableHttpResponse =  httpClient.execute(httpMethod)){;
            response.setStatus(closeableHttpResponse.getStatusLine().getStatusCode());
            if (request.getRequestURI().indexOf(".svg") > -1) {
                response.setContentType("image/svg+xml");
            }

            if(closeableHttpResponse.getEntity() != null) {
                InputStream responseBodyStream = closeableHttpResponse.getEntity().getContent();
                Header contentEncodingHeader = closeableHttpResponse.getLastHeader("Content-Encoding");
                if (contentEncodingHeader != null && contentEncodingHeader.getValue().equalsIgnoreCase("gzip")) {
                    responseBodyStream = new GZIPInputStream(responseBodyStream);
                }
                write(responseBodyStream, response.getOutputStream());
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

    private List<String> getContextPaths() {
        List<String> contextPaths = new ArrayList<>();
        contextPaths.add(SDC1);
        contextPaths.add(ONBOARDING);
        contextPaths.add(STYLES);
        contextPaths.add(SCRIPTS);
        contextPaths.add(LANGUAGES);
        contextPaths.add(CONFIGURATIONS);
        return contextPaths;
    }

    private String getUri(HttpServletRequest request, Map<String, String[]> requestParameters) throws UnsupportedEncodingException {
        String suffix = request.getRequestURI();
        if (getContextPaths().stream().anyMatch(request.getRequestURI()::contains)) {
            suffix = alignUrlProxy(suffix);
        }
        StringBuilder query = alignUrlParameters(requestParameters);
        String uri = String.format("%s%s", new Object[]{this.url.toString() + suffix, query.toString()});
        return uri;
    }

    private HttpRequestBase createHttpMethod(HttpServletRequest request, MethodEnum methodEnum, String uri) throws IOException {
        HttpRequestBase proxyMethod = null;
        ServletInputStream inputStream = null;
        InputStreamEntity entity = null;

        switch (methodEnum) {
            case GET:
                proxyMethod = new HttpGet(uri);
                break;
            case POST:
                proxyMethod = new HttpPost(uri);
                inputStream = request.getInputStream();
                entity = new InputStreamEntity(inputStream, getContentType(request));
                ((HttpPost) proxyMethod).setEntity(entity);
                break;
            case PUT:
                proxyMethod = new HttpPut(uri);
                inputStream = request.getInputStream();
                entity = new InputStreamEntity(inputStream, getContentType(request));
                ((HttpPut) proxyMethod).setEntity(entity);
                break;
            case DELETE:
                proxyMethod = new HttpDelete(uri);
                break;
        }
        return proxyMethod;
    }

    private ContentType getContentType(HttpServletRequest request) {
        String contentTypeStr = request.getContentType();
            if (contentTypeStr == null ){
                contentTypeStr = request.getHeader("contentType");
            }
        ContentType contentType = ContentType.parse(contentTypeStr);
        return ContentType.create(contentType.getMimeType());
    }

    private String getUseridFromRequest(HttpServletRequest request) {

        String userIdHeader = request.getHeader("USER_ID");
        if (userIdHeader != null) {
            return userIdHeader;
        }
        Object o = request.getAttribute("HTTP_IV_USER");
        if (o != null) {
            return o.toString();
        }
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (int i = 0; i < cookies.length; ++i) {
                if (cookies[i].getName().equals("USER_ID")) {
                    userIdHeader = cookies[i].getValue();
                }
            }
        }
        return userIdHeader;
    }

    private static void addHeadersToMethod(HttpUriRequest proxyMethod, User user, HttpServletRequest request) {

        proxyMethod.setHeader(ReservedHeaders.HTTP_IV_USER.name(), user.getUserId());
        proxyMethod.setHeader(ReservedHeaders.USER_ID.name(), user.getUserId());
        proxyMethod.setHeader(ReservedHeaders.HTTP_CSP_FIRSTNAME.name(), user.getFirstName());
        proxyMethod.setHeader(ReservedHeaders.HTTP_CSP_EMAIL.name(), user.getEmail());
        proxyMethod.setHeader(ReservedHeaders.HTTP_CSP_LASTNAME.name(), user.getLastName());
        proxyMethod.setHeader(ReservedHeaders.HTTP_IV_REMOTE_ADDRESS.name(), "0.0.0.0");
        proxyMethod.setHeader(ReservedHeaders.HTTP_CSP_WSTYPE.name(), "Intranet");
		proxyMethod.setHeader(ReservedHeaders.HTTP_CSP_EMAIL.name(), "me@mail.com");

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			if (!RESERVED_HEADERS.contains(headerName)) {
				Enumeration<String> headers = request.getHeaders(headerName);
				while (headers.hasMoreElements()) {
					String headerValue = headers.nextElement();
					proxyMethod.setHeader(headerName, headerValue);
				}
			}
		}
    }

    private String alignUrlProxy(String requestURI) {

        int i = requestURI.indexOf(ONBOARDING);
        if (-1 != i) {
            return requestURI.substring(i);
        }

        i = requestURI.indexOf(SDC1 + SDC1);
        if (-1 != i) {
            return requestURI.substring(SDC1.length());
        }

        i = requestURI.indexOf(SDC1);
        if (-1 != i) {
            return requestURI;
        }

        return SDC1 + requestURI;
    }

    private static StringBuilder alignUrlParameters(Map<String, String[]> requestParameters) throws UnsupportedEncodingException {
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

                query.append(String.format("&%s=%s", new Object[]{name, value}));
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

    enum ReservedHeaders {
        HTTP_IV_USER("HTTP_IV_USER"), USER_ID("USER_ID"), HTTP_CSP_FIRSTNAME("HTTP_CSP_FIRSTNAME"), HTTP_CSP_EMAIL("HTTP_CSP_EMAIL"), HTTP_CSP_LASTNAME("HTTP_CSP_LASTNAME"), HTTP_IV_REMOTE_ADDRESS("HTTP_IV_REMOTE_ADDRESS"), HTTP_CSP_WSTYPE("HTTP_CSP_WSTYPE"), HOST("Host"), CONTENTLENGTH("Content-Length");

        private String value;

        ReservedHeaders(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    private static CloseableHttpClient buildRestClient() throws NoSuchAlgorithmException, KeyStoreException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(),
                NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .build();
    }
}
