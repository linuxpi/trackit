package com.lpu.android.trackme.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.*;
import java.util.ArrayList;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import static com.lpu.android.trackme.servlet.Constants.*;

@WebServlet("/AppServer")
public class AppServer extends HttpServlet {

	String preUnKey = null;
	String share = null;
	String regId = null;
	String unKey = null;;
	String locString = null;
	String serial = null;
	
	Connection conn = null;
	Statement st = null;

	public AppServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		 share = request.getParameter("shareRegId");
		 regId = request.getParameter("regId");
		 unKey = request.getParameter("unKey");
		 locString = request.getParameter("locString");
		 serial = request.getParameter("serial");
		 
		 System.out.println("Unique key :: " + unKey);
		
		initializeDatabaseParams();

		if ((share != null && !share.isEmpty())
				&& (unKey != null && !unKey.isEmpty())  && (share.equals("1"))) {
			
			if ((serial != null && !serial.isEmpty())) {
				
				//get the previous unKey using serial
				preUnKey = getPrevUnKeyFromSerial(serial);
				
				System.out.println("serial is not null");
				fulFillUserRequest(serial);
			}else if((regId != null && !regId.isEmpty())){
				//first time user
				//add entry to databse and get the serial key
				System.out.println("serial is null");
				ResultSet rs;
				String newSerial=null;
				try {
					rs = st.executeQuery("SELECT serial_num FROM users WHERE regId='"+regId+"';");
					if(rs.next()){
						//not a new user
						newSerial= Integer.toString(rs.getInt(1));
						System.out.println("Serial key from DB :: " + newSerial);
						
						//get the previous unKey using serial
						preUnKey = getPrevUnKeyFromSerial(newSerial);
						
						updateUnKeyWithLocation(newSerial, unKey, locString);
					}else{
						newSerial = addNewUserToDatabase(regId,unKey);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(newSerial!=null){
					
					fulFillUserRequest(newSerial);
				}else{
					System.out.println("Serial is null");
				}
				
			}
		}else if((share != null && !share.isEmpty())
				&& (unKey != null && !unKey.isEmpty()) && (serial != null && !serial.isEmpty()) &&(regId != null && !regId.isEmpty())  && (share.equals("2"))){
			System.out.println("Request received for location");
			String loc = getLocationFromSerial(serial);
			
			//send location data to user
			sendLocationToSingleUser(regId,loc);
		}
		
	}

	private void sendLocationToSingleUser(String regId2, String loc) {
		// TODO Auto-generated method stub
		
		Result result = null;
		
		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		Message message=null;
		message = new Message.Builder().timeToLive(30)
				.delayWhileIdle(true).addData(MESSAGE_KEY, "geo"+loc)
				.build();
		System.out.println(message.toString());
		
		try {
			result = sender.send(message, regId2, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private String getLocationFromSerial(String serial2) {
		// TODO Auto-generated method stub
		
		System.out.println("SELECT location FROM users WHERE serial_num="+serial2);
		
		String query = "SELECT location FROM users WHERE serial_num="+serial2;
		String loc;
		try {
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				loc = rs.getString(1);
				if(loc.equals("null") || loc.isEmpty()){
					return null;
				}
				else{
					return loc;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

	private String addNewUserToDatabase(String regId2,String unKey2) {
		// TODO Auto-generated method stub
		String query = "INSERT INTO users (unKey,regId,location) VALUES ('"+unKey2+"','"+regId2+"','"+locString+"');";
		int result=0;
		try {
			result = st.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result!=0){
			return getSerialFromRegId(regId2);
			
		}
		
		return null;
	}

	private String getSerialFromRegId(String regId2) {
		// TODO Auto-generated method stub
		String query = "SELECT serial_num FROM users WHERE regId='"+regId2+"';";
		try {
			ResultSet rs = st.executeQuery(query);
			return Integer.toString(rs.getInt(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void fulFillUserRequest(String serial) {
		System.out.println("Fulfilling the user req");
		System.out.println("serial :: " + serial + "pre Un KEy :: " + preUnKey + "un kkey :: "+unKey);
		if (unKey.equals(UNACTIVE_USER)) {
			if (preUnKey != null) {
				if (preUnKey.equals(PASSIVE_USER)) {
					//user was in passive mode
					//just update the parameter in DB 
					//nothing else required

					boolean result = updateUnKeyWithLocation(serial, unKey,locString);
					if (result) {
						//success
					} else {
						//log error or retry.
					}

				} else if (preUnKey.equals(UNACTIVE_USER)) {
					//should'nt be possible
				} else {
					//the user was in active mode
					//send the minus of unKey to all passive users to indicate the update
					//then update the database
					
					System.out.println("Sending remove request to all passive users");
					sendRemoveRequestToAllPassive(serial, preUnKey);
					
					updateUnKeyWithLocation(serial, unKey,locString);

				}
			}
		} else if (unKey.equals(PASSIVE_USER)) {
			//There can be 2 cases
			//1. active to passive, or
			//2. directly passive
			//in both cases we need to do the same thing as active to passive is handles by the app
			//when transition from active to passive 
			//2 requests are sent on making the unkey=0 other unkey=100000

			//send the list of active users to device
			String passRegID = getRegIdFromSerial(serial);

			sendActiveListToPassive(passRegID);

		} else if (isActiveUnKey(unKey)) {
			//no need to check previous unkey
			//update the database and notify passive users

			updateUnKeyWithLocation(serial, unKey,locString);

			System.out.println("serail :: " + serial + "unKey ::" +unKey);
			sendAddRequestToAllPassive(serial,unKey);

		}
	}

	private void sendAddRequestToAllPassive(String serial2,String unKey2) {
		// TODO Auto-generated method stub
		try {
			ResultSet rsPassiveUsers = st
					.executeQuery(PASSIVE_USER_LIST_QUERY);
			System.out
			.println("add unique key req to send to all passive users is : "
					+ unKey);

			String regID;		
			Result result = null;
		
			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message=null;
			message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).addData(MESSAGE_KEY, "+"+unKey2+"|"+serial2)
					.build();
			
			System.out.println("Sending data >>>>> :: +" +unKey2+"|"+serial2);
			
			while (rsPassiveUsers.next()) {
				regID = rsPassiveUsers.getString(1);
				
				System.out.println("Reg ID :: " + regID);
				
				if (regID.length() > 10) { // to check if not a sample
											// reg entered by developer
											// for testing purpose
					// safe to send message to passive device
					if(message!=null){
						try {
							result = sender.send(message, regID, 1);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						System.out.println("message body null");
					}
					System.out.println("result from gcm ::" + result);
				}
			}
			rsPassiveUsers.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isActiveUnKey(String unKey2) {
		// TODO Auto-generated method stub
		int i = Integer.parseInt(unKey2);
		if(i>0 && i<100000){
			return true;
		}
		return false;
	}

	private void sendActiveListToPassive(String passRegID) {
		// TODO Auto-generated method stub
		
		ResultSet rsActiveUsers;
		try {
			rsActiveUsers = st.executeQuery(ACTIVE_USERS_UNKEY_LIST_QUERY);
			ArrayList<String> activeUserList = new ArrayList<String>();
			
			while(rsActiveUsers.next()){
				activeUserList.add(rsActiveUsers.getString(1)+"|"+rsActiveUsers.getInt(1));
			}
			
			if(activeUserList.size()>0){
				Sender sender = new Sender(GOOGLE_SERVER_KEY);
				Message message = new Message.Builder().timeToLive(30)
						.delayWhileIdle(true).addData(MESSAGE_KEY, activeUserList.toString())
						.build();
				System.out.println("Sending data ::" +  activeUserList.toString());
				System.out.println("reg id ::" +  passRegID);
				Result result = sender.send(message, passRegID, 1);
				System.out.println("result from gcm ::" + result);
			}else{
				//no active user!
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO: handle exception
		}
		
		
	}

	private String getRegIdFromSerial(String serial2) {
		// TODO Auto-generated method stub
		String query = "SELECT regId FROM users WHERE serial_num="+serial2+";";
		try {
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				return rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void sendRemoveRequestToAllPassive(String serial2, String unKey2) {
		// TODO Auto-generated method stub
		
//		String serial2 = getSerialFromRegId(regId2);
		
		try {
			ResultSet rsPassiveUsers = st
					.executeQuery(PASSIVE_USER_LIST_QUERY);
			System.out
			.println("remove unique key req to send to all passive users is : "
					+ unKey);

			String regID;		
			Result result = null;
		
			Sender sender = new Sender(GOOGLE_SERVER_KEY);
			Message message=null;
			message = new Message.Builder().timeToLive(30)
					.delayWhileIdle(true).addData(MESSAGE_KEY, "-"+unKey2+"|"+serial2)
					.build();
			
			System.out.println("Sending data >>>>> :: -" + unKey2);
			
			while (rsPassiveUsers.next()) {
				regID = rsPassiveUsers.getString(1);
				
				System.out.println("Reg ID :: " + regID);
				
				if (regID.length() > 10) { // to check if not a sample
											// reg entered by developer
											// for testing purpose
					// safe to send message to passive device
					if(message!=null){
						try {
							result = sender.send(message, regID, 1);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						System.out.println("message body null");
					}
					System.out.println("result from gcm ::" + result);
				}
			}
			rsPassiveUsers.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private boolean updateUnKeyWithLocation(String serial2, String unKey2,String locString2) {
		// TODO Auto-generated method stub
		String query = "UPDATE users SET unKey='"+unKey2+"',location='"+locString2+"' WHERE serial_num="+serial2+";";
		int result = 0;
		try {
			result = st.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result==0){
			return false;
		}else{
			return true;
		}
	}

	private void initializeDatabaseParams() {
		
		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			
			conn = DriverManager.getConnection("jdbc:mysql://"
					+ MYSQL_DATABASE_HOST + ":" + MYSQL_DATABASE_PORT + "/"
					+ MYSQL_DATABASE_NAME, "admin7z5GufY", "uCn8Qi2Zz91S");
			System.out.println("Database connected");

			st = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getPrevUnKeyFromSerial(String serial2) {
		// TODO Auto-generated method stub
		String query = "SELECT unKey FROM users WHERE serial_num="+serial2;
		try {
			ResultSet rs = st.executeQuery(query);
			if(rs.next()){
				return rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}









//	private void updateActiveListOfPassiveUsers(Statement st, String unKey,
//			String regId, String serial) {
//		// TODO Auto-generated method stub
//		try {
//			// String
//			// selectUnkey="SELECT unKey FROM users WHERE regId='"+regId+"';";
//			//
//			// System.out.println(selectUnkey);
//			// ResultSet singleUnKey = st.executeQuery(selectUnkey);
//			// String removeUnKey=null;
//			// if(singleUnKey.next()){
//			// removeUnKey = singleUnKey.getString(1);
//			// }
//			//
//			// singleUnKey.close();
//
//			ResultSet rsPassiveUsers = st
//					.executeQuery("SELECT regID FROM users WHERE flag=1 AND unKey=100000"); // passive
//																							// users
//
//			System.out.println("Reached the initial checkpoint");
//			System.out.println("unique key to send to all passive users is : "
//					+ unKey);
//
//			ArrayList<String> arRegIds = new ArrayList<>();
//			String regID;
//
//			Result result = null;
//
//			Sender sender = new Sender(GOOGLE_SERVER_KEY);
//			Message message = null;
//			if (preUnKey != null) {
//				message = new Message.Builder().timeToLive(30)
//						.delayWhileIdle(true)
//						.addData(MESSAGE_KEY, "-" + preUnKey + "|" + serial)
//						.build();
//			}
//			System.out.println("Sending data >>>>> =" + preUnKey);
//			while (rsPassiveUsers.next()) {
//				System.out.println("data :" + rsPassiveUsers.getString(1));
//				// arRegIds.add(rsPassive.getString(1));
//				regID = rsPassiveUsers.getString(1);
//				if (regID.length() > 10) { // to check if not a sample
//											// reg entered by developer
//											// for testing purpose
//					// safe to send message to passive device
//					if (message != null) {
//						try {
//							result = sender.send(message, regID, 1);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					} else {
//						System.out.println("mesage body null");
//					}
//					System.out.println("result from gcm ::" + result);
//				}
//			}
//			rsPassiveUsers.close();
//		} catch (SQLException se) {
//			se.printStackTrace();
//		}
//	}
//}


//int flagKey;
//// if unKey is zero then user is stopping the broadcast
//if (unKey.equals("0")) {
//	flagKey = 0;
//} else {
//	flagKey = 1;
//}
//
//
//try {
//
//	String insertQuery = "INSERT INTO users (flag,unKey,regId,location) VALUES ("
//			+ Integer.toString(flagKey)
//			+ ","
//			+ Integer.parseInt(unKey) + ",'" + regId + "','"+locString+"');";
//	String updateQuery = "UPDATE users SET unKey="
//			+ Integer.parseInt(unKey) + ",flag="
//			+ Integer.toString(flagKey) + ",location='" + locString + "' WHERE regId='" + regId
//			+ "';";
//	
//	System.out.println("UPDATE>>>>>>>>>>>>>>."+updateQuery);
//	
//	String selectUnkey="SELECT unKey FROM users WHERE regId='"+regId+"';";
//
//	// Context initCtx = new InitialContext();
//	// ////
//	// Context envCtx = (Context) initCtx.lookup("java:comp/env");
//	// ////
//	// DataSource ds = (DataSource)
//	// envCtx.lookup("jdbc/trackMeUsers");
//	// //
//	// conn = ds.getConnection();
//
//	try {
//		Class.forName("com.mysql.jdbc.Driver");
//	} catch (ClassNotFoundException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	System.out.println("Driver loaded");
//
//	// Connect to a database
//	
//	int resUpdate;
//
//	if (flagKey == 1) {
//		// check if the entry is present in the database for this
//		// user or not
//		ResultSet rsNewUserCheck = st
//				.executeQuery("SELECT flag FROM users WHERE regId='"
//						+ regId + "';");
//		if (rsNewUserCheck.next()) {
//			// the user is already present in the database.
//			
//			ResultSet singleUnKey = st.executeQuery(selectUnkey);
////			String removeUnKey=null;
//			if(singleUnKey.next()){
//				preUnKey = singleUnKey.getString(1);
//			}
//			
//			singleUnKey.close();
//			
//			resUpdate = st.executeUpdate(updateQuery);
//			
//		} else {
//			// new user - first time entry
//			resUpdate = st.executeUpdate(insertQuery);
//		}
//		rsNewUserCheck.close();
//	} else {
//		// update only possible!
//		//first get the previous unKey
//		System.out.println(selectUnkey);
//		ResultSet singleUnKey = st.executeQuery(selectUnkey);
//		String removeUnKey=null;
//		if(singleUnKey.next()){
//			removeUnKey = singleUnKey.getString(1);
//		}
//		
//		singleUnKey.close();
//		System.out.println("uddating active to unactive");
//		resUpdate = st.executeUpdate(updateQuery);
//		
//		//send request to all passive users to remove the key
//		ResultSet rsPassiveUsers = st
//				.executeQuery("SELECT regID FROM users WHERE flag=1 AND unKey=100000"); // passive
//																						// users
//
//		System.out.println("Reached the initial checkpoint");
//		System.out
//				.println("unique key to send to all passive users is : "
//						+ unKey);
//
//		ArrayList<String> arRegIds = new ArrayList<>();
//		String regID;
//
//		Result result = null;
//
//		Sender sender = new Sender(GOOGLE_SERVER_KEY);
//		Message message=null;
//		if(removeUnKey!=null){
//		message = new Message.Builder().timeToLive(30)
//				.delayWhileIdle(true).addData(MESSAGE_KEY, "-"+removeUnKey)
//				.build();
//		}
//		System.out.println("Sending data >>>>> =" + removeUnKey);
//		while (rsPassiveUsers.next()) {
//			System.out.println("data :"
//					+ rsPassiveUsers.getString(1));
//			// arRegIds.add(rsPassive.getString(1));
//			regID = rsPassiveUsers.getString(1);
//			if (regID.length() > 10) { // to check if not a sample
//										// reg entered by developer
//										// for testing purpose
//				// safe to send message to passive device
//				if(message!=null){
//					result = sender.send(message, regID, 1);
//				}else{
//					System.out.println("mesage body null");
//				}
//				System.out.println("result from gcm ::" + result);
//			}
//		}
//		rsPassiveUsers.close();
//	}
//	
//	String getSerailQuery = "SELECT serial_num FROM users WHERE regId='"+regId+"';";
//	ResultSet rsSerial = st.executeQuery(getSerailQuery);
//	if(rsSerial.next()){
//		serial = Integer.toString(rsSerial.getInt(1));
//		System.out.println("Got serial :: " + serial);
//	}
//
//	// send message to passive users about the update
//	// if and only if new active user i.e. flag=1 AND unKey>0 AND
//	// unKey <100000
//
//	// passive users are those who have flag as 1 and unKey as
//	// 100000
//	// active users are those who have flag as 1 and unKey > 0
//	// users neither in active nor in passive mode are with unKey=0
//	// and flag=0;
//
//	if (!unKey.equals(UNACTIVE_USER) && !unKey.equals(PASSIVE_USER)) {
//		// now notify all the passive users
//		ResultSet rsPassiveUsers = st
//				.executeQuery("SELECT regID FROM users WHERE flag=1 AND unKey=100000"); // passive
//																						// users
//
//		System.out.println("Reached the initial checkpoint");
//		System.out
//				.println("unique key to send to all passive users is : "
//						+ unKey);
//
//		ArrayList<String> arRegIds = new ArrayList<>();
//		String regID;
//
//		Result result;
//
//		Sender sender = new Sender(GOOGLE_SERVER_KEY);
//		Message message = new Message.Builder().timeToLive(30)
//				.delayWhileIdle(true).addData(MESSAGE_KEY, unKey+"|"+serial)
//				.build();
//
//		while (rsPassiveUsers.next()) {
//			System.out.println("data :"
//					+ rsPassiveUsers.getString(1));
//			// arRegIds.add(rsPassive.getString(1));
//			regID = rsPassiveUsers.getString(1);
//			if (regID.length() > 10) { // to check if not a sample
//										// reg entered by developer
//										// for testing purpose
//				// safe to send message to passive device
//				result = sender.send(message, regID, 1);
//				System.out.println("result from gcm ::" + result);
//			}
//		}
//		rsPassiveUsers.close();
//		//
//	}else if(!unKey.equals(UNACTIVE_USER) && unKey.equals(PASSIVE_USER)){
//		//passive user online - fetch a list of all the active users and send
//		
//		updateActiveListOfPassiveUsers(st,unKey,regId,serial);
//		
//		ResultSet rsActiveUsers = st.executeQuery(ACTIVE_USERS_UNKEY_LIST_QUERY);
//		ArrayList<String> activeUserList = new ArrayList<String>();
//		
//		while(rsActiveUsers.next()){
//			activeUserList.add(Integer.toString(rsActiveUsers.getInt(1))+"|"+serial);
//		}
//		
//		if(activeUserList.size()>0){
//			Sender sender = new Sender(GOOGLE_SERVER_KEY);
//			Message message = new Message.Builder().timeToLive(30)
//					.delayWhileIdle(true).addData(MESSAGE_KEY, activeUserList.toString())
//					.build();
//			System.out.println("Sending data ::" +  activeUserList.toString());
//			Result result = sender.send(message, regId, 1);
//			System.out.println("result from gcm ::" + result);
//		}
//	}
//	
//	
//} catch (SQLException se) {
//	se.printStackTrace();
//}
//
//}
//
//else if((share != null && !share.isEmpty())
//	&& (serial != null && !serial.isEmpty())
//	&& (unKey != null && !unKey.isEmpty())  && (share.equals("2"))){
//
////location
//
//}
//
