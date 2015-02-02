package protego.com.protegomaximus;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener{

    public static StringBuilder result = new StringBuilder();
    private StringBuilder parameters= new StringBuilder();
    private Button startButton,stopButton, eval;
    private String m_chosenDir = "", out1;
    private boolean m_newFolderEnabled = true;
    int chosen_dir_changed =0;
    TCPdump tcpdump;
    TCPdumpHandler tcpDumpHandler;

    DrawerLayout drawer;
    ListView drawerList;
    String[] choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        parameters.append(" -nvv|tr -d ')[],'|awk '{if($14==\"TCP\" && $22==\"cksum\" && $24==\"(correct\"){printf \"%s %s %s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),$21,substr($24,index($24,\"(\")+1,2),substr($25,index($25,\"(\")+1,2)}else if($14==\"TCP\" && $22==\"cksum\" && $24==\"(incorrect\"){printf \"%s %s %s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),$21,substr($24,index($24,\"(\")+1,2),substr($27,index($27,\"(\")+1,2)}else if($14==\"TCP\" && $22!=\"cksum\"){next}else if($14==\"UDP\" && $23 ~ /[0-9]+/){printf \"%s %s %s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),\".\",\"co\",$23}else if($14==\"UDP\" && $23!~ /[0-9]+/ && $21==\"bad\"){printf \"%s %s %s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),\".\",\"in\",\"0\"}else if($14==\"UDP\" && $23!~ /[0-9]+/ && $23==\"ok\"){printf \"%s %s %s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18,substr($20,0,index($20,\":\")-1),\".\",\"co\",\"0\"}}'");
        tcpdump = new TCPdump();
        // Creating a TCPdump handler for the TCPdump object created after.
        tcpDumpHandler = new TCPdumpHandler(tcpdump, this, this,result);

        // For the drawer in the UI
        choice = getResources().getStringArray(R.array.drawer_items);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.list_view);
        drawerList.setSelector(android.R.color.white);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.drawer_item_list, choice);
        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawer.closeDrawers();
                Bundle args = new Bundle();
                args.putString("Choice", choice[position]);
                Fragment detail = new DetailFragment();
                detail.setArguments(args);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_frame, detail).commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.e("directory", m_chosenDir );
                break;

            case R.id.select_dir:
                showDirectoryDialog();
                chosen_dir_changed=1;
                Log.e("directory", m_chosenDir);
                GlobalVariables.chosen_Dir=m_chosenDir;
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void initialize() {
        setContentView(R.layout.activity_main);
        startButton= (Button) findViewById(R.id.startButton);
        stopButton= (Button) findViewById(R.id.stopButton);
        eval = (Button) findViewById(R.id.eval);
        stopButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        eval.setOnClickListener(this);
    }

    public  void showDirectoryDialog() {
        DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(MainActivity.this,
                new DirectoryChooserDialog.ChosenDirectoryListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                        m_chosenDir = chosenDir;
                        Toast.makeText(MainActivity.this, "Chosen directory: " + chosenDir, Toast.LENGTH_LONG).show();
                    }
                });
        // Toggle new folder button enabling
        directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        directoryChooserDialog.chooseDirectory(m_chosenDir);
        m_newFolderEnabled = ! m_newFolderEnabled;
    }

    private void startTCPdump() {
        if (tcpDumpHandler.checkNetworkStatus()) {

            switch (tcpDumpHandler.start(parameters.toString())) {
                case 0:
                    Toast.makeText(MainActivity.this, "tcpdump started",
                            Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alert= new AlertDialog.Builder(this);
                    alert.setMessage(result.toString());
                    //alert.show();
                    break;
                case -1:
                    Toast.makeText(MainActivity.this,
                            "tcpdump already started",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Device not Rooted")
                            .setMessage(
                                    "Device not rooted")
                            .setNeutralButton("OK", null).show();
                    break;
                case -4:
                    new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                            .setMessage("Command error")
                            .setNeutralButton("OK", null).show();
                    break;
                case -5:
                    new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                            .setMessage("outputstream error")
                            .setNeutralButton("OK", null).show();
                    break;
                default:
                    new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                            .setMessage("Unknown error")
                            .setNeutralButton("OK", null).show();
            }
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Network connection error")
                    .setMessage(
                            "Network connection error message")
                    .setPositiveButton("Open Settings",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    startActivity(new Intent(
                                            Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).setNegativeButton("Cancel", null)
                    .show();
        }
    }

    /**
     * Calls TCPdumpHandler to try to stop the packet capture.
     */
    private void stopTCPdump() {
        switch (tcpDumpHandler.stop()) {
            case 0:
                Toast.makeText(MainActivity.this,"tcpdump stopped",
                        Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alert= new AlertDialog.Builder(this);
                alert.setMessage(result.toString());
                alert.show();

                break;
            case -1:
                Toast.makeText(MainActivity.this,"tcpdump already stopped",
                        Toast.LENGTH_SHORT).show();
                break;
            case -2:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Device not rooted")
                        .setMessage("Device not rooted")
                        .setNeutralButton("OK", null).show();
                break;
            case -4:
                new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                        .setMessage("Command error")
                        .setNeutralButton("OK", null).show();
            case -5:
                new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                        .setMessage("output stream error")
                        .setNeutralButton("OK", null).show();
                break;
            case -6:
                new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                        .setMessage("close shell error")
                        .setNeutralButton("OK", null).show();
                break;
            case -7:
                new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                        .setMessage("process finish error")
                        .setNeutralButton("OK", null).show();
            default:
                new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                        .setMessage("unknown error")
                        .setNeutralButton("OK", null).show();
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.startButton :
                startTCPdump();
                break;

            case R.id.stopButton:
                stopTCPdump();
                Intent i = new Intent(this, ProcessDataService.class);
                stopService(i);
                break;

            case R.id.eval:
                Intent in = new Intent("protego.com.protego.EVALUATE");
                startActivity(in);
                break;
        }
    }
}
