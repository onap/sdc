package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemHandler {

  public List<String> getItemList() {
    ResultSet resultSet = NoSqlDbFactory.getInstance().createInterface()
        .getMappingManager().createAccessor(ItemAccessor.class).list();
    List<Row> rows = resultSet.all();

    if (rows != null) {
      return rows.stream().map(row -> row.getString("item_id")).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  @Accessor
  interface ItemAccessor {
    @Query("SELECT item_id FROM zusammen_dox.item")
    ResultSet list();
  }

}