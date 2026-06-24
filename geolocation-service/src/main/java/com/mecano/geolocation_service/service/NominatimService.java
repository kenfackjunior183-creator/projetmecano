package com.mecano.geolocation_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NominatimService {

    @Value("${application.geolocation.nominatim-url}")
    private String nominatimUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Géocode une adresse → retourne {lat, lng}
     * Utilise OpenStreetMap Nominatim (gratuit, sans clé API)
     */
    @SuppressWarnings("unchecked")
    public double[] geocodeAddress(String address) {
        String url = nominatimUrl + "/search?q=" + address.replace(" ", "+") + "&format=json&limit=1";
        
        // Nominatim exige un User-Agent
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("User-Agent", "MecanoApp/1.0");
        var entity = new org.springframework.http.HttpEntity<>(headers);
        
        var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, java.util.List.class);
        var results = response.getBody();
        
        if (results == null || results.isEmpty()) 
            throw new RuntimeException("Adresse introuvable : " + address);
            
        Map<String, Object> first = (Map<String, Object>) results.get(0);
        double lat = Double.parseDouble((String) first.get("lat"));
        double lng = Double.parseDouble((String) first.get("lon"));
        return new double[]{lat, lng};
    }

    /**
     * Géocodage inverse : coordonnées → adresse lisible
     */
    @SuppressWarnings("unchecked")
    public String reverseGeocode(double lat, double lng) {
        String url = nominatimUrl + "/reverse?lat=" + lat + "&lon=" + lng + "&format=json";
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("User-Agent", "MecanoApp/1.0");
        var entity = new org.springframework.http.HttpEntity<>(headers);
        
        var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();
        
        if (body == null) return "Adresse inconnue";
        return (String) body.get("display_name");
    }
}
