package com.lpu.android.trackme.servlet;

public class Constants {
	
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
	
	public static final String ACTIVE_USERS_UNKEY_LIST_QUERY = "SELECT unKey,serial_num,location FROM users WHERE unKey <> '000000' AND unKey<>'100000'";
	
	public static final String PASSIVE_USER_LIST_QUERY = "SELECT regId FROM users WHERE unKey='100000'";

	private static final long serialVersionUID = 1L;

	
	// Put your Google API Server Key here
		public static final String GOOGLE_SERVER_KEY = "AIzaSyBhcUIKx-dlfBQxNE9N0SL9R7F_otaxR-0";
		public static final String MESSAGE_KEY = "message";

}
