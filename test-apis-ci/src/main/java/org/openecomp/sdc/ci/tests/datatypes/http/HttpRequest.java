/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.datatypes.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.protocol.https.DefaultHostnameVerifier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequest {
	static Logger logger = LoggerFactory.getLogger(HttpRequest.class.getName());

//	-----------------------------Http------------------------------------------------------------------------
	public RestResponse httpSendGetInternal(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		url = url.replaceAll("\\s", "%20");
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");
		addHttpRequestHEaders(headers, con);

		int responseCode = con.getResponseCode();
		logger.debug("Send GET http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = new StringBuffer();
		String result;
		try {

			result = IOUtils.toString(con.getInputStream());
			response.append(result);
		} catch (Exception e) {
			logger.debug("Fail with exception", e);
		}
		try {
			result = IOUtils.toString(con.getErrorStream());
			response.append(result);
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}

		logger.debug("Response body: {}" ,response);

		// print result
		setHttpResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}

	public RestResponse httpSendByMethodInternal(String url, String method, String body, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
// add request method
		con.setRequestMethod(method);
// add request headers
		addHttpRequestHEaders(headers, con);
		if (body != null && !body.isEmpty() && !method.equals("DELETE")) {
// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(body);
			wr.flush();
			wr.close();
		}

		int responseCode = con.getResponseCode();
		logger.debug("Send {} http request, url: {}",method,url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = generateHttpResponse(con);
		String result;
		try {
			result = IOUtils.toString(con.getErrorStream());
			response.append(result);
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}
		logger.debug("Response body: {}",response);
// print result
		setHttpResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}

	public RestResponse httpSendDelete(String url, Map<String, String> headers) throws IOException {
		if (url.matches("^(https)://.*$")){
			return httpsSendDelete(url, headers);
		}
		return httpSendDeleteInternal(url, headers);
	}

	public RestResponse httpSendGet(String url, Map<String, String> headers) throws IOException {
		if (url.matches("^(https)://.*$")){
			return httpsSendGet(url, headers);
		}
		return httpSendGetInternal(url, headers);
	}

	public RestResponse httpSendByMethod(String url, String method, String body, Map<String, String> headers) throws IOException {
		if (url.matches("^(https)://.*$")){
			return httpsSendByMethod(url, method, body, headers);
		}
		return httpSendByMethodInternal(url, method, body, headers);
	}

	public RestResponse httpSendPost(String url, String body, Map<String, String> headers) throws IOException {
		if (url.matches("^(https)://.*$")){
			return httpsSendByMethod(url, "POST", body, headers);
		}
		return httpSendByMethod(url, "POST", body, headers);
	}

	public RestResponse httpSendPut(String url, String body, Map<String, String> headers) throws IOException {
		if (url.matches("^(https)://.*$")){
			return httpsSendByMethod(url, "PUT", body, headers);
		}
		return httpSendByMethod(url, "PUT", body, headers);
	}


	public RestResponse httpSendDeleteInternal(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		addHttpRequestHEaders(headers, con);

		con.setDoOutput(true);
		con.setRequestMethod("DELETE");
		int responseCode = con.getResponseCode();
		logger.debug("Send DELETE http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = generateHttpResponse(con);
		String result;
		try {
			result = IOUtils.toString(con.getErrorStream());
			response.append(result);
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}
		logger.debug("Response body: {}",response);

// print result
		setHttpResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}

	public static RestResponse sendHttpPostWithEntity(HttpEntity requestEntity, String url, Map<String, String> headers)
			throws IOException, ClientProtocolException {
		CloseableHttpResponse response = null;
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			RestResponse restResponse = new RestResponse();
			for (Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey(), entry.getValue());
			}

			httpPost.setEntity(requestEntity);
			response = client.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			String responseBody = null;
			if (responseEntity != null) {
				InputStream instream = responseEntity.getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(instream, writer);
				responseBody = writer.toString();
				try {

				} finally {
					instream.close();
				}
			}

			restResponse.setErrorCode(response.getStatusLine().getStatusCode());
			restResponse.setResponse(responseBody);

			return restResponse;

		} finally {
			closeResponse(response);
			closeHttpClient(client);

		}
	}

	private static void closeHttpClient(CloseableHttpClient client) {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			logger.debug("failed to close client or response: ", e);
		}
	}

	private static void closeResponse(CloseableHttpResponse response) {
		try {
			if (response != null) {
				response.close();
			}
		} catch (IOException e) {
			logger.debug("failed to close client or response: ", e);
		}
	}


	//	-----------------------------Https------------------------------------------------------------------------
	public RestResponse httpsSendGet(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		url = url.replaceAll("\\s", "%20");
		URL obj = new URL(null, url, new sun.net.www.protocol.https.Handler());
		HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
// optional default is GET
		con.setRequestMethod("GET");
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
		con.setHostnameVerifier(hostnameVerifier);
		addHttpsRequestHeaders(headers, con);

		int responseCode = con.getResponseCode();
		logger.debug("Send GET http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = generateHttpsResponse(con);
		String result;
		try {
			if(con.getErrorStream()!=null) {
				result = IOUtils.toString(con.getErrorStream());
				response.append(result);
			}
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}
		logger.debug("Response body: {}",response);
// print result
		setHttpsResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}


	public RestResponse httpsSendPost(String url, String body, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(null, url, new sun.net.www.protocol.https.Handler());
		HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
		con.setHostnameVerifier(hostnameVerifier);
// add request method
		con.setRequestMethod("POST");
// add request headers
		addHttpRequestHEaders(headers, con);
// Send post request
		if (body != null) {
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(body);
			wr.flush();
			wr.close();
		}
		int responseCode = con.getResponseCode();
		logger.debug("Send POST http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = generateHttpResponse(con);
		String result;
		try {
			if(con.getErrorStream()!=null) {
				result = IOUtils.toString(con.getErrorStream());
				response.append(result);
			}
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}
		logger.debug("Response body: {}",response);
// print result
		setHttpResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}

	public RestResponse httpsSendByMethod(String url, String method, String body, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(null, url, new sun.net.www.protocol.https.Handler());
		HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
		con.setHostnameVerifier(hostnameVerifier);
// add request method
		con.setRequestMethod(method);
// add request headers
		addHttpRequestHEaders(headers, con);
		if (body != null && !body.isEmpty() && !method.equals("DELETE")) {
// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(body);
			wr.flush();
			wr.close();
		}

		int responseCode = con.getResponseCode();
		logger.debug("Send {} http request, url: {}",method,url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = generateHttpResponse(con);
		String result;
		try {
			if(con.getErrorStream()!=null) {
				result = IOUtils.toString(con.getErrorStream());
				response.append(result);
			}
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}
		logger.debug("Response body: {}",response);
// print result
		setHttpResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}


	public RestResponse httpsSendDelete(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(null, url, new sun.net.www.protocol.https.Handler());
		HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
		HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
		con.setHostnameVerifier(hostnameVerifier);
// add request headers
		addHttpRequestHEaders(headers, con);

		con.setDoOutput(true);
		con.setRequestMethod("DELETE");
		int responseCode = con.getResponseCode();
		logger.debug("Send DELETE http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = generateHttpResponse(con);
		String result;
		try {
			if(con.getErrorStream()!=null) {
				result = IOUtils.toString(con.getErrorStream());
				response.append(result);
			}
		} catch (Exception e) {
//			logger.debug("Fail with exception", e);
		}
		logger.debug("Response body: {}",response);
// print result
		setHttpResponseToObject(restResponse, con, responseCode, response);
		con.disconnect();

		return restResponse;
	}

	//	---------------------------------------
	private void addHttpsRequestHeaders(Map<String, String> headers, HttpsURLConnection con) {
		// add request header
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}
	}

	private void addHttpRequestHEaders(Map<String, String> headers, HttpURLConnection con) {
		// add request header
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}
	}

	private void setHttpResponseToObject(RestResponse restResponse, HttpURLConnection con, int responseCode, StringBuffer response) throws IOException {
		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);
	}

	private StringBuffer generateHttpResponse(HttpURLConnection con) {
		StringBuffer response = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			logger.debug("response body is null");
		}
		return response;
	}

	private void setHttpsResponseToObject(RestResponse restResponse, HttpsURLConnection con, int responseCode, StringBuffer response) throws IOException {
		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		restResponse.setErrorCode(responseCode);
		// restResponse.setResponse(result);
		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);
	}

	private StringBuffer generateHttpsResponse(HttpsURLConnection con) {
		StringBuffer response = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			logger.debug("response body is null");
		}
		return response;
	}


	@NotThreadSafe
	class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
		public static final String METHOD_NAME = "DELETE";

		public String getMethod() {
			return METHOD_NAME;
		}

		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}

		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}

		public HttpDeleteWithBody() {
			super();
		}
	}

}
