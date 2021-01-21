package org.petero.droidfish.activities;
import org.petero.droidfish.DroidFishApp;
import org.petero.droidfish.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;


public class CPUWarning extends Activity {
    public static class Fragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.cpu_warning)
                    .create();
        }
        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            Activity a = getActivity();
            if (a != null)
                a.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogFragment df = new Fragment();
        df.show(getFragmentManager(), "");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(DroidFishApp.setLanguage(newBase, false));
    }
}
