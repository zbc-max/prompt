package com.lowcode.generator.controller;

import com.lowcode.generator.model.GenModels.GenContext;
import com.lowcode.generator.service.CodeGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gen")
public class GeneratorController {

    private final CodeGeneratorService codeGeneratorService;

    public GeneratorController(CodeGeneratorService codeGeneratorService) {
        this.codeGeneratorService = codeGeneratorService;
    }

    @PostMapping("/preview")
    public Map<String, String> preview(@RequestBody GenContext context) {
        return codeGeneratorService.generatePreview(context);
    }

    @PostMapping("/zip")
    public ResponseEntity<byte[]> zip(@RequestBody GenContext context) {
        byte[] bytes = codeGeneratorService.generateZip(context);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=codegen.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
