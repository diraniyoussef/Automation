package de.dirani.readButton;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;

abstract class WiFiConnection {
    private static WifiManager wifiManager;

/*
    static void turnWiFiOff() {
        wifiManager.setWifiEnabled(false);
    }

    static boolean isWiFiOn(){
        return(wifiManager.isWifiEnabled());
    }
*/
    static boolean wiFiValid( Context applicationContext, boolean silentToast, Toasting toasting, boolean localNotInternet ) {
        if( !localNotInternet ) {
            return true;
        }
        wifiManager = (WifiManager) applicationContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean isValid = false;
        boolean wiFiOff = ssidState(applicationContext);
        if (wiFiOff) {
            toasting.toast("Please turn on your WiFi...", Toast.LENGTH_SHORT, silentToast);
        } else {
            isValid = true;
        }
        return (isValid);
    }

    private static boolean ssidState(Context appContext) {
        //setDictionary();
        String currentSsid = getCurrentSsid(appContext);
        //I removed some of the old code. Anyway there must be a better mechanism to know if we're local or foreign.
        return( TextUtils.isEmpty(currentSsid) );
    }

    private static String getCurrentSsid(Context context) {
        //Context context = getApplicationContext();
        //it seems like getApplicationContext is the best parameter.
        //since later when context.getSystemService(Context.WIFI_SERVICE); is called, other contexts may cause memory leaks?!
        String ssid = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return null;
        }

        if (networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null) {
                    if (!TextUtils.isEmpty(connectionInfo.getSSID())) {
                        ssid = connectionInfo.getSSID(); //this returns extra quotes at the beginning and end of it.
                        ssid = ssid.replaceAll("^\"(.*)\"$", "$1"); //this removes the quotes
                    }
                }
            }
        }
        return ssid;
    }

}

class SocketConnection { //some methods are not static in this class, this is why it is not made static.
    //My 2 sockets issue**********************************
    static int maxClientsNumber = 2; //this should be constant
    volatile Socket client[] = new Socket[maxClientsNumber];
    volatile int active_client_index = -1; //convention is that the value is either 0 or 1 and that it starts with -1
    volatile PrintWriter[] printWriter = new PrintWriter[maxClientsNumber];
    //****************************************************
    ServerConfig selectedServerConfig; //being protected instead of private is useful only in one place, but still is not so interesting.
    static int i;
    CommunicateWithServer communication;

    private volatile boolean newSocketIsCurrentlyUnderCreation = false;
    private volatile boolean socketCreationSuccessful;
    private Toasting toasting;
    static private PeriodicCheck_Data socket_waitingData = new PeriodicCheck_Data(50,1500);
    static PeriodicCheck_Data read_waitingData = new PeriodicCheck_Data(20,3000);

    static boolean silentToast;

    private boolean localNotInternet;

    SocketConnection(Toasting toasting, boolean isSilentToast, Activity act, Context applicationContextParam,
                     ServerConfig selectedServerConfig_arg, int NUMBER_OF_POINTS_arg, char[] includedPinIndexAsChar_arg,
                     Switch[] userSwitch_arg, TextView[] textView_arg, boolean localNotInternet, String owner_part, String mob_part, String mob_Id) { //constructor
        this.localNotInternet = localNotInternet;
        selectedServerConfig = selectedServerConfig_arg;
        communication = new CommunicateWithServer( toasting, act, applicationContextParam, this,
                selectedServerConfig.panel_index, selectedServerConfig.panel_name,
                NUMBER_OF_POINTS_arg, includedPinIndexAsChar_arg, userSwitch_arg, textView_arg,
                localNotInternet, owner_part, mob_part, mob_Id );
        this.toasting = toasting;
        silentToast = isSilentToast;
    }

    //The purpose of this method is to message the server whenever we have a socket...
    void socketConnectionSetup(String message) throws IOException {
        if (active_client_index == -1) {//only the first time
            active_client_index = 0;
            //disableSwitches(); //user recognizes disabling so no need to toast.
            //if (createNewSocketAndEnableSwitches(active_client_index)) {
            if (createNewSocket(active_client_index)) {
                Log.i("Youssef Connection.java", "finished creating a new socket client 0." + " For panel " + selectedServerConfig.panel_name);
                communication.delayToManipulateSockets.startTiming();
                communication.setThreads(message, false);
            }
        } else {
            Log.i("Youssef Connection.java", "to initiate a new order without creating initially a new socket."
                    + " For panel " + selectedServerConfig.panel_name);
            communication.setThreads(message, true);
        }
    }

