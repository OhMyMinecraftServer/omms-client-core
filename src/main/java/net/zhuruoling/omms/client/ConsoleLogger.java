package net.zhuruoling.omms.client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger {
    public static void info(Object content){
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern(" hh:mm:ss.SSS"))+ " " + Thread.currentThread().getName() + "|INFO " + content.toString());
    }
}
