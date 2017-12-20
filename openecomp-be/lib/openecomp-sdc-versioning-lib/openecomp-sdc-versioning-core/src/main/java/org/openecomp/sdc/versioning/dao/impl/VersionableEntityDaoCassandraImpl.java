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

package org.openecomp.sdc.versioning.dao.impl;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.UDTMapper;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.dao.VersionableEntityDao;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class VersionableEntityDaoCassandraImpl implements VersionableEntityDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static Logger Logger =
      (Logger) LoggerFactory.getLogger(VersionableEntityDaoCassandraImpl.class);
  private static UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  private static String commaSeparatedQuestionMarks(int size) {
    StringBuilder sb = new StringBuilder(size * 2 - 1);
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('?');
    }
    return sb.toString();

  }

  @Override
  public void initVersion(VersionableEntityMetadata metadata, String entityId, Version baseVersion,
                          Version newVersion) {
    ResultSet rows = loadVersionRows(metadata, entityId, baseVersion);
    List<String> columnNames =
        rows.getColumnDefinitions().asList().stream().map(ColumnDefinitions.Definition::getName)
            .collect(Collectors.toList());

    String insertCql = String.format("insert into %s (%s) values (%s)", metadata.getName(),
        CommonMethods.listToSeparatedString(columnNames, ','),
        commaSeparatedQuestionMarks(columnNames.size()));
    Logger.debug("insertCql", insertCql);

    for (Row row : rows) {
      List<Object> columnValues = new ArrayList<>();
      Map<String, Object> columnNameToValue = new HashMap<>();

      for (String columnName : columnNames) {
        if (metadata.getVersionIdentifierName().equals(columnName)) {
          columnValues.add(versionMapper.toUDT(newVersion));
          columnNameToValue.put(columnName, newVersion.toString());
        } else {
          Object value = row.getObject(columnName);
          columnValues.add(value);
          columnNameToValue.put(columnName, value);
        }
      }

      initRowUniqueValues(metadata.getUniqueValuesMetadata(), columnNameToValue);

      noSqlDb.execute(insertCql, columnValues.toArray());
    }
  }

  @Override
  public void deleteVersion(VersionableEntityMetadata metadata, String entityId,
                            Version versionToDelete, Version backToVersion) {
    deleteRowsUniqueValues(metadata, entityId, versionToDelete);

    String deleteCql = String.format("delete from %s where %s=? and %s=?", metadata.getName(),
        metadata.getIdentifierName(), metadata.getVersionIdentifierName());
    noSqlDb.execute(deleteCql, entityId, versionMapper.toUDT(versionToDelete));
  }

  @Override
  public void closeVersion(VersionableEntityMetadata versionableTableMetadata, String entityId,
                           Version versionToClose) {
    // redundant in cassandra impl.
  }

  private ResultSet loadVersionRows(VersionableEntityMetadata metadata, String entityId,
                                    Version version) {
    String selectCql = String.format("select * from %s where %s=? and %s=?", metadata.getName(),
        metadata.getIdentifierName(), metadata.getVersionIdentifierName());
    Logger.debug("selectCql", selectCql);
    Logger.debug("entityId", entityId);
    Logger.debug("version", version);

    return noSqlDb.execute(selectCql, entityId, versionMapper.toUDT(version));
  }

  private void initRowUniqueValues(List<UniqueValueMetadata> metadata,
                                   Map<String, Object> columnNameToValue) {
    for (UniqueValueMetadata uniqueMetadata : metadata) {
      List<String> uniqueValueCombination = uniqueMetadata.getUniqueConstraintIdentifiers().stream()
          .map(colName -> (String) columnNameToValue.get(colName)).collect(Collectors.toList());
      UniqueValueUtil.createUniqueValue(uniqueMetadata.getType(),
          uniqueValueCombination.toArray(new String[uniqueValueCombination.size()]));
    }
  }

  private void deleteRowUniqueValues(List<UniqueValueMetadata> metadata,
                                     Map<String, Object> columnNameToValue) {
    for (UniqueValueMetadata uniqueMetadata : metadata) {
      List<String> uniqueValueCombination = uniqueMetadata.getUniqueConstraintIdentifiers().stream()
          .map(colName -> (String) columnNameToValue.get(colName)).collect(Collectors.toList());
      UniqueValueUtil.deleteUniqueValue(uniqueMetadata.getType(),
          uniqueValueCombination.toArray(new String[uniqueValueCombination.size()]));
    }
  }

  private void deleteRowsUniqueValues(VersionableEntityMetadata metadata, String entityId,
                                      Version version) {
    if (metadata.getUniqueValuesMetadata().isEmpty()) {
      return;
    }
    ResultSet rows = loadVersionRows(metadata, entityId, version);
    List<String> columnNames =
        rows.getColumnDefinitions().asList().stream().map(ColumnDefinitions.Definition::getName)
            .collect(Collectors.toList());

    for (Row row : rows) {
      Map<String, Object> columnNameToValue =
          columnNames.stream().filter(name -> row.getObject(name) != null).collect(Collectors
              .toMap(Function.identity(),
                  columnName -> metadata.getVersionIdentifierName().equals(columnName) ? version
                      .toString() : row.getObject(columnName)));
      deleteRowUniqueValues(metadata.getUniqueValuesMetadata(), columnNameToValue);
    }
  }
}
