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

        int result = instance.connect("fixmux", "password", "", "192.168.10.10:32615", "tcpip", 0, 2);

        System.err.println(String.format("Result of connect call: %d", result));

        instance.subscribe("DEV.FIX.SEB.Prices", 0);
        instance.subscribe("DEV.FIX.OANDA.Prices", 0);

        instance.addListener(new EventHandler() {
            private int count = 0;
            private long startTime = System.currentTimeMillis();
            private int idx = 0;
            private int len = 0;

            private double[] sample = new double[20];


            @Override
            public void onMessage(Message msg) {
                count += 1;
                if(count % 100 == 0) {
                    long elapsed = System.currentTimeMillis()-startTime;
                    double rate = (100.0 / (elapsed/1000.0));

                    sample[idx] = rate;
                    idx = ((idx + 1) % 20);
                    if(len < 20) {
                        len++;
                    }

                    System.out.println(String.format("Processed %3d messages in %3d ms at a instantaneous rate of %.2f rq/s, average rate is %.2f rq/s", count, System.currentTimeMillis()-startTime, rate, average()));

                    count = 0;
                    startTime = System.currentTimeMillis();
                }

            }

            private double average() {
                double avg = 0.0;
                for(int i = 0; i < len; i++) {
                    avg += sample[i];
                }
                return avg/len;
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
