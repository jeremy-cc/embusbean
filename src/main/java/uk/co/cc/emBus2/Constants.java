package uk.co.cc.emBus2;

/**
 * Created by IntelliJ IDEA.
 * User: Jeremy Botha
 * Date: 4/22/14
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static final String VERSION="1.0.4";
    
    public static final long SO_SLEEP_DURATION = 1l;
    public static final long EVENT_SLEEP_DURATION = 20l;

    public static final Integer err_connectiontimeout = new Integer(5001);
    public static final Integer err_invalid_user_error = new Integer(5002);
    public static final Integer err_already_conn = new Integer(5003);
    public static final char tts_fs = '\037';
    public static final char tts_bs = '\036';
    public static final char tts_ms = '\n';
    public static final char tts_lf = '\r';
    public static final char tts_tempsep = '\033';
    public static final char tts_crtempsep = '\034';
    public static final char tts_lftempsep = '\032';
    public static final String tts_signed = "0";
    public static final String tts_publish = "1";
    public static final String tts_subscribe = "2";
    public static final String tts_password = "3";
    public static final String tts_errortext = "4";
    public static final String tts_error = "5";
    public static final String tts_time = "6";
    public static final String tts_date = "7";
    public static final String tts_branch = "8";
    public static final String tts_subscription = "9";
    public static final String tts_leaf = "a";
    public static final String tts_data = "b";
    public static final String tts_msg = "c";
    public static final String tts_msgtype = "d";
    public static final String tts_sendto = "e";
    public static final String tts_unsubscribe = "f";
    public static final String tts_heartbeat = "g";
    public static final String tts_server_sending_time = "h";
    public static final String tts_connect = "i";
    public static final String tts_source = "j";
    public static final String tts_destination = "k";
    public static final String tts_msgid = "l";
    public static final String tts_timetolive = "m";
    public static final String tts_activate_heartbeat = "n";
    public static final String tts_delivered = "o";
    public static final String tts_connectresp = "p";
    public static final String tts_serverkey = "q";
    public static final String tts_macvalue = "r";
    public static final String tts_messageresp = "s";
    public static final String tts_dontsendtoself = "t";
    public static final String tts_error_no = "u";
    public static final String tts_unknownmessage = "v";
    public static final String tts_serverside = "w";
    public static final String tts_is_blocking = "x";
    public static final String tts_internal_disconnect = "y";
    public static final String tts_expirytime = "z";
    public static final String tts_system_status_msg = "A";
    public static final String tts_packet_number = "B";
    public static final String tts_from_packet_number = "C";
    public static final String tts_server_name = "D";
    public static final String tts_kek = "E";
    public static final String tts_no_registry_store = "F";
    public static final String tts_forwarded_packet = "G";
    public static final String tts_binaryflag = "H";
    public static final String tts_remove_all_interests = "I";
    public static final String tts_domainkeya = "J";
    public static final String tts_userkey = "K";
    public static final String tts_domainkeycurrent = "L";
    public static final String tts_newdomainkey = "M";
    public static final String tts_version = "N";
    public static final String tts_ack = "O";
    public static final String tts_nack = "P";
    public static final String tts_options = "Q";
    public static final String tts_initialise_marshaling = "R";
    public static final String tts_permissionserror = "S";
    public static final String tts_loggingmessage = "T";
    public static final String tts_is_compressed = "U";
    public static final String tts_is_image = "V";
    public static final String tts_linkuser = "W";
    public static final String tts_is_tainted = "X";
    public static final String tts_connected_users = "Y";
    public static final String tts_user_maintenance_string = "Z";
    public static final String tts_context = "!";
    public static final String tts_message_source = "#";

}
