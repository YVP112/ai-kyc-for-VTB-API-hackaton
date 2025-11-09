package com.mvp.kyc.controller;

import com.mvp.kyc.model.KycResult;
import com.mvp.kyc.service.AiKycOcrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/kyc/ocr")
public class AiKycOcrController {

    private final AiKycOcrService service;

    public AiKycOcrController(AiKycOcrService service) {
        this.service = service;
    }

    @PostMapping("/passport")
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file) {
        try {
            KycResult result = service.analyze(file.getBytes());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new KycResult(false, e.getMessage()));
        }
    }
}
