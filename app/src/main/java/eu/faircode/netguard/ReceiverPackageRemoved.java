package eu.faircode.netguard;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class ReceiverPackageRemoved extends BroadcastReceiver {
    private static final String TAG = "NetGuard.Receiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Received " + intent);
        Util.logExtras(intent);

        String action = (intent == null ? null : intent.getAction());
        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            if (uid > 0) {
                DatabaseHelper dh = DatabaseHelper.getInstance(context);
                dh.clearLog(uid);
                dh.clearAccess(uid, false);

                NotificationManagerCompat.from(context).cancel(uid); // installed notification
                NotificationManagerCompat.from(context).cancel(uid + 10000); // access notification
            }
        }
    }
}
