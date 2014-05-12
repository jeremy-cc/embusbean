package uk.co.cc.emBus2.transport;

import org.junit.Test;
import uk.co.cc.emBus2.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jeremyb on 09/05/2014.
 */
public class SocketManagerTest {
    @Test
    public void testRead() throws Exception {
        SocketManagerMock mock = new SocketManagerMock();

        mock.readBuffered();
    }

    public byte[] testData() {
        try {
            return ("0000012dpl\0371399642920\037j\037fixmux_seb\0379\037DEV.FIX.SEB.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=220\00135=S\00149=SEB\00150=SEBprices\00152=20140509-13:42:00\00134=794975\001132=2.0813\001133=2.0820\001647=0" +
                    "\001134=4140000\001648=0\001135=4140000\001188=2.0813\001190=2.0820\00138=1000\00164=20140512\001131=20140508-KWNNXM\001117=1E68DS0023BV\0011=FXCAPIT\00155=USD/TRY\00115=USD\00110=bf\037\n" +
            "0000012dpl\0371399642920\037j\037fixmux_seb\0379\037DEV.FIX.SEB.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=220\00135=S\00149=SEB\00150=SEBprices\00152=20140509-13:42:00\00134=794976\001132=2.8703\001133=2.8716\001647=0" +
                    "\001134=3000000\001648=0\001135=3000000\001188=2.8703\001190=2.8716\00138=1000\00164=20140513\001131=20140508-MBQFLV\001117=1E68H80023BV\0011=FXCAPIT\00155=EUR/TRY\00115=EUR\00110=a7\037\n" +
            "0000012dpl\0371399642920\037j\037fixmux_seb\0379\037DEV.FIX.SEB.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=220\00135=S\00149=SEB\00150=SEBprices\00152=20140509-13:42:00\00134=794977\001132=2.3549\001133=2.3562\001647=0" +
                    "\001134=3660000\001648=0\001135=3660000\001188=2.3549\001190=2.3562\00138=1000\00164=20140513\001131=20140508-JSZBBH\001117=1E68LO0023BV\0011=FXCAPIT\00155=CHF/TRY\00115=CHF\00110=9a\037\n" +
            "0000012dpl\0371399642920\037j\037fixmux_seb\0379\037DEV.FIX.SEB.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=220\00135=S\00149=SEB\00150=SEBprices\00152=20140509-13:42:00\00134=794978\001132=3.5109\001133=3.5125\001647=0" +
                    "\001134=2520000\001648=0\001135=2520000\001188=3.5109\001190=3.5125\00138=1000\00164=20140513\001131=20140508-VMVZQQ\001117=1E68LS0023BV\0011=FXCAPIT\00155=GBP/TRY\00115=GBP\00110=c7\037\n" +
            "0000012bpl\0371399642920\037j\037fixmux_seb\0379\037DEV.FIX.SEB.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=218\00135=S\00149=SEB\00150=SEBprices\00152=20140509-13:42:00\00134=794979\001132=48.85\001133=48.87\001647=0\001134=24720000\001648=0\001135=24720000\001188=48.85\001190=48.87\00138=1000\00164=20140513\001131=20140508-FPJJMW\001117=1E68M80023BV\0011=FXCAPIT\00155=TRY/JPY\00115=TRY\00110=9e\037\n" +
            "00000131pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=220\00134=3008069\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=EUR/ZAR\001270=14.3028\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=EUR/ZAR\001270=14.31353\001271=10000000\001272=20140509\001273=13:42:00\00110=24\037\n" +
            "00000132pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=221\00134=3008070\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=GBP/ZAR\001270=17.49463\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=GBP/ZAR\001270=17.50791\001271=10000000\001272=20140509\001273=13:42:00\00110=41\037\n" +
            "00000132pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=221\00134=3008071\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=CHF/ZAR\001270=11.73446\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=CHF/ZAR\001270=11.74432\001271=10000000\001272=20140509\001273=13:42:00\00110=22\037\n" +
            "00000131pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=220\00134=3008072\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=USD/ZAR\001270=10.3742\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=USD/ZAR\001270=10.38115\001271=10000000\001272=20140509\001273=13:42:00\00110=1c\037\n" +
            "00000130pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=219\00134=3008073\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=EUR/TRY\001270=2.87063\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=EUR/TRY\001270=2.87145\001271=10000000\001272=20140509\001273=13:42:00\00110=2a\037\n" +
            "00000130pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=219\00134=3008074\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=USD/DKK\001270=5.41328\001271=10000000\001272=20140509\001273=13:42:00\0012").getBytes(Constants.CHARSET);
        }catch(Exception exc) {
            throw new RuntimeException(exc);
        }

    }

    public byte [] testData2() {
        try {
            return ("79=1001\001269=1\00155=USD/DKK\001270=5.41369\001271=10000000\001272=20140509\001273=13:42:00\00110=df037\n" +
            "00000130pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=219\00134=3008075\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=USD/TRY\001270=2.08145\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=USD/TRY\001270=2.08212\001271=10000000\001272=20140509\001273=13:42:00\00110=1a\037\n" +
                    "00000130pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=219\00134=3008076\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=NZD/CAD\001270=0.94042\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=NZD/CAD\001270=0.94069\001271=10000000\001272=20140509\001273=13:42:00\00110=b9\037\n" +
                    "0000012fpl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=218\00134=3008077\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=USD/TRY\001270=2.0814\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=USD/TRY\001270=2.08212\001271=10000000\001272=20140509\001273=13:42:00\00110=e6\037\n" +
                    "00000130pl\0371399642920\037j\037fixmux_oanda\0379\037DEV.FIX.OANDA.Prices\037d\0371\037!\037\037c\0378=FIX.4.3\0019=219\00134=3008078\00135=X\00149=oanda\00152=20140509-13:42:00\001262=20140508-MDKZBZ\001268=2\001279=1\001269=0\00155=EUR/TRY\001270=2.87034\001271=10000000\001272=20140509\001273=13:42:00\001279=1\001269=1\00155=EUR/TRY\001270=2.87168\001271=10000000\001272=20140509\001273=13:42:00\00110=32\037\n").getBytes(Constants.CHARSET);
        }catch(Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    public boolean canProcess() {
        return true;
    }

    class SocketManagerMock extends SocketManager {
        private InputStream mockInputStream = new ByteArrayInputStream(SocketManagerTest.this.testData());

        public SocketManagerMock() {
            super(null, "", "");
        }

        InputStream getSocketInputStream() {
            return mockInputStream;
        }

        boolean canProcess() {
            try {
                return mockInputStream.available() > 0;
            }catch(IOException ioe) {
                ioe.printStackTrace(System.err);
                return false;
            }
        }
    }
}
