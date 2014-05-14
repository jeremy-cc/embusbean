package uk.co.cc.emBus2.util;

import uk.co.cc.emBus2.Constants;
import uk.co.cc.emBus2.EmbusInstance;

/**
 * Created by jeremyb on 14/05/2014.
 */
public class Logger {

    public static void log_info(String message, Object ... args) {
        if(EmbusInstance.isLoggingEnabled()) {
            System.out.println(String.format("%s (%s): " + String.format(message.trim(), args), Constants.APP_NAME, Constants.VERSION, message));
        }
    }

    public static void log_error(String message, Object ... args) {
        System.err.println(String.format("ERROR %s (%s): " + String.format(message.trim(), args), Constants.APP_NAME, Constants.VERSION, message));
    }
}
