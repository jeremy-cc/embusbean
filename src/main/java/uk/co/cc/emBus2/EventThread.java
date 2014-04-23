package uk.co.cc.emBus2;

import uk.co.cc.emBus2.transport.ProtocolMessage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reimplementation of eMBusNotification
 * Created by jeremyb on 22/04/2014.
 */
public class EventThread implements Runnable {
    private EmbusInstance engine = null;
    private ConcurrentLinkedQueue<ProtocolMessage> queue;
    private AtomicBoolean hasStarted = new AtomicBoolean(false);
    private AtomicBoolean proceed = new AtomicBoolean(false);

    public EventThread(EmbusInstance engine) {
        queue = new ConcurrentLinkedQueue<ProtocolMessage>();
        this.engine = engine;
        this.engine.setEventThread(this);
    }

    public void stop() {
        this.hasStarted.set(false);
        this.proceed.set(false);
    }

    public void run() {
        this.hasStarted.set(true);
        this.proceed.set(true);

        try
        {
            while ((this.proceed.get()))
            {
                ProtocolMessage message;
                // process in batches so that we respond more gracefully to shutdown requests
                for(int count = 0; this.proceed.get() && (message = this.queue.poll()) != null && count < 10;) {
                    this.engine.messageHandler(message);
                    count++;
                }
                Thread.sleep(Constants.EVENT_SLEEP_DURATION);
            }
        }
        catch(InterruptedException ie) {

        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public boolean incoming(ProtocolMessage message)
    {
        return queue.add(message);
    }

    public boolean hasStarted() {
        return hasStarted.get();
    }

    public ConcurrentLinkedQueue<ProtocolMessage> getCommunicationsQueue() {
        return queue;
    }
}
