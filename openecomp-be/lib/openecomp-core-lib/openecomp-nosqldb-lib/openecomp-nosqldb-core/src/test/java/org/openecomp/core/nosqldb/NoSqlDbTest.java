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

package org.openecomp.core.nosqldb;

import org.openecomp.core.nosqldb.api.NoSqlDb;


public class NoSqlDbTest {

  private NoSqlDb noSqlDb;
/*

  @Test
  public void testNoSqlDbFactoryFactoryInit() {
    this.noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    Assert.assertNotNull(this.noSqlDb);
    Assert.assertEquals(this.noSqlDb.getClass().getName(),
        "org.openecomp.core.nosqldb.impl.cassandra.CassandraNoSqlDbImpl");
  }

  @Test(dependsOnMethods = {"testNoSqlDbFactoryFactoryInit"})
  public void testCreateTable() {
    this.noSqlDb.execute("test.drop", null);
    this.noSqlDb.execute("test.create", null);
  }

  @Test(dependsOnMethods = {"testCreateTable"})
  public void testInsertTable() {
    this.noSqlDb
        .insert("test", new String[]{"name", "value"}, new String[]{"TestName", "testValue"});
    this.noSqlDb.execute("test.insert", new String[]{"TestName2", "testValue2"});
  }

  @Test(dependsOnMethods = {"testInsertTable"})
  public void gettestSelectTable() {
    ResultSet result = this.noSqlDb.execute("test.select.all", null);
    List<Row> rows = result.all();
    Assert.assertEquals(rows.size(), 2);
    for (Row row : rows) {
      System.out.format("%s %s\n", row.getString("name"), row.getString("value"));
    }
  }

  */
}
