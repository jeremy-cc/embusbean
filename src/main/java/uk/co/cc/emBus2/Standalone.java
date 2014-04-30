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

        instance.connect("pricing_engine", "password", "", "lon1devlcis002.ccycloud.com:42615", "tcpip", 0, 2);

        instance.subscribe("PDT.FIX.SEB.Prices", 0);
        instance.subscribe("PDT.FIX.SEB.PriceRequest", 0);
        instance.subscribe("PDT.FIX.SEB.Quotes", 0);
        instance.subscribe("PDT.FIX.SEB.QuoteRequest", 0);

        instance.addListener(new EventHandler() {

            @Override
            public void onMessage(Message msg) {
                System.out.println(msg.getMessage());
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
