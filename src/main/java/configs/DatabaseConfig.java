package configs;

import enums.DBProperty;
import utilities.EnvReader;

public class DatabaseConfig {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String dbName;
    private final String url;


    private DatabaseConfig() {
        EnvReader.load("src/main/java","db.env"); // load .env

        this.host     = EnvReader.get(DBProperty.HOST.getKey());
        this.port     = EnvReader.getInt(DBProperty.PORT.getKey(), 3306);
        this.username = EnvReader.get(DBProperty.USERNAME.getKey());
        this.password = EnvReader.get(DBProperty.PASSWORD.getKey());
        this.dbName   = EnvReader.get(DBProperty.DB_NAME.getKey());
        this.url      = buildUrl();
    }

    private String buildUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName;
    }

    public String getHost()     { return host; }
    public int getPort()        { return port; }
    public String getUsername() { return username; }
    public String getPassword() { return "2561998"; }
    public String getDbName()   { return dbName; }
    public String getUrl()      { return url; }

    @Override
    public String toString() {
        return "configs.DatabaseConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='***'" +  // never log password
                ", dbName='" + dbName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }




    private static DatabaseConfig instance;
    public static DatabaseConfig getInstance(){
        if (instance == null){
            instance = new DatabaseConfig();
        }
        return instance;
    }





}