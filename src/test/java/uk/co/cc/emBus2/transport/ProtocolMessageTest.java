package uk.co.cc.emBus2.transport;

import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jeremyb on 08/05/2014.
 */
public class ProtocolMessageTest extends junit.framework.TestCase {
    List<String> testMessages;

    @Before
    public void setup() {

    }

    @org.junit.Test
    public void test_toMap() throws Exception {
/*
        0000000: 3030 3030 3031 3334 7064 1f31 1f21 1f1f  00000134pd.1.!..
        0000010: 391f 4445 562e 4649 582e 4f41 4e44 412e  9.DEV.FIX.OANDA.
        0000020: 5072 6963 6573 1f51 1f1f 631f 383d 4649  Prices.Q..c.8=FI
        0000030: 582e 342e 3301 393d 3232 3101 3334 3d38  X.4.3.9=221.34=8
        0000040: 3837 3336 3538 0133 353d 5801 3439 3d6f  873658.35=X.49=o
        0000050: 616e 6461 0135 323d 3230 3134 3035 3037  anda.52=20140507
        0000060: 2d31 343a 3035 3a34 3401 3236 323d 3230  -14:05:44.262=20
        0000070: 3134 3035 3034 2d42 5650 5a58 4801 3236  140504-BVPZXH.26
        0000080: 383d 3201 3237 393d 3101 3236 393d 3001  8=2.279=1.269=0.
        0000090: 3535 3d47 4250 2f5a 4152 0132 3730 3d31  55=GBP/ZAR.270=1
        00000a0: 372e 3739 3231 3101 3237 313d 3130 3030  7.79211.271=1000
        00000b0: 3030 3030 0132 3732 3d32 3031 3430 3530  0000.272=2014050
        00000c0: 3701 3237 333d 3134 3a30 353a 3434 0132  7.273=14:05:44.2
        00000d0: 3739 3d31 0132 3639 3d31 0135 353d 4742  79=1.269=1.55=GB
        00000e0: 502f 5a41 5201 3237 303d 3137 2e38 3036  P/ZAR.270=17.806
        00000f0: 3631 0132 3731 3d31 3030 3030 3030 3001  61.271=10000000.
        0000100: 3237 323d 3230 3134 3035 3037 0132 3733  272=20140507.273
        0000110: 3d31 343a 3035 3a34 3401 3130 3d37 331f  =14:05:44.10=73.
        0000120: 6a1f 6669 786d 7578 5f6f 616e 6461 1f6c  j.fixmux_oanda.l
        0000130: 1f31 3339 3934 3731 3534 340a            .1399471544.
*/

        String strMsg = "00000134pd\0371\037!\037\0379\037DEV.FIX.OANDA.Prices\037Q\037\037c\0378=FIX.4.3^A9=221^A34=8873658^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/ZAR^A270=17.79211^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/ZAR^A270=17.80661^A271=10000000^A272=20140507^A273=14:05:44^A10=73\037j\037fixmux_oanda\037l\0371399471544\n";

        ProtocolMessage msg = new ProtocolMessage(strMsg, "", "");

        Map<String,String> map = msg._toMap();

//        System.out.println(map.toString());

        assertEquals("1", map.get("0000134pd"));
        assertEquals("", map.get("!"));
        assertEquals("DEV.FIX.OANDA.Prices", map.get("9"));
        assertEquals("", map.get("Q"));
        assertEquals("8=FIX.4.3^A9=221^A34=8873658^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/ZAR^A270=17.79211^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/ZAR^A270=17.80661^A271=10000000^A272=20140507^A273=14:05:44^A10=73", map.get("c"));
        assertEquals("fixmux_oanda", map.get("j"));
        assertEquals("1399471544", map.get("l"));

    }


    @org.junit.Test
    public void testToMap() throws Exception {
        String strMsg = "00000134pd\0371\037!\037\0379\037DEV.FIX.OANDA.Prices\037Q\037\037c\0378=FIX.4.3^A9=221^A34=8873658^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/ZAR^A270=17.79211^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/ZAR^A270=17.80661^A271=10000000^A272=20140507^A273=14:05:44^A10=73\037j\037fixmux_oanda\037l\0371399471544\n";

        ProtocolMessage msg = new ProtocolMessage(strMsg, "", "");

        Map<String,String> map = msg.toMap();

        assertEquals("1", map.get("0000134pd"));
        assertEquals("", map.get("!"));
        assertEquals("DEV.FIX.OANDA.Prices", map.get("9"));
        assertEquals("", map.get("Q"));
        assertEquals("8=FIX.4.3^A9=221^A34=8873658^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/ZAR^A270=17.79211^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/ZAR^A270=17.80661^A271=10000000^A272=20140507^A273=14:05:44^A10=73", map.get("c"));
        assertEquals("fixmux_oanda", map.get("j"));
        assertEquals("1399471544", map.get("l"));
    }

