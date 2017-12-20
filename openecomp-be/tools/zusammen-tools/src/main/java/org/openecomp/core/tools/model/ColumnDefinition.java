/**
 * Copyright © 2016-2017 European Support Limited.
 */
package org.openecomp.core.tools.model;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;

import java.util.Arrays;

public class ColumnDefinition {

    private String keyspace;
    private String table;
    private String name;
    private String type;

    public ColumnDefinition() {
    }

    public ColumnDefinition(String keyspace, String table, String name, DataType type) {
        this.keyspace = keyspace;
        this.table = table;
        this.name = name;
        this.type = type.getName().toString();
    }

    public ColumnDefinition(Definition definition) {
        this(definition.getKeyspace(), definition.getTable(), definition.getName(), definition.getType());
    }

    /**
     * The name of the keyspace this column is part of.
     *
     * @return the name of the keyspace this column is part of.
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Returns the name of the table this column is part of.
     *
     * @return the name of the table this column is part of.
     */
    public String getTable() {
        return table;
    }

    /**
     * Returns the name of the column.
     *
     * @return the name of the column.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the column.
     *
     * @return the type of the column.
     */
    public String getType() {
        return type;
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(new Object[]{keyspace, table, name, type});
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ColumnDefinition))
            return false;

        ColumnDefinition other = (ColumnDefinition) o;
        return keyspace.equals(other.keyspace)
                && table.equals(other.table)
                && name.equals(other.name)
                && type.equals(other.type);
    }
}
