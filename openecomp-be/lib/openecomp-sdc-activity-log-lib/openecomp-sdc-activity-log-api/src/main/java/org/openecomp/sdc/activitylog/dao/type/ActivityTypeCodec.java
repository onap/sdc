package org.openecomp.sdc.activitylog.dao.type;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

import java.nio.ByteBuffer;

public class ActivityTypeCodec implements TypeCodec<ActivityType> {

    @Override
    public GenericType<ActivityType> getJavaType() {
        return GenericType.of(ActivityType.class);
    }

    @Override
    public com.datastax.oss.driver.api.core.type.DataType getCqlType() {
        return DataTypes.TEXT;
    }

    @Override
    public ByteBuffer encode(ActivityType value, ProtocolVersion protocolVersion) {
        if (value == null) return null;
        return TypeCodecs.TEXT.encode(value.name(), protocolVersion);
    }

    @Override
    public ActivityType decode(ByteBuffer bytes, ProtocolVersion protocolVersion) {
        if (bytes == null) return null;
        String s = TypeCodecs.TEXT.decode(bytes, protocolVersion);
        return ActivityType.valueOf(s);
    }

    @Override
    public String format(ActivityType value) {
        return value == null ? "NULL" : "'" + value.name() + "'";
    }

    @Override
    public ActivityType parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL")) return null;
        return ActivityType.valueOf(value.replace("'", ""));
    }
}