    @org.junit.Test
    public void testCorrespondance() {
        testMessages = new ArrayList<String>();

        // add test messages
        // oanda pricing message
        testMessages.add("00000134pd\0371\037!\037\0379\037DEV.FIX.OANDA.Prices\037Q\037\037c\0378=FIX.4.3^A9=221^A34=8873658^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/ZAR^A270=17.79211^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/ZAR^A270=17.80661^A271=10000000^A272=20140507^A273=14:05:44^A10=73\037j\037fixmux_oanda\037l\0371399471544\n");
        testMessages.add("00000139pd\0371\037!\037\0379\037DEV.FIX.SEB.Prices\037Q\037\037c\0378=FIX.4.3^A9=234^A35=S^A49=SEB^A50=MarketSimulation^A52=20140507-14:08:14^A132=0.607533414337789^A133=0.614216281895504^A134=1000000^A135=1000000^A188=0.607533414337789^A190=0.614216281895504^A131=P20140507-YVMTRX^A117=k7yhm1jt1c27vhm4^A1=FXCAPIT^A55=GBP/USD^A15=USD^A10=2c\037j\037fixmux\037l\0371399471694\n");
        testMessages.add("00000139pd\0371\037!\037\0379\037DEV.FIX.SEB.Prices\037Q\037\037c\0378=FIX.4.3^A9=234^A35=S^A49=SEB^A50=MarketSimulation^A52=20140507-14:08:14^A132=0.755287009063444^A133=0.763595166163142^A134=1000000^A135=1000000^A188=0.755287009063444^A190=0.763595166163142^A131=P20140507-WHDLSN^A117=m4eu2m35j9yz6by8^A1=FXCAPIT^A55=EUR/USD^A15=USD^A10=f5\037j\037fixmux\037l\0371399471694\n");
        testMessages.add("00000132pd\0371\037!\037\0379\037DEV.FIX.OANDA.Prices\037Q\037\037c\0378=FIX.4.3^A9=219^A34=8873751^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/JPY^A270=172.875^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/JPY^A270=172.907^A271=10000000^A272=20140507^A273=14:05:44^A10=1f\037j\037fixmux_oanda\037l\0371399471544\n");

        for(String message : testMessages) {
            ProtocolMessage msg = new ProtocolMessage(message, "", "");

            assertTrue(compare(msg.toMap(), msg._toMap()));
        }
    }

    public boolean compare(Map<String,String> s1, Map<String,String> s2) {
        assertEquals(s1.keySet().size(), s2.keySet().size());
        for(String key : s1.keySet()) {
            assertEquals(s1.get(key), s2.get(key));
        }
        return true;
    }

    @org.junit.Test
    public void testSpeed() {
        String strMsg = "00000134pd\0371\037!\037\0379\037DEV.FIX.OANDA.Prices\037Q\037\037c\0378=FIX.4.3^A9=221^A34=8873658^A35=X^A49=oanda^A52=20140507-14:05:44^A262=20140504-BVPZXH^A268=2^A279=1^A269=0^A55=GBP/ZAR^A270=17.79211^A271=10000000^A272=20140507^A273=14:05:44^A279=1^A269=1^A55=GBP/ZAR^A270=17.80661^A271=10000000^A272=20140507^A273=14:05:44^A10=73\037j\037fixmux_oanda\037l\0371399471544\n";

        long oldTime = 0;
        long newTime = 0;

        long oldStart = 0;
        long newStart = 0;

        long oldEnd = 0;
        long newEnd = 0;

        ProtocolMessage msg;
        Map<String,String> toMap;

        for(int i = 0; i < 10000; i++) {
            oldStart = System.nanoTime();
            msg = new ProtocolMessage(strMsg, "", "");
            toMap = msg.toMap();
            oldEnd = System.nanoTime();
            oldTime += (oldEnd -oldStart);

            newStart = System.nanoTime();
            msg = new ProtocolMessage(strMsg, "", "");
            toMap = msg._toMap();
            newEnd = System.nanoTime();
            newTime += (newEnd -newStart);
        }

        System.out.println(String.format("Took %d ms to convert %d messages via the old convertor", oldTime/1000000, 10000));
        System.out.println(String.format("Took %d ms to convert %d messages via the new convertor", newTime/1000000, 10000));
    }
}
