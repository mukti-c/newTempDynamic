package protego.com.protegomaximus;

public class GlobalVariables {
    public static PastConnQueue last100Conn;
    public static LastTwoSecQueue lastTwoSec;
    public static long startTime = 0;
    public static long endTime = 0;
    public static String connSourceIP = null;
    public static String connDestIP = null;
    public static int connSourcePort = 0;
    public static int connDestPort = 0;
    public static String connProtocol = null;
    public static String connService = null;
    public static String stateHistory = "";
    public static String chosen_Dir="";
    public static int numPacketsReceived = 0;
    public static int numPacketsProcessed = 0;
    public static Boolean anomalyDetected =false;

    public static void clearVar() {
        startTime = 0;
        endTime = 0;
        connSourceIP = null;
        connSourcePort = 0;
        connDestIP = null;
        connDestPort = 0;
        connProtocol = null;
        connService = null;
        stateHistory = "";          // may be null.
    }

    public static void findStateHistory  (Flags flags, String srcIP) {
        // Referred to: https://www.bro.org/sphinx/_downloads/main20.bro
        // If the packet is from the originator, then the letter is in upper-case. Else, lower-case.
        if (flags.FIN) {
            // with FIN bit
            stateHistory += (srcIP.equals(connSourceIP))?"F":"f";
        } else if (flags.SYN) {
            if (flags.ACK) {
                // Handshake
                stateHistory += (srcIP.equals(connSourceIP)) ? "H" : "h";
            } else if (!flags.ACK) {
                // SYN without the ACK bit set
                stateHistory += (srcIP.equals(connSourceIP)) ? "S" : "s";
            } else if (flags.SYN && flags.RST) {
                // inconsistent packets
                stateHistory += (srcIP.equals(connSourceIP)) ? "I" : "i";
            }
        } else if (flags.ACK) {
            // pure ACK
            stateHistory += (srcIP.equals(connSourceIP))?"A":"a";
        } else if (flags.RST) {
            // with RST bit
            stateHistory +=  (srcIP.equals(connSourceIP))?"R":"r";
        }
    }
}
