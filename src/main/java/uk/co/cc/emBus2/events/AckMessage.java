package uk.co.cc.emBus2.events;

import uk.co.cc.emBus2.EmbusInstance;

import java.util.EventObject;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class AckMessage extends EventObject {
    private String sourceUser;
    private long messageId;

    public AckMessage(EmbusInstance source, String sourceUser, long messageId) {
        super(source);
        this.messageId = messageId;
        this.sourceUser = sourceUser;
    }

    public String getSourceUser() {
        return sourceUser;
    }

    public long getMessageId() {
        return messageId;
    }
}
