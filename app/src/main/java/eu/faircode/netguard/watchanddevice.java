package eu.faircode.netguard;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.text.AlphabeticIndex;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class watchanddevice<DnsName, searchPaths, dnsCredibility> extends AppCompatActivity{


    private static final String TAG = null;
    Button btnstart;
    Button btnstop;
    ImageView icanchor;
    Animation roundingalone;
    Button btuNetworking;
    private boolean enabled;

    private boolean running = false;

    private AlertDialog dialogVpn = null;

    private static final int REQUEST_VPN = 1;

    public static final int REQUEST_ROAMING = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_watchanddevice);

        btnstart = findViewById(R.id.btnstart);
        btnstop = findViewById(R.id.btnstop);
        icanchor = findViewById(R.id.icanchor);
        btuNetworking = findViewById(R.id.button);

        // create optional animation
        btnstop.setAlpha(0);

        running = true;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        enabled=prefs.getBoolean("enabled",false);

        //load animations
        roundingalone = AnimationUtils.loadAnimation(this, R.anim.roundingalone);

        if(enabled){
            icanchor.startAnimation(roundingalone);
            btnstop.animate().alpha(1).translationY(-80).setDuration(300).start();
            btnstart.animate().alpha(0).setDuration(300).start();
            startService(new Intent(watchanddevice.this,ServiceSinkhole.class));
            startActivity(new Intent(watchanddevice.this,ActivityMain.class));
        }

        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("CHecking Network");
        progressDialog.setCancelable(false);

        btuNetworking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                ConnectivityManager ConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                assert ConnectionManager != null;
                NetworkInfo networkInfo;
                networkInfo = ConnectionManager.getActiveNetworkInfo();
                progressDialog.dismiss();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Toast.makeText(watchanddevice.this, "Network available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(watchanddevice.this, "Network Not Available", Toast.LENGTH_LONG).show();
                }
            }
        });


        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icanchor.startAnimation(roundingalone);
                btnstop.animate().alpha(1).translationY(-80).setDuration(300).start();
                btnstart.animate().alpha(0).setDuration(300).start();
                    String alwaysOn = Settings.Secure.getString(getContentResolver(), "always_on_vpn_app");
                    Log.i(TAG, "Always-on=" + alwaysOn);
                    if (!TextUtils.isEmpty(alwaysOn))
                        if (getPackageName().equals(alwaysOn)) {
                            if (prefs.getBoolean("filter", false)) {
                                int lockdown = Settings.Secure.getInt(getContentResolver(), "always_on_vpn_lockdown", 0);
                                Log.i(TAG, "Lockdown=" + lockdown);
                                if (lockdown != 0) {
                                    Toast.makeText(watchanddevice.this, R.string.msg_always_on_lockdown, Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        } else {
                            Toast.makeText(watchanddevice.this, R.string.msg_always_on, Toast.LENGTH_LONG).show();
                            return;
                        }

                    boolean filter = prefs.getBoolean("filter", false);
                    if (filter && Util.isPrivateDns(watchanddevice.this))
                        Toast.makeText(watchanddevice.this, R.string.msg_private_dns, Toast.LENGTH_LONG).show();

                    try {
                        final Intent prepare = VpnService.prepare(watchanddevice.this);
                        if (prepare == null) {
                            Log.i(TAG, "Prepare done");
                            onActivityResult(REQUEST_VPN, RESULT_OK, null);
                        } else {
                            // com.android.vpndialogs.ConfirmDialog required
                            startActivityForResult(prepare, REQUEST_VPN);
                        }
                    } catch (Throwable ex) {
                        // Prepare failed
                        Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                        prefs.edit().putBoolean("enabled", false).apply();
                    }
            }

        });

        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceSinkhole.stop("switch off", watchanddevice.this, false);
                btnstop.animate().alpha(0).setDuration(300).start();
                btnstart.animate().alpha(1).setDuration(300).start();
                icanchor.clearAnimation();
                prefs.edit().putBoolean("enabled",false).apply();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult request=" + requestCode + " result=" + requestCode + " ok=" + (resultCode == RESULT_OK));
        Util.logExtras(data);

        if (requestCode == REQUEST_VPN) {
            // Handle VPN approval
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply();
            if (resultCode == RESULT_OK) {
                ServiceSinkhole.start("prepared", this);
                startActivity(new Intent(watchanddevice.this,ActivityMain.class));
            } else if (resultCode == RESULT_CANCELED)
                Toast.makeText(this, "VPN Denied", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ROAMING)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ServiceSinkhole.reload("permission granted", this, false);
                startActivity(new Intent(watchanddevice.this,ActivityMain.class));
            }
    }
}


