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

package org.openecomp.core.nosqldb.impl.cassandra;


import com.datastax.driver.core.Session;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

public class CassandraNoSqlDbFactoryImpl extends NoSqlDbFactory {

  @Override
  public NoSqlDb createInterface() {


    return new CassandraNoSqlDbImpl(ReferenceHolder.CASSANDRA);
  }

  protected void stop() {
    ReferenceHolder.CASSANDRA.close();
  }

  private static class ReferenceHolder {
    private static final Session CASSANDRA = CassandraSessionFactory.getSession();
  }
}
