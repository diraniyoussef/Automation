package de.dirani.readButton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    //to convert a string to an integer think of
    //port = Integer.parseInt(etPort.getText().toString());
    private Button P1_Button, P2_Button, P3_Button, manual_reflector_button, inform_elec_Button, inform_inject_Button; /*the button
    extra_settings_button is declared local below*/
    private EditText P1_EditText, P2_EditText, P3_EditText, manual_reflector_EditText, inform_elec_EditText, inform_inject_EditText;

    private SharedPreferences mPrefs;
    private String P1_index, P2_index, P3_index, manual_reflector_index, other_panel_index, inform_elec_index, inform_inject_index;
    //////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.activity_main);

        final Bundle bundle = getIntent().getExtras();
        if( bundle == null ) {
            finish();
            return;
        }

        P1_Button = findViewById(R.id.button_P1);
        P2_Button = findViewById(R.id.button_P2);
        P3_Button = findViewById(R.id.button_P3);
        manual_reflector_button = findViewById(R.id.button_ManualRef);
        inform_elec_Button = findViewById(R.id.button_Inform_elec);
        inform_inject_Button = findViewById(R.id.button_Inform_inject);

        //These indices are so critical and are in coordination with the panels.
        P1_index = "1";
        P2_index = "1";
        P3_index = "1";
        manual_reflector_index = "1";
        inform_elec_index = "13";
        inform_inject_index = "1";
        other_panel_index = "-1"; //I can make it any value but -1 is preferred

        final RelativeLayout extra_button_parent = findViewById(R.id.extra_button_Parent);
        final Intent intent = new Intent();
        final String action_status = bundle.getString("action_status");
        boolean localNotInternet;
        if( !action_status.equals("settings") ) {
            extra_button_parent.setVisibility(View.GONE);
            String owner_part = "Electrotel_Store";
            //String mob_part = "S7_Edge";
            //String mob_part = "S4";
            String mob_part = "usr1"; //usr2 is kept in the store
            //String mob_part = "Mom_Tab";
            String mob_Id = owner_part + ":" + mob_part + ":";

            if( action_status.equals("local") ) {
                localNotInternet = true;
            } else {
                localNotInternet = false;
            }
            intent.putExtra("action_status", action_status);
            intent.putExtra("owner_part", owner_part);
            intent.putExtra("mob_part", mob_part);
            intent.putExtra("mob_Id", mob_Id);

            //Setting the title (similar to android:label in AndroidManifest.xml)
            if (localNotInternet) {
                setTitle(getApplicationContext().getString(R.string.app_name) + " - Local WiFi");
            } else {
                setTitle( getApplicationContext().getString(R.string.app_name) );
                //setTitle(getApplicationContext().getString(R.string.app_name) + " - Internet");
            }

            //restricting access to app user according to usage policy
            final RelativeLayout manual_reflector_button_parent = findViewById(R.id.manual_reflector_button_Parent);
            final RelativeLayout inform_inject_button_parent = findViewById(R.id.inform_inject_button_Parent);
            if (localNotInternet) {
                //make the manual reflector panel button invisible.
                manual_reflector_button_parent.setVisibility(View.GONE);
            } else {
                //make the injecting panel button invisible
                inform_inject_button_parent.setVisibility(View.GONE);
            }

            inform_elec_Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelIndex", inform_elec_index );
                    intent.setClass(getApplicationContext(), Informing_Electricity.class);
                    startActivity(intent);
                }
            });

        } else {
            /*Please note that it is possible one day that the Local WIFI list of panels is not the same of the Internet list.
            * In this case, the list that will be shown here in the Network Configuration list will be the same as the Local
            * WiFI's list. (Of course in addition to the extra panel)
             */
            setTitle(getApplicationContext().getString(R.string.app_name) + " - Network Configuration");

            extra_button_parent.setVisibility(View.VISIBLE); /*This is set to visible in case there are some panels which are not
             * visible in the list of Local WIFI panels. I.e. if the LOCAL WIFI list contains all the panels then we must not show this
             * button.
             */
            Button extra_settings_button = findViewById(R.id.extra_setting_button);
            extra_settings_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelIndex", other_panel_index ); //panel index will be used to set the static IP.
                    //if the panel index corresponds to this "other panel" we won't assign then any static IP.
                    intent.putExtra("otherPanelIndex", other_panel_index ); /*used to compare the panel index with
                    this extra panel index*/
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });

            P1_Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelIndex",P1_index );
                    intent.putExtra("otherPanelIndex", other_panel_index );
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });
            P2_Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelIndex",P2_index );
                    intent.putExtra("otherPanelIndex", other_panel_index );
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });
            P3_Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelIndex",P3_index );
                    intent.putExtra("otherPanelIndex", other_panel_index );
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });
            manual_reflector_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelIndex", manual_reflector_index );
                    /*you have to add some Extra things to refer to the "known" panels,whether the user is dealing with an informing or an
                    * obeying panel, because it's easy for him to confuse things*/
                    intent.putExtra("panelType", "obeying" );
                    intent.putExtra("otherPanelIndex", other_panel_index );
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });
            inform_elec_Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelType", "informing" );
                    intent.putExtra("reflectorsNumber", "1" );
                    intent.putExtra("panelIndex", inform_elec_index );
                    intent.putExtra("otherPanelIndex", other_panel_index );
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });
            inform_inject_Button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button pressed_button = (Button) v;
                    intent.putExtra("panelName", pressed_button.getText().toString());
                    intent.putExtra("panelType", "informing" );
                    intent.putExtra("reflectorsNumber", "2" );
                    intent.putExtra("panelIndex", inform_inject_index );
                    intent.putExtra("otherPanelIndex", other_panel_index );
                    intent.setClass(getApplicationContext(), ConfigPanel.class);
                    startActivity(intent);
                }
            });
        }

        //any declared variables shouldn't be consuming much resources. I'm sacrificing the memory for the sake of rapidity when onResume is repeatedly accessed.
        P1_EditText = findViewById(R.id.editText_P1);
        P2_EditText = findViewById(R.id.editText_P2);
        P3_EditText = findViewById(R.id.editText_P3);
        manual_reflector_EditText = findViewById(R.id.editText_ManualRef);
        inform_elec_EditText = findViewById(R.id.editText_Inform_elec);
        inform_inject_EditText = findViewById(R.id.editText_Inform_inject);

        LinearLayout parentContainer_Layout = findViewById(R.id.Parent_Container_MainActivity);
        //P1_Button_Parent = (RelativeLayout) findViewById(R.id.P1_button_Parent);
        //P2_Button_Parent = (RelativeLayout) findViewById(R.id.P2_button_Parent);
        //P3_Button_Parent = (RelativeLayout) findViewById(R.id.P3_button_Parent);

        mPrefs = getSharedPreferences("editTexts_MainActivity", 0 );
        P1_Button.setText(mPrefs.getString("button1Name", "Panel 1"));
        P2_Button.setText(mPrefs.getString("button2Name", "Panel 2"));
        P3_Button.setText(mPrefs.getString("button3Name", "Panel 3"));
        manual_reflector_button.setText(mPrefs.getString("button4Name", "Manual Reflector"));
        inform_elec_Button.setText(mPrefs.getString("button5Name", "النميرية (الحرف)"));
        inform_inject_Button.setText(mPrefs.getString("button6Name", "Injecting Info"));

        P1_EditText.setText(mPrefs.getString("button1Name", "Panel 1"));
        P2_EditText.setText(mPrefs.getString("button2Name", "Panel 2"));
        P3_EditText.setText(mPrefs.getString("button3Name", "Panel 3"));
        manual_reflector_EditText.setText(mPrefs.getString("button4Name", "Manual Reflector"));
        inform_elec_EditText.setText(mPrefs.getString("button5Name", "النميرية (الحرف)"));
        inform_inject_EditText.setText(mPrefs.getString("button6Name", "Injecting Info"));

        //In this scenario (the onPause), you don’t need to re-initialize components that were created during any of the callback methods leading up to the Resumed state. If the activity becomes completely invisible, the system calls onStop().

        parentContainer_Layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                P1_Button.setVisibility(View.VISIBLE); //if P1_Button is still invisible, that is because P1_button_Parent is invisible
                P2_Button.setVisibility(View.VISIBLE);
                P3_Button.setVisibility(View.VISIBLE);
                manual_reflector_button.setVisibility(View.VISIBLE);
                inform_elec_Button.setVisibility(View.VISIBLE);
                inform_inject_Button.setVisibility(View.VISIBLE);
            }
        });

        P1_Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                P1_Button.setVisibility(View.GONE);
                if( P1_EditText.requestFocus() ) {
                    InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
                    imm.showSoftInput( P1_EditText, InputMethodManager.SHOW_IMPLICIT );
                }
                return true;
            }
        });


        P2_Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                P2_Button.setVisibility(View.GONE);
                if(P2_EditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(P2_EditText, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            }
        });

        P3_Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                P3_Button.setVisibility(View.GONE);
                if(P3_EditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(P3_EditText, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            }
        });


        manual_reflector_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                manual_reflector_button.setVisibility(View.GONE);
                if(manual_reflector_EditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(manual_reflector_EditText, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            }
        });

        inform_elec_Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inform_elec_Button.setVisibility(View.GONE);
                if(inform_elec_EditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(inform_elec_EditText, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            }
        });

        inform_inject_Button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inform_inject_Button.setVisibility(View.GONE);
                if(inform_inject_EditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(inform_inject_EditText, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            }
        });

        final View backToWelcome_Button = findViewById(R.id.button_backToWelcomeFromMain);
        backToWelcome_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), readButton.WelcomeActivity.class));
                finish();
            }
        });
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //There's another code, which may be better
        //https://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside/28939113#28939113
        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);
        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            try{
                w.getLocationOnScreen(scrcoords);
            } catch( Exception e) {
                Log.i("Youssef MainActi..java", "dispatchTouchEvent getLocationOnScreen caused a Null Pointer exception to be caught");
                return ret;
            }
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            //Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
            Log.i("Youssef MainActi..java", "dispatchTouchEvent ran in MainActivity");
            if (event.getAction() == MotionEvent.ACTION_DOWN &&
                    (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
                //The following 2 lines hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                } catch( Exception e) {
                    Log.i("Youssef MainActi..java", "dispatchTouchEvent getWindowToken caused a Null Pointer exception to be caught");
                    return ret;
                }
                //you can make the cursor disappear by putting the following below without the need
                //to struggle with xml file...
                Log.i("Youssef MainActi..java", "Keyboard should be hidden in MainActivity");

                P1_EditText.clearFocus();
                P2_EditText.clearFocus();
                P3_EditText.clearFocus();
                manual_reflector_EditText.clearFocus();
                inform_elec_EditText.clearFocus();
                inform_inject_EditText.clearFocus();

                P1_Button.setVisibility(View.VISIBLE);
                P1_Button.setText(P1_EditText.getText().toString());
                P2_Button.setVisibility(View.VISIBLE);
                P2_Button.setText(P2_EditText.getText().toString());
                P3_Button.setVisibility(View.VISIBLE);
                P3_Button.setText(P3_EditText.getText().toString());
                manual_reflector_button.setVisibility(View.VISIBLE);
                manual_reflector_button.setText(manual_reflector_EditText.getText().toString());
                inform_elec_Button.setVisibility(View.VISIBLE);
                inform_elec_Button.setText(inform_elec_EditText.getText().toString());
                inform_inject_Button.setVisibility(View.VISIBLE);
                inform_inject_Button.setText(inform_inject_EditText.getText().toString());

                SharedPreferences.Editor mEditor = mPrefs.edit();
                String name;
                name = P1_EditText.getText().toString();
                mEditor.putString( "button1Name", name ).apply();
                name = P2_EditText.getText().toString();
                mEditor.putString( "button2Name", name ).apply();
                name = P3_EditText.getText().toString();
                mEditor.putString( "button3Name", name ).apply();
                name = manual_reflector_EditText.getText().toString();
                mEditor.putString( "button4Name", name ).apply();
                name = inform_elec_EditText.getText().toString();
                mEditor.putString( "button5Name", name ).apply();
                name = inform_inject_EditText.getText().toString();
                mEditor.putString( "button6Name", name ).apply();
            }
        }
        return ret;
    }


}


