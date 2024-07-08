package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    private static long idCounter = 0;
    private static List<Holiday> holidays = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        holidays = loadHolidays();
        HolidayHandler holidayHandler = new HolidayHandler(holidays);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/createHoliday", holidayHandler);
        server.createContext("/getHolidays", holidayHandler);
        server.createContext("/getHoliday", holidayHandler);
        server.createContext("/updateHoliday", holidayHandler);
        server.createContext("/deleteHoliday", holidayHandler);
        server.createContext("/resetRatings", holidayHandler);
        server.createContext("/rateHoliday", holidayHandler);

        server.setExecutor(null);
        server.start();
    }

    public static List<Holiday> loadHolidays() {
        try (FileReader reader = new FileReader("holidays.json")) {
            Gson gson = new Gson();
            Holiday[] holidaysArray = gson.fromJson(reader, Holiday[].class);
            return new ArrayList<>(List.of(holidaysArray));
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public static long getNextId() {
        return holidays.stream()
                .mapToLong(Holiday::getId)
                .max()
                .orElse(0) + 1;
    }
}