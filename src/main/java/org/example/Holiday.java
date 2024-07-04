package org.example;

import java.util.Arrays;

public class Holiday {
    private long id;
    private String title;
    private String country;
    private String city;
    private String duration;
    private String season;
    private String description;
    private double price;
    private String[] photos;
    private int[] rating;
    private double averageRating;

    public Holiday() {
    }

    public Holiday(String title, String country, String city, String duration, String season, String description, double price, String[] photos, int[] rating) {
        this.title = title;
        this.country = country;
        this.city = city;
        this.duration = duration;
        this.season = season;
        this.description = description;
        this.price = price;
        this.photos = photos;
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public int[] getRating() {
        return rating;
    }

    public void setRating(int[] rating) {
        this.rating = rating;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", duration='" + duration + '\'' +
                ", season='" + season + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", photos=" + Arrays.toString(photos) +
                ", rating=" + Arrays.toString(rating) +
                '}';
    }
}