    static int nextClientIndex(int i) {
        if (i < maxClientsNumber - 1){
            i++;
        } else { //i == maxClientsNumber - 1
            i = 0;
        }
        return(i);
    }

    void destroySocket(int client_index) { //No need to kill the bufferThread, bufferedReader will simply update with the new socket.
        //testAndFixConnection thread also isn't available to be killed in the first place!
        // Other threads just continue their work with no real harm and finish peacfully.
        try {
            communication.null_bufferThread(client_index); //interesting since I want to kill the thread.

            if (printWriter[client_index] != null){
                printWriter[client_index].close(); //returns void
                printWriter[client_index] = null;
            }
            if (client[client_index] != null) {
                if(client[client_index].isConnected()) //I added this line on 06062019
                    client[client_index].close(); //returns void    //this actually shows to the server since it's a proper closing of a socket.
                //client[client_index] = null;
            }
            Log.i("Youssef Connection.java","Now socket " + client_index + " is destroyed." + " For panel " + selectedServerConfig.panel_name);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Youssef Connection.java","Error in closing socket, printWriter, or reader................"
                    + " For panel " + selectedServerConfig.panel_name);
        }
    }

    void destroyAllSockets() {
        communication.delayToManipulateSockets.cancelTimer(); //usually there is a 2 minutes timer to manipulate the sockets, this should be cancelled.
        //it's like         communication.delayToManipulateSockets.cancelTimer();
        communication.destroySocketTimer.cancelTimer(); //there might be one socket waiting to be destroyed after sometime, so I will cancel the timer and close the socket
        //in the current method.
        destroySocket(0);
        destroySocket(1);
        active_client_index = -1;
    }

    boolean createNewSocket(int client_index_param) {
        //createSocketAsyncTaskInstance.execute();
        socketCreationSuccessful = true;
        newSocketIsCurrentlyUnderCreation = true;
        //I made the object instance createSocketThreadInstance local to the createNewSocket method (it's pretty safe to me to make it local since the CreateSocket thread
        // will finish before this createNewSocket method ends -- even though I guess the current method doesn't end as long as there is a thread running from it.)
        CreateSocket createSocketThreadInstance = new CreateSocket(client_index_param);
        createSocketThreadInstance.start();
        //execution after AsyncTask goes to afterSocketCreation()
        //I'm joining the thread now... Maybe thread.join was a better choice. Anyway.
        int iterationTimeout = 0; //this variable can change, right?????????????????????????

        while ((iterationTimeout < socket_waitingData.maxIterations) && (newSocketIsCurrentlyUnderCreation)) {
            iterationTimeout = iterationTimeout + 1;
            try {
                Thread.sleep(socket_waitingData.divisionInterval);
            } catch (Exception e) {
                //Thread.currentThread().interrupt();
                e.printStackTrace();
                Log.i("Youssef Connection.java","Error: Sleeping in createNewSocket." + " For panel " + selectedServerConfig.panel_name);
            }
        }
        if (iterationTimeout == socket_waitingData.maxIterations) { //wrongly it was before read_waitingData.maxIterations. Even now I'm not sure if it's needed
            //kill the asyncTask although not needed.
            newSocketIsCurrentlyUnderCreation = false;
            if( localNotInternet ) {
                toasting.toast("Connection failed presumably for panel " + selectedServerConfig.panel_name + "\n" +
                        "You may check if electricity is down on the module.", Toast.LENGTH_SHORT, silentToast);
            } else {
                toasting.toast("Couldn't connect to server.\n" +
                        " Please check Internet connection of the mobile.", Toast.LENGTH_LONG, silentToast);
                //Really, when connected to the internet through the data conection of the mobile operator, if  the signal is weak, it may not connect to the
                // server to establish a connected socket
            }
            if(client[client_index_param] != null) {
                try {
                    client[client_index_param].close();
                } catch (Exception e) {
                    //Thread.currentThread().interrupt();
                    e.printStackTrace();
                    Log.i("Youssef Connection.java", "Error: closing socket." + " For panel index " + selectedServerConfig.panel_index);
                }
            }
            return (false);
        } else {
            if (socketCreationSuccessful){
                return (true);
            } else {
                if(client[client_index_param] != null) {
                    try {
                        client[client_index_param].close();
                    } catch (Exception e) {
                        //Thread.currentThread().interrupt();
                        e.printStackTrace();
                        Log.i("Youssef Connection.java", "Error: closing socket." + " For panel index " + selectedServerConfig.panel_index);
                    }
                }
                return (false);
            }
        }
    }

    class CreateSocket extends Thread {
        int index;
        CreateSocket(int client_index_param){
            index = client_index_param;
        }

        @Override
        public void run() {
            try {
                i++; //only for debugging
                if( i == 10 ) {
                    i = 0;//I don't want i to grow indefinitely and potentially cause an overflow.
                }
                Log.i("Youssef Connection.java", "This is the " + i + "th time we enter in CreateSocket");
                        //client = new Socket(wiFiConnection.chosenIPConfig.staticIP, port); //should be further developed.
                //client.connect(new InetSocketAddress(wiFiConnection.chosenIPConfig.staticIP, port), 1500);
                client[index] = new Socket();
                Log.i("Youssef Connection.java", "client[index " + index + "] is fine to connect to IP " +
                        selectedServerConfig.staticIP + " on port " + selectedServerConfig.getPortFromIndex(index));
                //client.connect(new InetSocketAddress("192.168.4.201", port),1500);
                client[index].connect( new InetSocketAddress(selectedServerConfig.staticIP,
                        selectedServerConfig.getPortFromIndex(index)), socket_waitingData.timeout );

                //client.setSoTimeout(0); //no need to set it to infinite since all it does, if it were not infinite, is to throw an exception; it does not affect the socket.
                Log.i("Youssef Connection.java", "Socket " + index + " " +
                        "is connected, for panel index " + selectedServerConfig.panel_index + " on port " +
                        "selectedServerConfig.getPortFromIndex(index)");

                printWriter[index] = new PrintWriter( client[index].getOutputStream() );
                Log.i("Youssef Connection.java", "New printWriter is made, for panel index " + selectedServerConfig.panel_index);

                communication.renew_bufferThread(index);
                //Log.i("Youssef Connection.java", "New bufferThread is made." + " For panel  index " + selectedServerConfig.panel_index);

            } catch (Exception e) {//I hope this includes the IOException or the UnknownHostException because this will be thrown
                //in case the IP is wrong or the electricity on module is down.
                e.printStackTrace();
                if(client[index] != null) {
                    try {
                        client[index].close();
                    } catch (Exception exc) {
                        //Thread.currentThread().interrupt();
                        exc.printStackTrace();
                        Log.i("Youssef Connection.java", "Error: closing socket." + " For panel index " + selectedServerConfig.panel_index);
                    }
                }
                //it's probably better to call      destroySocket(index);       but to be checked later.
                Log.i("Youssef Connection.java", "Exception is thrown. For panel index " + selectedServerConfig.panel_index +
                    " on port " + selectedServerConfig.getPortFromIndex(index));
                socketCreationSuccessful = false;
                //Now turn off the WiFi
/*
                if (WiFiConnection.isWiFiOn()) {
                    WiFiConnection.turnWiFiOff(); //this sometimes solves a problem..............
                }
                Generic.toasting.toast("Couldn't connect.\nPlease turn on the WiFi to refresh...", Toast.LENGTH_LONG, silentToast);
*/
                if ( localNotInternet ) {
                    toasting.toast("Couldn't connect to panel " + selectedServerConfig.panel_name +
                            "\nPlease try again later," +
                            "\nor check if electricity is down.", Toast.LENGTH_LONG, silentToast);
                } else {
                    toasting.toast("Couldn't connect to Server..." +
                            "\nPlease check if Internet connection is valid.", Toast.LENGTH_LONG, silentToast);
                }

                e.printStackTrace();
            }
            newSocketIsCurrentlyUnderCreation = false;
        }
    }
}

