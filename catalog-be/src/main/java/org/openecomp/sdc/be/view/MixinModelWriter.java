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
package org.openecomp.sdc.be.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * A class that is then responsible for converting a message payload with a dedicated mixin from an instance of a specific Java type into a json
 * representation.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class MixinModelWriter implements MessageBodyWriter<Object> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return hasResponseViewAndMixinTargetAnnotations(annotations) && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public long getSize(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        List<MixinSourceTarget> mixinSourceTargets = getMixinSourceTarget(annotations);
        mixinSourceTargets.forEach(mixinSourceTarget -> objectMapper.addMixIn(mixinSourceTarget.getTarget(), mixinSourceTarget.getMixinSource()));
        objectMapper.writeValue(entityStream, object);
    }

    private List<MixinSourceTarget> getMixinSourceTarget(Annotation[] annotations) {
        return Stream.of(annotations).filter(annotation -> annotation.annotationType().equals(ResponseView.class))
            .map(annotation -> (ResponseView) annotation).flatMap(responseView -> Stream.of(responseView.mixin()))
            .map(mixinClass -> new MixinSourceTarget(mixinClass, mixinClass.getAnnotation(MixinTarget.class).target())).collect(Collectors.toList());
    }

    private boolean hasResponseViewAndMixinTargetAnnotations(Annotation[] annotations) {
        return Stream.of(annotations).filter(annotation -> annotation.annotationType().equals(ResponseView.class))
            .map(annotation -> (ResponseView) annotation).flatMap(responseView -> Stream.of(responseView.mixin()))
            .anyMatch(mixinClass -> Objects.nonNull(mixinClass.getAnnotation(MixinTarget.class)));
    }
}
