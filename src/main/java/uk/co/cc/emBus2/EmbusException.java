package uk.co.cc.emBus2;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class EmbusException extends Exception {
    public static final int CONNECTION_ERROR = 1;
    public static final int CREATE_ERROR = 2;
    public static final int DECYPTION_ERROR = 3;
    public static final int UNKNOWN_ERROR = 9999;

    protected int reasonCode = 0;

    public EmbusException() {}

    public EmbusException(String message)
    {
        super(message);
    }

    public EmbusException(String message, int iReason)
    {
        super(message);
        this.reasonCode = iReason;
    }

    public int getReasonCode()
    {
        return this.reasonCode;
    }
}
