package ru.javarush.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RedisClientPropertyReader {

    private static final Logger logger = LogManager.getLogger(RedisClientPropertyReader.class);

    private final String host;

    private final int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public RedisClientPropertyReader(){
        FileInputStream fis;
        Properties property = new Properties();

        try {
            fis = new FileInputStream("src/main/resources/redisConnect.properties");
            property.load(fis);

            host = property.getProperty("host");
            port = Integer.parseInt(property.getProperty("port"));

        } catch (IOException e) {
            logger.error("property file is missing");
            throw new RuntimeException(e);
        }
    }
}