package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HolidayHandler implements HttpHandler {
    private List<Holiday> holidays;
    private final Gson gson;

    public HolidayHandler(List<Holiday> holidays) {
        this.holidays = holidays;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String method = exchange.getRequestMethod();
        handleCORS(exchange);

        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if (method.equals("POST") && uri.getPath().equals("/createHoliday")) {
            handleCreateHoliday(exchange);
        }
        if (method.equals("GET") && uri.getPath().equals("/getHolidays")) {
            handleGetHolidays(exchange);
        }
        if (method.equals("GET") && uri.getPath().equals("/getHoliday")) {
            handleGetHoliday(exchange);
        }
        if (method.equals("POST") && uri.getPath().equals("/updateHoliday")) {
            handleUpdateHoliday(exchange);
        }
        if (method.equals("POST") && uri.getPath().equals("/deleteHoliday")) {
            handleDeleteHoliday(exchange);
        }
        if (method.equals("POST") && uri.getPath().equals("/resetRatings")) {
            handleResetHolidayRatings(exchange);
        }
        if (method.equals("POST") && uri.getPath().equals("/rateHoliday")) {
            handleRateHoliday(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    private void handleRateHoliday(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);
        long id = Long.parseLong(params.get("id"));
        int rating = Integer.parseInt(params.get("rating"));
        Holiday holiday = holidays.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
        if (rating < 1 || rating > 5) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        if (holiday != null) {
            int[] newRating = new int[holiday.getRating().length + 1];
            System.arraycopy(holiday.getRating(), 0, newRating, 0, holiday.getRating().length);
            newRating[newRating.length - 1] = rating;
            holiday.setRating(newRating);

            holiday.setAverageRating(RatingCalculator.calculateAverageRating(holiday.getRating()));
            saveHolidays();
            String response = "Rating submitted successfully";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleResetHolidayRatings(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);
        long id = Long.parseLong(params.get("id"));
        Holiday holiday = holidays.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
        if (holiday != null) {
            holiday.setRating(new int[0]);
            saveHolidays();
            String response = "Holiday rating has been reset";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleGetHolidays(HttpExchange exchange) throws IOException {
        List<Holiday> holidaysWithRatings = new ArrayList<>();
        for (Holiday holiday : holidays) {
            int[] ratings = holiday.getRating();
            double averageRating = RatingCalculator.calculateAverageRating(ratings);
            holiday.setAverageRating(averageRating);
            holidaysWithRatings.add(holiday);
        }
        String response = gson.toJson(holidaysWithRatings);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private void handleGetHoliday(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);
        long id = Long.parseLong(params.get("id"));
        Holiday holiday = holidays.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
        if (holiday != null) {
            int[] ratings = holiday.getRating();
            double averageRating = RatingCalculator.calculateAverageRating(ratings);
            holiday.setAverageRating(averageRating);
            String response = gson.toJson(holiday);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleUpdateHoliday(HttpExchange exchange) throws IOException {
        Holiday updatedHoliday = requestHoliday(exchange);
        holidays.stream()
                .filter(h -> h.getId() == updatedHoliday.getId())
                .findFirst()
                .ifPresent(existingHoliday -> {
                    int index = holidays.indexOf(existingHoliday);
                    holidays.set(index, updatedHoliday);
                });
        saveHolidays();
        String response = "{\"success\": true, \"message\": \"Holiday has been updated successfully\"}";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private Holiday requestHoliday(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        String dataString = "";
        String line;

        while ((line = reader.readLine())!= null) {
            dataString += line;
        }
        reader.close();
        return gson.fromJson(dataString, Holiday.class);
    }

    private void handleDeleteHoliday(HttpExchange exchange) throws IOException {
        Holiday holidayToDelete = requestHoliday(exchange);
        boolean removed = holidays.removeIf(h -> h.getId() == holidayToDelete.getId());
        if (removed) {
            saveHolidays();
            String response = "{\"success\": true, \"message\": \"Holiday has been deleted successfully\"}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            String response = "{\"success\": false, \"message\": \"Holiday not found\"}";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            } else {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), "");
            }
        }
        return result;
    }

    private void saveHolidays() {
        try (FileWriter writer = new FileWriter("holidays.json")) {
            gson.toJson(holidays, writer);
        } catch (IOException e) {

            System.out.println(e);

            e.printStackTrace();
        }
    }

    private void handleCreateHoliday(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        String dataString = "";
        String line;
        while ((line = reader.readLine()) != null) {
            dataString += line;
        }

        reader.close();
        Holiday holiday = gson.fromJson(dataString, Holiday.class);
        holiday.setId(Main.getNextId());

        holidays.add(holiday);
        saveHolidays();
        String response = "Holiday: " + holiday.getTitle() + " has been created successfully";
        exchange.sendResponseHeaders(201, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}