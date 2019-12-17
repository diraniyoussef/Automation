package de.dirani.readButton;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.TextView;

public class Informing_Electricity extends AppCompatActivity {
    //Panel 2 is the "Garden Panel" which has 6 points.
    private SharedPreferences mPrefs;
    private SocketConnection socketConnection;
    //**********************************************************************************
    private static final int NUMBER_OF_POINTS = 2;
    //Please note that the order is in accordance with the body of onResume
    private static final char[] includedPinIndexAsChar = {'5', '6'}; //'k' is dummy
    //**********************************************************************************

    private static EditText[] editText = new EditText [NUMBER_OF_POINTS];      //their values are taken from cache memory maybe
    private static TextView[] textView = new TextView[NUMBER_OF_POINTS];     //their values are controlled by the switches programmatically

    private String message;
    private String message_header;

    final Toasting toasting = new Toasting( this );
    private boolean localNotInternet;

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

        Bundle bundle = getIntent().getExtras();
        if( bundle == null ) {
            finish();
            return;
        }
        setContentView(R.layout.informing_electricity);

        final String panel_name = bundle.getString("panelName");
        final String panel_index = bundle.getString("panelIndex");
        ServerConfig selectedServerConfig = new ServerConfig( panel_index, panel_name );
        localNotInternet = bundle.getString("action_status").equals("local");
        if (localNotInternet) {
            String dummy_default_IP =  "192.168.1.215";
            final SharedPreferences network_prefs = getSharedPreferences("network_config", 0 );
            selectedServerConfig.staticIP = network_prefs.getString( panel_index, dummy_default_IP);
        } else {
            Log.i("Youssef Infor..Elec", "setting Internet Dictionary" );
            selectedServerConfig.setInternetDictionary();
        }
        String owner_part = bundle.getString("owner_part");
        String mob_part = bundle.getString("mob_part");
        String mob_Id = bundle.getString("mob_Id");

        //Setting the title (similar to android:label in AndroidManifest.xml)
        if( localNotInternet ) {
            setTitle( "Local WiFi - " + panel_name );
        } else {
            //setTitle( "Internet - " + panel_name );
            setTitle( panel_name);
        }


        textView[0] = findViewById(R.id.inform_elec_textView0);
        textView[1] = findViewById(R.id.inform_elec_textView1);

        textView[0].setText("Status");
        textView[0].setBackgroundColor(Color.LTGRAY);
        textView[1].setText("Status");
        textView[1].setBackgroundColor(Color.LTGRAY);

        editText[0] = findViewById(R.id.inform_elec_editText0);
        editText[1] = findViewById(R.id.inform_elec_editText1);

        //loading the value of editText1 each time I enter the activity
        mPrefs = getSharedPreferences("editTexts_informing_elec_places", 0);
        setEditTextPlaces();
        //now to know if the user has changed the editText (the description of the point) then to memorize it...
        // The goal is to update the saved values of editText1 in memory if the user edited it
        editText[0].addTextChangedListener( new PrefsTextWatcher(mPrefs, editText[0], "Informing_elec_point0Name") );
        editText[1].addTextChangedListener( new PrefsTextWatcher(mPrefs, editText[1], "Informing_elec_point1Name") );

        Button refreshButton = this.findViewById( R.id.inform_elec_button_refresh );

        message_header = mob_Id + String.valueOf( selectedServerConfig.panel_index ) + ":";

        //In this scenario (the onPause), you don’t need to re-initialize components that were created during any of the callback methods leading up to the Resumed state. If the activity becomes completely invisible, the system calls onStop().
        socketConnection = new SocketConnection(toasting, true, this, getApplicationContext(), selectedServerConfig,
                NUMBER_OF_POINTS, includedPinIndexAsChar, null, textView, localNotInternet, owner_part, mob_part, mob_Id);

        SocketConnection.silentToast = true;
        message = message_header + "R?\0"; //this is the request message to the server like this "mob1:R?\0". R as of Report.
        messageTheServer(true);//nothing special about switchParent[0][0] except to tell
        //the messageTheServer method that it's the first panel

        //for the button presses toasts aren't silent
        SocketConnection.silentToast = false;
//        SocketConnection.communication.silentToast = false;

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                message = message_header + "R?\0";
                messageTheServer(false);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if( socketConnection != null ) {
            socketConnection.destroyAllSockets();
            socketConnection.communication = null;
        }
        socketConnection = null;
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    private void messageTheServer( boolean silent_wifi ) {
        if( WiFiConnection.wiFiValid( getApplicationContext(), silent_wifi, toasting, localNotInternet ) ) {
            try {
                    Log.i("Youssef info..electr", "message is " + message);
                    socketConnection.socketConnectionSetup(message);
            }
            catch (Exception e) {
                Log.i("Youssef info..electr","Error in messageTheServer");
                e.printStackTrace();
            }
        }
    }

    private void setEditTextPlaces() {
        editText[0].setText(mPrefs.getString("Informing_elec_point0Name", "Electricity Mains") );
        editText[1].setText(mPrefs.getString("Informing_elec_point1Name", "Generator") );
    }

    //The following method is to hide the keyboard when the user presses outside of an EditText.
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            //Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
                //The following 2 lines hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                //you can make the cursor disappear by putting the following below without the need
                //to struggle with xml file...
                for (int i = 0; i < NUMBER_OF_POINTS; i++)
                    editText[i].clearFocus();
            }
        }
        return ret;
    }

}
