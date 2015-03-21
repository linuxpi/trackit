package com.lpu.android.trackme.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

import javax.sql.*;
import javax.naming.*;

import com.google.android.gcm.server.InvalidRequestException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.lpu.android.trackme.data.UserData;

/**
 * Servlet implementation class AppServer
 */
@WebServlet("/AppServer")
public class AppServer extends HttpServlet {
	public static final String ACTIVE_USERS_LIST_QUERY = "SELECT unKey FROM users WHERE flag=1 AND unKey>0 AND unKey<100000";

	private static final long serialVersionUID = 1L;

	public static final String MYSQL_USERNAME = System
			.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
	public static final String MYSQL_PASSWORD = System
			.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
	public static final String MYSQL_DATABASE_HOST = System
			.getenv("OPENSHIFT_MYSQL_DB_HOST");
	public static final String MYSQL_DATABASE_PORT = System
			.getenv("OPENSHIFT_MYSQL_DB_PORT");
	public static final String MYSQL_DATABASE_NAME = "trackMeUsers";

	public static final String UNACTIVE_USER = "0";
	public static final String PASSIVE_USER = "100000";
	String preUnKey=null;

	// Put your Google API Server Key here
	private final String GOOGLE_SERVER_KEY = "AIzaSyBhcUIKx-dlfBQxNE9N0SL9R7F_otaxR-0";
	static final String MESSAGE_KEY = "message";

	public AppServer() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		String share = request.getParameter("shareRegId");
		String regId = request.getParameter("regId");
		String unKey = request.getParameter("unKey");
		String locString = request.getParameter("locString");
		String serial = request.getParameter("serial");

		if ((share != null && !share.isEmpty())
				&& (regId != null && !regId.isEmpty())
				&& (unKey != null && !unKey.isEmpty())  && (share.equals("1"))) {

			int flagKey;
			// if unKey is zero then user is stopping the broadcast
			if (unKey.equals("0")) {
				flagKey = 0;
			} else {
				flagKey = 1;
			}

			Connection conn = null;

			try {

				String insertQuery = "INSERT INTO users (flag,unKey,regId,location) VALUES ("
						+ Integer.toString(flagKey)
						+ ","
						+ Integer.parseInt(unKey) + ",'" + regId + "','"+locString+"');";
				String updateQuery = "UPDATE users SET unKey="
						+ Integer.parseInt(unKey) + ",flag="
						+ Integer.toString(flagKey) + ",location='" + locString + "' WHERE regId='" + regId
						+ "';";
				
				System.out.println("UPDATE>>>>>>>>>>>>>>."+updateQuery);
				
				String selectUnkey="SELECT unKey FROM users WHERE regId='"+regId+"';";

				// Context initCtx = new InitialContext();
				// ////
				// Context envCtx = (Context) initCtx.lookup("java:comp/env");
				// ////
				// DataSource ds = (DataSource)
				// envCtx.lookup("jdbc/trackMeUsers");
				// //
				// conn = ds.getConnection();

				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Driver loaded");

				// Connect to a database
				conn = DriverManager.getConnection("jdbc:mysql://"
						+ MYSQL_DATABASE_HOST + ":" + MYSQL_DATABASE_PORT + "/"
						+ MYSQL_DATABASE_NAME, "admin7z5GufY", "uCn8Qi2Zz91S");
				System.out.println("Database connected");

				Statement st = conn.createStatement();
				int resUpdate;

				if (flagKey == 1) {
					// check if the entry is present in the database for this
					// user or not
					ResultSet rsNewUserCheck = st
							.executeQuery("SELECT flag FROM users WHERE regId='"
									+ regId + "';");
					if (rsNewUserCheck.next()) {
						// the user is already present in the database.
						
						ResultSet singleUnKey = st.executeQuery(selectUnkey);
//						String removeUnKey=null;
						if(singleUnKey.next()){
							preUnKey = singleUnKey.getString(1);
						}
						
						singleUnKey.close();
						
						resUpdate = st.executeUpdate(updateQuery);
						
					} else {
						// new user - first time entry
						resUpdate = st.executeUpdate(insertQuery);
					}
					rsNewUserCheck.close();
				} else {
					// update only possible!
					//first get the previous unKey
					System.out.println(selectUnkey);
					ResultSet singleUnKey = st.executeQuery(selectUnkey);
					String removeUnKey=null;
					if(singleUnKey.next()){
						removeUnKey = singleUnKey.getString(1);
					}
					
					singleUnKey.close();
					System.out.println("uddating active to unactive");
					resUpdate = st.executeUpdate(updateQuery);
					
					//send request to all passive users to remove the key
					ResultSet rsPassiveUsers = st
							.executeQuery("SELECT regID FROM users WHERE flag=1 AND unKey=100000"); // passive
																									// users

					System.out.println("Reached the initial checkpoint");
					System.out
							.println("unique key to send to all passive users is : "
									+ unKey);

					ArrayList<String> arRegIds = new ArrayList<>();
					String regID;

					Result result = null;

					Sender sender = new Sender(GOOGLE_SERVER_KEY);
					Message message=null;
					if(removeUnKey!=null){
					message = new Message.Builder().timeToLive(30)
							.delayWhileIdle(true).addData(MESSAGE_KEY, "-"+removeUnKey)
							.build();
					}
					System.out.println("Sending data >>>>> =" + removeUnKey);
					while (rsPassiveUsers.next()) {
						System.out.println("data :"
								+ rsPassiveUsers.getString(1));
						// arRegIds.add(rsPassive.getString(1));
						regID = rsPassiveUsers.getString(1);
						if (regID.length() > 10) { // to check if not a sample
													// reg entered by developer
													// for testing purpose
							// safe to send message to passive device
							if(message!=null){
								result = sender.send(message, regID, 1);
							}else{
								System.out.println("mesage body null");
							}
							System.out.println("result from gcm ::" + result);
						}
					}
					rsPassiveUsers.close();
				}
				
				String getSerailQuery = "SELECT serial_num FROM users WHERE regId='"+regId+"';";
				ResultSet rsSerial = st.executeQuery(getSerailQuery);
				if(rsSerial.next()){
					serial = Integer.toString(rsSerial.getInt(1));
					System.out.println("Got serial :: " + serial);
				}

				// send message to passive users about the update
				// if and only if new active user i.e. flag=1 AND unKey>0 AND
				// unKey <100000

				// passive users are those who have flag as 1 and unKey as
				// 100000
				// active users are those who have flag as 1 and unKey > 0
				// users neither in active nor in passive mode are with unKey=0
				// and flag=0;

				if (!unKey.equals(UNACTIVE_USER) && !unKey.equals(PASSIVE_USER)) {
					// now notify all the passive users
					ResultSet rsPassiveUsers = st
							.executeQuery("SELECT regID FROM users WHERE flag=1 AND unKey=100000"); // passive
																									// users

					System.out.println("Reached the initial checkpoint");
					System.out
							.println("unique key to send to all passive users is : "
									+ unKey);

					ArrayList<String> arRegIds = new ArrayList<>();
					String regID;

					Result result;

					Sender sender = new Sender(GOOGLE_SERVER_KEY);
					Message message = new Message.Builder().timeToLive(30)
							.delayWhileIdle(true).addData(MESSAGE_KEY, unKey+"|"+serial)
							.build();

					while (rsPassiveUsers.next()) {
						System.out.println("data :"
								+ rsPassiveUsers.getString(1));
						// arRegIds.add(rsPassive.getString(1));
						regID = rsPassiveUsers.getString(1);
						if (regID.length() > 10) { // to check if not a sample
													// reg entered by developer
													// for testing purpose
							// safe to send message to passive device
							result = sender.send(message, regID, 1);
							System.out.println("result from gcm ::" + result);
						}
					}
					rsPassiveUsers.close();
					//
				}else if(!unKey.equals(UNACTIVE_USER) && unKey.equals(PASSIVE_USER)){
					//passive user online - fetch a list of all the active users and send
					
					updateActiveListOfPassiveUsers(st,unKey,regId,serial);
					
					ResultSet rsActiveUsers = st.executeQuery(ACTIVE_USERS_LIST_QUERY);
					ArrayList<String> activeUserList = new ArrayList<String>();
					
					while(rsActiveUsers.next()){
						activeUserList.add(Integer.toString(rsActiveUsers.getInt(1))+"|"+serial);
					}
					
					if(activeUserList.size()>0){
						Sender sender = new Sender(GOOGLE_SERVER_KEY);
						Message message = new Message.Builder().timeToLive(30)
								.delayWhileIdle(true).addData(MESSAGE_KEY, activeUserList.toString())
								.build();
						System.out.println("Sending data ::" +  activeUserList.toString());
						Result result = sender.send(message, regId, 1);
						System.out.println("result from gcm ::" + result);
					}
				}
				
				
			} catch (SQLException se) {
				se.printStackTrace();
			}

		}
		
