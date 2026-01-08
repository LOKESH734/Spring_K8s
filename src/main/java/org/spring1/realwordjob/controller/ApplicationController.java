package org.spring1.realwordjob.controller;

import org.spring1.realwordjob.DTO.JobStatsDTO;
import org.spring1.realwordjob.model.Application;
import org.spring1.realwordjob.model.ApplicationStatus;
import org.spring1.realwordjob.model.User;
import org.spring1.realwordjob.service.ApplicationService;
import org.spring1.realwordjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@RestController
@RequestMapping("/applications")
@Tag(name = "Applications", description = "Job application management APIs")
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ============== APPLY FOR JOB ==============
    @PostMapping(value = "/apply", consumes = {"multipart/form-data"})
    @Operation(summary = "Apply for a job", description = "Submit an application with resume upload")
    public ResponseEntity<Map<String, Object>> applyForJob(
            @Parameter(description = "Job ID being applied for", example = "202", required = true)
            @RequestParam Long jobId,

            @Parameter(description = "Resume file (PDF or DOCX)", required = true,
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestPart("resume") MultipartFile resumeFile,

            Authentication authentication
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email).orElse(null);
            if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));

            Long userId = user.getId();

            if (resumeFile == null || resumeFile.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Resume file missing"));

            if (applicationService.existsByUserIdAndJobId(userId, jobId))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Already applied for this job"));

            Application application = new Application();
            application.setUserId(userId);
            application.setJobId(jobId);
            application.setResume(resumeFile.getBytes());
            application.setStatus(ApplicationStatus.PENDING);
            applicationService.save(application);

            // Send confirmation email
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Application Submitted Successfully");
                message.setText("Hello " + user.getName() + ",\n\nYour application for Job ID " + jobId + " has been submitted successfully.");
                mailSender.send(message);
            } catch (Exception ignored) {}

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "applicationId", application.getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    // ============== GET ALL APPLICATIONS ==============
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    // ============== DOWNLOAD RESUME ==============
    @GetMapping("/{applicationId}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long applicationId) {
        return applicationService.findById(applicationId)
                .map(application -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=resume_" + applicationId + ".pdf");
                    headers.set(HttpHeaders.CONTENT_TYPE, "application/pdf"); // important
                    return new ResponseEntity<>(application.getResume(), headers, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ============== UPDATE STATUS ==============
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String customMessage) {

        return applicationService.findById(applicationId)
                .map(application -> {
                    application.setStatus(status);
                    applicationService.save(application);

                    User user = userService.findUser(application.getUserId());
                    if (user != null && customMessage != null && !customMessage.isEmpty()) {
                        try {
                            SimpleMailMessage message = new SimpleMailMessage();
                            message.setTo(user.getEmail());
                            message.setSubject("Application Status Update");
                            message.setText("Hello " + user.getName() + ",\n\nYour application status has been updated to: "
                                    + status + "\nMessage: " + customMessage);
                            mailSender.send(message);
                        } catch (Exception ignored) {}
                    }

                    return ResponseEntity.ok(Map.of("status", "success", "application", application));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Application not found")));
    }

    // ============== JOB STATS ==============
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobStatsDTO>> getJobStats() {
        return ResponseEntity.ok(applicationService.getJobStatistics());
    }
}
