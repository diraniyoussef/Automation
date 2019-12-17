import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

/*This is about forwarding the message from mob to mod and vice versa.
 * It also involves updating and reading the arraylists.
 */

public class Printer {
	String owner;
	Socket client_socket;
	PrintWriter print_writer;
	String id;
	ClientSocket client;
	
	Printer( String owner, String id, Socket client_socket, PrintWriter print_writer, ClientSocket client ) {				
		this.owner = owner;
		this.client_socket = client_socket; 
		this.print_writer = print_writer;
		this.id = id;			
		this.client = client;
		/*
		 * client_socket_arg is probably not needed if print_writer.write( message ); 
		 * can throw exception in case e.g. the socket was closed. Anyway keeping it and 
		 * testing the socket if closed or not makes no harm.
		 */		
		System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " +
				"a new printer has just been assigned");
	}
	
	boolean sendMessage( String message ) {
		boolean successful = true;
		if( client_socket != null ) {
			/*
			 * Do we need to close the socket in case the socket was ruined or unexpetedly 
			 * closed during sendMessage() operation? No, because the BufferListener of the socket in question, which 
			 * is running in its own thread, will know that the socket connection is bad and will close everything.   
			 */
			System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
					"sendMessage    message is " + message );
			if( print_writer!= null && client_socket.isConnected() && !client_socket.isClosed() ) {								
				PrintWriterThread print_writer_thread = new PrintWriterThread( print_writer, message );
				System.gc();
				try {
					System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
							"sendMessage starting print_writer_thread.");
					print_writer_thread.start(); //start() can be outside the try block but I think it's better inside. 				
					/*System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
							"sendMessage    Now preparing to join.");
					print_writer_thread.join();					
					System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
							"sendMessage  " + message + "  should have been sent by now after joining is done.");
					*/
				} catch (Exception e) { //it may be InterruptedException (maybe if there was an error in joining) or IOException if the socket was closed
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " +
							"sendMessage   Exception!");
					successful = false;
				}
				print_writer_thread = null;
			} else {
				System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
						"sendMessage   message not sent because the client_socket or print_writer are closed?!");
				successful = false;
			}
		} else {
			System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
					"sendMessage   message not sent because client_socket is null!");
			successful = false;
		}
		System.out.println(new Timestamp(System.currentTimeMillis()) + " Printers.java " + 
				"sendMessage    message successfully sent.");
		return successful;				
	}
}

class PrintWriterThread extends Thread {
	PrintWriter print_writer;
	String message;
	
	PrintWriterThread( PrintWriter print_writer_arg, String message_arg ) {		
		print_writer = print_writer_arg;
		message = message_arg;
	}
	
	@Override
	public void run() {
		print_writer.write( message );
		print_writer.flush();		
	}
		
}