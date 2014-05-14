package uk.co.cc.emBus2.transport;

import uk.co.cc.emBus2.Constants;
import uk.co.cc.emBus2.EmbusInstance;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static uk.co.cc.emBus2.util.Logger.log_info;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class SocketManager {
    private AtomicBoolean proceed = new AtomicBoolean(false);
    private AtomicBoolean connected = new AtomicBoolean(false);
    public AtomicBoolean connectionPending = new AtomicBoolean(false);
//    private int LENGTH_FIELD = 8;

    private ConcurrentLinkedQueue<ProtocolMessage> outputQueue;
    private ConcurrentLinkedQueue<ProtocolMessage> inputQueue = new ConcurrentLinkedQueue<ProtocolMessage>();

    private Socket socket;
    private SocketReader reader;
    private SocketWriter writer;
    private MessagePusher processor;

    private Thread writerThread;
    private Thread readerThread;
    private Thread processorThread;

    String PING_MESSAGE = "d\037g\n";

    public String key;
    public String sessionKey;
    private EmbusInstance dispatcher;

    public SocketManager(EmbusInstance dispatcher, String sKek, String sSessionKey)
    {
        this.dispatcher = dispatcher;

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

            this.connected.set(true);

            this.reader = new SocketReader();
            this.writer = new SocketWriter();
            this.processor = new MessagePusher();

            readerThread = new Thread(reader);
            readerThread.start();

            processorThread = new Thread(processor);
            processorThread.start();

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
        this.connectionPending.set(result);
        return result;
    }

    public void read()
    {
        try
        {
            int msg_length_data = 0;
            InputStream input = getSocketInputStream();

            int counter = 0;
            char [] log = new char[200];

            while (canProcess()) {
                try
                {
                    int available = input.available();

                    if(available > 0) {
                        if(msg_length_data == 0) {
                            if(available > ProtocolMessage.HEADER_LENGTH) {// we have more bytes awaiting than the message length field
                                byte[] lenBuffer = new byte[ProtocolMessage.HEADER_LENGTH];
                                input.read(lenBuffer);
                                msg_length_data = hexStringToInt(latinise(lenBuffer));
                                System.out.println(msg_length_data);
                            }
                        } else { // dealing with a message
                            byte [] msgData = new byte[msg_length_data];
                            input.read(msgData); // block and wait for the rest of the message; pointless to read incrementally here.
                            msg_length_data = 0; // reset

                            StringBuilder msgBuf = new StringBuilder(latinise(msgData));
                            ProtocolMessage msg = new ProtocolMessage(msgData, msgBuf, this.key, this.sessionKey);
//
//                            log[counter] = '|';
//                            counter++;
                            this.inputQueue.add(msg);
                            synchronized (this.inputQueue) {
                                this.inputQueue.notifyAll();
                            }
                        }
                    } else {
                        log[counter] = '.';
                        counter++;
                        Thread.sleep(Constants.SO_SLEEP_DURATION);
                    }
                    if(counter % 200 == 0) {
                        System.out.println(log);
                        log = new char[200];
                        counter = 0;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.err);
                    sendError(Constants.err_connectiontimeout);
                }
            }
            input.close();
        }
        catch(InterruptedException ie) {

        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
            sendError(Constants.err_connectiontimeout);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            sendError(Constants.err_connectiontimeout);
        }
    }

    InputStream getSocketInputStream() throws IOException {
        return new DataInputStream(this.socket.getInputStream());
    }

    private String latinise(byte[] data) {
        return new String(data, Charset.forName(Constants.CHARSET));
    }

    // attempt to pull multiple messages off the socket at the same time.
    public void readBuffered() {
        try {

            int chunk_length = 0;
            InputStream input = getSocketInputStream();

            byte [] buffer = null;

            byte [] remainder = null;
            byte [] currentHeader = null;

            while (canProcess())
            {
                try {
                    int available = input.available();
                    if(available > 0) {
                        int offset = (remainder == null ? 0 : remainder.length);
                        buffer = new byte[available + offset];

                        if (null != remainder) {
                            System.arraycopy(remainder, 0, buffer, 0, remainder.length);
                            remainder = null;

                        }
                        // ensure we start at the offset position as required.
                        input.read(buffer, offset, available);

                        // chunk the buffer
                        for(int idx = 0; idx < buffer.length;) {

                            // read the header of a chunk
                            if (chunk_length == 0) {
                                if((buffer.length - idx)>=ProtocolMessage.HEADER_LENGTH) {
                                    currentHeader = new byte[ProtocolMessage.HEADER_LENGTH];
                                    System.arraycopy(buffer, idx, currentHeader, 0, ProtocolMessage.HEADER_LENGTH);
                                    idx += ProtocolMessage.HEADER_LENGTH;

                                    //                                log("chunk length block is %s", latinise(currentHeader));
                                    try {
                                        chunk_length = hexStringToInt(latinise(currentHeader));
                                    } catch (NumberFormatException nfe) {
                                        nfe.printStackTrace(System.err);
                                        throw nfe;
                                    }
                                }
                                else {
                                    // we discovered insufficient bytes left to get the length field.
                                    int len = buffer.length - idx; // create enough space to push the read bytes back.
                                    remainder = new byte[len];
                                    // push the header back into the buffer
                                    System.arraycopy(buffer, idx, remainder, 0, len);
                                    break;
                                }
                            }

                            // if we're dealing with the body of the message:
                            if (chunk_length > 0) {
                                // if there is enough space in the buffer to read the rest of this chunk completely
                                if (buffer.length - idx >= chunk_length) {
                                    byte[] data = new byte[chunk_length];
                                    System.arraycopy(buffer, idx, data, 0, chunk_length);
                                    idx += chunk_length;

                                    StringBuilder built = new StringBuilder(latinise(data));
                                    this.inputQueue.add(new ProtocolMessage(data, built, this.key, this.sessionKey));

                                    currentHeader = null;
                                    chunk_length = 0; // reset

                                } else {
                                    // we discovered insufficient bytes to fully read the message.
                                    // not enough left, we need to wait for additional input.
                                    remainder = new byte[buffer.length - idx + 8];

                                    // push the header back into the buffer
                                    System.arraycopy(currentHeader, 0, remainder, 0, currentHeader.length);
                                    System.arraycopy(buffer, idx, remainder, currentHeader.length, remainder.length - currentHeader.length ); // copy the remaining unconsumed buffer to the remainder

                                    currentHeader = null;
                                    chunk_length = 0; // reset

                                    break; // break
                                }
                            } else {
                                break;
                            }
                        }

                        synchronized (this.inputQueue) {
                            if(this.inputQueue.size() > 0) {
                                inputQueue.notifyAll();
                            }
                        }
                    } else {
                        Thread.sleep(Constants.SO_SLEEP_DURATION);
                    }


                } catch(IOException ioe) {
                    ioe.printStackTrace(System.err);
                    sendError(Constants.err_connectiontimeout);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
            sendError(Constants.err_connectiontimeout);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            sendError(Constants.err_connectiontimeout);
        }
    }

    public void handleRead() {
        try {
            while(canProcess()) {

                ProtocolMessage msg = null;

                for(msg = inputQueue.poll(); msg != null;) {
                    this.dispatcher.messageHandler(msg);
                    msg = inputQueue.poll();
                }
                try {
                    synchronized(inputQueue) {
                        inputQueue.wait(Constants.SO_SLEEP_DURATION);
                    }
                } catch(InterruptedException ie) {
                    ie.printStackTrace(System.err);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
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

                        byte [] bytesOut = packageForDispatch(msg);
                        log_info("Writing %s bytes to outputstream for request %s", bytesOut.length, msg.toString());
                        output.write(bytesOut);
                        output.flush();
                    }
                    synchronized (outputQueue) {
                        outputQueue.wait(Constants.SO_SLEEP_DURATION);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.err);
                    sendError(Constants.err_connectiontimeout);
                }
            }
        }
        catch(InterruptedException ie) {

        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
            sendError(Constants.err_connectiontimeout);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            sendError(Constants.err_connectiontimeout);
        }
    }

    public byte[] packageForDispatch(ProtocolMessage msg) throws UnsupportedEncodingException {

        byte [] encodedMessage = msg.generateBytesFromMsg(msg.toString(), false);
        byte [] buffer = new byte[ProtocolMessage.HEADER_LENGTH + encodedMessage.length];

        System.arraycopy(msg.messageLengthAsHex().getBytes(), 0, buffer, 0,  ProtocolMessage.HEADER_LENGTH);
        System.arraycopy(encodedMessage, 0, buffer, ProtocolMessage.HEADER_LENGTH, encodedMessage.length);

        return buffer;
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

    boolean canProcess() {
        return (this.connected.get() && this.proceed.get());
    }

    public void shutdown() {
        close();
    }

    protected void close()
    {
        this.connected.set(false);
        this.proceed.set(false);

        terminate(writerThread);
        terminate(readerThread);
        terminate(processorThread);
    }

    private void terminate(Thread t) {
        try {
            if(null != t) {
                t.interrupt();
                t.join();
            }
        } catch (Exception e) {
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
        synchronized(this.inputQueue) {
            inputQueue.notifyAll();
        }

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
            SocketManager.this.readBuffered();
        }
    }

    class MessagePusher implements Runnable
    {
        public void run() {
            SocketManager.this.handleRead();
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
