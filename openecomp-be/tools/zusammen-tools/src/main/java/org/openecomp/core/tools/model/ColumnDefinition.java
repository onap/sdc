/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T European Support Limited. All rights reserved.
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
package org.openecomp.core.tools.model;

import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import java.util.Arrays;

public class ColumnDefinition {

    private String keyspace;
    private String table;
    private String name;
    private String type;
    private DataType dataType; // store actual DataType

    public ColumnDefinition() {
    }

    public ColumnDefinition(String keyspace, String table, String name, String type) {
        this.keyspace = keyspace;
        this.table = table;
        this.name = name;
        this.type = type;
    }

    public ColumnDefinition(String keyspace, String table, String name, DataType type) {
        this.keyspace = keyspace;
        this.table = table;
        this.name = name;
        this.type = type.asCql(false, false);
        this.dataType = type; // store DataType
    }

   public ColumnDefinition(ColumnMetadata columnMetadata, String tableName) {
        this.keyspace = columnMetadata.getKeyspace().asInternal();
        this.table = tableName; // pass the table name explicitly
        this.name = columnMetadata.getName().asInternal();
        this.type = columnMetadata.getType().asCql(false, false); // CQL type as string
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    // New getter for DataType
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(new Object[]{keyspace, table, name, type});
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ColumnDefinition)) {
            return false;
        }
        ColumnDefinition other = (ColumnDefinition) o;
        return keyspace.equals(other.keyspace)
            && table.equals(other.table)
            && name.equals(other.name)
            && type.equals(other.type);
    }
}
