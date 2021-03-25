/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.validation.util;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.validation.impl.util.YamlValidatorUtil;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.parser.ParserException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class YamlValidatorUtilTest {
    @Test
    public void testIsEmpty() {
        MarkedYAMLException markedYamlException = Mockito.mock(MarkedYAMLException.class);
        when(markedYamlException.getMessage()).thenReturn("test");
        assertEquals("test", YamlValidatorUtil.getParserExceptionReason(markedYamlException));

        Exception exception = Mockito.mock(Exception.class);
        when(exception.getCause()).thenReturn(new Exception());
        assertEquals("general parser error", YamlValidatorUtil.getParserExceptionReason(exception));

        when(markedYamlException.getCause()).thenReturn(new Exception());
        when(exception.getCause()).thenReturn(markedYamlException);
        assertEquals("test", YamlValidatorUtil.getParserExceptionReason(exception));

        ParserException parserException = Mockito.mock(ParserException.class);
        when(parserException.getMessage()).thenReturn("parseException");
        when(markedYamlException.getCause()).thenReturn(parserException);
        when(exception.getCause()).thenReturn(markedYamlException);
        assertEquals("parseException", YamlValidatorUtil.getParserExceptionReason(exception));
    }
}
