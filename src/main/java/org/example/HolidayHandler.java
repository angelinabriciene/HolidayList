package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
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
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);

        long id = Long.parseLong(params.get("id"));
        String title = params.get("title");
        String country = params.get("country");
        String city = params.get("city");
        String duration = params.get("duration");
        String season = params.get("season");
        String description = params.get("description");
        double price = Double.parseDouble(params.get("price"));
        String[] photos = params.get("photos").split(",");
        int[] rating = new int[0];

        Holiday updatedHoliday = new Holiday(title, country, city, duration, season, description, price, photos, rating);
        updatedHoliday.setId(id);
        for (Holiday holiday : holidays) {
            if (holiday.getId() == id) {
                holiday.setTitle(updatedHoliday.getTitle());
                holiday.setCountry(updatedHoliday.getCountry());
                holiday.setCity(updatedHoliday.getCity());
                holiday.setDuration(updatedHoliday.getDuration());
                holiday.setSeason(updatedHoliday.getSeason());
                holiday.setDescription(updatedHoliday.getDescription());
                holiday.setPrice(updatedHoliday.getPrice());
                holiday.setPhotos(updatedHoliday.getPhotos());
                saveHolidays();
                String response = "Holiday has been updated successfully";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
        }
        exchange.sendResponseHeaders(404, -1);
    }

    private void handleDeleteHoliday(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);
        long id = Long.parseLong(params.get("id"));
        Holiday holiday = holidays.stream().filter(h -> h.getId() == id).findFirst().orElse(null);
        if (holiday != null) {
            holidays.remove(holiday);
            saveHolidays();
            String response = "Holiday has been deleted successfully";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(404, -1);
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
            e.printStackTrace();
        }
    }


    private void handleCreateHoliday(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);

        String title = params.get("title");
        String country = params.get("country");
        String city = params.get("city");
        String duration = params.get("duration");
        String season = params.get("season");
        String description = params.get("description");
        double price = Double.parseDouble(params.get("price"));
        String[] photos = params.get("photos").split(",");
        int[] rating = new int[0];

        Holiday holiday = new Holiday(title, country, city, duration, season, description, price, photos, rating);
        holiday.setId(Main.getNextId());
        holidays.add(holiday);
        saveHolidays();
        System.out.println(holidays);
        String response = "Holiday: " + title + " has been created successfully";
        exchange.sendResponseHeaders(201, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}