package de.dirani.readButton;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class WelcomeActivity extends AppCompatActivity {
    private boolean isThisTheFirstTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isThisTheFirstTime = true; /*a better way to achieve the same result is
        * Start any other activity by calling startActivityForResult and when you return back here you onActivityResult will get called*/
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    private void startMainActivity( String action_status ) {
        Intent intent = new Intent( getApplicationContext(), MainActivity.class );
        intent.putExtra("action_status", action_status); //where v is button that is clicked, you will find it as a parameter to onClick method
        startActivity(intent);
    }
    /*
    private void startPanelActivity( String action_status ) {
        Intent intent = new Intent( getApplicationContext(), Informing_Electricity.class );
        intent.putExtra("action_status", action_status); //where v is button that is clicked, you will find it as a parameter to onClick method
        startActivity(intent);
    }
*/
    @Override
    protected void onResume(){
        super.onResume();
        setContentView(R.layout.activity_welcome);

        final Toasting toasting = new Toasting( this );

        final Button localWiFi_Button = findViewById( R.id.button_localWiFi );
        localWiFi_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Current Implementation: on button click check WiFi.
                 * A potential variation is to automatically run a background task that senses any changes to WiFi connection, in which case the result of the
                 * background process is reliable. But I don't prefer background processes.
                 */
                //the following method is a process that interacts with the user if the WiFi connection didn't work
                // and returns true in case the WiFi connection is ok
                if( WiFiConnection.wiFiValid( getApplicationContext(), false, toasting, true ) ) {
                    startMainActivity("local");
                }
            }
        });
        //String s = BuildConfig.APPLICATION_ID; //for debugging
        //Log.i("Youssef WlcAct.java", "app id is : " + s );

        final Button internet_Button = findViewById(R.id.button_Internet);
        internet_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity("internet");
            }
        });

        final Button enter_Button = findViewById(R.id.button_enter);
        enter_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity("internet");
            }
        });


        final Button chooseLocalOrInternet_Button = findViewById( R.id.button_chooseLocalInternet );

        final Handler makeTheChooseLocalOrInternetButtonInvisible_Handler = new Handler();
        final Runnable runnable = new Runnable() {
            public void run() {
                chooseLocalOrInternet_Button.setVisibility(View.GONE);
            }
        };

        chooseLocalOrInternet_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOut(chooseLocalOrInternet_Button);
                fadeIn(localWiFi_Button);
                localWiFi_Button.setVisibility(View.VISIBLE);
                fadeIn(internet_Button);
                internet_Button.setVisibility(View.VISIBLE);
                /*It's not enough to fade the chooseLocalOrInternet_Button, in addition I want to set its visibility to Gone
                 * so that it cannot accidentally be clicked
                 */
                makeTheChooseLocalOrInternetButtonInvisible_Handler.postDelayed(runnable, 500);
            }
        });

        if( !isThisTheFirstTime ) {
            localWiFi_Button.setVisibility(View.VISIBLE);
            localWiFi_Button.setAlpha(1);
            internet_Button.setVisibility(View.VISIBLE);
            internet_Button.setAlpha(1);
            //localWiFi_Button.setImageResource(R.drawable.button_semi_transparent); //this had troubles with imageButton. With Buttons it worked fine.
            chooseLocalOrInternet_Button.setVisibility(View.GONE);
        }

        isThisTheFirstTime = false; //this is interesting in order not to show the 2 buttons : local and Internet

        final ImageButton settings_Button = findViewById(R.id.settings_button);

        settings_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity("settings");
            }
        });

        final Button about_Button = findViewById(R.id.button_about);
        about_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AppDescription.class));
            }
        });

    }

    private void fadeIn(Button mButton) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mButton, "alpha", 0f, 1f);
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }

    private void fadeOut(Button mButton) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mButton, "alpha", 1f, 0f);
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }


}
