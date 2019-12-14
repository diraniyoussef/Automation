import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

	/*
	 * The following 2 array lists,
	 * ArrayList<MobPrinter> mob_list and ArrayList<ModPrinter> mod_list
	 *  are for the client_socket's (differentiated by server port number) that are valid
	 * to send (not receive) messages through them. Simply put, it's the most recently connected socket per mobile or module.
	 */
	
/*
 * Although I think using "synchronized" keyword is enough, here is further reading about multi-threading 
 * https://stackoverflow.com/questions/2104719/why-do-we-need-synchronized-arraylists-when-we-already-have-vectors
 * https://docs.oracle.com/javase/6/docs/api/java/util/Collections.html#synchronizedList(java.util.List)
 */
class PrintListOperations {	
	static volatile ArrayList<Printer> list = new ArrayList<Printer>();
	
	private static boolean updateSocketAndPrintWriter( String owner, String id, 
			Socket client_socket, PrintWriter print_writer, ClientSocket client ) {
		/* According to owner and id, this method fetches the printer and will
		 * update its client_socket and print_writer.
		 */
		/* I won't close the print_writer of the previous element although it's safe because of "synchronized"
		 * since no need to; it will close when the socket closes.
		 */ 
		System.out.println( new Timestamp( System.currentTimeMillis() ) + " PrintListOperations.java " +
					"PrintListOperations updateSocketAndPrintWriter"); 
		boolean is_printer_updated = false;
		if( list != null ) {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"MobPrintListOperations updateSocketAndPrintWriter   list initially has " + list.size() + " elements."); 			
			for( Printer printer : list ) { 
				System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
						"PrintListOperations updateSocketAndPrintWriter  iterating a printer in list  Info to find are " +
						"owner: " + owner + " and id: " + id + 
						". Printer in iteration is   owner: " + printer.owner + ", id: " + printer.id);
				if( printer.owner.equalsIgnoreCase(owner) && printer.id.equalsIgnoreCase(id) ) {	
					//Entering here means a previous socket to client_socket was already connected 
					System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
							"PrintListOperations updateSocketAndPrintWriter We found the particular printer to update, "
							+ "no need to create a new printer");
					//now updating the socket and printWriter
					printer.client_socket = client_socket;
					printer.print_writer = print_writer;
					printer.client = client; //this is useful to not let a dead socket remove an updated printer
					client.sender_printer = printer;
					is_printer_updated = true;
					break;
				}
			}
		} else {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"PrintListOperations Odd; list is null!");
		}
		return is_printer_updated;		
	}

	private static void createPrinter( String owner, String id, 
			Socket client_socket, PrintWriter print_writer, ClientSocket client ) {
		if( list != null ) {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"PrintListOperations    createPrinter   list initially has " + list.size() + " elements.");
		}
		System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
				"PrintListOperations        createPrinter creating a printer in list. " +
				"Owner: " + owner + ", id: " + id);		
		Printer printer = new Printer(owner, id, client_socket, print_writer, client);		/*it is ok to declare mob_printer
			in this method since it will be referenced by other objects on still alive threads.*/
		System.gc();
		client.sender_printer = printer;
		list.add(printer);
	}
	
	static synchronized void makePrinter( ClientSocket client ) {
		//search within the array list for the corresponding printer 
		// if it exists or not
		if( !updateSocketAndPrintWriter( client.owner, client.sender_id,
					client.client_socket, client.print_writer, client ) ) { //didn't find it
			createPrinter( client.owner, client.sender_id,
					client.client_socket, client.print_writer, client );
		}
	}
	
	/*
	static synchronized void removePrinter( String owner, String mob_id, MobClientSocket mob_client_socket ) { //mob printer to be removed
		//search within the array list for the corresponding printer and close the socket and printwriter
		
		if( mob_list != null ) {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"removePrinter   mob_list initially has " + mob_list.size() + " elements.");
			for( MobPrinter printer : mob_list ) { 
				System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
						"removePrinter  Iterating a printer in mob_list  Info to find are " +
						"owner: " + owner + " and mob_id: " + mob_id + 
						" Printer in iteration is   owner: " + printer.owner + ", mob_id: " + printer.mob_id);
				if( printer.owner.equalsIgnoreCase(owner) && printer.mob_id.equalsIgnoreCase(mob_id) && 
						printer.mob_client.equals( mob_client_socket ) ) {			
					//now removing the socket and printWriter
					System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
							"            Printer has been found and removed.");
					printer.mob_client = null;
					mob_list.remove(printer);
					break;
				}
			}
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"removePrinter   Nothing was done actually.");
		} else {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"Odd; mob_list is null!");
		}
	}
*/
	static synchronized void removePrinter( ClientSocket client ) { //mob printer to be removed
		if( list != null && client != null ) {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"PrintListOperations        removePrinter   list initially has " + list.size() + " elements.");
			if(  client.sender_printer != null ) {	
				if(  client.sender_printer.client.equals( client ) ) {	//this means that the printer has NOT been updated to a new client		
					//now removing the socket and printWriter					
					client.sender_printer.client = null;
					if( list.contains( client.sender_printer ) ) {
						list.remove( client.sender_printer );						
					}
					System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
							"PrintListOperations     Printer is removed.");
				}
			}
		} else {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"PrintListOperations       Odd; either list is null or client is null!");
		}
	}
	
	static synchronized boolean fetchPrintWriterAndSendMessage( String owner, String receiver_id, String message ) {
		if( list != null ) {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"PrintListOperations  fetchPrintWriterAndSendMessage   list initially has " + list.size() + " elements.");
			for( Printer printer : list ) { 
				System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
						"PrintListOperations           fetchPrintWriterAndSendMessage      Iterating a printer in list  Info to find are " +
						" Owner: " + owner + " and receiver_id: " + receiver_id + 
						"         Printer in iteration is   owner: " + printer.owner + ", id: " + printer.id);
				if( printer.owner.equalsIgnoreCase(owner) && printer.id.equalsIgnoreCase(receiver_id) ) { //this test is very important since the receiver might not be online yet for his own reasons
					if( printer.sendMessage( message ) ) {
						return true;
					} else {
						return false;
					}
				}
			}			
			System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
					"PrintListOperations              fetchPrintWriterAndSendMessage   "
					+ "Failed to send a message because couldn't find the correct printer.");
			return false;
		}
		System.out.println( new Timestamp(System.currentTimeMillis()) + " PrintListOperations.java " +
				"PrintListOperations                    fetchPrintWriterAndSendMessage   Failed to send a message because list is null.");		
		return false;		
	}
}
