package org.openecomp.server.interceptors;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collections;

@ControllerAdvice
public class EmptyOutputOutInterceptor implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // Apply to all responses
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // Only wrap if body is null and status is 200
        if (body == null && response instanceof org.springframework.http.server.ServletServerHttpResponse) {
            int status = ((org.springframework.http.server.ServletServerHttpResponse) response).getServletResponse().getStatus();

            if (status == HttpStatus.OK.value()) {
                DefaultOutput output = new DefaultOutput(HttpStatus.OK.value(), new InternalEmptyObject());
                output.addMetadata(Collections.emptyMap()); // you can customize this
                return output;
            }
        }

        return body;
    }
}
