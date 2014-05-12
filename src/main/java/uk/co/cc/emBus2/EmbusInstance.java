package uk.co.cc.emBus2;

import uk.co.cc.emBus2.api.EventHandler;
import uk.co.cc.emBus2.events.ErrorMessage;
import uk.co.cc.emBus2.events.Message;
import uk.co.cc.emBus2.transport.ProtocolMessage;
import uk.co.cc.emBus2.transport.SocketManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jeremyb on 22/04/2014.
 */
public class EmbusInstance {
    private String protocol;
    private String address;
    private int port = 0;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private AtomicBoolean connectionPending = new AtomicBoolean(false);
    private AtomicBoolean isDisconnecting = new AtomicBoolean(false);

    private SocketManager socketManager;

//    private EventThread eventThread;

    private Thread eventThreadRunner;

    private String user = "";
    private String key;
    private String sessionKey = null;

    private Map errorTable = new HashMap();

    private List<EventHandler> listeners = Collections.synchronizedList(new ArrayList<EventHandler>(16));

    public EmbusInstance() {

    }

    public int getOutputQueueSize() {
        return this.socketManager.getOutputQueueSize();
    }

    public int getInputQueueSize() {
        return this.socketManager.getInputQueueSize();
    }

    public boolean isConnectionPending() {
        return connectionPending.get();
    }