		else if((share != null && !share.isEmpty())
				&& (serial != null && !serial.isEmpty())
				&& (unKey != null && !unKey.isEmpty())  && (share.equals("2"))){
			
			//location
			
		}
		
	}

	private void updateActiveListOfPassiveUsers(Statement st,String unKey,String regId,String serial) {
		// TODO Auto-generated method stub
		try{
//		String selectUnkey="SELECT unKey FROM users WHERE regId='"+regId+"';";
//		
//		System.out.println(selectUnkey);
//		ResultSet singleUnKey = st.executeQuery(selectUnkey);
//		String removeUnKey=null;
//		if(singleUnKey.next()){
//			removeUnKey = singleUnKey.getString(1);
//		}
//		
//		singleUnKey.close();
		
		ResultSet rsPassiveUsers = st
				.executeQuery("SELECT regID FROM users WHERE flag=1 AND unKey=100000"); // passive
																						// users

		System.out.println("Reached the initial checkpoint");
		System.out
				.println("unique key to send to all passive users is : "
						+ unKey);

		ArrayList<String> arRegIds = new ArrayList<>();
		String regID;

		Result result = null;

		Sender sender = new Sender(GOOGLE_SERVER_KEY);
		Message message=null;
		if(preUnKey!=null){
		message = new Message.Builder().timeToLive(30)
				.delayWhileIdle(true).addData(MESSAGE_KEY, "-"+preUnKey+"|"+serial)
				.build();
		}
		System.out.println("Sending data >>>>> =" + preUnKey);
		while (rsPassiveUsers.next()) {
			System.out.println("data :"
					+ rsPassiveUsers.getString(1));
			// arRegIds.add(rsPassive.getString(1));
			regID = rsPassiveUsers.getString(1);
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
					System.out.println("mesage body null");
				}
				System.out.println("result from gcm ::" + result);
			}
		}
		rsPassiveUsers.close();
		}catch(SQLException se){
			se.printStackTrace();
		}
	}
}
