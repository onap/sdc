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
package org.openecomp.sdc.be.model.tosca;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.validators.TimestampValidator;

/**
 * The primitive type that TOSCA YAML supports.
 *
 * @author mkv
 */
@AllArgsConstructor
public enum ToscaType {
    // @formatter:off
	STRING("string"),
	INTEGER("integer"),
	FLOAT("float"),
	BOOLEAN("boolean"),
	TIMESTAMP("timestamp"),
	VERSION("version"),
	LIST("list"),
	MAP("map"),
    RANGE("range"),
	SCALAR_UNIT("scalar-unit"),
	SCALAR_UNIT_SIZE("scalar-unit.size"),
	SCALAR_UNIT_TIME("scalar-unit.time"),
	SCALAR_UNIT_BITRATE("scalar-unit.bitrate"),
	SCALAR_UNIT_FREQUENCY("scalar-unit.frequency");
    // @formatter:on
    @Getter
    private final String type;

    // @formatter:off
    private static final List<String> SCALAR_UNIT_BITRATE_VALID_UNITS = Arrays.asList("TiBps", "TBps", "GiBps", "GBps", "MiBps", "MBps", "KiBps", "KBps", "Bps", "Tibps", "Tbps", "Gibps", "Gbps", "Mibps", "Mbps", "Kibps", "Kbps", "bps");
    private static final String SCALAR_UNIT_BITRATE_PATTERN = "(^[0-9]+\\.?[0-9]*) ?(" + SCALAR_UNIT_BITRATE_VALID_UNITS.stream().collect(Collectors.joining("|")) + ")$";
    private static final List<String> SCALAR_UNIT_TIME_VALID_UNITS = Arrays.asList("d", "h", "m", "s", "ms", "us", "ns");
    private static final String SCALAR_UNIT_TIME_PATTERN = "(^[0-9]+\\.?[0-9]*) ?(" + SCALAR_UNIT_TIME_VALID_UNITS.stream().collect(Collectors.joining("|")) + ")$";
    private static final List<String> SCALAR_UNIT_SIZE_VALID_UNITS = Arrays.asList("TiB", "TB", "GiB", "GB", "MiB", "MB", "KiB", "kB", "B");
    private static final String SCALAR_UNIT_SIZE_PATTERN = "(^[0-9]+\\.?[0-9]*) ?(" + SCALAR_UNIT_SIZE_VALID_UNITS.stream().collect(Collectors.joining("|")) + ")$";
    private static final List<String> SCALAR_UNIT_FREQUENCY_VALID_UNITS = Arrays.asList("GHz", "MHz", "kHz", "Hz");
    private static final String SCALAR_UNIT_FREQUENCY_PATTERN = "(^[0-9]+\\.?[0-9]*) ?(" + SCALAR_UNIT_FREQUENCY_VALID_UNITS.stream().collect(Collectors.joining("|")) + ")$";
    // @formatter:on
    private static final double B_IN_TiB = Math.pow(1024, 4);
    private static final double B_IN_GiB = Math.pow(1024, 3);
    private static final double B_IN_MiB = Math.pow(1024, 2);
    private static final double B_IN_KiB = Math.pow(1024, 1);
    private static final double B_IN_TB = Math.pow(1000, 4);
    private static final double B_IN_GB = Math.pow(1000, 3);
    private static final double B_IN_MB = Math.pow(1000, 2);
    private static final double B_IN_KB = Math.pow(1000, 1);

