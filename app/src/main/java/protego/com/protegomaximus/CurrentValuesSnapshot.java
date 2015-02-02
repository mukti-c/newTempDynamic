package protego.com.protegomaximus;

public class CurrentValuesSnapshot {
    public static long startTime = 0;
    public static long endTime = 0;
    public static String connSourceIP = null;
    public static String connDestIP = null;
    public static int connSourcePort = 0;
    public static int connDestPort = 0;
    public static String connProtocol = null;
    public static String connService = null;
    public static String stateHistory = "";

    public static void currentSnapshot() {
        startTime = GlobalVariables.startTime;
        endTime = GlobalVariables.endTime;
        connSourceIP = GlobalVariables.connSourceIP;
        connDestIP = GlobalVariables.connDestIP;
        connSourcePort = GlobalVariables.connSourcePort;
        connDestPort = GlobalVariables.connDestPort;
        connProtocol = GlobalVariables.connProtocol;
        connService = GlobalVariables.connService;
        stateHistory = GlobalVariables.stateHistory;
    }
}
