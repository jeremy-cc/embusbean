package uk.co.cc.emBus2.events;

import uk.co.cc.emBus2.EmbusInstance;

import java.util.EventObject;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class ErrorMessage extends EventObject {
    protected String errorMessage;
    protected int errorNumber;

    public ErrorMessage(EmbusInstance source, int errorNumber, String errorMessage)
    {
        super(source);
        this.errorNumber = errorNumber;
        this.errorMessage = errorMessage;
    }

    String getErrorMessage()
    {
        return errorMessage;
    }

    int getErrorNumber()
    {
        return this.errorNumber;
    }
}