    public void setConnectionPending(boolean value) {
        connectionPending.set(value);
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public void setConnected(boolean value) {
        isConnected.set(value);
    }

    public boolean isDisconnecting() {
        return isDisconnecting.get();
    }

    public void setDisconnecting(boolean value) {
        isDisconnecting.set(value);
    }

    public void setKey(String strKey) {
        this.key = strKey;
    }

    public String getKey() {
        return this.key;
    }

    public int connect(String user, String password, String key, String address, String protocol, long options, long timeout) {
        int result = -1;
        try {
            this.protocol = protocol;
            int iColonPos = address.indexOf(':');
            if (address.indexOf(':') != -1) {
                String[] parts = address.split(":", 2);
                this.address = parts[0];
                this.port = Integer.parseInt(parts[1]);
            }
            setKey(key);
            if (!(this.isConnected() || this.isConnectionPending())) {
                this.socketManager = new SocketManager(this, key, sessionKey);
                if (!this.socketManager.connect(this.address, this.port)) {
                    protocolError(Constants.err_connectiontimeout, "");
                } else {
                    Map<String, String> messageMap = new HashMap<String, String>();
                    messageMap.put("d", "i");
                    messageMap.put("j", user);
                    messageMap.put("3", password);

                    this.user = user;

                    ProtocolMessage output = new ProtocolMessage(messageMap, this.key, this.sessionKey);
                    socketManager.enqueue(output);
                    setConnectionPending(false);

                    if (timeout == 0L) {
                        timeout = 20L;
                    }
                    for (int i = 0; i < 100 && !this.isConnectionPending(); i++) {
                        synchronized (EmbusInstance.class) {
                            Thread.sleep(timeout);
                        }
                    }
                    if (this.isConnectionPending()) {
                        protocolError(Constants.err_connectiontimeout, "");
                        setConnectionPending(false);
                    } else if (!this.isConnected()) {
                        protocolError(Constants.err_connectiontimeout, "");
                    } else {
                        result = 0; // success
                    }
                }
            }
            System.out.print("Embus Interconnect version " + Constants.VERSION + " connecting...");
            if ((!this.isConnected()) && (result != 0)) {
                disconnect();
                System.out.print(String.format("failed, result = %d\n", result));
            }
            if(result == 0) {
                System.out.print(String.format("success\n"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        return result;
    }

    public int disconnect() {
        if (this.isConnected()) {
            this.setDisconnecting(true);
            this.setConnectionPending(false);
            this.setConnected(false);
            this.listeners.clear();

            eventThreadRunner.interrupt();
            try {
                eventThreadRunner.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace(System.err);
            }
        }
        if (null != this.socketManager) {
            this.socketManager.shutdown();
        }
        this.setDisconnecting(false);
        return 0;
    }

    public void addListener(EventHandler handler) {
        this.listeners.add(handler);
    }

    public void removeListener(EventHandler handler) {
        this.listeners.remove(handler);
    }

    public boolean MsgHandler(ProtocolMessage msg) {
        return messageHandler(msg);
    }

    public boolean messageHandler(ProtocolMessage msg) {
        msg.setKey(this.key);
        msg.setSessionKey(this.sessionKey);
        try {
            Map<String, String> messageMap = msg.toMap();
            String strMsgType = messageMap.get("d");
            if(null == strMsgType) {
                System.err.println(String.format("Potential parsing issue: %s", msg));
            } else if (strMsgType.equals("p")) {
                if (messageMap.get("5") == null) {
                    this.setConnectionPending(false);
                    this.setConnected(true);
                    this.socketManager.connected();
                } else {
                    String strErrCode = messageMap.get("u");
                    String strErrText = messageMap.get("4");
                    Integer iErrCode = new Integer(strErrCode);
                    protocolError(iErrCode, strErrText);
                }
            } else if ((strMsgType.equals("1")) || (strMsgType.equals("e"))) {
                String l_sMsgID = messageMap.get("l");
                if (null == l_sMsgID || l_sMsgID.equals("")) {
                    l_sMsgID = "0";
                }
                String strMsg = incomingFindAndReplaceMessageSeps(messageMap.get("c"));
                Message event = new Message(this, strMsg, Integer.parseInt(l_sMsgID), messageMap.get("9"),
                        messageMap.get("j"), messageMap.get("!"));
                for (EventHandler handler : this.listeners) {
                    handler.onMessage(event);
                }
            } else if (strMsgType.equals("5")) {
                String strErrCode = messageMap.get("u");
                String strErrText = messageMap.get("4");
                Integer iErrCode = new Integer(strErrCode);
                protocolError(iErrCode, strErrText);
            } else if (strMsgType.equals("g")) {
                this.socketManager.enqueue(msg);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return false;
    }

    protected int protocolError(Integer iErr, String strErrText) {
        if (strErrText.compareTo("") == 0) {
            if (this.errorTable.containsKey(iErr)) {
                strErrText = (String) this.errorTable.get(iErr);
            }
        }
        ErrorMessage errEvent = new ErrorMessage(this, iErr, strErrText);

        System.err.println(String.format("protocol error: %d, message %s", iErr, strErrText));

        for (EventHandler handler : this.listeners) {
            handler.onError(errEvent);
        }
        return iErr.intValue();
    }

    public boolean subscribe(String strSubscription, long lStyle) {
        if (this.isConnected()) {
            Map<String, String> fields = new HashMap<String, String>();
            fields.put("d", "2");
            fields.put("9", strSubscription);
            fields.put("j", this.user);
            fields.put("Q", Integer.toString((int) lStyle));
            ProtocolMessage msg = new ProtocolMessage(fields, this.key, this.sessionKey);
            return this.socketManager.enqueue(msg);
        }
        return false;
    }

    public boolean unsubscribe(String strSubscription) {
        if (this.isConnected()) {
            Map<String, String> fields = new HashMap<String, String>();
            fields.put("d", "f");
            fields.put("9", strSubscription);
            fields.put("j", this.user);
            ProtocolMessage msg = new ProtocolMessage(fields, this.key, this.sessionKey);
            return this.socketManager.enqueue(msg);
        }
        return false;
    }

    public boolean publish(String strMsg, String strSubscription, long lMsgId, long lOptions, String strContext) {
        if (this.isConnected()) {
            strMsg = outboundFindAndReplaceMessageSeps(strMsg);

            Map<String, String> fields = new HashMap<String, String>();
            fields.put("d", "1");
            fields.put("9", strSubscription);
            fields.put("c", strMsg);
            fields.put("j", this.user);

            fields.put("l", Integer.toString((int) lMsgId));
            fields.put("!", strContext);
            ProtocolMessage msg = new ProtocolMessage(fields, this.key, this.sessionKey);
            return this.socketManager.enqueue(msg);
        } else {
            System.err.println("Failed to publish message as not connected to server");
            return false;
        }
    }

    public boolean delivered(String strMsgSource, long lMsgId) {
        if (this.isConnected()) {
            Map<String, String> fields = new HashMap<String, String>();
            fields.put("d", "o");
            fields.put("#", strMsgSource);
            fields.put("l", Integer.toString((int) lMsgId));
            ProtocolMessage msg = new ProtocolMessage(fields, this.key, this.sessionKey);
            return this.socketManager.enqueue(msg);
        }
        return false;
    }

    public synchronized boolean sendTo(String strMsg, String strDestination, long lMsgId, long lOptions, String strContext) {
        if (this.isConnected()) {
            strMsg = outboundFindAndReplaceMessageSeps(strMsg);
            Map<String, String> fields = new HashMap<String, String>();
            fields.put("d", "e");
            fields.put("k", strDestination);
            fields.put("c", strMsg);
            fields.put("j", this.user);
            fields.put("Q", Integer.toString((int) lOptions));
            fields.put("l", Integer.toString((int) lMsgId));
            fields.put("!", strContext);
            ProtocolMessage msg = new ProtocolMessage(fields, this.key, this.sessionKey);
            return this.socketManager.enqueue(msg);
        } else {
            System.err.println("Failed to send message as not connected to server");
            return false;
        }
    }

    protected String outboundFindAndReplaceMessageSeps(String strMessage) {
        String output = strMessage.replace('\037', '\033');
        output = output.replace('\n', '\034');
        output = output.replace('\r', '\032');

        return output;
    }

    protected String incomingFindAndReplaceMessageSeps(String strMessage) {
        String output = strMessage.replace('\033', '\037');
        output = output.replace('\034', '\n');
        output = output.replace('\032', '\r');

        return output;
    }
}
