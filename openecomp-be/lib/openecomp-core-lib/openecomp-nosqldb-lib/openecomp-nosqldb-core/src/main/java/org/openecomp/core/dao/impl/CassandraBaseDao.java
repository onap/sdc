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
package org.openecomp.core.dao.impl;

import org.openecomp.core.dao.BaseDao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;

public abstract class CassandraBaseDao<T> implements BaseDao<T> {

    protected final CqlSession session;

    public CassandraBaseDao(CqlSession session) {
        this.session = session;
    }

    protected abstract Object[] getKeys(T entity);
    protected abstract String getTableName();
    protected abstract String[] getColumns(T entity);
    protected abstract Object[] getValues(T entity);


    public void create(T entity) {
        String[] columns = getColumns(entity);
        Object[] values = getValues(entity);
        String placeholders = String.join(",", java.util.Collections.nCopies(values.length, "?"));
        String query = "INSERT INTO " + getTableName() + " (" + String.join(",", columns) + ") VALUES (" + placeholders + ")";
        session.execute(SimpleStatement.builder(query).addPositionalValues(values).build());
    }

 
    public void update(T entity) {
        // Implement based on primary key columns
        create(entity); // simple upsert in Cassandra
    }


    public T get(T entity) {
        // Implement select based on primary key
        throw new UnsupportedOperationException("Get by primary key should be implemented in subclass");
    }


    public void delete(T entity) {
        // Implement delete based on primary key
        throw new UnsupportedOperationException("Delete by primary key should be implemented in subclass");
    }
}
