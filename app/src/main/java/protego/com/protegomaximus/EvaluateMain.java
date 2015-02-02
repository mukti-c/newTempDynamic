package protego.com.protegomaximus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class EvaluateMain extends Activity {

    private TextView filecon;
    private String out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_main);
        filecon = (TextView) findViewById(R.id.filecon);
        new EvalTask().execute();
    }

    private class EvalTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress;
        @Override
        protected Void doInBackground(Void... params) {
            Tranny t1 = new Tranny();
            out = t1.evaluate();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(EvaluateMain.this, "", "Please wait while the model is evaluated...", true);
            progress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();
            filecon.setText(out);
        }
    }
}
