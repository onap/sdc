package org.openecomp.sdc.be.catalog.api;

import java.io.Serializable;

import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;



/**
 * Represent Component (service, resource etc...) change message added to the
 * message queue by sdc backend.<br>
 * 
 * @author ms172g
 *
 */
public interface IComponentMessage extends Serializable, ITypeMessage {
	/**
	 * Change Type
	 * @return
	 */
	ChangeTypeEnum getChangeType();
	CatalogUpdateTimestamp getCatalogUpdateTimestamp();

}
