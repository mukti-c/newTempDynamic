package protego.com.protegomaximus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class TCPdumpHandler extends Service {


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
                    if (MainActivity.outputText.length() + buffer.length >= bufferSize)
                        MainActivity.outputText.setText("");
                    MainActivity.outputText.append(new String(buffer));
                    GlobalVariables.numPacketsReceived++;
                    checkBufferValue= new String(buffer);
                    DataParcel data = new DataParcel();
                    data.hashMap = CreateHashObject.createObject(checkBufferValue);
                    Intent intent = new Intent(TCPdumpHandler.this, ProcessDataService.class);
                    intent.putExtra("dataParcel", data);
                    startService(intent);
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
            MainActivity.outputText.setText("standard output enabled");
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
        final ConnectivityManager connMgr = (ConnectivityManager) this
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        tcpdump = new TCPdump();
        isHandler = new Handler();
        //this.outputText = (TextView) activity.findViewById(outputId);

    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {

        if (checkNetworkStatus()) {

            switch (start(MainActivity.parameters.toString())) {
                case 0:
                    Toast.makeText(this, "tcpdump started",
                            Toast.LENGTH_SHORT).show();
                    //AlertDialog.Builder alert= new AlertDialog.Builder(this);
                    //alert.setMessage(result.toString());
                    //alert.show();
                    break;
                case -1:
                    Toast.makeText(this,
                            "tcpdump already started",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -2:


                    buildNotification("Error!!","Device not rooted");
                    break;
                case -4:


                    buildNotification("Error!!","Command error");
                    break;
                case -5:


                    buildNotification("Error!!","OutputStream error");
                    break;
                default:
                    buildNotification("Error!!","Unknown error");
            }
        } else {
            buildNotification("Error!!","NetworkCommection error");

        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        switch (stop()) {
            case 0:
                Toast.makeText(this,"tcpdump stopped",
                        Toast.LENGTH_SHORT).show();
                buildNotification("Result","tcpdump successfully executed");
                break;
            case -1:
                Toast.makeText(this,"tcpdump already stopped",
                        Toast.LENGTH_SHORT).show();
                break;
            case -2:
                buildNotification("Error!!","Device not rooted");
                break;
            case -4:
                buildNotification("Error!!","Command error");
            case -5:
                buildNotification("Error!!","OutputStream error");
                break;
            case -6:
                buildNotification("Error!!","Close shell error");
                break;
            case -7:
                buildNotification("Error!!","Process finish error");
            default:
                buildNotification("Error!!","Unknown error");
        }

    }




    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void buildNotification(String message,String title)
    {

        NotificationCompat.Builder  mBuilder =
                new NotificationCompat.Builder(this);
        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        // mBuilder.setNumber(notification_number);
        Intent resultIntent = new Intent(this,MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(0 ,mBuilder.build());
    }




    /**
     * Generates the parameters that TCPdump will use by reading the options and
     * copies it in the parameters EditText.
     *
     * @return A string with the parameters.
     */
}