package org.openecomp.sdc.itempermissions.dao;

import java.util.Optional;
import java.util.Set;

import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import com.datastax.oss.driver.api.core.PagingIterable;

public class ItemPermissionsDaoImpl {

    private final ItemPermissionsDao dao;

    public ItemPermissionsDaoImpl(ItemPermissionsDao dao) {
        this.dao = dao;
    }

    // list all permissions for an item
    public PagingIterable<ItemPermissionsEntity> listItemPermissions(String itemId) {
        // DAO method returns PagingIterable already
        return dao.getItemPermissions(itemId);
    }

    // add or remove permissions
    public void updateItemPermissions(String itemId, String permission,
                                      Set<String> addedUsersIds,
                                      Set<String> removedUsersIds) {

        // Add permissions
        addedUsersIds.forEach(userId ->
            dao.addPermission(new ItemPermissionsEntity(itemId, userId, permission))
        );

        // Remove permissions
        removedUsersIds.stream()
            .map(userId -> dao.getUserItemPermissionEntity(itemId, userId))
            .filter(opt -> opt.map(e -> e.getPermission().equals(permission)).orElse(false))
            .forEach(opt -> opt.ifPresent(perm -> dao.deletePermission(perm)));
    }

    // get a single user’s permission for an item
    public Optional<String> getUserItemPermission(String itemId, String userId) {
        return dao.getUserItemPermissionEntity(itemId, userId)
                  .map(ItemPermissionsEntity::getPermission);
    }

    // delete all permissions for an item
    public void deleteItemPermissions(String itemId) {
        dao.deleteItemPermissions(itemId);
    }
}
