//This class is actually about client sockets

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class ClientSocket extends Thread { //the main role is a message listener
	Socket client_socket;
	//boolean active = true; //by default any new socket is "active" -- forget about the active issue...
	// in the arraylist only the client_socket with the particular couple (owner - mob) id is kept and the old ones
	// are removed. 
	String owner; 
	String sender_id;
	PrintWriter print_writer;
	Printer sender_printer; //mainly this is to send ack in a decent manner 
	
	MessageOperations message_operations = new MessageOperations( this );
	
	boolean is_new_client_socket = true; //This is used to know if the object is declared for the first time or not.
	
	BufferedReader buffered_reader;
	static int bufferSize = 100;//*******************
	
	private Timer timer;
	volatile boolean timer_reached = false;	
	
	//int attempt_number_to_close = 0; //just for debugging
	//String mod_id; //this is per message and it's not wrong since each new message controls the thread.  
	
	ClientSocket( Socket client_socket_arg ) {
		client_socket = client_socket_arg;
		timer = new Timer();
		timer.schedule( new TimerTask() {
			  @Override
			  public void run() {
				  System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
							"Closing client_socket after 2 minutes and a half have passed.");
				  timer_reached = true;	//this variable will provoke the end of the thread of this ClientSocket instance			
			  }
			}, (long) (3 * 60 * 1000) ); //3 minutes is fine
	}
	
	boolean isReadyToStart() {
		try {			
			print_writer = new PrintWriter(client_socket.getOutputStream());
			buffered_reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return((print_writer!= null) && (buffered_reader!= null));
	}
	
	void closeSocket() {
		if( !timer_reached ) {
			System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
					"Cancelling the timer since we're closing the socket gracefully");
			timer.cancel();
		}
		/*attempt_number_to_close++;
		System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
					"closeSocket()       Attempt number " + attempt_number_to_close + " to close mob socket.");
		*/
		
		if( message_operations != null ) {
			message_operations.client = null;
			message_operations = null;
		}				
		System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
				"closeSocket()       Wanting to remove mob printer from array list.");
		//MobPrintListOperations.removePrinter( owner, mob_id, this );
		//if( attempt_number_to_close == 1 ) {
		try {
			PrintListOperations.removePrinter( this );
		} catch( Exception e ) {
			System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
					"MobClientSocket       Caught exception while trying to remove mob printer.");
		}
		//}
		sender_printer = null; //this should be after the call to removePrinter, and it's not necessary by the way
		try {			
			if( print_writer != null ) {
				print_writer.close(); //returns void
				print_writer = null;
				System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						"Closing print_writer.");
            }
			if (buffered_reader != null) {
            	buffered_reader.close(); //returns void
            	System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
            			"Closing buffered_reader.\n");               
                buffered_reader = null;
            }
			if( client_socket.isConnected() && !client_socket.isClosed() ) {
				System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						"Closing client_socket.");
				client_socket.close();				
			}
		} catch (IOException e) {
			System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
					"error closing something in the client socket.");
			e.printStackTrace();
		}
		System.gc();
	}
		
	@Override
	public void run() { //this is about listening and creating a new thread on each received message to analyze it and process it.
		//get a message in a loop 
		
		boolean is_client_socket_disconnected = false;
		System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
    			"We've started to listen to this new mob.");
		do {
			if( buffered_reader != null && client_socket != null ) {
				if( client_socket.isConnected() && !client_socket.isClosed() ) { //useless condition
	                try {
						if (buffered_reader.ready()) {
							System.gc();
						    /*
						     * Instead of the ready method you may use the read method and set the timeout of the socket
	 					     * using setSoTimeout. <- not sure of that. 
							 * But the final decision is: never use read() without preceding it with ready()
							 * Without ready() when the client is not sending anything read() returns -1 and doesn't
							 * block the code execution. 
							 */
							//System.out.println("before read.");
						    int readCharAsInt;
						    int bufferCharIndex = 0;                    
						    String s = "";
						    try {
						        readCharAsInt = buffered_reader.read();/*THIS IS BASIC - IT'S FOR READING THE FIRST 
						        *CHAR AND THE FOLLOWING WHILE() WILL READ THE REST.
						        */
						        //System.out.println("Read char is: " + String.valueOf( readCharAsInt ) );
						        
						        /*The following "while" block reads the WHOLE buffer and processes all the messages that ended
						         * by '\0'. We may have more than one message by the way.  
						         *So I don't think I need to flush it ever more!
						         */
						        while( readCharAsInt != -1 && bufferCharIndex < bufferSize ) { //I don't think that readCharAsInt will ever be -1 since we test with ready() first
						            //SocketConnection.client.getChannel().position(0);
						            bufferCharIndex++;
						            if( ( (char) readCharAsInt == '\0' || (char) readCharAsInt == '\\' ) && s.length() != 0 ) {
						            	//BLOCK_1
						            	//WE ENTER THIS BLOCK_1 AFTER PASSING BY BLOCK_2 AND NEXT TO BLOCK_3
						            	System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						            			"ClientSocket   Getting ready to analyze the message: " + s); 
						            	if( message_operations.analyze( s ) ) {
						            		/* Clearly, analyze will involve consulting the dictionary and will 
						            		 *  update owner and mob_id accordingly.  
						            		 */
						            		if( is_new_client_socket ) {
						            			System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						        						"ClientSocket   This is a new client socket and we're adding a new printer to the array list");
						            			//make a printer
						            			PrintListOperations.makePrinter( this );
						            		} else {
						            			/*All other messages after the first one are expected to have the same owner and sender_id.
						            			 * Of course, it's better to check that! But I'm not doing it here. 
						            			 */
						            		}
						            		is_new_client_socket = false;
						            		
						            		//now sending an ack
						            		System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						    						"ClientSocket    Trying to send an ack to this same mob socket.");
						            		sender_printer.sendMessage( owner + ":" + message_operations.receiver_id + ":" + 
						            				 sender_id + ":ACK\0" );						            		
						            		
						            		//now forwarding the received message
						            		System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						    						"ClientSocket   Trying to forward a message from sender to receiver.");
						            		s = s + String.valueOf('\0');
				            				if( !PrintListOperations.fetchPrintWriterAndSendMessage(
				            						owner, message_operations.receiver_id, s) ) {				            									            					
				            					/* 
				            					 * See discussion about calling closeSocket() in the method's body there.
				            					 */
				            					
				            					/* 
				            					 * We can send back to the mob a message telling him that
				            					 * the mod is disconnected from server.
				            					 * From another hand, the feedback system on the mobile can
				            					 * be enough without this explicit notification. 
				            					 * SO DO NOT DO ANYTHING FOR NOW.
				            					 */						            									            					
				            				} else {
				            					System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
							    						"MobClientSocket   Message sent successfully.");
				            				}
						            	} else {
						            		System.out.println( new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						    						"MobClientSocket   Message " + s + " analysis result was negative." );
						            	}
						                s = "";
						            } else {
						                //now discussing the non-validity of the previous "if"
						            	if( ( (char) readCharAsInt == '\0' || (char) readCharAsInt == '\\' ) && s.length() == 0 ) { 
						                	//BLOCK_5
						                	//THIS BLOCK IS ENTERED ONLY WITHOUT ENTERING BLOCK_2   
						                	
						                	//System.out.println("Read char is null and s has no length");
						                    //break;
						                	s = "";
						                }
						            	if ( (char) readCharAsInt != '\0' && (char) readCharAsInt != '\\' ) { //whether "s" had already a length of 0 or had some characters
						                	//System.out.println("Read char is not null");
						                	//BLOCK_2 
						                	//THIS IS BASIC - IT'S FOR MAKING THE RECEIVED MESSAGE
						                	s = s + String.valueOf((char) readCharAsInt);
						                }						               
						            }
						            /*
						             * N.b: BLOCK_1, BLOCK_2, and BLOCK_5 are all the possibilities, no third case
						             * exists.
						             */ 
						            if( buffered_reader.ready() ) {
						            	//System.out.println("Read char again is: " + String.valueOf( (char) readCharAsInt ) );
						            	//THIS IS BASIC - IT'S FOR READING THE SECOND CHAR TILL THE END
						            	//BLOCK_3
						            	readCharAsInt = buffered_reader.read(); //read() must be preceded with ready()
						            	//IF WE ENTERED BLOCK_3 RIGHT AFTER BLOCK_1 THEN WE MIGHT BE DEALING 
						            	// WITH A NEW MESSAGE IN THE SAME BUFFER. 
						            } else {
						                if (s.length() == 0) {
						                    break;
						                }
						                System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						                		"MobClientSocket   Our message " + s + " is not ending with a null char so we discard it and leave.");						                						               
						                /*BLOCK_4
						                 *THIS IS REACHED AFTER BLOCK_2 AND WITHOUT "DIRECTLY" PASSING 
						                 *THROUGH BLOCK_1, BLOCK_5, OR BLOCK_3. 
						                 *SO THE SEQUENCE WAS: BLOCK_3 -> BLOCK_2 -> BLOCK_4.
						                 * We have no more chars in this buffer now, so 
						                 * do we want to process a message that didn't end with a '\0' ?
						                 * I guess no.  
						                 */
						                s = "";
						                break;
						            }
						        }
						    } catch (Exception e) {
						        e.printStackTrace();
						        System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						        		"MobClientSocket   error in reading");
						    }
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
								"MobClientSocket   error in MobClientSocket being ready");
						e.printStackTrace();
					}
				} else { //client_socket is no more connected, so we need to close and free everything related to it 
					is_client_socket_disconnected = true;				
				}
            } else {
            	is_client_socket_disconnected = true;
            }
				
			if( is_client_socket_disconnected ) {
				System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						"MobClientSocket   Mob client socket is considered diconnected.");
				break; //outside the loop, everything related to the socket will be closed.
			}
					
			try {
				//System.out.println("Sleeping for 100 milliseconds");
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println(new Timestamp(System.currentTimeMillis()) + " BufferListeners.java " +
						"MobClientSocket   Error in sleeping for 100 milliseconds");
				e.printStackTrace();
			}
			
		} while( !timer_reached && 
				 !Setup.keyboard_input.equals("stop") && !Setup.force_stop );
		
		closeSocket();  	
	}
	

}