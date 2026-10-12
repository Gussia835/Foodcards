package ru.mealcard.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mealcard.utils.Constants;
import ru.mealcard.utils.filename.FileManager;
import ru.mealcard.service.FileReceiverService;
import ru.mealcard.service.impl.visitor.validator.EnrollValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class ChunkController {
    private final FileReceiverService service;
    private final FileManager fileManager;
    private final EnrollValidator validator;

    @PostMapping("/chunk")
    public ResponseEntity<String> getChunk(@RequestHeader("filename") String filename,
                                           InputStream requestStream
                                           ) {
        log.info("Received chunked stream for file: {}", filename);

        if (!validator.isValidFilename(Paths.get(filename))) {
            log.error("Chunk upload rejected. {}: {}", Constants.MSG_INVALID_FILENAME, filename);
            return ResponseEntity.badRequest().body(Constants.MSG_INVALID_FILENAME);
        }
        Path filepath = fileManager.saveChunk(filename, requestStream);
        service.processFile(filepath);

        return ResponseEntity.ok(Constants.MSG_UPLOAD_SUCCESS);
    }
}
