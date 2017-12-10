package org.openecomp.sdc.asdctool.migration.task;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.scanner.ClassScanner;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MigrationTasksTest  {

    public static final String MIGRATIONS_BASE_PACKAGE = "org.openecomp.sdc.asdctool.migration.tasks";
    private List<Migration> migrations;

    @BeforeMethod
    public void setUp() throws Exception {
        ClassScanner classScanner = new ClassScanner();
        migrations = classScanner.getAllClassesOfType(MIGRATIONS_BASE_PACKAGE, Migration.class);
    }

    @Test
    public void testNoTasksWithSameVersion() throws Exception {
        Map<DBVersion, List<Migration>> migrationsByVersion = migrations.stream().collect(Collectors.groupingBy(Migration::getVersion));
        migrationsByVersion.forEach((version, migrations) -> {
            if (migrations.size() > 1) {
                System.out.println(String.format("the following migration tasks have the same version %s. versions must be unique", version.toString()));
                Assert.fail(String.format("migration tasks %s has same version %s. migration tasks versions must be unique.", getMigrationsNameAsString(migrations), version.toString()));
            }
        });
    }

    @Test
    public void testNoTaskWithVersionGreaterThanCurrentVersion() throws Exception {
        Set<Migration> migrationsWithVersionsGreaterThanCurrent = migrations.stream().filter(mig -> mig.getVersion().compareTo(DBVersion.CURRENT_VERSION) > 0)
                .collect(Collectors.toSet());

        if (!migrationsWithVersionsGreaterThanCurrent.isEmpty()) {
            Assert.fail(String.format("migrations tasks %s have version which is greater than DBVersion.CURRENT_VERSION %s. did you forget to update current version?",
                               getMigrationsNameAsString(migrationsWithVersionsGreaterThanCurrent),
                               DBVersion.CURRENT_VERSION.toString()));
        }
    }

    private String getMigrationsNameAsString(Collection<Migration> migrations) {
        return StringUtils.join(migrations.stream().map(mig -> mig.getClass().getName()).collect(Collectors.toList()), ",");
    }
}
