import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gf38 on 05/04/16.
 */
public class DBAccessor {

    public static String FIELD_DATA = "study_results";
    public static String FIELD_IV = "initialisation_vector";
    public static String FIELD_SECRETKEY = "symmetric_key";
    public static String FIELD_RECORD_TIMESTAMP = "time_recorded";
    public static String FIELD_DEVICE_ID = "device_identifier";
    public static String FIELD_RESEARCHER_ID = "trial_conductor";

    private static String MYSQL_DB_DRIVER = "com.mysql.jdbc.Driver";

    private static String MYSQL_DB_HOST = "jdbc:mysql://gf38.host.cs.st-andrews.ac.uk/";
    private static String MYSQL_DB_NAME = "gf38_ePegDB";
    private static String MYSQL_DB_USER = "gf38";
    private static String MYSQL_DB_PW = "uSc.Zdb2SBar8j";

    private static String SQL_FETCH_STUDIES = "SELECT * FROM studies";

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public List<DBEntry> readDatabase(){

        ArrayList<DBEntry> dbEntries = null;

        try{
            dbEntries = new ArrayList<>();

            Class.forName(MYSQL_DB_DRIVER);

            connect = DriverManager.getConnection(MYSQL_DB_HOST + MYSQL_DB_NAME + "?user="+ MYSQL_DB_USER + "&password=" + MYSQL_DB_PW);

            statement = connect.createStatement();

            resultSet = statement.executeQuery(SQL_FETCH_STUDIES);

            while ( resultSet.next() ){

                String data = resultSet.getString(FIELD_DATA);
                String iv = resultSet.getString(FIELD_IV);
                String secretkey = resultSet.getString(FIELD_SECRETKEY);
                Timestamp recordTimestamp = resultSet.getTimestamp(FIELD_RECORD_TIMESTAMP);

                System.out.println("Data: " + data);
                System.out.println("IV: " + iv);
                System.out.println("Secretkey: " + secretkey);
                System.out.println("Recorded: " + recordTimestamp.toString());

                dbEntries.add(new DBEntry(data, iv, secretkey, recordTimestamp));
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return dbEntries;
    }

}
