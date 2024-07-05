package org.example;

public class RatingCalculator {
    public static double calculateAverageRating(int[] ratings) {
        int sum = 0;
        for (int rating : ratings) {
            sum += rating;
        }
        return Double.parseDouble(String.format("%.1f", (double) sum / ratings.length));
    }
}