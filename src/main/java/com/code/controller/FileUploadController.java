package com.code.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
public class FileUploadController {

    Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // Get the filename and create a new file in the server's file system
            String filename = file.getOriginalFilename();
            Path filepath = Paths.get("uploads", filename);
            try {
                Files.createDirectories(filepath.getParent());
                Files.write(filepath, file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Return a success response
            logger.info("File uploaded successfully!");
            return ResponseEntity.ok("File uploaded successfully!");

        } catch (Exception e) {
            // Return an error response if there was an error writing the file
            logger.error("Failed to upload file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
        }
    }
    @GetMapping("/randomLine")
    public ResponseEntity<?> getRandomLine(@RequestHeader("Accept") String acceptHeader) {
        // Choose a random line from the uploaded file

        //String filename = file.getOriginalFilename();
        Path filepath = Paths.get("uploads");
        File[] files = filepath.toFile().listFiles();

        List<String> lines;
        try {
            lines = Files.readAllLines(Arrays.stream(files).findAny().get().toPath());
        } catch (IOException e) {
            logger.error("Failed to read file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file.");
        }
        int index = new Random().nextInt(lines.size());
        String randomLine = lines.get(index);

            // Return the random line as plain text, JSON, or XML depending on the request's Accept header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);

            String accept = headers.getAccept().toString();
            if (MediaType.APPLICATION_JSON.includes(MediaType.valueOf(acceptHeader))) {
                return ResponseEntity.ok().headers(headers).body("{\"randomLine\": \"" + randomLine + "\"}");
            } else if (MediaType.APPLICATION_XML.includes(MediaType.valueOf(acceptHeader))) {
                return ResponseEntity.ok().headers(headers).body("<randomLine>" + randomLine + "</randomLine>");
            } else {
                logger.info("File successfully getting random line!");
                return ResponseEntity.ok().headers(headers).body(randomLine);

            }
    }

    @GetMapping("/randomLineWithName")
    public ResponseEntity<?> getRandomLineWithName(@RequestHeader("Accept") String acceptHeader) {
        // Choose a random line from the uploaded file
        Path filepath = Paths.get("uploads");
        File[] files = filepath.toFile().listFiles();

        List<String> lines;
        try {
            lines = Files.readAllLines(Arrays.stream(files).findAny().get().toPath());
        } catch (IOException e) {
            logger.error("Failed to read file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file.");
        }
        int index = new Random().nextInt(lines.size());
        String randomLine = lines.get(index);

        // Determine the line number, filename, and most frequent letter for the chosen line
        int lineNumber = index + 1;
        String filename = filepath.getFileName().toString();
        char mostFrequentChar = getMostFrequentChar(randomLine);

        String mostFrequentCharacter = String.valueOf(mostFrequentChar);
        if (mostFrequentCharacter.equals("\0"))
        {
            mostFrequentCharacter = "multiple characters occur with the same highest count";
        }

        // Return the random line and additional details as plain text, JSON, or XML depending on the request's Accept header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        if (MediaType.APPLICATION_JSON.includes(MediaType.valueOf(acceptHeader)) ||
                MediaType.APPLICATION_XML.includes(MediaType.valueOf(acceptHeader))) {
            // Build a JSON or XML object with the line number, filename, and most frequent letter
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("randomLine", randomLine)
                    .add("lineNumber", lineNumber)
                    .add("filename", filename)
                    .add("mostFrequentChar", mostFrequentCharacter)
                    .build();

            if (MediaType.APPLICATION_JSON.includes(MediaType.valueOf(acceptHeader))) {
                // Return the JSON object
                logger.info("File successfully getting random line info!");
                return ResponseEntity.ok().headers(headers).body(jsonObject.toString());
            } else {
                // Return the XML object
                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<response>\n" +
                        "  <randomLine>" + randomLine + "</randomLine>\n" +
                        "  <lineNumber>" + lineNumber + "</lineNumber>\n" +
                        "  <filename>" + filename + "</filename>\n" +
                        "  <mostFrequentChar>" + mostFrequentCharacter + "</mostFrequentChar>\n" +
                        "</response>";
                logger.info("File successfully getting random line info!");
                return ResponseEntity.ok().headers(headers).body(xml);
            }
        } else {
            // Return only the random line as plain text
            logger.info("File successfully getting random line info!");
            return ResponseEntity.ok().headers(headers).body(randomLine);
        }
    }

