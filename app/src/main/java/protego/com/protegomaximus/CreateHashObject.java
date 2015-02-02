package protego.com.protegomaximus;

import android.util.Log;

import java.util.HashMap;
import java.util.regex.Pattern;

public class CreateHashObject {
    public static HashMap createObject(String checkBufferValue ) {

        String[] splited = checkBufferValue.split("\\s+");
        Log.e("Log", splited[0] + " " + splited[1] + " " + splited[2] + " " + splited[3] + " " + splited[4] + splited[5] + " " + splited[6] + " " + splited[7] + " " + splited[8]);
        //String a= splited[4];

        String[] src_ip= splited[4].split(Pattern.quote("."));
        String[] dst_ip= splited[5].split(Pattern.quote("."));

        Packet.TIMESTAMP=splited[0];
        Packet.FLAG_IP= splited[1];
        Packet.PROTOCOL=splited[2];
        Packet.LENGTH=splited[3];
        Packet.SRC_IP=src_ip[0]+"."+src_ip[1]+"."+src_ip[2]+"."+src_ip[3];
        Packet.DEST_IP=dst_ip[0]+"."+dst_ip[1]+"."+dst_ip[2]+"."+dst_ip[3];
        Packet.SRC_PORT=src_ip[4];
        Packet.DEST_PORT=dst_ip[4];
        Packet.FLAG_TCP=splited[6];
        Packet.CHECKSUM=splited[7];
        Packet.DATA_LENGTH=splited[8];

        HashMap<Integer,String> hash = new HashMap<>();
        hash.put(1,Packet.TIMESTAMP);
        hash.put(2,Packet.FLAG_IP);
        hash.put(3,Packet.PROTOCOL);
        hash.put(4,Packet.LENGTH);
        hash.put(5,Packet.SRC_IP);
        hash.put(6,Packet.DEST_IP);
        hash.put(7,Packet.SRC_PORT);
        hash.put(8,Packet.DEST_PORT);
        hash.put(9,Packet.FLAG_TCP);
        hash.put(10,Packet.CHECKSUM);
        hash.put(11,Packet.DATA_LENGTH);
        return hash;
    }
}
