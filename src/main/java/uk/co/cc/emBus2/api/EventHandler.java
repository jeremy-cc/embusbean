package uk.co.cc.emBus2.api;

import uk.co.cc.emBus2.events.AckMessage;
import uk.co.cc.emBus2.events.ErrorMessage;
import uk.co.cc.emBus2.events.Message;

/**
 * Created by jeremyb on 22/04/2014.
 */
public abstract interface EventHandler {

    public abstract void onMessage(Message msg);
    public abstract void onAcknowledgement(AckMessage message);
    public abstract void onError(ErrorMessage message);
}
