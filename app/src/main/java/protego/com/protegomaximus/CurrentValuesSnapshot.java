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

    public static CurrentValuesSnapshot currentSnapshot() {
        CurrentValuesSnapshot c = new CurrentValuesSnapshot();
        c.startTime = GlobalVariables.startTime;
        c.endTime = GlobalVariables.endTime;
        c.connSourceIP = GlobalVariables.connSourceIP;
        c.connDestIP = GlobalVariables.connDestIP;
        c.connSourcePort = GlobalVariables.connSourcePort;
        c.connDestPort = GlobalVariables.connDestPort;
        c.connProtocol = GlobalVariables.connProtocol;
        c.connService = GlobalVariables.connService;
        c.stateHistory = GlobalVariables.stateHistory;
        return c;
    }
}
