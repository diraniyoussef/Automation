
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

class MessageOperations {
	/*it's ok that these static methods be accessed from many threads as long as no actual shared object is being
	 * modified, which is not the case.
	 */
	ClientSocket client; //this will give an access to the owner and mob_id strings
	//String message;
	String receiver_id = ""; //mod_id and message_content change in each new incoming message to the mod socket
	String message_content = ""; //It is safe to use mod_id and message_content in a MobMessageOperations
	// object because we don't do multithreading inside a client socket thread.
	
	MessageOperations( ClientSocket client ) {
		this.client = client;
	}
	
	/*analyze() is a check of intelligibility 
	 * It will fetch the id's of the message and compare them with those of the dictionary. If the 
 	 * id's exist then it will instantiate "owner" and "mob_id" of mob_client object. 
	 * But I'm not considering this as enough to return true.
	 * And I'm comparing mod_id with the dictionary
	 */  
	private boolean checkTableExistence( String owner ) {
		//there was a specific method example (getCapacityGreaterThan) here http://www.sqlitetutorial.net/sqlite-java/select/ but I won't be using it
		String sql = "SELECT 1 FROM sqlite_master WHERE type='table' AND name='" + owner + "' LIMIT 1";  
		/* I didn't use SELECT EXISTS because I can still test it reliably without entering into the details of the
		 * result of my query's execution.
		 * The query I used either returns a record or not. Whilst SELECT EXISTS always returns a record of which 
		 * the value is either 1 or 0.
		 */
        try( Statement stmt  = Setup.conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql) ){           
            if (rs.next()) {
                System.out.println("Table Found!");
                return true;
            } else {
            	System.out.println("Table not found.");
            	return false;
            }
            
        } catch (SQLException e) {
        	e.printStackTrace();
            System.out.println(e.getMessage()); /*should not get any exception... Unless somehow the database was
            * like deleted or something, so I guess I will be getting an exception. Through this exception I have 
            * to terminate the program!
            */
            System.out.println("Something was wrong while trying to check if the owner table exists or not.");
            Setup.force_stop = true;
            return false;
        }
	}
	
	private boolean checkSignature (String owner, String entity1, String entity2) {
		String sql = "SELECT 1 FROM " + owner + " WHERE (Entity1 = '" + entity1 + "' AND Entity2 = '" +
				entity2 + "') OR (Entity1 = '"+ entity2 + "' AND Entity2 = '" + entity1 + "')";
		try( Statement stmt  = Setup.conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql) ){           
            if (rs.next()) {            
                System.out.println("Signature Found!");
                return true;
            } else {
            	System.out.println("Signature not found.");
            	return false;
            }
        } catch (SQLException e) {
        	e.printStackTrace();
            System.out.println(e.getMessage()); /*Attention: It is possible to get an exception here if
            * any of the 2 fields Entity1 and Entity2 weren't in the table.
            * I'm not checking whether the 2 fields are there or not, although they must be there. No reason
            * no to be! Anyway, it's the exception that I'm relying on now. 
            */ 
            System.out.println("Something was wrong while trying to check if the signature was right or not." + 
            		"Please check whether the 2 columns Entity1 and Entity2 are typed correctly in the table " + owner + "!!!");
            Setup.force_stop = true;
            return false;
        }
	}
	
	boolean analyze( String message ) { 			
		int i1 = message.indexOf(":");
		if (i1 == -1) {
			System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
	    			"MobMessageOperations   analyzing message: Couldn't find first semicolon!");
			return false;
		}
		int i2 = 0;		
		try {
			String owner = message.substring( 0, i1 );
			if( owner.equals("") ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      owner field is empty!");
				return false;
			}
			
			//now check if the table of this owner exists in the db
			if( !checkTableExistence( owner ) ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      owner table does not exist in the db!");
				return false;			
			}			
			
			i2 = message.indexOf(":", i1 + 1);
			if( i2 == -1 ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      Couldn't find second semicolon!");
				return false;
			}
			String sender_id = message.substring(i1 + 1, i2);
			if( sender_id.equals("") ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      sender field is empty!");
				return false;
			}
		
			i1 = i2;
			i2 = message.indexOf(":", i1 + 1);
			if( i2 == -1 ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      couldn't find third semicolon!");
				return false;
			}
			String receiver_id1 = message.substring(i1 + 1, i2);
			if( receiver_id1.equals("") ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      receiver field does not exist!");
				return false;
			}

			//let's check whether the sender and receiver couple exist in the db
			if( !checkSignature( owner, sender_id, receiver_id1) ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"analyze(...)      record corresponding to the signature does not exist in the table in the db!");
				return false;
			}			
			
			i2 = message.lastIndexOf(":"); //not necessary but fine
			message_content = message.substring(i2 + 1);
			if( message_content.equals("") ) {
				System.out.println( new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
		    			"MobMessageOperations   analyzing message: message field is empty!");
				return false;
			}
			/*
			 * By the way, even if mob_client.owner, mob_client.mob_id, and mod_id were set before it won't make 
			 * a difference but politely it's better to set them when the message is considered right.
			 */
			//if( mob_client.is_new_client_socket ) {
			client.owner = owner;
			client.sender_id = sender_id;
			//}
			receiver_id = receiver_id1;
			System.out.println(new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
					"MobMessageOperations  analyze(...)  message is correctly read");
			return true;			
		}
		catch( Exception e ) { //It throws IndexOutOfBoundsException If the beginIndex is less than zero OR beginIndex > endIndex OR endIndex is greater than the length of String.
			e.printStackTrace();
	        System.out.println(new Timestamp(System.currentTimeMillis()) + " MessageOperations.java " +
	        		"MobMessageOperations   analyzing message: Exception is thrown - not sure if received message has ':'");
	        return false;
		}	
	}
}
