package uk.co.cc.emBus2.transport;

import java.util.*;

public class ProtocolMessage {
    public static final char CH36 = '\036'; // separator for 'b' messages
    public static final char CH37 = '\037'; // separator for 'a' messages

    public boolean m_bAck = false;
    public boolean m_bNak = false;
    private byte[] binaryMsgSize = new byte[4];
    public String strKek;
    public String sessionKey;
    private int binaryMessagePos;
    private StringBuilder messageBuffer;
    private byte[] array;

    public ProtocolMessage(String sKek, String sSessionKey) {
        this.strKek = sKek;
        this.sessionKey = sSessionKey;
    }

    public void setKey(String key) {
        this.strKek = key;
    }

    public void setSessionKey(String key) {
        this.sessionKey = key;
    }

    public ProtocolMessage(String strMsg, String sKek, String sSessionKey) {
        this.strKek = sKek;
        this.sessionKey = sSessionKey;
        if ((strMsg.charAt(0) == 'A') || (strMsg.charAt(0) == 'E')) {
            this.m_bAck = (strMsg.charAt(0) == 'A');
            this.m_bNak = (strMsg.charAt(0) == 'E');
            this.messageBuffer = new StringBuilder(strMsg.substring(1));
            this.array = strMsg.getBytes();
        } else {
            this.m_bAck = false;
            this.m_bNak = false;
            this.messageBuffer = new StringBuilder(strMsg);
            this.array = strMsg.getBytes();
        }
        this.binaryMessagePos = 0;
    }

    public ProtocolMessage(byte[] arrayIn, StringBuilder decryptChar, String sKek, String sSessionKey) {
        this.strKek = sKek;
        this.sessionKey = sSessionKey;
        this.array = arrayIn;
        this.messageBuffer = decryptChar;
        this.binaryMessagePos = 0;
    }

    public ProtocolMessage(Hashtable msgfields, byte[] msgSize, String sKek, String sSessionKey) {
        this.strKek = sKek;
        this.sessionKey = sSessionKey;


        this.messageBuffer = new StringBuilder("p");
        for (Enumeration x = msgfields.keys(); x.hasMoreElements(); ) {
            String strKey = (String) x.nextElement();
            this.messageBuffer.append(strKey);
            if (strKey.compareTo("b") == 0) {
                this.messageBuffer.append(CH36);
                this.messageBuffer.append((String) msgfields.get(strKey));
            } else {
                this.messageBuffer.append(CH37);
                this.messageBuffer.append((String) msgfields.get(strKey));
            }
            this.messageBuffer.append(CH37);
        }
        this.messageBuffer.append('\n');


        String strMsg = this.messageBuffer.toString().substring(1);
        this.binaryMessagePos = strMsg.indexOf(CH36);
        this.binaryMessagePos += 1;

        setBinaryMsgSize(msgSize);

        this.array = generateBytesFromMsg(new String(this.messageBuffer).substring(1), true);
    }

    public ProtocolMessage(Map<String, String> messageFields, String sKek, String sSessionKey) {
        this.strKek = sKek;
        this.sessionKey = sSessionKey;
        this.messageBuffer = new StringBuilder("p");

        for (String strKey : messageFields.keySet()) {
            this.messageBuffer.append(strKey);
            if (strKey.compareTo("b") == 0) {
                this.messageBuffer.append(CH36);
                this.messageBuffer.append(messageFields.get(strKey));
            } else {
                this.messageBuffer.append(CH37);
                this.messageBuffer.append(messageFields.get(strKey));
            }
            this.messageBuffer.append(CH37);
        }
        this.messageBuffer.append('\n');
    }

