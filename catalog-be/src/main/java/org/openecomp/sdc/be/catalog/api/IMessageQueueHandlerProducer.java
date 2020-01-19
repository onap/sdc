package org.openecomp.sdc.be.catalog.api;

public interface IMessageQueueHandlerProducer {
    
    IStatus pushMessage(ITypeMessage message);
    IStatus init();

}
