package protego.com.protegomaximus;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class DataParcel implements Parcelable {
    // All the variables included in the Parcel.
    public HashMap<Integer, String> hashMap = new HashMap<Integer, String>();

    // No arguments constructor
    public DataParcel() {}

    // Additional information on how to process the parcel

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeMap(hashMap);
    }

    @Override
    public int describeContents() {
        // Ignore for now
        return 0;
    }

    // Static object used to regenerate objects
    public static final Creator<DataParcel> CREATOR = new Creator<DataParcel>() {
        @Override
        public DataParcel createFromParcel(Parcel source) {
            return new DataParcel(source);
        }

        @Override
        public DataParcel[] newArray(int size) {
            return new DataParcel[size];
        }
    };

    // Constructor from Parcel, reads back fields IN THE ORDER they were written
    public DataParcel(Parcel parcel) {
        hashMap = parcel.readHashMap(HashMap.class.getClassLoader());
    }
}