    public ProtocolMessage(Map<String, String> messageFields, boolean bSort, String sKek, String sSessionKey) {
        this.strKek = sKek;
        this.sessionKey = sSessionKey;

        this.messageBuffer = new StringBuilder("p");

        this.messageBuffer.append("b");
        this.messageBuffer.append(CH37);
        this.messageBuffer.append(messageFields.get("d"));

        List<String> sortedKeys = new ArrayList<String>();
        sortedKeys.addAll(messageFields.keySet());
        Collections.sort(sortedKeys);

        for (String strKey : sortedKeys) {
            if (strKey.compareTo("d") != 0) {
                this.messageBuffer.append(CH37);
                this.messageBuffer.append(strKey);
                if (strKey.compareTo("b") == 0) {
                    this.messageBuffer.append(CH36);
                    this.messageBuffer.append(messageFields.get(strKey));
                } else {
                    this.messageBuffer.append(CH37);
                    this.messageBuffer.append(messageFields.get(strKey));
                }
            }
        }
        this.messageBuffer.append('\n');

        String strMsg = this.messageBuffer.toString().substring(1);
        this.binaryMessagePos = strMsg.indexOf(CH36);
        this.binaryMessagePos += 1;

        this.array = generateBytesFromMsg(new String(this.messageBuffer).substring(1), true);
    }

    public String toString() {
        return this.messageBuffer.toString();
    }

