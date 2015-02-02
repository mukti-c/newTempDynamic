package protego.com.protegomaximus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class ProcessDataService extends Service {

    private static final String TAG = "ProcessDataService";
    public Set<DataFromLog> connSet = null;
    public DataFromLog temp = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("dataParcel")) {
            DataParcel dataParcel = (DataParcel) intent.getParcelableExtra("dataParcel");
            Log.d(TAG, "Time started: " + System.nanoTime());
            temp = getPacketData(dataParcel, new DataFromLog());
            addToConnSet(temp, connSet);
            Log.d(TAG, "Time finished: " + System.nanoTime());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //}

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d ("DE", "destroy");
        CurrentValuesSnapshot sp = CurrentValuesSnapshot.currentSnapshot();
        KDDConnection.createConnectionRecord(connSet, sp);
        connSet.clear();
        GlobalVariables.clearVar();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connSet = new HashSet<DataFromLog>();
    }

    /*@Override
    protected void onHandleIntent(Intent intent) {
        DataParcel dataParcel = (DataParcel) intent.getParcelableExtra("dataParcel");
        Log.d(TAG, "Time started: " + System.nanoTime());
        temp = getPacketData(dataParcel, new DataFromLog());
        addToConnSet(temp, connSet);
        Log.d(TAG, "Time finished: " + System.nanoTime());
    }*/

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
                CreateLogFile.logData.append(GetTime.getCurrentTime()+": New Connection set created.\nOngoing Connection.\n");
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
                    CreateLogFile.logData.append(GetTime.getCurrentTime()+": Previous connection terminated\n");
                    Log.d("EEEStateHistory", GlobalVariables.stateHistory);
                    CurrentValuesSnapshot sp = CurrentValuesSnapshot.currentSnapshot();
                    Log.d("EEEEStateCurr", sp.stateHistory);
                    KDDConnection.createConnectionRecord(connSet, sp);
                    CreateLogFile.logData.append(GetTime.getCurrentTime()+": Record for the terminated connection created\n");
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
                    CreateLogFile.logData.append(GetTime.getCurrentTime()+": New Connection set created\n");
                }
            }
        }
    }

    private DataFromLog getPacketData(DataParcel dataParcel, DataFromLog data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        sdf.setTimeZone(TimeZone.getDefault());
        try{
            Date date = sdf.parse("1970-01-01 " + dataParcel.hashMap.get(1));
            data.TIMESTAMP = date.getTime();
        } catch (ParseException p) {
            data.TIMESTAMP = 0;
        }
        data.PROTOCOL = dataParcel.hashMap.get(3).toLowerCase();
        if (dataParcel.hashMap.get(4) != null && dataParcel.hashMap.get(11) != null) {
            data.LENGTH = Integer.parseInt(dataParcel.hashMap.get(4)) + Integer.parseInt(dataParcel.hashMap.get(11));
        } else {
            if (dataParcel.hashMap.get(4) == null && dataParcel.hashMap.get(11) != null) {
                data.LENGTH = Integer.parseInt(dataParcel.hashMap.get(11));
            } else if (dataParcel.hashMap.get(4) != null && dataParcel.hashMap.get(11) == null) {
                data.LENGTH = Integer.parseInt(dataParcel.hashMap.get(4));
            } else {
                data.LENGTH = 0;
            }
        }
        data.SRC_IP = dataParcel.hashMap.get(5);
        data.DEST_IP = dataParcel.hashMap.get(6);
        data.SRC_PORT = Integer.parseInt(dataParcel.hashMap.get(7));
        data.DEST_PORT = Integer.parseInt(dataParcel.hashMap.get(8));
        data.FLAGS = extractFlags(dataParcel.hashMap.get(9));
        data.CHECKSUM_DESC = dataParcel.hashMap.get(10);
        data.SERVICE = (data.PROTOCOL.equals("icmp"))? DataFromLog.assignIcmpService(data.PROTOCOL, data.SRC_PORT, data.DEST_PORT):DataFromLog.assignService(data);
        return data;
    }

    private Flags extractFlags (String flag) {
        Flags f = new Flags();
        if (flag.equals("")) {
            f.none = true;
            return f;
        } else {
            f.FIN = flag.contains("F");
            f.ACK = flag.contains(".");
            f.PSH = flag.contains("P");
            f.RST = flag.contains("R");
            f.URG = flag.contains("U");
            f.SYN = flag.contains("S");
        }
        return f;
    }
}
