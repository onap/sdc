package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ConsumerEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditConsumerEventFactory extends AuditBaseEventFactory {

    protected static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" ECOMP_USER = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    private final ConsumerEvent event;

    public AuditConsumerEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier, ConsumerDefinition ecompUser) {
        super(action);
        event = new ConsumerEvent(getAction().getName(), commonFields, buildConsumerName(ecompUser), buildUserName(modifier));
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getModifier(), event.getEcompUser(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }



    static String buildConsumerName(ConsumerDefinition consumer) {
        StringBuilder ecompUser = new StringBuilder();
        if (consumer != null) {
            appendIfNotEmpty(consumer.getConsumerName(), ecompUser, ",");
            appendIfNotEmpty(consumer.getConsumerSalt(), ecompUser, ",");
            appendIfNotEmpty(consumer.getConsumerPassword(), ecompUser, ",");
        }
        return ecompUser.toString();
    }
}
