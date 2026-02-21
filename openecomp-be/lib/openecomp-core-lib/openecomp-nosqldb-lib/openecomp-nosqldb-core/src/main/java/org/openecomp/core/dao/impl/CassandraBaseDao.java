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

import com.datastax.driver.mapping.Mapper;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import java.util.function.Supplier;
import org.openecomp.core.dao.BaseDao;

public abstract class CassandraBaseDao<T> implements BaseDao<T> {

    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("sdc-cassandra-dao");

    protected abstract Mapper<T> getMapper();

    protected abstract Object[] getKeys(T entity);

    private static <R> R traced(String spanName, Supplier<R> operation) {
        Span span = tracer.spanBuilder(spanName)
            .setAttribute("db.system", "cassandra")
            .startSpan();
        try {
            return operation.get();
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private static void tracedVoid(String spanName, Runnable operation) {
        Span span = tracer.spanBuilder(spanName)
            .setAttribute("db.system", "cassandra")
            .startSpan();
        try {
            operation.run();
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void create(T entity) {
        tracedVoid("CassandraBaseDao.create", () -> getMapper().save(entity));
    }

    @Override
    public void update(T entity) {
        tracedVoid("CassandraBaseDao.update", () -> getMapper().save(entity));
    }

    @Override
    public T get(T entity) {
        return traced("CassandraBaseDao.get", () -> getMapper().get(getKeys(entity)));
    }

    @Override
    public void delete(T entity) {
        tracedVoid("CassandraBaseDao.delete", () -> getMapper().delete(entity));
    }
}
