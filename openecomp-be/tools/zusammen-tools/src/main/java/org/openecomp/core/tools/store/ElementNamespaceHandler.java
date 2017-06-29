package org.openecomp.core.tools.store;


import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;

public class ElementNamespaceHandler {

    private static  NoSqlDb nnoSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static  ElementNamespaceAccessor accessor = nnoSqlDb.getMappingManager().createAccessor(ElementNamespaceAccessor.class);

    public void createElementNamespace(ElementEntity elementEntity) {
         accessor.create(elementEntity.getSpace(),elementEntity.getItemId(),elementEntity.getElement_id(),elementEntity.getNamespace());
    }

    @Accessor
    interface ElementNamespaceAccessor {
        @Query("UPDATE zusammen_dox.element_namespace SET namespace=:ns WHERE space=:space AND item_id=:item AND element_id=:id ")
        void create(@Param("space") String space, @Param("item") String item, @Param("id") String id, @Param("ns") String ns);
    }


}
