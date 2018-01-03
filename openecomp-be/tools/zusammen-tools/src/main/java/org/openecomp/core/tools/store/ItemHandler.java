package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import java.util.ArrayList;
import java.util.List;

public class ItemHandler {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static ItemAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ItemAccessor.class);


  public List<String> getItemList() {
    ResultSet resultSet = accessor.list();
    List<Row> rows = resultSet.all();

    List<String> items = new ArrayList<>();
    if (rows != null) {
      rows.forEach(row -> items.add(row.getString("item_id")));
    }
    return items;
  }

  @Accessor
  interface ItemAccessor {


    @Query("SELECT item_id FROM zusammen_dox.item")
    ResultSet list();
  }

}