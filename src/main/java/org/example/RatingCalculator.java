package org.example;

public class RatingCalculator {
    public static double calculateAverageRating(int[] ratings) {
        int sum = 0;
        for (int rating : ratings) {
            sum += rating;
        }
        return (double) sum / ratings.length;
    }
}