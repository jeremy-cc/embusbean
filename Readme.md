# Embus Java interconnect client aka Embus2
### version 1.0.7

This java library is a rewrite of the embusbean interconnect used by our legacy applications.  It makes use of several IO-related enhancements in an attempt to generate better throughput, while being more verbose wrt its handling of connection negotiation and errors detected.

This library will dump errors to System.err instead of just delivering Embus protocol error messages to the remote system in the vain hope that they will be logged there.

## Embus protocol messages

* Embus makes use of messages that specify the payload length in an 8-byte length header
* There is no delimiter between messages
* Individual fields within the messages are delimited with character 0x01
* Messages take the format *c* = *v*  (where *c* is a single character key encoded in Constants.java and *v* is a string value that may be null)

## Read strategy

Embus2 attempts to be intelligent about socket communications.  Given a socket *S*, with an inputstream *I*, Embus2 will attempt to read all available data from *I* before splitting this data into actual messages.

For example, were Embus2 to enter a read cycle and receive the following data from *I*

    I: 00000111pd1!9DEV.FIX.SEB.PricesQc8=FIX.4.39=19035=S49=SEB50=MarketSimulation52=20140512-08:24:07132=1.646133=1.62954134=1000000135=1000000188=1.646190=1.62954131=P20140512-TSYZXL117=2zcdm411k05xp70l1=FXCAPIT55=GBP/USD15=GBP10=afjfixmux_sebl1399883047
       000

It would read the entire byte array into memory, then process the first 8 bytes, determine the length of the message ( 273 bytes ) and then process 273 bytes more of input, parsing this into a message which would look as follows:

    p = d (tts_connectresp )
    ! = null (tts_context)
    9 = DEV.FIX.SEB.Prices (tts_subscription)
    Q = null (tts_options)
    c = 8=FIX.4.39=19035=S49=SEB50=MarketSimulation52=20140512-08:24:07132=1.646133=1.62954134=1000000135=1000000188=1.646190=1.62954131=P20140512-TSYZXL117=2zcdm411k05xp70l1=FXCAPIT55=GBP/USD15=GBP10=af (the fix message payload)
    j = fixmux_seb  (tts_source)
    l = 1399883047  (tts_msgid)

having processed the first block, it would then attempt to process the next header block of 8 bytes, at which point it would determine that there were insufficient bytes buffered and it would need to read more bytes from the inputstream *I*

The intent behind this behaviour is to make Embus2 faster at processing input, both by using Java 6 NIO and by buffering.  Inter-thread communication is kept to a minimum, and Java 6 Concurrent Linked Queues are used wherever two threads need to communicate.

