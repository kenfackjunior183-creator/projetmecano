package com.mecano.geolocation_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GeolocationServiceApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("✅ Contexte Spring chargé avec succès !");
	}

}
