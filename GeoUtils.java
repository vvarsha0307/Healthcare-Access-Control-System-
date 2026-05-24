package com.example.demo.util;

import org.springframework.stereotype.Component;

@Component
public class GeoUtils {

    private final double HOSPITAL_LAT = 9.956508;
    private final double HOSPITAL_LNG = 78.068504;

    // Increased to 5km to handle GPS drift from browser
    private final double RADIUS_KM = 5.0;

    public boolean isWithinHospital(double userLat, double userLng) {
        return distanceKm(userLat, userLng, HOSPITAL_LAT, HOSPITAL_LNG) <= RADIUS_KM;
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}