package uk.co.cc.emBus2.transport;

import uk.co.cc.emBus2.Constants;
import uk.co.cc.emBus2.EventThread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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

    private Socket socket;
    private SocketReader reader;
    private SocketWriter writer;
    
    private Thread writerThread;
    private Thread readerThread;

    String PING_MESSAGE = "d\037g\n";

    public String key;
    public String sessionKey;
    private EventThread dispatcher;
    private ConcurrentLinkedQueue<ProtocolMessage> queue;

    public SocketManager(EventThread dispatcher, String sKek, String sSessionKey)
    {
        this.dispatcher = dispatcher;
        this.queue = dispatcher.getCommunicationsQueue();
        this.proceed.set(true);
        this.connected.set(false);
        this.key = sKek;
        this.sessionKey = sSessionKey;
        this.outputQueue = new ConcurrentLinkedQueue<ProtocolMessage>();
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
            byte[] m_byteStreamIn = null;
            String m_strMsgLength = "";
            int m_iLength = 0;
            int m_iReadLen = 0;
            int m_iReadOffset = 0;

            DataInputStream input = new DataInputStream(this.socket.getInputStream());

            StringBuilder msgBuf;
            while (canProcess()) {
                try
                {
                    if (m_strMsgLength.length() < this.LENGTH_FIELD && input.available() > 0)
                    {
                        int iByte = input.read();
                        m_strMsgLength += (char)iByte;
                    }
                    else if ((m_iLength == 0) && (m_strMsgLength.length() == this.LENGTH_FIELD))
                    {
                        m_iLength = hexStringToInt(m_strMsgLength);
                        m_byteStreamIn = new byte[m_iLength];
                    }
                    else
                    {
                        msgBuf = new StringBuilder();
                        if(input.available() > 0) {
                            int iLen = input.read(m_byteStreamIn, m_iReadOffset, m_iLength - m_iReadOffset);
                            if (iLen > 0) {
                                m_iReadLen += iLen;
                                m_iReadOffset += iLen;
                                if (m_iReadLen == m_iLength) {
                                    m_strMsgLength = "";
                                    m_iLength = 0;
                                    m_iReadLen = 0;
                                    m_iReadOffset = 0;
                                    msgBuf.append(new String(m_byteStreamIn));

                                    ProtocolMessage msg = new ProtocolMessage(m_byteStreamIn, msgBuf, this.key, this.sessionKey);

                                    this.queue.add(msg);
                                }
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
                    for (int count = 0; canProcess() && (msg = outputQueue.poll()) != null && count < 10;)
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
                        Thread.sleep(Constants.SO_SLEEP_DURATION);

                        count++;
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
            this.writerThread.interrupt();
            this.writerThread.join();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            this.readerThread.interrupt();
            this.readerThread.join();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        try {
            this.socket.close();
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

        this.queue.add(protocolMessage);
    }

    public int getOutputQueueSize()
    {
        return this.outputQueue.size();
    }

    public int getInputQueueSize()
    {
        return this.queue.size();
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
