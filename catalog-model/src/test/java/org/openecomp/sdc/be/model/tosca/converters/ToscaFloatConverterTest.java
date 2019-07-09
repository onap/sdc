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

package org.openecomp.sdc.be.model.tosca.converters;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNull;


public class ToscaFloatConverterTest {

    @Test
    public void convertEmptyString_returnNull() {
        assertNull(executeFloatConversion(""));
    }

    @Test
    public void convertNull_returnNull() {
        assertNull(executeFloatConversion(null));
    }

    @Test
    public void convertWholeNumber() {
        assertThat(executeFloatConversion("1234"))
                .isEqualTo("1234");
    }

    @Test
    public void convertFloatNumber() {
        assertThat(executeFloatConversion("3.141"))
                .isEqualTo("3.141");
    }

    @Test
    public void convertNotValidFloat() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("123.55.66"));
    }

    @Test
    public void convertNumericWithSpecialChars() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("123,55"));
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("123&55"));
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("123$$55"));
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("123#55"));
    }

    @Test
    public void convertNonNumeric() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("1234ABC"));
    }

    @Test
    public void convertNumericWithCapitalFloatSign() {
        assertThat(executeFloatConversion("1234F"))
                .isEqualTo("1234");
    }

    @Test
    public void convertNumericWithSmallLetterFloatSign() {
        assertThat(executeFloatConversion("1234f"))
                .isEqualTo("1234");
    }

    @Test
    public void convertNumericWithFloatSignNotAtTheEnd_ThrowsException() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> executeFloatConversion("12f34"));
    }

    private String executeFloatConversion(String s) {
        return ToscaFloatConverter.getInstance().convert(s, null, Collections.emptyMap());
    }
}
