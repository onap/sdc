package org.openecomp.sdc.asdctool.migration.core;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DBVersionTest {


    @DataProvider(name = "invalidVersionStringsProvider")
    private Object[][] invalidVersionStringsProvider() {
        return new Object[][] {
                {"1.1.1"},
                {"1.a"},
                {"a.1"},
                {"1"}
        };
    }

    @Test(dataProvider = "invalidVersionStringsProvider", expectedExceptions = MigrationException.class)
    public void testFromString_invalidVersionString(String invalidVersion) {
        DBVersion.fromString(invalidVersion);
    }

    @DataProvider(name = "validVersionStringsProvider")
    private Object[][] validVersionStringsProvider() {
        return new Object[][] {
                {"1.1", "1.1"},
                {"10100.0001", "10100.1"},
                {"000.1", "0.1"},
                {"01.00001000", "1.1000"},
        };
    }

    @Test(dataProvider = "validVersionStringsProvider")
    public void testFromString(String validString, String expectedVersionString) {
        assertEquals(expectedVersionString, DBVersion.fromString(validString).toString());
    }

    @DataProvider(name = "versionComparisionProvider")
    public static Object[][] versionComparisionProvider() {
        return new Object[][] {
                {"1.1", "001.00001", 0},
                {"10.1", "0010.00001", 0},
                {"1.1", "001.000010", -1},
                {"1.1", "0010.00001", -1},
                {"10.10", "0010.00001", 1},
                {"1.1", "001.00", 1},
        };
    }

    @Test(dataProvider = "versionComparisionProvider")
    public void testVersionCompareTo2(String firstVersion, String otherVersion, int expectedComparisionResult) throws Exception {
        assertEquals(DBVersion.fromString(firstVersion).compareTo(DBVersion.fromString(otherVersion)), expectedComparisionResult);
    }
}
