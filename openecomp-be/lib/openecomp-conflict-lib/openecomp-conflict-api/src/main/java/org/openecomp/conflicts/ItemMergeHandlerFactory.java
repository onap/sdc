package org.openecomp.conflicts;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

import java.util.Optional;

public abstract class ItemMergeHandlerFactory
    extends AbstractComponentFactory<ItemMergeHandler> {

  public static ItemMergeHandlerFactory getInstance() {
    return AbstractFactory.getInstance(ItemMergeHandlerFactory.class);
  }

  public abstract Optional<ItemMergeHandler> createInterface(String itemId);
}