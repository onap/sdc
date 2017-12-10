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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class HttpRequest {
	static Logger logger = LoggerFactory.getLogger(HttpRequest.class.getName());
	
	public RestResponse httpSendGet(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		url = url.replaceAll("\\s", "%20");
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");
		// add request header
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}

		int responseCode = con.getResponseCode();
		logger.debug("Send GET http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = new StringBuffer();
		String result;

		try {

			result = IOUtils.toString(con.getInputStream());
			response.append(result);

		} catch (Exception e) {			
		}

		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e) {
		}

		logger.debug("Response body: {}" ,response);

		// print result

		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		restResponse.setErrorCode(responseCode);
		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();

		return restResponse;
	}

	public RestResponse httpsSendGet(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");
		// add request header
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}

		int responseCode = con.getResponseCode();
		logger.debug("Send GET http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

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

		String result;

		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			// result = null;
		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		restResponse.setErrorCode(responseCode);
		// restResponse.setResponse(result);
		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();

		return restResponse;
	}

	public RestResponse httpSendByMethod(String url, String method, String body, Map<String, String> headers)
			throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request method
		con.setRequestMethod(method);

		// add request headers
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}
		if (body != null && !body.isEmpty() && !method.equals("DELETE")) {
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(body);
			wr.flush();
			wr.close();
		}

		// con.connect();

		int responseCode = con.getResponseCode();
		logger.debug("Send {} http request, url: {}",method,url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			// response = null;
			logger.debug("response body is null");
		}

		String result;
		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			result = null;
		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);
		// if (response == null) {
		// restResponse.setResponse(null);
		// } else {
		// restResponse.setResponse(response.toString());
		// }

		if (response != null) {
			restResponse.setResponse(response.toString());
		}
		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();
		return restResponse;

	}

	public RestResponse sendHttpPost(String url, String body, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
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

		// con.connect();

		int responseCode = con.getResponseCode();
		logger.debug("Send POST http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

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

		String result;

		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			result = null;
		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();
		return restResponse;

	}

	public RestResponse httpSendPost(String url, String body, Map<String, String> headers) throws IOException {
		return httpSendPost(url, body, headers, "POST");
	}

	public RestResponse httpSendPut(String url, String body, Map<String, String> headers) throws IOException {
		return httpSendPost(url, body, headers, "PUT");
	}

	public RestResponse httpSendPost(String url, String body, Map<String, String> headers, String methodType)
			throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request method
		con.setRequestMethod(methodType);

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

		// con.connect();

		int responseCode = con.getResponseCode();
		logger.debug("Send POST http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

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

		String result;

		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			result = null;
		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();
		return restResponse;

	}

	public RestResponse httpSendDeleteWithBody2(String url, String body, Map<String, String> headers)
			throws ClientProtocolException, IOException {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		RestResponse restResponse = new RestResponse();
		HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);

		// add request headers
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				httpDelete.addHeader(key, value);
			}
		}

		// add body to request
		StringEntity input = new StringEntity(body, ContentType.APPLICATION_JSON);
		httpDelete.setEntity(input);

		// execute request
		CloseableHttpResponse response = httpclient.execute(httpDelete);

		restResponse.setErrorCode(response.getStatusLine().getStatusCode());

		return restResponse;
	}

	public RestResponse httpSendDeleteWithBody(String url, String body, Map<String, String> headers)
			throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request method
		con.setRequestMethod("DELETE");

		// add request headers
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}
		}

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(body);
		wr.flush();
		wr.close();

		// con.connect();

		int responseCode = con.getResponseCode();
		logger.debug("Send DELETE http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

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

		String result;

		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			result = null;
		}
		logger.debug("Response body: {}", response);

		// print result

		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();
		return restResponse;

	}

	public RestResponse httpSendPostWithOutBody(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
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

		// con.connect();

		int responseCode = con.getResponseCode();
		logger.debug("Send POST http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			// response = null;
			logger.debug("response body is null");
		}

		String result;
		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			result = null;
		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);
		// if (response == null) {
		// restResponse.setResponse(null);
		// } else {
		// restResponse.setResponse(response.toString());
		// }

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();
		return restResponse;

	}

	public RestResponse httpSendPostMultipart(String url, Map<String, String> headers, String jsonLocation,
			String zipLocation) throws IOException {

		Gson gson = new Gson();
		String gsonToSend = null;
		RestResponse restResponse = new RestResponse();
		BufferedReader br = null;
		//
		//
		//
		//
		// try {
		//
		// String sCurrentLine;
		//
		// br = new BufferedReader(new FileReader(jsonLocation));
		//
		// while ((sCurrentLine = br.readLine()) != null) {
		// System.out.println(sCurrentLine);
		// }
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		// try {
		// if (br != null)br.close();
		// gsonToSend = br.toString();
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// }
		// }

		gsonToSend = new Scanner(new File(jsonLocation)).useDelimiter("\\Z").next();
		logger.debug("gsonToSend: {}",gsonToSend);

		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();
		mpBuilder.addPart("resourceZip", new FileBody(new File(zipLocation)));
		mpBuilder.addPart("resourceMetadata", new StringBody(gsonToSend, ContentType.APPLICATION_JSON));

		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("USER_ID", "adminid");
		httpPost.setEntity(mpBuilder.build());

		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(httpPost);
		try {
			logger.debug("----------------------------------------");
			logger.debug("response.getStatusLine(): {}",response.getStatusLine());
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				logger.debug("Response content length: {}",resEntity.getContentLength());
			}
			EntityUtils.consume(resEntity);
		} finally {

			response.close();
			client.close();
		}

		restResponse.setErrorCode(response.getStatusLine().getStatusCode());
		restResponse.setResponse(response.getEntity().toString());

		return restResponse;

	}

	public RestResponse httpSendPostWithAuth(String url, String body, Map<String, String> headers, String username,
			String password) throws IOException {

		String userPassword = username + ":" + password;
		String encoding = Base64.encodeBase64String(userPassword.getBytes());
		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request method
		con.setRequestMethod("POST");

		con.setRequestProperty("Authorization", "Basic " + encoding);

		// add request headers
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(body);
		wr.flush();
		wr.close();

		// con.connect();

		int responseCode = con.getResponseCode();
		logger.debug("Send POST http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

		StringBuffer response = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			response = null;

		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);
		if (response == null) {
			restResponse.setResponse(null);
		} else {
			restResponse.setResponse(response.toString());
		}

		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

		con.disconnect();
		return restResponse;

	}

	public RestResponse httpSendDelete(String url, Map<String, String> headers) throws IOException {

		RestResponse restResponse = new RestResponse();
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				String key = header.getKey();
				String value = header.getValue();
				con.setRequestProperty(key, value);
			}

		}

		con.setDoOutput(true);
		con.setRequestMethod("DELETE");
		int responseCode = con.getResponseCode();
		logger.debug("Send DELETE http request, url: {}",url);
		logger.debug("Response Code: {}",responseCode);

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

		String result;

		try {

			result = IOUtils.toString(con.getErrorStream());
			response.append(result);

		} catch (Exception e2) {
			result = null;
		}
		logger.debug("Response body: {}",response);

		// print result

		restResponse.setErrorCode(responseCode);

		if (response != null) {
			restResponse.setResponse(response.toString());
		}

		restResponse.setErrorCode(con.getResponseCode());
		Map<String, List<String>> headerFields = con.getHeaderFields();
		restResponse.setHeaderFields(headerFields);
		String responseMessage = con.getResponseMessage();
		restResponse.setResponseMessage(responseMessage);

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
