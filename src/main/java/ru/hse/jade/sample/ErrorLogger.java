package ru.hse.jade.sample;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ErrorLogger {

    public static final Logger logger = Logger.getLogger(ErrorLogger.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("src/main/java/logs/error_logs");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            logger.warning("Could not create log file: " + e.getMessage());
        }
    }
}
