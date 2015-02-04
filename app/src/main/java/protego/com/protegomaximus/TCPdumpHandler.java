package protego.com.protegomaximus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;


public class TCPdumpHandler {


    private static final int defaultRefreshRate = 100;
    private static final int defaultBufferSize = 80;

    // Your Main activity's ids for the View.

    private static final int outputId = R.id.textView;


    // TextView's refresh rate in ms.
    private int refreshRate = defaultRefreshRate;

    // Byte[] buffer's size.
    private int bufferSize = defaultBufferSize;
    private boolean refreshingActive = false;
    private TCPdump tcpdump = null;
    private Handler isHandler = null;
    private Context mContext = null;
    private TextView outputText = null;
    String checkBufferValue;
    StringBuilder result = new StringBuilder();

    /**
     * This runnable is used for refreshing the TCPdump's process standard
     * output.
     */
    private Runnable updateOutputText = new Runnable() {
        public void run() {
            try {
                if ((tcpdump.getInputStream().available() > 0) == true) {
                    byte[] buffer = new byte[bufferSize];
                    try {
                        tcpdump.getInputStream().read(buffer, 0, bufferSize);
                    } catch (IOException e) {
                        stopRefreshing();
                        return;
                    }

                    // Clears the screen if it's full.
                    if (outputText.length() + buffer.length >= bufferSize)
                        outputText.setText("");
                        outputText.append(new String(buffer));
                        GlobalVariables.numPacketsReceived++;
                        checkBufferValue= new String(buffer);
                        DataParcel data = new DataParcel();
                        data.hashMap = CreateHashObject.createObject(checkBufferValue);
                        Intent intent = new Intent(mContext, ProcessDataService.class);
                        intent.putExtra("dataParcel", data);
                        mContext.startService(intent);
                        Log.d ("LOG",checkBufferValue);
                    result.append(new String(buffer));
                }
            } catch (IOException e) {
                stopRefreshing();
                return;
            }
            isHandler.postDelayed(updateOutputText, refreshRate);
        }
    };

    public TCPdumpHandler(TCPdump tcpdump, Context mContext, Activity activity,
                          StringBuilder result) {
        this.tcpdump = tcpdump;
        isHandler = new Handler();
        this.outputText = (TextView) activity.findViewById(outputId);
        this.mContext = mContext;
        this.result=result;
    }

    /**
     * Starts a TCPdump process, enables refreshing and posts a notification.
     *
     * @param params
     *            The parameters that TCPdump will use. For example: -i
     *            [interface name] -s [snaplen size] -w [filename]
     *
     * @return 0 Everything went OK.<br>
     *         -1 TCPdump is already running.<br>
     *         -2 The device isn't rooted.<br>
     *         -4 Error when running the TCPdump command.<br>
     *         -5 Error when flushing the DataOutputStream.
     */

    public int start(String params) {
        int TCPdumpReturn;
        if ((TCPdumpReturn = tcpdump.start(params)) == 0) {
            outputText.setText("standard output enabled");
            startRefreshing();
            return 0;
        } else
            return TCPdumpReturn;
    }

    /**
     * Stops the TCPdump process, disables refreshing and removes the
     * notification.
     *
     *
     * @return 0: Everything went OK.<br>
     *         -1: TCPdump wasn't running.<br>
     *         -2: The device isn't rooted.<br>
     *         -4: Error when running the killall command.<br>
     *         -5: Error when flushing the output stream.<br>
     *         -6: Error when closing the shell.<br>
     *         -7: Error when waiting for the process to finish.
     */

    public int stop() {
        int TCPdumpReturn;
        if ((TCPdumpReturn = tcpdump.stop()) == 0) {
            stopRefreshing();
            return 0;
        } else
            return TCPdumpReturn;
    }

    /**
     * Starts refreshing the TextView.
     */

    private void startRefreshing() {
        if (!refreshingActive) {
            isHandler.post(updateOutputText);
            refreshingActive = true;
        }
    }

    /**
     * Stops refreshing the TextView.
     */

    private void stopRefreshing() {
        if (refreshingActive) {
            isHandler.removeCallbacks(updateOutputText);
            refreshingActive = false;
        }
    }

    /**
     * Sets the refreshRate value. refreshRate must be > 0.
     *
     * @param refreshRate
     *            The TextView's refresh rate in ms.
     * @return true if the new value has been set.<br>
     *         false if refreshRate hasn't been modified.
     */

    public boolean setRefreshRate(int refreshRate) {
        if ((refreshRate > 0) && (tcpdump.getProcessStatus() == false)) {
            this.refreshRate = refreshRate;
            return true;
        } else
            return false;
    }

    /**
     * Sets the bufferSize value. bufferSize must be > 0.
     *
     * @param bufferSize
     *            The bufferSize must be > 0.
     * @return true if the new value has been set.<br>
     *         false if bufferSize hasn't been modified.
     */

    public boolean setBufferSize(int bufferSize) {
        if ((bufferSize > 0) && (tcpdump.getProcessStatus() == false)) {
            this.bufferSize = bufferSize;
            return true;
        } else
            return false;
    }

    /**
     * Checks if the device's interface that will be used for capturing is up.
     *
     * @return true if the selected interface is up.<br>
     *         false if the selected interface is down.
     */
    public boolean checkNetworkStatus() {

        // Variables used for checking the network state.
        final ConnectivityManager connMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi.isConnected() == true) || (mobile.isConnected() == true)) {
            return true;
        } else
            return false;
    }

    /**
     * Generates the parameters that TCPdump will use by reading the options and
     * copies it in the parameters EditText.
     *
     * @return A string with the parameters.
     */
}