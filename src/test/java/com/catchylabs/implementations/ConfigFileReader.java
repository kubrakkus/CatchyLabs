package com.catchylabs.implementations;

import org.apache.log4j.Logger;
import org.assertj.core.api.Assertions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

public class ConfigFileReader {
    public static Properties prop;
    private static ConfigFileReader config;
    private final Logger logger;

    private ConfigFileReader() {
        logger = Logger.getLogger(ConfigFileReader.class);
        configRead();
    }

    public static ConfigFileReader getInstance() {
        if (config == null) {
            config = new ConfigFileReader();
        }
        return config;
    }

    /**
     * Properties dosyasını okur ve prop değişkenine atar.
     */
    public void configRead() {
        BufferedReader reader;
        String propPath = "src/test/resources/config.properties";
        try {
            reader = new BufferedReader(new FileReader(propPath));
            prop = new Properties();
            prop.load(reader);
            reader.close();
        } catch (NoSuchFileException e) {
            e.printStackTrace();
            logger.error("Properties okunamadı dosya bulunamadı. Path = " + propPath + " " + e.getMessage());
            Assertions.fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Properties dosyası açılamadı. Path = " + propPath + " " + e.getMessage());
            Assertions.fail(e.getMessage());
        }
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri String olarak döner.
     */
    public String getProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            logger.error(key + " keyi ile değer bulunamadı. Lütfen kontrol ediniz.");
            Assertions.fail(key + " keyi ile değer bulunamadı. Lütfen kontrol ediniz.");
        }
        return value;
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri Integer olarak döner.
     */
    public int getInteger(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri String olarak döner.
     */
    public String getString(String key) {
        return getProperty(key);
    }
}

