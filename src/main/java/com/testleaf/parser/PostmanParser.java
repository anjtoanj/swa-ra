package com.testleaf.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PostmanParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String parseCollection(String postmanFilePath) {
        try {
            // Read the file
            Path path = Paths.get(postmanFilePath);
            String fileContent = new String(Files.readAllBytes(path));

            // Parse the Postman collection JSON
            JsonNode rootNode = objectMapper.readTree(fileContent);

            // Build API details map from the collection
            Map<String, Object> apiDetailsMap = buildApiDetailsMap(rootNode);

            // Return as pretty printed JSON
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiDetailsMap);

        } catch (Exception e) {
            System.err.println("❌ Error while parsing Postman Collection: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildApiDetailsMap(JsonNode rootNode) {
        Map<String, Object> apiDetails = new LinkedHashMap<>();

        // Collection Info
        JsonNode infoNode = rootNode.path("info");
        apiDetails.put("Title", infoNode.path("name").asText());
        apiDetails.put("Description", infoNode.path("description").asText());
        apiDetails.put("Schema", infoNode.path("schema").asText());

        // List to hold endpoints
        List<Map<String, Object>> endpoints = new ArrayList<>();

        // Iterate over items (Postman collection may have nested items/folders)
        JsonNode items = rootNode.path("item");
        items.forEach(item -> {
            Map<String, Object> endpoint = new LinkedHashMap<>();
            endpoint.put("Name", item.path("name").asText());

            // Check if the item has a request
            JsonNode request = item.path("request");
            if (request != null) {
                endpoint.put("Method", request.path("method").asText());
                endpoint.put("URL", request.path("url").path("raw").asText());

                // Headers
                List<String> headers = new ArrayList<>();
                request.path("header").forEach(header ->
                        headers.add(header.path("key").asText() + ": " + header.path("value").asText())
                );
                endpoint.put("Headers", headers);

                // Body (if available)
                endpoint.put("Body", request.path("body").path("raw").asText());
            }

            // Add the endpoint details to the list
            endpoints.add(endpoint);
        });

        // Add endpoints to the final API details
        apiDetails.put("Endpoints", endpoints);

        return apiDetails;
    }
}
