package org.example;

public class RatingCalculator {
    public static double calculateAverageRating(int[] ratings) {
        if (ratings == null || ratings.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int rating : ratings) {
            sum += rating;
        }
        return (double) sum / ratings.length;
    }
}