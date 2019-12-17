import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;

class ServerSocketListener extends Thread {	//the main purpose is a socket listener
	
	int port;
	ServerSocket server_socket;
	Socket client_socket; //just to hand it to ClientSocket 
	
	ServerSocketListener( int port_arg ) {
		port = port_arg;		
	}
	
	boolean createListenerSocket() {
		try {	    		    
			/*
			//server_socket = new ServerSocket(port); //BTW, my IP is 192.168.1.21 and MAC is 34:e6:ad:32:d3:a9
			* BUT BE CARFUL that this showed a problem ! like couldn't connect from 2 different client IPs.
			* Sort of only one of them work. Odd but don't know why. So I have to go with the following usage 
			* of ServerSocket 
			*/
			server_socket  = new ServerSocket(port, 100, InetAddress.getByName("192.168.1.21"));
			server_socket.setSoTimeout(0); //lets the "accept" method block forever - infinite timeout
			System.out.println( new Timestamp(System.currentTimeMillis()) + " Servers.java " +
					"Successfully created socket on port " + port );		
	    	//serverSocket.setReuseAddress(true);
	    	//System.out.println("All is fine.");
	    	return true;
	    }
	    catch (IOException e) {
	    	System.out.println(e);
	    	e.printStackTrace();
	    	System.err.println(new Timestamp(System.currentTimeMillis()) + " Servers.java " +
	    			"Could not make a listener socket on port " + port + ". It's ok if we're ending.");
	        return false;
	    }	
	}	

	void closeSocket() {
		try {
			if( !server_socket.isClosed() ) {
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Servers.java " +
						"Closing server socket on port " + port);			
				server_socket.close();
			}
		} catch (Exception e) {
			System.out.println(new Timestamp(System.currentTimeMillis()) + " Servers.java " +
					"error closing the server socket on port " + port);
			e.printStackTrace();
		}
	}	
	
	@Override
	public void run() { //this is about listening and creating a new thread on each received message to analyze it and process it.
		//get a message in a loop
		if( createListenerSocket() ) {
			ClientSocket client;
			do {
				try {				
					client_socket = null;
					client_socket = server_socket.accept(); //code is blocked here until a connection is made.
					System.out.println(new Timestamp(System.currentTimeMillis()) + " Servers.java " +
							"Listener   A new client socket is now accepted on port " + port);
					
				} catch (Exception e) {
					System.out.println(new Timestamp(System.currentTimeMillis()) + " Servers.java " +
							"ServerSocketListener   A problem in accepting a new client on port " + port);
					e.printStackTrace();
				}

				if (client_socket != null) {
					client = null;					
					client = new ClientSocket( client_socket );
					System.gc();
					if (client.isReadyToStart()) {
						client.start(); //this makes clientSocket listen through bufferedReader and further process.											
						//we will add it to the specific array list after we read the incoming message and make the
						// other elements of the array list with the same owner and mob id non"active".
						/*Once we got a new accepted socket, the reference to the old object is lost 
						 * and this is fine, the thread of the old object will continue normally.
						 */
						System.out.println(new Timestamp(System.currentTimeMillis()) + " Servers.java " +
								"ServerSocketListener   	A new client has just started on port " + port);
					} else {				
						System.out.println(new Timestamp(System.currentTimeMillis()) +  " Servers.java " +
								"ServerSocketListener	   Odd case! something wet wrong in either printWriter or bufferedReader" +
								" while client_socket is still fine.");
						client_socket = null;
					}				
					
				} else {
					System.out.println(new Timestamp(System.currentTimeMillis()) +  " Servers.java " +
							"ServerSocketListener   Unfortunately client_socket was not assigned correctly for port " + port + "! Check program flow.");
				}			
				
			} while( !Setup.keyboard_input.equals("stop") && !Setup.force_stop );
			
			PrintListOperations.list = null;
			closeSocket();
			System.gc();
		}
	}	
}


