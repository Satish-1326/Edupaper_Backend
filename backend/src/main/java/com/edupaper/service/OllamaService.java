package com.edupaper.service;

import com.edupaper.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OllamaService {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;


    public String generateQuestion(
            String prompt
    ) {

        String url =
                baseUrl + "/api/generate";


        Map<String, Object> request =
                new HashMap<>();

        request.put(
                "model",
                model
        );

        request.put(
                "prompt",
                prompt
        );

        /*
         * Tell Ollama that we want
         * machine-readable JSON.
         */
        request.put(
                "format",
                "json"
        );

        /*
         * We don't want the model to
         * continue generating unnecessarily.
         */
        request.put(
                "stream",
                false
        );


        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );


        HttpEntity<Map<String, Object>>
                entity =
                new HttpEntity<>(
                        request,
                        headers
                );


        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            String.class
                    );


            if (!response.getStatusCode()
                    .is2xxSuccessful()) {

                throw new BadRequestException(
                        "Ollama returned HTTP status "
                                + response.getStatusCode()
                );
            }


            JsonNode root =
                    objectMapper.readTree(
                            response.getBody()
                    );


            JsonNode responseNode =
                    root.get("response");


            if (responseNode == null ||
                    responseNode.isNull()) {

                throw new BadRequestException(
                        "Ollama returned an empty response."
                );
            }


            return responseNode.asText();


        } catch (Exception e) {

            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }

            throw new BadRequestException(
                    "Failed to communicate with Ollama: "
                            + e.getMessage()
            );
        }
    }
}