package com.weihhsu.app.resource;

import com.example.api.BrushUpResumeApi;
import com.example.model.ResumeUpdateRequest;
import com.weihhsu.app.service.BrushUpResumeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brush_up_resume")
public class CoreResource implements BrushUpResumeApi {
    private final BrushUpResumeService brushUpResumeService;

    public CoreResource(BrushUpResumeService brushUpResumeService) {
        this.brushUpResumeService = brushUpResumeService;
    }

    @Operation(summary = "Get brush-up resume info")
    @GetMapping
    public ResponseEntity<Void> brushUpResume() {
        String result = brushUpResumeService.getBrushUpResume();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update resume")
    @PostMapping("/updateResume")
    public ResponseEntity<Void> updateResume(@Valid @RequestBody ResumeUpdateRequest request) {
        brushUpResumeService.updateResume(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Submit request")
    @PostMapping("/submit")
    public ResponseEntity<Void> submitRequest() {
        brushUpResumeService.submitRequest();
        return ResponseEntity.ok().build();
    }
}
