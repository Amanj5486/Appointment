package com.example.appointment.utils;

public class DistanceCalculatorUtils {

    private static final int EARTH_RADIUS = 6371; // Radius of the earth in kilometers

    /**
     * Calculate the distance between two points in kilometers.
     *
     * @param startLat Start point latitude
     * @param startLon Start point longitude
     * @param endLat   End point latitude
     * @param endLon   End point longitude
     * @return Distance in kilometers
     */
    public static double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLon = Math.toRadians(endLon - startLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in kilometers
    }
}

