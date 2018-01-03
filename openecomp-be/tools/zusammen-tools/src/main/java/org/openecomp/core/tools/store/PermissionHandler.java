package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import java.util.Objects;
import java.util.Optional;

public class PermissionHandler {

  private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static PermissionAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(PermissionAccessor.class);


  public Optional<String> getItemUserPermission(String itemId, String user) {
    ResultSet resultSet = accessor.getItemUserPermission(itemId, user);
    Row row = resultSet.one();

    if (Objects.nonNull(row)) {
      return Optional.of(row.getString("permission"));
    } else {
      return Optional.empty();
    }
  }

  public void setItemUserPermission(String itemId, String user, String permission) {
    accessor.setItemUserPermission(itemId, user, permission);
  }


  @Accessor
  interface PermissionAccessor {


    @Query("INSERT into dox.item_permissions (item_id,user_id,permission)  VALUES (?,?,?)")
    void setItemUserPermission(String permission, String itemId, String userId);


    @Query("SELECT permission FROM dox.item_permissions WHERE item_id=? AND user_id=?")
    ResultSet getItemUserPermission(String itemId, String userId);
  }

}