package com.weihhsu.app.service;

import com.example.model.ResumeUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Service layer for brush-up resume operations.
 * Handles fetching, updating, and submitting resumes.
 */
@Component
public class BrushUpResumeService {

    // Simulate storage for a single user’s resume
    private final AtomicReference<String> resumeStorage = new AtomicReference<>("Initial resume content");

    // Simulate submission status
    private boolean submitted = false;

    /**
     * Fetch the current brush-up resume content.
     * @return the current resume content
     */
    public String getBrushUpResume() {
        return resumeStorage.get();
    }

    /**
     * Update the user's resume content.
     * @param request contains the new resume content
     */
    public void updateResume(ResumeUpdateRequest request) {
        if (request.getResumeContent() == null || request.getResumeContent().isBlank()) {
            throw new IllegalArgumentException("Resume content cannot be empty");
        }
        resumeStorage.set(request.getResumeContent());
        System.out.println("Resume updated to: " + request.getResumeContent());
    }

    /**
     * Submit the current resume.
     * Once submitted, the resume cannot be modified.
     */
    public void submitRequest() {
        if (submitted) {
            throw new IllegalStateException("Resume has already been submitted");
        }
        submitted = true;
        System.out.println("Resume submitted successfully: " + resumeStorage.get());
    }

    /**
     * Check if the resume has been submitted.
     * @return true if submitted, false otherwise
     */
    public boolean isSubmitted() {
        return submitted;
    }
}
