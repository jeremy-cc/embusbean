package uk.co.cc.emBus2;

import uk.co.cc.emBus2.api.EventHandler;
import uk.co.cc.emBus2.events.AckMessage;
import uk.co.cc.emBus2.events.ErrorMessage;
import uk.co.cc.emBus2.events.Message;

/**
 * Created by jeremyb on 24/04/2014.
 */
public class Standalone {
    public static void main(String [] args) {
        EmbusInstance instance = new EmbusInstance();
        EventThread thread = new EventThread(instance);
        instance.setEventThread(thread);

        instance.connect("fixmux", "password", "", "192.168.10.10:32615", "tcpip", 0, 2);

        instance.subscribe("DEV.FIX.SEB.Prices", 0);
//        instance.subscribe("PDT.FIX.SEB.PriceRequest", 0);
//        instance.subscribe("PDT.FIX.SEB.Quotes", 0);
//        instance.subscribe("PDT.FIX.SEB.QuoteRequest", 0);

        instance.addListener(new EventHandler() {
            private int count = 0;
            private long startTime = System.currentTimeMillis();

            @Override
            public void onMessage(Message msg) {
                count += 1;
                if(count % 100 == 0) {
                    long elapsed = System.currentTimeMillis()-startTime;
                    double rate = (100.0 / (elapsed/1000.0));
                    System.out.println(String.format("Processed %3d messages in %3d ms at a rate of %.2f rq/s", count, System.currentTimeMillis()-startTime, rate));

                    count = 0;
                    startTime = System.currentTimeMillis();
                }

            }

            @Override
            public void onAcknowledgement(AckMessage message) {

            }

            @Override
            public void onError(ErrorMessage message) {
                System.out.println(message.toString());
            }
        });

    }
}
