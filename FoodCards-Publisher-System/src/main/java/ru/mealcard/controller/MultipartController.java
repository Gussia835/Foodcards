package ru.mealcard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mealcard.service.impl.visitor.validator.EnrollValidator;
import ru.mealcard.utils.Constants;
import ru.mealcard.utils.filename.FileManager;
import ru.mealcard.service.FileReceiverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class MultipartController {
    private final FileReceiverService service;
    private final FileManager fileManager;
    private final EnrollValidator validator;

    @PostMapping("multipart")
    public ResponseEntity<String> getMultipart(@RequestParam("file")MultipartFile file) {
        if (file.isEmpty()) {
            log.error(Constants.MSG_FILE_EMPTY);
            return ResponseEntity.badRequest().body(Constants.MSG_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        log.info("Received multipart file: {}", filename);

        if (!validator.isValidFilename(Paths.get(filename))) {
            log.error("{}: {}", Constants.MSG_INVALID_FILENAME, filename);
            return ResponseEntity.badRequest().body(Constants.MSG_INVALID_FILENAME);
        }

        Path filepath = fileManager.writeFile(filename, file);
        service.processFile(filepath);

        return ResponseEntity.ok(Constants.MSG_MULTIPART_SUCCESS);
    }
}
