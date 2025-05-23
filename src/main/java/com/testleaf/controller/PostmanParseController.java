package com.testleaf.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import com.testleaf.parser.PostmanParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.testleaf.parser.SwaggerParser;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class PostmanParseController {

    private final PostmanParser postmanParser = new PostmanParser();

    @PostMapping(value = "/parseCollection", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> parsePostmanCollectionFile(@RequestParam("file") MultipartFile file) {
        try {
            String tempFileName = System.getProperty("java.io.tmpdir")
                    + File.separator
                    + UUID.randomUUID() + "_" + file.getOriginalFilename();

            Files.write(Paths.get(tempFileName), file.getBytes());

            String apiDetailsJson = postmanParser.parseCollection(tempFileName);

            if (apiDetailsJson == null || apiDetailsJson.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Failed to parse the Collection file. Check if it is a valid specification.");
            }

            return ResponseEntity.ok(apiDetailsJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error parsing file: " + e.getMessage());
        }
    }

}
