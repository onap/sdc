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
import org.openecomp.core.dao.BaseDao;

public abstract class CassandraBaseDao<T> implements BaseDao<T> {
  protected abstract Mapper<T> getMapper();

  protected abstract Object[] getKeys(T entity);

  @Override
  public void create(T entity) {
    getMapper().save(entity);
  }

  @Override
  public void update(T entity) {
    getMapper().save(entity);
  }

  @Override
  public T get(T entity) {
    return getMapper().get(getKeys(entity));
  }

  @Override
  public void delete(T entity) {
    getMapper().delete(entity);
  }
}