    private char getMostFrequentChar(String line) {
        // Count the occurrences of each character in the line
        HashMap<Character, Integer> charCounts = new HashMap<>();
        for (char c : line.toCharArray()) {
            charCounts.put(c, charCounts.getOrDefault(c, 0) + 1);
        }

        // Find the character with the highest count
        int maxCount = 0;
        char mostFrequentChar = '\0';
        for (Map.Entry<Character, Integer> entry : charCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentChar = entry.getKey().charValue();
            } else if (entry.getValue() == maxCount) {
                mostFrequentChar = '\0'; // multiple characters occur with the same highest count
            }
        }

        return mostFrequentChar;
    }


        @GetMapping("/randomLineBackwards")
    public ResponseEntity<?> getRandomLineBackwards() {
        // Get a list of all uploaded files
        Path uploadsPath = Paths.get("uploads");
        File[] files = uploadsPath.toFile().listFiles();

        // Choose a random file and a random line from the file
        if (files != null && files.length > 0) {
            Random random = new Random();
            File randomFile = files[random.nextInt(files.length)];
            List<String> lines;
            try {
                lines = Files.readAllLines(randomFile.toPath());
            } catch (IOException e) {
                logger.error("Failed to read file.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file.");
            }
            String randomLine = lines.get(random.nextInt(lines.size()));

            // Return the random line in reverse order
            String reversedLine = new StringBuilder(randomLine).reverse().toString();
            logger.info("File successfully getting random line reversed!");
            return ResponseEntity.ok(reversedLine);
        } else {
            logger.warn("File not getting random line");
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/longest100Lines")
    public ResponseEntity<List<String>> getLongest100Lines() {
        // Get a list of all uploaded files
        Path uploadsPath = Paths.get("uploads");
        File[] files = uploadsPath.toFile().listFiles();

        // Iterate over each file and each line to find the 100 longest lines
        List<String> longestLines = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                try (Stream<String> lines = Files.lines(file.toPath())) {
                    lines.flatMap(line -> Arrays.stream(line.split("\\r?\\n")))
                            .sorted((s1, s2) -> Integer.compare(s2.length(), s1.length()))
                            .limit(100)
                            .forEach(longestLines::add);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
                }
            }
        }
        // Return the 100 longest lines
        logger.info("File successfully getting longest line info!");
        return ResponseEntity.ok(longestLines);
    }

    @GetMapping("/longest20Lines")
    public ResponseEntity<List<String>> getLongest20Lines() {
        // Get a list of all uploaded files
        Path uploadsPath = Paths.get("uploads");
        File[] files = uploadsPath.toFile().listFiles();

        // Select a random or latest file and return its 20 longest lines
        if (files != null && files.length > 0) {
            File selectedFile;
            if (Math.random() < 0.5) {
                // Select a random file
                int randomIndex = (int) (Math.random() * files.length);
                selectedFile = files[randomIndex];
            } else {
                // Select the latest file
                selectedFile = Arrays.stream(files)
                        .max(Comparator.comparing(File::lastModified))
                        .orElse(null);
            }
            if (selectedFile != null) {
                try (Stream<String> lines = Files.lines(selectedFile.toPath())) {
                    List<String> longestLines = lines.sorted((s1, s2) -> Integer.compare(s2.length(), s1.length()))
                            .limit(20)
                            .collect(Collectors.toList());
                    logger.info("File successfully getting longest line!");
                    return ResponseEntity.ok(longestLines);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
                }
            }
        }

        // Return an error response if no files were uploaded
        logger.error("No files were uploaded");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
    }
}
