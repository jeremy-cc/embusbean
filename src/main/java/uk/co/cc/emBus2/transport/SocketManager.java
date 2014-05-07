package uk.co.cc.emBus2.transport;

import uk.co.cc.emBus2.Constants;
import uk.co.cc.emBus2.EventThread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class SocketManager {
    private AtomicBoolean proceed = new AtomicBoolean(false);
    private AtomicBoolean connected = new AtomicBoolean(false);
    public AtomicBoolean connectionPending = new AtomicBoolean(false);
    private int LENGTH_FIELD = 8;

    private ConcurrentLinkedQueue<ProtocolMessage> outputQueue;
    private ConcurrentLinkedQueue<ProtocolMessage> inputQueue;

    private Socket socket;
    private SocketReader reader;
    private SocketWriter writer;

    private Thread writerThread;
    private Thread readerThread;

    String PING_MESSAGE = "d\037g\n";

    public String key;
    public String sessionKey;
    private EventThread dispatcher;

    public SocketManager(EventThread dispatcher, String sKek, String sSessionKey)
    {
        this.dispatcher = dispatcher;

        this.inputQueue = dispatcher.getCommunicationsQueue();
        this.outputQueue = new ConcurrentLinkedQueue<ProtocolMessage>();

        this.proceed.set(true);
        this.connected.set(false);
        this.key = sKek;
        this.sessionKey = sSessionKey;
    }

    public void connected() {
        this.connectionPending.set(false);
        this.connected.set(true);
    }

    public boolean connect(String address, int port)
    {
        boolean result = false;
        try
        {
            this.socket = new Socket(address, port);
            this.reader = new SocketReader();
            this.writer = new SocketWriter();

            readerThread = new Thread(reader);
            readerThread.start();
            
            writerThread = new Thread(writer);
            writerThread.start();

            result=true;
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace(System.err);
        }
        catch (IOException e)
        {
            sendError(Constants.err_connectiontimeout);
            e.printStackTrace(System.err);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
        this.connected.set(result);
        this.connectionPending.set(result);
        return result;
    }

    public void read()
    {
        try
        {
            int msg_length_data = 0;
            DataInputStream input = new DataInputStream(this.socket.getInputStream());

            while (canProcess()) {
                try
                {
                    int available = input.available();

                    if(available > 0) {
                        if(msg_length_data == 0) {
                            if(available > this.LENGTH_FIELD) {// we have more bytes awaiting than the message length field
                                byte[] lenBuffer = new byte[this.LENGTH_FIELD];
                                input.read(lenBuffer);
                                msg_length_data = hexStringToInt(new String(lenBuffer, Charset.forName("ISO-8859-1")));
                            }
                        } else { // dealing with a message
                            byte [] msgData = new byte[msg_length_data];
                            input.read(msgData); // block and wait for the rest of the message; pointless to read incrementally here.
                            msg_length_data = 0; // reset

                            StringBuilder msgBuf = new StringBuilder(new String(msgData, Charset.forName("ISO-8859-1")));
                            ProtocolMessage msg = new ProtocolMessage(msgData, msgBuf, this.key, this.sessionKey);

                            this.inputQueue.add(msg);
                            synchronized (this.inputQueue) {
                                this.inputQueue.notifyAll();
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    sendError(Constants.err_connectiontimeout);
                }
                Thread.sleep(Constants.SO_SLEEP_DURATION);
            }
            input.close();
        }
        catch(InterruptedException ie) {

        }
        catch (IOException e)
        {
            sendError(Constants.err_connectiontimeout);
        }
        catch (Exception e)
        {
            sendError(Constants.err_connectiontimeout);
        }
    }

    public void write()
    {
        try
        {
            DataOutputStream output = new DataOutputStream(this.socket.getOutputStream());

            int iLength;
            while (canProcess()) {
                try
                {
                    ProtocolMessage msg;
                    for (;canProcess() && (msg = outputQueue.poll()) != null;)
                    {
                        String strMsg = msg.toString();

                        iLength = strMsg.length();
                        String strLength = toHex(iLength);
                        while (strLength.length() < this.LENGTH_FIELD) {
                            strLength = "0" + strLength;
                        }
                        output.writeBytes(strLength);

                        byte[] bytesOut = msg.generateBytesFromMsg(strMsg, false);
                        output.write(bytesOut);

                        output.flush();
                    }
                    synchronized (outputQueue) {
                        outputQueue.wait(Constants.SO_SLEEP_DURATION);
                    }
                }
                catch (IOException e)
                {
                    sendError(Constants.err_connectiontimeout);
                }
            }
        }
        catch(InterruptedException ie) {

        }
        catch (IOException e)
        {
            sendError(Constants.err_connectiontimeout);
        }
        catch (Exception e)
        {
            sendError(Constants.err_connectiontimeout);
        }
    }

    public boolean enqueue(ProtocolMessage msg)
    {
        boolean rc = false;
        if (canProcess())
        {
            this.outputQueue.add(msg);
            synchronized (outputQueue) {
                outputQueue.notifyAll();
            }
            rc = true;
        }
        return rc;
    }

    private boolean canProcess() {
        return (this.connected.get() && this.proceed.get());
    }

    public void shutdown() {
        close();
    }

    protected void close()
    {
        this.connected.set(false);
        this.proceed.set(false);

        try {
            if(null != this.writerThread) {
                this.writerThread.interrupt();
                this.writerThread.join();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            if(null != this.readerThread) {
                this.readerThread.interrupt();
                this.readerThread.join();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            if(null != this.socket) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    protected void sendError(Integer iErrorCode)
    {
        this.connected.set(false);
        Map<String,String> message = new HashMap<String, String>();
        message.put("d", "5");
        message.put("u", iErrorCode.toString());
        message.put("4", "Server Disconnect");

        ProtocolMessage protocolMessage = new ProtocolMessage(message, this.key, this.sessionKey);

        this.inputQueue.add(protocolMessage);
    }

    public int getOutputQueueSize()
    {
        return this.outputQueue.size();
    }

    public int getInputQueueSize()
    {
        return this.inputQueue.size();
    }

    private int hexStringToInt(String sHexString)
    {
        return Integer.parseInt(sHexString.trim(), 16);
    }

    private String toHex(int iValue)
    {
        return Integer.toHexString(iValue);
    }

    class SocketReader implements Runnable
    {
        public void run()
        {
            SocketManager.this.read();
        }
    }

    class SocketWriter implements Runnable
    {
        public void run()
        {
            SocketManager.this.write();
        }
    }
}
