package com.example.subscriptions.it;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PlanControllerIT {
    @LocalServerPort
    int port;

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void createAndList() {
        String body = "{\"code\":\"BASIC\",\"name\":\"Básico\",\"priceCents\":9900,\"period\":\"MONTHLY\",\"trialDays\":7}";

        given().contentType("application/json").body(body)
                .when().post("/api/plans")
                .then().statusCode(HttpStatus.CREATED.value());

        given().when().get("/api/plans").then().statusCode(200);
    }
}