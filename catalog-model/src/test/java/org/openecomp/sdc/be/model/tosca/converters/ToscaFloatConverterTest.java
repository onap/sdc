package org.openecomp.sdc.be.model.tosca.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;


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