    public Map<String, String> _toMap() {
        Map<String, String> map = new HashMap<String, String>();
        String strKey = null;
        String strField = null;
        boolean bFid = true;

        if (this.messageBuffer.length() > 1) {
            String strMsg = this.messageBuffer.toString().substring(1);
            this.binaryMessagePos = strMsg.indexOf(CH36) + 1;
            int arraypos = this.binaryMessagePos;
            try {
                int idx = 0;
                boolean lookForBinary = false;
                boolean nextFieldIsBinary = false;

                for (; idx < strMsg.length(); ) {
                    int separatorPosition = strMsg.indexOf(CH37, idx);
                    int binarySeparatorPosition = strMsg.indexOf(CH36, idx);

                    // determine whether the next field is binary
                    if (((binarySeparatorPosition < separatorPosition) || separatorPosition < 0) && binarySeparatorPosition > -1) {
                        separatorPosition = binarySeparatorPosition;
                        nextFieldIsBinary = true;
                    } else {
                        nextFieldIsBinary = false;
                    }

                    if (separatorPosition < 0) {
                        separatorPosition = strMsg.indexOf('\n', idx);
                    }

                    if (separatorPosition > 0 || idx < strMsg.length()) {
                        if (lookForBinary) {
                            int messageLength = 0;
                            int binaryBlockLength = 0;
                            int multiplicand = 0;

                            this.binaryMsgSize = new byte[4];
                            while (binaryBlockLength < 4) {
                                if (binaryBlockLength == 0) {
                                    multiplicand = 1;
                                } else {
                                    multiplicand *= 256;
                                }
                                // read the message length data out of this block - appears to be little-endian?? Wow.
                                // this looks like a two's complement operation - are we explicitly preserving the sign here? why else bitwise & with 11111111 ?
                                messageLength += multiplicand * (this.array[arraypos] & 0xFF);

                                this.binaryMsgSize[binaryBlockLength] = this.array[arraypos];
                                binaryBlockLength++;
                                arraypos++;
                            }
                            messageLength += 4;

                            strField = strMsg.substring(0, messageLength);
                            if (idx < (strMsg.length() - (separatorPosition + 2))) {
                                idx += (messageLength + 1); // strMsg = strMsg.substring(messageLength + 1);
                            } else {
                                idx = strMsg.length();
                            }
                            nextFieldIsBinary = false;
                        } else {
//                            System.out.println(String.format("idx=%d, sep=%d, len=%d", idx, separatorPosition, strMsg.length()));
                            if (separatorPosition < strMsg.length()) {
                                strField = strMsg.substring(idx, separatorPosition);
//                                System.out.println(strField);
                                if (separatorPosition < strMsg.length()) {
                                    idx = separatorPosition + 1;
                                } else {
                                    idx = strMsg.length();
                                }
                            } else {
                                idx = strMsg.length();
                            }
                        }

                        if (bFid) {
                            strKey = strField;
                            bFid = false;
                        } else {
                            if (null != strKey && strKey.length() > 0) {
                                map.put(strKey, strField);
                                bFid = true;
                            }
                        }
                    }
                    lookForBinary = nextFieldIsBinary;
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        return map;
    }

    public Map<String, String> toMap() {
        return _toMap();
    }
//    public Map<String,String> __toMap()
//    {
//        Map<String,String> map = new HashMap<String, String>();
//        String strKey = null;
//        String strField = null;
//        boolean bFid = true;
//
//        boolean bNextFieldIsBinary = false;
//        boolean bSetNextFieldIsBinary = false;
//
//        if (this.messageBuffer.length() > 1)
//        {
//            String strMsg = this.messageBuffer.toString().substring(1);
//            this.binaryMessagePos = strMsg.indexOf(CH36);
//            this.binaryMessagePos += 1;
//            int arraypos = this.binaryMessagePos;
//            try
//            {
//                while (strMsg.length() > 0)
//                {
//                    int seppos = strMsg.indexOf(CH37);
//                    int bseppos = strMsg.indexOf(CH36);
//                    if (((bseppos < seppos) || (seppos == -1)) && (bseppos != -1))
//                    {
//                        seppos = bseppos;
//                        bSetNextFieldIsBinary = true;
//                    }
//                    else
//                    {
//                        bSetNextFieldIsBinary = false;
//                    }
//                    if (seppos == -1) {
//                        seppos = strMsg.indexOf('\n');
//                    }
//                    if ((seppos > 0) || (strMsg != ""))
//                    {
//                        if (!bNextFieldIsBinary)
//                        {
//                            strField = strMsg.substring(0, seppos);
//                            if (strMsg.length() > seppos + 2) {
//                                strMsg = strMsg.substring(seppos + 1);
//                            } else {
//                                strMsg = "";
//                            }
//                        }
//                        else
//                        {
//                            int iLength = 0;int iLengthChar = 0;int iPower = 0;
//
//
//                            this.binaryMsgSize = new byte[4];
//                            while (iLengthChar < 4)
//                            {
//                                if (iLengthChar == 0) {
//                                    iPower = 1;
//                                } else {
//                                    iPower *= 256;
//                                }
//                                iLength += iPower * (this.array[arraypos] & 0xFF);
//
//                                this.binaryMsgSize[iLengthChar] = this.array[arraypos];
//                                iLengthChar++;
//                                arraypos++;
//                            }
//                            iLength += 4;
//                            strField = new String(strMsg.substring(0, iLength));
//                            if (strMsg.length() > seppos + 2) {
//                                strMsg = strMsg.substring(iLength + 1);
//                            } else {
//                                strMsg = "";
//                            }
//                            bSetNextFieldIsBinary = false;
//                        }
//                        if (bFid)
//                        {
//                            strKey = strField;
//                            bFid = false;
//                        }
//                        else
//                        {
//                            if(null != strKey && strKey.length() > 0) {
//                                map.put(strKey, strField);
//                                bFid = true;
//                            }
//                        }
//                    }
//                    bNextFieldIsBinary = bSetNextFieldIsBinary;
//                }
//            }
//            catch (Exception e) {
//                e.printStackTrace(System.err);
//            }
//        }
//        return map;
//    }

    public byte[] generateBytesFromMsg(String strMsg, boolean bFirstCharRemoved)
    {
        byte[] bytes = strMsg.getBytes();
        int iPos = 0;
        if (this.binaryMessagePos > 0)
        {
            iPos = this.binaryMessagePos;
            if (!bFirstCharRemoved) {
                iPos++;
            }
            for (int i = 0; i < 4; i++)
            {
                bytes[iPos] = this.binaryMsgSize[i];

                iPos++;
            }
        }
        return bytes;
    }

    public void setBinaryMsgSize(byte[] bytes)
    {
        for (int i = 0; i < 4; i++) {
            this.binaryMsgSize[i] = bytes[i];
        }
    }

//    public byte[] getBinaryMsgSize()
//    {
//        return this.binaryMsgSize;
//    }


}
