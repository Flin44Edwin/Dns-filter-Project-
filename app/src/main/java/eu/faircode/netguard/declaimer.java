package eu.faircode.netguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class declaimer<declaimerActivity> extends AppCompatActivity {


    Button btnget2;


    TextView tvSplash2 , tvSubSplash2;
    Animation atg, btgone, btgtwo;
    ImageView ivSplash2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declaimer);

        tvSplash2 = findViewById(R.id.tvSplash2);
        tvSubSplash2 = findViewById(R.id.tvSubSplash2);
        btnget2 = findViewById(R.id.btnget2);
        ivSplash2 = findViewById(R.id.ivSplash2);


        // load animation
        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(this, R.anim.btgtwo);

        // passing animation
        ivSplash2.startAnimation(atg);
        tvSplash2.startAnimation(btgone);
        tvSubSplash2.startAnimation(btgone);
        btnget2.startAnimation(btgtwo);



        //passing event for button
        btnget2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(declaimer.this, watchanddevice.class);
                a.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(a);
            }
        });


    }
}

