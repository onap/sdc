package org.openecomp.sdc.versioning.dao.types;


import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;


public class VersionUdtCodec extends MappingCodec<UdtValue, Version> {

    private final UserDefinedType versionUdtType;

    public VersionUdtCodec(TypeCodec<UdtValue> innerCodec, UserDefinedType versionUdtType) {
        super(innerCodec, GenericType.of(Version.class));
        this.versionUdtType = versionUdtType;
    }

    @Override
    protected Version innerToOuter(UdtValue value) {
        if (value == null) {
            return null;
        }

        Version version = new Version();
        version.setMajor(value.getInt("major"));
        version.setMinor(value.getInt("minor"));

        return version;
    }

    @Override
    protected UdtValue outerToInner(Version version) {
        if (version == null) {
            return null;
        }

        UdtValue udtValue = versionUdtType.newValue();
        udtValue.setInt("major", version.getMajor());
        udtValue.setInt("minor", version.getMinor());

        return udtValue;
    }
}