    public static ToscaType getToscaType(String typeName) {
        if (typeName == null) {
            return null;
        }
        for (ToscaType type : ToscaType.values()) {
            if (type.getType().equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isPrimitiveType(String dataTypeName) {
        if (!ToscaPropertyType.MAP.getType().equals(dataTypeName) && !ToscaPropertyType.LIST.getType().equals(dataTypeName)) {
            return isValidType(dataTypeName) != null;
        }
        return false;
    }

    public static ToscaType isValidType(String typeName) {
        if (typeName == null) {
            return null;
        }
        for (ToscaType type : ToscaType.values()) {
            if (type.getType().equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isCollectionType(String type) {
        return ToscaPropertyType.MAP.getType().equals(type) || ToscaPropertyType.LIST.getType().equals(type);
    }

    public boolean isValueTypeValid(Object value) {
        switch (this) {
            case BOOLEAN:
                return value.equals(true) || value.equals(false);
            case FLOAT:
                return value instanceof Float;
            case INTEGER:
                return value instanceof Integer;
            case STRING:
            case SCALAR_UNIT_SIZE:
            case SCALAR_UNIT_TIME:
            case SCALAR_UNIT_BITRATE:
            case SCALAR_UNIT_FREQUENCY:
            case TIMESTAMP:
            case VERSION:
                return value instanceof String;
            case LIST:
            case MAP:
                return true;
            case SCALAR_UNIT:
            default:
                return false;
        }
    }

    public boolean isValidValue(String value) {
        switch (this) {
            case BOOLEAN:
                return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            case FLOAT:
                return isFloat(value);
            case INTEGER:
                return isInteger(value);
            case SCALAR_UNIT_SIZE:
                return isScalarUnitSize(value);
            case SCALAR_UNIT_TIME:
                return isScalarUnitTime(value);
            case SCALAR_UNIT_BITRATE:
                return isScalarUnitBitrate(value);
            case SCALAR_UNIT_FREQUENCY:
                return isScalarUnitFrequency(value);
            case STRING:
                return true;
            case TIMESTAMP:
                return TimestampValidator.getInstance().isValid(value, null);
            case VERSION:
                return VersionUtil.isValid(value);
            case LIST:
                return isList(value);
            case MAP:
                return isMap(value);
            case SCALAR_UNIT:
            default:
                return false;
        }
    }

    private boolean isList(String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readValue(value, new TypeReference<List<Object>>() {
            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean isMap(String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean isFloat(String value) {
        try {
            Float.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean isInteger(String value) {
        try {
            Long.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isScalarUnit() {
        switch (this) {
            case SCALAR_UNIT_TIME:
            case SCALAR_UNIT_SIZE:
            case SCALAR_UNIT_BITRATE:
            case SCALAR_UNIT_FREQUENCY:
                return true;
            default:
                return false;
        }
    }

    public Object convert(String value) {
        switch (this) {
            case STRING:
                return value;
            case SCALAR_UNIT_TIME:
                return convertScalarUnitTime(value);
            case SCALAR_UNIT_BITRATE:
                return convertScalarUnitBitrate(value);
            case SCALAR_UNIT_SIZE:
                return convertScalarUnitSize(value);
            case SCALAR_UNIT_FREQUENCY:
                return convertScalarUnitFrequency(value);
            case BOOLEAN:
                return Boolean.valueOf(value);
            case FLOAT:
                return Float.valueOf(value);
            case INTEGER:
                return Long.valueOf(value);
            case TIMESTAMP:
                try {
                    return new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.US).parse(value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Value must be a valid timestamp", e);
                }
            case VERSION:
                return VersionUtil.parseVersion(value);
            case LIST:
                try {
                    return ConstraintUtil.parseToCollection(value, new TypeReference<List<Object>>() {
                    });
                } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
                    throw new IllegalArgumentException("Value must be a valid List", e);
                }
            case MAP:
                try {
                    return ConstraintUtil.parseToCollection(value, new TypeReference<Map<String, Object>>() {
                    });
                } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
                    throw new IllegalArgumentException("Value must be a valid Map", e);
                }
            case SCALAR_UNIT:
            default:
                return null;
        }
    }

    private Long convertScalarUnitSize(final String value) {
        if (isScalarUnitSize(value)) {
            final Matcher matcher = Pattern.compile(SCALAR_UNIT_SIZE_PATTERN).matcher(value.trim());
            if (matcher.find()) {
                switch (matcher.group(2)) {
                    case "TiB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_TiB);
                    case "TB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_TB);
                    case "GiB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_GiB);
                    case "GB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_GB);
                    case "MiB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_MiB);
                    case "MB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_MB);
                    case "KiB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_KiB);
                    case "kB":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_KB);
                    case "B":
                        return (long) (Double.parseDouble(matcher.group(1)));
                    default:
                        throw new IllegalArgumentException("Value must be a valid scalar-unit.size");
                }
            } else {
                throw new IllegalArgumentException("Value must be a valid 'scalar-unit.size': " + SCALAR_UNIT_SIZE_VALID_UNITS);
            }
        } else {
            throw new IllegalArgumentException("Value must be a valid 'scalar-unit.size': " + SCALAR_UNIT_SIZE_VALID_UNITS);
        }
    }

    private Long convertScalarUnitTime(final String value) {
        if (isScalarUnitTime(value)) {
            final Matcher matcher = Pattern.compile(SCALAR_UNIT_TIME_PATTERN).matcher(value.trim());
            if (matcher.find()) {
                switch (matcher.group(2)) {
                    case "d":
                        return (long) (Double.parseDouble(matcher.group(1)) * 24 * 60 * 60 * 1_000_000_000L);   //  24hours * 60minutes * 60seconds
                    case "h":
                        return (long) (Double.parseDouble(matcher.group(1)) * 60 * 60 * 1_000_000_000L);        //  60minutes * 60seconds
                    case "m":
                        return (long) (Double.parseDouble(matcher.group(1)) * 60 * 1_000_000_000L);             //  60seconds
                    case "s":
                        return (long) (Double.parseDouble(matcher.group(1)) * 1_000_000_000L);
                    case "ms":
                        return (long) (Double.parseDouble(matcher.group(1)) * 1_000_000L);
                    case "us":
                        return (long) (Double.parseDouble(matcher.group(1)) * 1_000L);
                    case "ns":
                        return (long) (Double.parseDouble(matcher.group(1)));
                    default:
                        throw new IllegalArgumentException("Value must be a valid scalar-unit.time");
                }
            } else {
                throw new IllegalArgumentException("Value must be a valid 'scalar-unit.time':" + SCALAR_UNIT_TIME_VALID_UNITS);
            }
        } else {
            throw new IllegalArgumentException("Value must be a valid 'scalar-unit.time':" + SCALAR_UNIT_TIME_VALID_UNITS);
        }
    }

    private Long convertScalarUnitFrequency(final String value) {
        if (isScalarUnitFrequency(value)) {
            final Matcher matcher = Pattern.compile(SCALAR_UNIT_FREQUENCY_PATTERN).matcher(value.trim());
            if (matcher.find()) {
                switch (matcher.group(2)) {
                    case "GHz":
                        return (long) (Double.parseDouble(matcher.group(1)) * 1_000_000_000L);
                    case "MHz":
                        return (long) (Double.parseDouble(matcher.group(1)) * 1_000_000L);
                    case "kHz":
                        return (long) (Double.parseDouble(matcher.group(1)) * 1_000L);
                    case "Hz":
                        return (long) (Double.parseDouble(matcher.group(1)));
                    default:
                        throw new IllegalArgumentException("Value must be a valid scalar-unit.frequency");
                }
            } else {
                throw new IllegalArgumentException("Value must be a valid 'scalar-unit.frequency':" + SCALAR_UNIT_FREQUENCY_VALID_UNITS);
            }
        } else {
            throw new IllegalArgumentException("Value must be a valid 'scalar-unit.frequency':" + SCALAR_UNIT_FREQUENCY_VALID_UNITS);
        }
    }

    private Long convertScalarUnitBitrate(final String value) {
        if (isScalarUnitBitrate(value)) {
            final Matcher matcher = Pattern.compile(SCALAR_UNIT_BITRATE_PATTERN).matcher(value.trim());
            if (matcher.find()) {
                switch (matcher.group(2)) {
                    case "TiBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_TiB);
                    case "TBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_TB);
                    case "GiBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_GiB);
                    case "GBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_GB);
                    case "MiBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_MiB);
                    case "MBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_MB);
                    case "KiBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_KiB);
                    case "KBps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8 * B_IN_KB);
                    case "Bps":
                        return (long) (Double.parseDouble(matcher.group(1)) * 8);
                    case "Tibps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_TiB);
                    case "Tbps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_TB);
                    case "Gibps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_GiB);
                    case "Gbps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_GB);
                    case "Mibps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_MiB);
                    case "Mbps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_MB);
                    case "Kibps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_KiB);
                    case "Kbps":
                        return (long) (Double.parseDouble(matcher.group(1)) * B_IN_KB);
                    case "bps":
                        return (long) (Double.parseDouble(matcher.group(1)));
                    default:
                        throw new IllegalArgumentException("Value must be a valid scalar-unit.bitrate");
                }
            } else {
                throw new IllegalArgumentException("Value must be a valid 'scalar-unit.bitrate':" + SCALAR_UNIT_BITRATE_VALID_UNITS);
            }
        } else {
            throw new IllegalArgumentException("Value must be a valid 'scalar-unit.bitrate':" + SCALAR_UNIT_BITRATE_VALID_UNITS);
        }
    }

    private boolean isScalarUnitBitrate(final String value) {
        final Matcher matcher = Pattern.compile(SCALAR_UNIT_BITRATE_PATTERN).matcher(value.trim());
        return matcher.find() && SCALAR_UNIT_BITRATE_VALID_UNITS.contains(matcher.group(2));
    }

    private boolean isScalarUnitSize(final String value) {
        final Matcher matcher = Pattern.compile(SCALAR_UNIT_SIZE_PATTERN).matcher(value.trim());
        return matcher.find() && SCALAR_UNIT_SIZE_VALID_UNITS.contains(matcher.group(2));
    }

    private boolean isScalarUnitTime(final String value) {
        final Matcher matcher = Pattern.compile(SCALAR_UNIT_TIME_PATTERN).matcher(value.trim());
        return matcher.find() && SCALAR_UNIT_TIME_VALID_UNITS.contains(matcher.group(2));
    }

    private boolean isScalarUnitFrequency(final String value) {
        final Matcher matcher = Pattern.compile(SCALAR_UNIT_FREQUENCY_PATTERN).matcher(value.trim());
        return matcher.find() && SCALAR_UNIT_FREQUENCY_VALID_UNITS.contains(matcher.group(2));
    }

    public String getValidValues() {
        switch (this) {
            case SCALAR_UNIT_TIME:
                return String.valueOf(SCALAR_UNIT_TIME_VALID_UNITS);
            case SCALAR_UNIT_SIZE:
                return String.valueOf(SCALAR_UNIT_SIZE_VALID_UNITS);
            case SCALAR_UNIT_BITRATE:
                return String.valueOf(SCALAR_UNIT_BITRATE_VALID_UNITS);
            case SCALAR_UNIT_FREQUENCY:
                return String.valueOf(SCALAR_UNIT_FREQUENCY_VALID_UNITS);
            default:
                return "";
        }
    }
}
