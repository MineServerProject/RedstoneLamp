/*
 * This file is part of RedstoneLamp.
 *
 * RedstoneLamp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedstoneLamp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RedstoneLamp.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redstonelamp;

import net.redstonelamp.config.ServerConfig;
import net.redstonelamp.config.YamlConfig;
import net.redstonelamp.ui.Log4j2ConsoleOut;
import net.redstonelamp.ui.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Main Startup file for RedstoneLamp.
 *
 * @author RedstoneLamp Team
 */
public class RedstoneLamp{
    public static final String SOFTWARE = "RedstoneLamp";
    public static final String SOFTWARE_VERSION = "1.2.0";
    public static final String SOFTWARE_STATE = "DEV";
    private static int SOFTWARE_BUILD;
    private static String SOFTWARE_COMMIT;
    private static String SOFTWARE_BUILD_DATE;
    public static final double API_VERSION = 1.5;
    public static Server SERVER;

    public static void main(String[] args){
        RedstoneLamp main = new RedstoneLamp();
        main.getDefaultResources();

        try{
            ServerConfig config = new ServerConfig(new File("server.properties"));
            YamlConfig conf = new YamlConfig("redstonelamp.yml");
            SERVER = new Server(new Logger(new Log4j2ConsoleOut("RedstoneLamp")), config, conf); //TODO: Correct logger
            SERVER.run();
        }catch(IOException e){
            LogManager.getRootLogger().fatal("Could not init server! " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String getBuildInformation(){
        return SOFTWARE + " build #" + SOFTWARE_BUILD + ", commit: " + SOFTWARE_COMMIT + ", built on: " + SOFTWARE_BUILD_DATE;
    }

    private void getDefaultResources(){
        if(!new File("server.properties").isFile()){
            URL url = getClass().getResource("/conf/server.properties");
            File dest = new File("./server.properties");
            try{
                FileUtils.copyURLToFile(url, dest);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        if(!new File("redstonelamp.yml").isFile()){
            URL url = getClass().getResource("/conf/redstonelamp.yml");
            File dest = new File("./redstonelamp.yml");
            try{
                FileUtils.copyURLToFile(url, dest);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        File log4j2 = new File("log4j2.xml");
        if(!log4j2.isFile()){
            URL url = getClass().getResource("/log4j2-default.xml");
            try{
                FileUtils.copyURLToFile(url, log4j2);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        InputStream is = getClass().getResourceAsStream("/buildInformation.properties");
        if(is != null){
            Properties prop = new Properties();
            try{
                prop.load(is);
                SOFTWARE_BUILD = Integer.parseInt(prop.getProperty("buildNum"));
                SOFTWARE_COMMIT = prop.getProperty("buildCommit");
                SOFTWARE_BUILD_DATE = prop.getProperty("buildDate");
            }catch(Exception e){
                System.err.println(e.getClass().getName() + " while attempting to get build info: " + e.getMessage());
                e.printStackTrace();
            }
        }else{
            SOFTWARE_BUILD = -1;
            SOFTWARE_COMMIT = "not available";
            SOFTWARE_BUILD_DATE = "not available";
        }
        System.setProperty("log4j.configurationFile", "log4j2.xml");
    }

    /**
     * Get the software's version string.
     *
     * @return The version string (example: RedstoneLamp v1.0.0-development)
     */
    public static String getSoftwareVersionString(){
        return SOFTWARE + " " + SOFTWARE_VERSION + "-" + SOFTWARE_STATE;
    }

}
