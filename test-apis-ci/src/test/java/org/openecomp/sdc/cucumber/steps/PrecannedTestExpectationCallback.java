package org.openecomp.sdc.cucumber.steps;

import static org.mockserver.model.HttpResponse.response;

import org.apache.http.entity.ContentType;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.google.common.net.HttpHeaders;

public class PrecannedTestExpectationCallback implements ExpectationCallback  {
	private static volatile int countRequests;

	static HttpResponse httpResponse = response()
			.withStatusCode(200)
			.withHeaders(new Header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()));

	@Override
	public HttpResponse handle(HttpRequest httpRequest) {
		countRequests++;
		
		System.out.println(
				String.format("MSO Server Simulator Recieved %s Final Distribution Complete Rest Reports From ASDC",
						countRequests));
		
		return httpResponse;
	}
	
}