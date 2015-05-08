import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;



public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	public ArrayList<ArrayList<String>> getTrainingSet() throws Exception{
		ArrayList<ArrayList<String>> trainingSet = new ArrayList<ArrayList<String>>();
		// This will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager.getConnection(getDatabaseConnectionString());
		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		resultSet = statement.executeQuery("SELECT subject, body, spam FROM " + getDatabaseName() + ".trainingset3;");
		while(resultSet.next()){
			String subject = resultSet.getString("subject");
			String body = resultSet.getString("body");
			String spam = resultSet.getString("spam");
			ArrayList<String> email = new ArrayList<String>();
			email.add(subject);
			email.add(body);
			email.add(spam);
			trainingSet.add(email);
		}
		close();
		return trainingSet;
	}
	
	public ArrayList<ArrayList<String>> getPartTrainingSet(int part) throws Exception{
		ArrayList<ArrayList<String>> partTrainingSet = new ArrayList<ArrayList<String>>();
		// This will load the MySQL driver, each DB has its own driver
		Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager.getConnection(getDatabaseConnectionString());
		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		resultSet = statement.executeQuery("SELECT subject, body, spam FROM " + getDatabaseName() + ".trainingset3 WHERE part = "
				+ part + ";");
		while(resultSet.next()){
			String subject = resultSet.getString("subject");
			String body = resultSet.getString("body");
			String spam = resultSet.getString("spam");
			ArrayList<String> email = new ArrayList<String>();
			email.add(subject);
			email.add(body);
			email.add(spam);
			partTrainingSet.add(email);
		}
		close();
		return partTrainingSet;
	}
	public ArrayList<ArrayList<String>> get9PartTrainingSet(int part) throws Exception{
		ArrayList<ArrayList<String>> part9TrainingSet = new ArrayList<ArrayList<String>>();
		//get other 9 ids
		int[] parts = new int[10];
		for(int i = 1; i <= 10; i++){
			if(i != part){
				parts[i-1] = i;
			}
		}
		
		Class.forName("com.mysql.jdbc.Driver");
		connect = DriverManager.getConnection(getDatabaseConnectionString());
		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		String query = "SELECT subject, body, spam FROM " + getDatabaseName() + ".trainingset3 WHERE (";
		for(int i = 0; i < parts.length; i++){
			if(parts[i] != 0){
				query += "part = " + parts[i] + " OR ";
			}
		}
		query = query.substring(0, query.length() - 4);
		query += ");";
		resultSet = statement.executeQuery(query);
		while(resultSet.next()){
			String subject = resultSet.getString("subject");
			String body = resultSet.getString("body");
			String spam = resultSet.getString("spam");
			ArrayList<String> email = new ArrayList<String>();
			email.add(subject);
			email.add(body);
			email.add(spam);
			part9TrainingSet.add(email);
		}
		close();
		return part9TrainingSet;
	}
	
	public String getDatabaseConnectionString(){
		return "jdbc:mysql://localhost/spam_filter?"+ "user=root&password=";
	}
	
	public String getDatabaseName(){
		return "spam_filter";	
	}

	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {
		}
	}
}
