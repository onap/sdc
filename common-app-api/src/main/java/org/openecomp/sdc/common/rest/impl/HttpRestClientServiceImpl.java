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

package org.openecomp.sdc.common.rest.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map.Entry;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.common.rest.api.IRestClient;
import org.openecomp.sdc.common.rest.api.RestClientServiceExeption;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;
import org.openecomp.sdc.common.rest.api.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestClientServiceImpl implements IRestClient {

	private Logger log = LoggerFactory.getLogger(HttpRestClientServiceImpl.class.getName());

	public final static int DEFAULT_CONNECTION_POOL_SIZE = 10;

	public final static int DEFAULT_CONNECT_TIMEOUT = 10;

	private DefaultHttpClient httpClient;

	private SSLSocketFactory sslFactory;

	private Logger logger = LoggerFactory.getLogger(HttpRestClientServiceImpl.class.getName());

	PoolingClientConnectionManager cm = null;

	public boolean init() {

		return init(new RestConfigurationInfo());

	}

	public boolean init(RestConfigurationInfo restConfigurationInfo) {

		boolean initialized = false;

		logger.debug("HttpRestClientServiceImpl::init - start. restConfigurationInfo=" + restConfigurationInfo);

		try {
			createHttpClient(restConfigurationInfo);

			initialized = true;
		} catch (KeyManagementException e) {
			String msg = "Failed creating client config for rest. " + e.getMessage();
			logger.error(msg, e);
			// throw new RestClientServiceExeption(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			String msg = "Failed creating client config for rest. " + e.getMessage();
			logger.error(msg, e);
			// throw new RestClientServiceExeption(msg);
		}

		logger.debug("HttpRestClientServiceImpl::init - finish successfully");

		return initialized;
	}

	public void destroy() {

		if (this.httpClient != null) {
			this.httpClient.getConnectionManager().shutdown();
			logger.info("After closing connection Manager of rest Client.");
		}

	}

	private void createHttpClient(RestConfigurationInfo restConfigurationInfo)
			throws KeyManagementException, NoSuchAlgorithmException {

		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();

		Integer connPoolSizeObj = restConfigurationInfo.getConnectionPoolSize();
		int connPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
		if (connPoolSizeObj != null) {
			connPoolSize = connPoolSizeObj.intValue();
			if (connPoolSize <= 0) {
				connPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
			}
		}
		cm.setMaxTotal(connPoolSize);

		this.httpClient = new DefaultHttpClient(cm);

		int timeoutInSec = restConfigurationInfo.getReadTimeoutInSec() == null ? 0
				: restConfigurationInfo.getReadTimeoutInSec();
		int connectTimeoutInSec = restConfigurationInfo.getConnectTimeoutInSec() == null ? DEFAULT_CONNECT_TIMEOUT
				: restConfigurationInfo.getConnectTimeoutInSec();
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeoutInSec * 1000);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeoutInSec * 1000);

		this.httpClient.setParams(params);

		Boolean ignoreCertificateObj = restConfigurationInfo.getIgnoreCertificate();
		boolean ignoreCertificate = false;
		if (ignoreCertificateObj != null) {
			ignoreCertificate = ignoreCertificateObj.booleanValue();
		}
		if (ignoreCertificate == true) {

			this.sslFactory = createSSLSocketFactory();

			Scheme scheme = new Scheme("https", 9443, sslFactory);
			this.httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
		}

		// addKeepAlive();

		this.cm = cm;
	}

	private void addKeepAlive() {

		this.httpClient.setReuseStrategy(new ConnectionReuseStrategy() {

			public boolean keepAlive(HttpResponse response, HttpContext context) {
				// TODO Auto-generated method stub
				return true;
			}

		});

		this.httpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				log.debug("============ In getKeepAliveDuration ================= ");

				HeaderIterator headerIterator = response.headerIterator(HTTP.CONN_KEEP_ALIVE);
				if (headerIterator != null) {
					HeaderElementIterator it = new BasicHeaderElementIterator(headerIterator);
					while (it.hasNext()) {
						HeaderElement he = it.nextElement();
						String param = he.getName();
						String value = he.getValue();
						if (value != null && param.equalsIgnoreCase("timeout")) {
							try {
								log.debug("============ In getKeepAliveDuration ================= {}", value);

								return Long.parseLong(value) * 1000;
							} catch (NumberFormatException ignore) {
								log.error("Failed parsing retrieved value of timeout header.", ignore);
							}
						}
					}
				}
				return 20;
			}
		});

	}

	protected SSLSocketFactory createSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager easyTrustManager = new javax.net.ssl.X509TrustManager() {

			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}

		};

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { easyTrustManager }, null);
		SSLSocketFactory sslFactory = new SSLSocketFactory(sslContext);

		sslFactory.setHostnameVerifier(new X509HostnameVerifier() {

			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return false;
			}

			public void verify(String host, SSLSocket ssl) throws IOException {
				// TODO Auto-generated method stub

			}

			public void verify(String host, java.security.cert.X509Certificate cert) throws SSLException {
				// TODO Auto-generated method stub

			}

			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
				// TODO Auto-generated method stub

			}

		});
		return sslFactory;
	}

	/**
	 * Executes RS-GET to perform FIND.
	 * 
	 * @param headerParameterKey
	 *            String
	 * @param headerParameterValue
	 *            String
	 * @return String
	 */
	public RestResponse doGET(String uri, Properties headers) {
		logger.debug("Before executing uri " + uri + ". headers = " + headers);

		HttpGet httpGet = new HttpGet(uri);

		RestResponse response = execute(httpGet, headers);

		return response;
	}

	private void addHeadersToRequest(HttpRequestBase httpRequestBase, Properties headers) {

		if (headers != null) {
			for (Entry<Object, Object> entry : headers.entrySet()) {
				httpRequestBase.addHeader(entry.getKey().toString(), entry.getValue().toString());
			}
		}

	}

	public RestResponse doPOST(String uri, Properties headers, Object objectToCreate) {

		logger.debug("Before executing uri " + uri + ". body = "
				+ (objectToCreate != null ? objectToCreate.toString() : null) + ". headers = " + headers);

		HttpPost httpPost = new HttpPost(uri);

		if (objectToCreate != null) {
			StringEntity se;
			try {
				se = new StringEntity(objectToCreate.toString());

				// se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				// "application/json"));
				httpPost.setEntity(se);
			} catch (UnsupportedEncodingException e) {
				String msg = "Failed creating Entity for post request." + e.getMessage();
				log.error(msg, e);
				return null;
				// throw new RestClientServiceExeption(msg);
			}
		}

		RestResponse response = execute(httpPost, headers);

		return response;

	}

	public RestResponse doPUT(String uri, Properties headers, Object objectToCreate) {

		logger.debug("Before executing uri " + uri + ". body = "
				+ (objectToCreate != null ? objectToCreate.toString() : null) + ". headers = " + headers);

		HttpPut httpPut = new HttpPut(uri);

		if (objectToCreate != null) {
			StringEntity se;
			try {
				se = new StringEntity(objectToCreate.toString());

				// se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				// "application/json"));
				httpPut.setEntity(se);
			} catch (UnsupportedEncodingException e) {
				String msg = "Failed creating Entity for post request." + e.getMessage();
				// throw new RestClientServiceExeption(msg);
				log.error(msg, e);
				return null;
			}
		}

		RestResponse response = execute(httpPut, headers);

		return response;
	}

	private RestResponse execute(HttpRequestBase httpRequestBase, Properties headers) {

		String response = null;
		String statusDesc = null;
		int statusCode = HttpStatus.SC_OK;

		try {

			addHeadersToRequest(httpRequestBase, headers);

			HttpResponse httpResponse = this.httpClient.execute(httpRequestBase);

			statusCode = httpResponse.getStatusLine().getStatusCode();
			statusDesc = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				response = EntityUtils.toString(entity);
			}
			// ensure the connection gets released to the manager
			EntityUtils.consume(entity);

			logResponse(response, httpRequestBase.getMethod());

		} catch (Exception exception) {
			httpRequestBase.abort();
			log.error("Failed to execute the " + httpRequestBase.getMethod() + " request " + httpRequestBase.getURI(),
					exception);
			// processAndThrowException(exception);
			return null;
		}

		RestResponse restResponse = new RestResponse(response, statusDesc, statusCode);

		if (logger.isDebugEnabled()) {
			URI uri = httpRequestBase.getURI();
			String url = uri.toString();
			logger.debug("After executing uri " + url + ". response = " + restResponse);
		}

		return restResponse;
	}

	public RestResponse doDELETE(String uri, Properties headers) {

		if (logger.isDebugEnabled()) {
			logger.debug("Before executing uri " + uri + ". headers = " + headers);
		}

		HttpDelete httpDelete = new HttpDelete(uri);

		RestResponse restResponse = execute(httpDelete, headers);

		return restResponse;

	}

	/**
	 * This method print the JSON response from the REST Server
	 * 
	 * @param response
	 *            the JSON response from the REST server
	 * @param method
	 *            name of method
	 */
	private void logResponse(String response, String method) {
		logger.debug(method + " response = " + response);
	}

	/**
	 * Exception during client invocation usually it happens when status code
	 * starting with 400 or 500 is returned
	 * 
	 * @param exception
	 *            Exception
	 * @throws RSClientServiceExeption
	 */
	private void processAndThrowException(Exception exception) throws RestClientServiceExeption {

		logger.debug("\n------------------------");
		logger.debug("FAILURE: " + exception.getMessage());

		throw new RestClientServiceExeption(exception);

	}

}
