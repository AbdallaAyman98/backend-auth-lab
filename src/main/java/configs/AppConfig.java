package configs;

import enums.AppProperty;
import utilities.EnvReader;

public class AppConfig {

    private final String JWT_SECRET;



    private AppConfig() {
        EnvReader.load("src/main/java","app.env"); // load .env

        this.JWT_SECRET = EnvReader.get(AppProperty.JWT_SECRET.getKey());
    }

    public String getJwtSecret()     { return JWT_SECRET; }

    private static AppConfig instance;
    public static AppConfig getInstance(){
        if (instance == null){
            instance = new AppConfig();
        }
        return instance;
    }





}