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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener{

    public static StringBuilder result = new StringBuilder();
    public static StringBuilder parameters= new StringBuilder();
    private Button startButton,stopButton, eval;
    private String m_chosenDir = "", out1;
    private boolean m_newFolderEnabled = true;
    int chosen_dir_changed =0;
    TCPdump tcpdump;
    TCPdumpHandler tcpDumpHandler;
    public static TextView resultTextView,outputText;

    DrawerLayout drawer;
    ListView drawerList;
    String[] choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        parameters.append("-nvv|tr -d ')[],'|awk '{if($14==\"TCP\" && $22==\"cksum\" && $24==\"(correct\"){printf \"%s %s %4s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),$21,substr($24,index($24,\"(\")+1,2),substr($25,index($25,\"(\")+1,2)}else if($14==\"TCP\" && $22==\"cksum\" && $24==\"(incorrect\"){printf \"%s %s %4s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),$21,substr($24,index($24,\"(\")+1,2),substr($27,index($27,\"(\")+1,2)}else if($14==\"TCP\" && $22!=\"cksum\"){next}else if($14==\"UDP\" && $23 ~ /^[0-9]+$/){printf \"%s %s %4s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),\".\",\"co\",$23}else if($14==\"UDP\" && $23!~ /^[0-9]+$/ && $21==\"bad\"){printf \"%s %s %4s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18, substr($20,0,index($20,\":\")-1),\".\",\"in\",\"0\"}else if($14==\"UDP\" && $23!~ /^[0-9]+$/ && $23==\"ok\"){printf \"%s %s %4s %4s %20s %20s %s %s %4s\\n\",$1, substr($12,0,2),$14,$17,$18,substr($20,0,index($20,\":\")-1),\".\",\"co\",\"0\"}else if($14==\"ICMP\" && $23==\"reply\"){printf \"%s %s %4s %4s %18s%s %18s%s %s %s %4s\\n\",$1,substr($12,0,2),$14,$17,$18,\".8\",substr($20,0,index($20,\":\")-1),\".0\",\".\",\"co\",$29}else if($14==\"ICMP\" && $23==\"request\"){printf \"%s %s %4s %4s %18s%s %18s%s %s %s %4s\\n\",$1,substr($12,0,2),$14,$17,$18,\".0\",substr($20,0,index($20,\":\")-1),\".0\",\".\",\"co\",$29}else if($14==\"ICMP\" && $24==\"port\" && $26==\"unreachable\"){printf \"%s %s %4s %4s %18s%s %18s%s %s %s %4s\\n\",$1,substr($12,0,2),$14,$17,$18,\".3\",substr($20,0,index($20,\":\")-1),\".3\",\".\",\"co\",$28}}'");

        // Creating a TCPdump handler for the TCPdump object created after.


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
        resultTextView= (TextView) findViewById(R.id.ResultTextView);
        outputText= (TextView) findViewById(R.id.textView);
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



    /**
     * Calls TCPdumpHandler to try to stop the packet capture.
     */

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TCPdumpHandler.class);
        switch(v.getId()) {
            case R.id.startButton :
                this.startService(intent);
                break;

            case R.id.stopButton:
                this.stopService(intent);
                Intent i = new Intent(this, ProcessDataService.class);
                stopService(i);
                Log.d ("Packets received: ", ""+GlobalVariables.numPacketsReceived);
                Log.d ("Packets processed: ", ""+GlobalVariables.numPacketsProcessed);
                break;

            case R.id.eval:
                Intent in = new Intent("protego.com.protego.EVALUATE");
                startActivity(in);
                break;
        }
    }
}
