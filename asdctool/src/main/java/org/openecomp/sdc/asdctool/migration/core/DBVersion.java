package org.openecomp.sdc.asdctool.migration.core;

import java.math.BigInteger;

public class DBVersion implements Comparable<DBVersion>{

    private static final String VERSION_PARTS_SEPARATOR = "\\.";
    private static final int MAJOR_PART_IDX = 0;
    private static final int MINOR_PART_IDX = 1;
    private BigInteger major;
    private BigInteger minor;

    /**
     * The current db version. should be tested against real db to verify it is compatible to the db version
     */
    public static final DBVersion CURRENT_VERSION = new DBVersion(1710, 0);

    private DBVersion(BigInteger major, BigInteger minor) {
        this.major = major;
        this.minor = minor;
    }

    private DBVersion(int major, int minor) {
        this.major = BigInteger.valueOf(major);
        this.minor = BigInteger.valueOf(minor);
    }

    public BigInteger getMajor() {
        return major;
    }

    public BigInteger getMinor() {
        return minor;
    }

    public static DBVersion from(BigInteger major, BigInteger minor) {
        return new DBVersion(major, minor);
    }

    public static DBVersion fromString(String version) {
        String[] split = version.split(VERSION_PARTS_SEPARATOR);
        if (split.length != 2) {
            throw new MigrationException("version must be of pattern: <major>.<minor>");
        }
        return new DBVersion(getVersionPart(split[MAJOR_PART_IDX]),
                             getVersionPart(split[MINOR_PART_IDX]));

    }

    private static BigInteger getVersionPart(String versionPart) {
        try {
            return new BigInteger(versionPart);
        } catch (NumberFormatException e) {
            throw new MigrationException(String.format("version part %s is non numeric", versionPart));
        }
    }

    @Override
    public String toString() {
        return String.format("%s.%s", major, minor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBVersion dbVersion = (DBVersion) o;

        return major.equals(dbVersion.major) && minor.equals(dbVersion.minor);
    }

    @Override
    public int hashCode() {
        int result = major.hashCode();
        result = 31 * result + minor.hashCode();
        return result;
    }

    @Override
    public int compareTo(DBVersion o) {
        if (o == null) {
            return 1;
        }
        int majorsComparision = this.major.compareTo(o.major);
        if (majorsComparision != 0) {
            return majorsComparision;
        }
        int minorsComparision = this.minor.compareTo(o.minor);
        if (minorsComparision != 0) {
            return minorsComparision;
        }
        return 0;
    }
}
