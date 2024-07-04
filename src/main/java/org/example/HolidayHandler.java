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
    private Gson gson;

    public HolidayHandler(List<Holiday> holidays) {
        this.holidays = holidays;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String method = exchange.getRequestMethod();

        if (method.equals("POST") && uri.getPath().equals("/createHoliday")) {
            handleCreateHoliday(exchange);
        } else if (method.equals("GET") && uri.getPath().equals("/getHolidays")) {
            handleGetHolidays(exchange);
        } else if (method.equals("GET") && uri.getPath().equals("/getHoliday")) {
            handleGetHoliday(exchange);
        } else if (method.equals("POST") && uri.getPath().equals("/updateHoliday")) {
            handleUpdateHoliday(exchange);
        } else if (method.equals("POST") && uri.getPath().equals("/deleteHoliday")) {
            handleDeleteHoliday(exchange);
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
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
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
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
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
        String response = "Holiday: " + title + " has been created successfully";
        exchange.sendResponseHeaders(201, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}