package ru.hse.jade.sample;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ProcessLogger {

    public static final Logger logger = Logger.getLogger(ProcessLogger.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("src/main/java/logs/process_logs");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            logger.warning("Could not create log file: " + e.getMessage());
        }
    }
}
