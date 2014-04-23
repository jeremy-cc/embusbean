package uk.co.cc.emBus2.events;

import uk.co.cc.emBus2.EmbusInstance;

import java.util.EventObject;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class Message extends EventObject {

    protected String message;
    protected String subscription;
    protected String source;
    protected String context;
    protected long messageId;

    public Message(EmbusInstance engine, String message, long messageId, String subscription, String source, String context)
    {
        super(engine);
        this.message = message;
        this.messageId = messageId;
        this.subscription = subscription;
        this.source = source;
        this.context = context;
    }

    public String getMessage() {
        return message;
    }

    public String getSubscription() {
        return subscription;
    }

    public String getSource() {
        return source;
    }

    public String getContext() {
        return context;
    }

    public long getMessageId() {
        return messageId;
    }
}
