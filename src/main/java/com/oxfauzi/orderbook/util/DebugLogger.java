package com.oxfauzi.orderbook.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static java.nio.file.StandardOpenOption.*;

public class DebugLogger {

    private static String logFileLocation;
    private static ZonedDateTime now;
    private static boolean forTest;

    public static void initialize() {
        initialize(false);
    }

    public static void initializeForTest() {
        initialize(true);
    }

    private static void initialize(boolean test) {
        logFileLocation = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath()
                .toString();
        now = ZonedDateTime.now();
        forTest = test;
    }

    public static void log(String message) {
        try {
            final String logLine = String.format("@ %s | %s\n", now.toString(), message);
            if (forTest) {
                System.out.println(logLine);
            } else {
                Files.write(Path.of(logFileLocation + FileSystems.getDefault().getSeparator() + "order-book-logs_" + toLocalTimeString(now) + ".txt"), logLine.getBytes(), CREATE, WRITE, APPEND);
            }
        } catch (IOException e) {
            System.err.println("Failed to write message to log file" + e);
            //deliberately ignore message as logging is inconsequential
        }
    }

    private static String toLocalTimeString(ZonedDateTime now) {
        return now.toLocalTime().toString().replaceAll(":", "_");
    }
}