class ServerConfig {

    ServerConfig( String panel_index , String panel_name ) {
        this.panel_index = panel_index;
        this.panel_name = panel_name;
    }

    String panel_name;
    String staticIP = "";

    static private int port1 = 11359; //choose good ports, e.g. 43111; doesn't work. 256 and 257 work.
    //static private int port1 = 11357;
    static private int port2 = 11360;
    //static private int port2 = 11358;

    String panel_index;
    int getPortFromIndex(int index) {
        if (index == 0) {
            return port1;
        } else if(index == 1) {
            return port2;
        } else {
            return -1;
        }
    }
    /*
    int getOtherPortFromIndex(int index) { //for debugging
        if (index == 0) {
            return port2;
        } else if(index == 1) {
            return port1;
        } else {
            return -1;
        }
    }
*/

    //MAKE SURE that the internet WiFi of the client at his home does not conflict with that of the modules
    //especially in terms of "192.168.4"
    //Anyway, it's a good practice to dedicate the last 55 IPs (thus from 192.168.4.251 till 192.168.4.255) to the modules,
    //so that later when a second mobile wants to use the network it assigns (increments) automatically his IP from his friend
    //and the IP assignment of the new mobile will imitate a loop (limited to the number of allowable mobiles)...

    void setInternetDictionary() {
        staticIP = "192.168.1.21";
    }
}