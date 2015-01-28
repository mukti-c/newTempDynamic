package protego.com.protegomaximus;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by muktichowkwale on 28/01/15.
 */

public class ProcessDataService extends IntentService {

    private static final String TAG = "ProcessDataService";
    //private final static String filename = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "tcpdump.pcap";
    //public final static String csvFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "connection.csv";
    public Set<DataFromLog> connSet = null;
    public DataFromLog temp = null;

    public ProcessDataService() {
        super("ProcessDataService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connSet = new HashSet<DataFromLog>();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DataParcel dataParcel = (DataParcel) intent.getParcelableExtra("dataParcel");
        Log.d(TAG, "Time started: " + System.nanoTime());
        temp = getPacketData(dataParcel, new DataFromLog());
        addToConnSet(temp, connSet);
        Log.d(TAG, "Time finished: " + System.nanoTime());
    }

    private void addToConnSet(DataFromLog data, Set<DataFromLog> connSet) {
        if (data.PROTOCOL != null && data.SERVICE != null && data.DEST_IP != null && data.SRC_IP != null) {
            if (connSet.size() == 0) {
                GlobalVariables.connProtocol = data.PROTOCOL;
                GlobalVariables.connService = data.SERVICE;
                GlobalVariables.connSourceIP = data.SRC_IP;
                GlobalVariables.connDestIP = data.DEST_IP;
                GlobalVariables.connSourcePort = data.SRC_PORT;
                GlobalVariables.connDestPort = data.DEST_PORT;
                GlobalVariables.startTime = data.TIMESTAMP;
                GlobalVariables.endTime = data.TIMESTAMP;
                GlobalVariables.findStateHistory(data.FLAGS, data.SRC_IP);
                connSet.add(data);
                KDDConnection.createConnectionRecord(connSet);
                CreateLogFile.logData.append(GetTime.getCurrentTime()+"New Connection set created.\nOngoing Connection.\n");
            } else {
                if (GlobalVariables.connProtocol.equals(data.PROTOCOL)
                        && GlobalVariables.connService.equals(data.SERVICE)
                        && ((GlobalVariables.connDestIP.equals(data.DEST_IP) && GlobalVariables.connSourceIP.equals(data.SRC_IP)) || (GlobalVariables.connDestIP.equals(data.SRC_IP) && GlobalVariables.connSourceIP.equals(data.DEST_IP)))) {
                    // The packet is from the same connection
                    GlobalVariables.endTime = data.TIMESTAMP;
                    GlobalVariables.findStateHistory(data.FLAGS, data.SRC_IP);
                    connSet.add(data);
                } else {
                    GlobalVariables.endTime = data.TIMESTAMP;
                    CreateLogFile.logData.append(GetTime.getCurrentTime()+"Previous connection terminated\n");
                    KDDConnection.createConnectionRecord(connSet);
                    CreateLogFile.logData.append(GetTime.getCurrentTime()+"Record for the terminated connection created\n");
                    connSet.clear();
                    GlobalVariables.clearVar();
                    GlobalVariables.startTime = data.TIMESTAMP;
                    GlobalVariables.connProtocol = data.PROTOCOL;
                    GlobalVariables.connService = data.SERVICE;
                    GlobalVariables.connSourceIP = data.SRC_IP;
                    GlobalVariables.connDestIP = data.DEST_IP;
                    GlobalVariables.connSourcePort = data.SRC_PORT;
                    GlobalVariables.connDestPort = data.DEST_PORT;
                    GlobalVariables.findStateHistory(data.FLAGS, data.SRC_IP);
                    connSet.add(data);
                    CreateLogFile.logData.append(GetTime.getCurrentTime()+"New Connection set created\n");
                }
            }
        }
    }

    private DataFromLog getPacketData(DataParcel dataParcel, DataFromLog data) {
        data.TIMESTAMP = Long.parseLong(dataParcel.hashMap.get("TIMESTAMP"));
        data.SRC_IP = dataParcel.hashMap.get("SRC_IP");
        data.DEST_IP = dataParcel.hashMap.get("DEST_IP");
        data.SRC_PORT = Integer.parseInt(dataParcel.hashMap.get("SRC_PORT"));
        data.DEST_PORT = Integer.parseInt(dataParcel.hashMap.get("DEST_PORT"));
        data.PROTOCOL = dataParcel.hashMap.get("PROTOCOL");
        data.LENGTH = Integer.parseInt(dataParcel.hashMap.get("LENGTH"));
        data.CHECKSUM_DESC = dataParcel.hashMap.get("CHECKSUM");
        data.SERVICE = (data.PROTOCOL.equals("icmp"))? DataFromLog.assignIcmpService(data.PROTOCOL, data.SRC_PORT, data.DEST_PORT):DataFromLog.assignService(data);
        data.FLAGS = null;
        return data;
    }
}
