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


import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.core.dao.UniqueValueMapper;
import org.openecomp.core.dao.UniqueValueMapperBuilder;
import org.openecomp.core.dao.types.UniqueValueEntity;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;


public class UniqueValueCassandraDaoImpl extends CassandraBaseDao<UniqueValueEntity> implements UniqueValueDao {

    private final UniqueValueDao dao;

    public UniqueValueCassandraDaoImpl() {
        super(NoSqlDbFactory.getInstance().createInterface().getSession());
        UniqueValueMapper mapper = new UniqueValueMapperBuilder(session).build();
        this.dao = mapper.uniqueValueDao();
    }


    @Override
    public void create(UniqueValueEntity entity) {
        dao.create(entity);
    }

    // @Override
    // public void update(UniqueValueEntity entity) {
    //     dao.update(entity);
    // }

    @Override
    public UniqueValueEntity get(UniqueValueEntity entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void delete(UniqueValueEntity entity) {
        dao.delete(entity);
    }

    public Collection<UniqueValueEntity> list(UniqueValueEntity entity) {
        return dao.listAll();
    }


    @Override
    protected Object[] getKeys(UniqueValueEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getKeys'");
    }


    @Override
    protected String getTableName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTableName'");
    }


    @Override
    protected String[] getColumns(UniqueValueEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getColumns'");
    }


    @Override
    protected Object[] getValues(UniqueValueEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValues'");
    }


    @Override
    public Optional<UniqueValueEntity> get(String key, String value) {
       return dao.get(key, value);
    }


    // @Override
    // public PagingIterable<UniqueValueEntity> list(String key) {
    //    return dao.list(key);
    // }


    @Override
    public List<UniqueValueEntity> listAll() {
        return dao.listAll();
    }
}
