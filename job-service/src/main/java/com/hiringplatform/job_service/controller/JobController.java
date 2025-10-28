package com.hiringplatform.job_service.controller;

import com.hiringplatform.job_service.client.AuthServiceClient;
import com.hiringplatform.job_service.client.CandidateServiceClient;
import com.hiringplatform.job_service.dto.UserDTO;
import com.hiringplatform.job_service.model.CandidateProfile;
import com.hiringplatform.job_service.model.JobPosting;
import com.hiringplatform.job_service.repository.JobPostingRepository;
import com.hiringplatform.job_service.repository.SearchRepository;
import com.hiringplatform.job_service.service.EmailService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for job posting management.
 * Handles job creation, retrieval, search, and application processing.
 */
@RestController
@RequestMapping("/posts")
public class JobController {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private CandidateServiceClient candidateServiceClient;

    /**
     * Retrieves all job postings.
     * @return List of all job postings
     */
    @GetMapping("/all")
    public List<JobPosting> getAllPosts() {
        return jobPostingRepository.findAll();
    }

    /**
     * Retrieves specific job posting by ID.
     * @param id Job posting ID
     * @return Job posting or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobPosting> getPostById(@PathVariable String id) {
        return jobPostingRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Searches job postings by text query.
     * @param text Search keyword
     * @return List of matching job postings
     */
    @GetMapping("/search/{text}")
    public List<JobPosting> search(@PathVariable String text) {
        return searchRepository.findByText(text);
    }

    /**
     * Creates new job posting (RECRUITER role required).
     * @param post Job posting data
     * @return Created job posting or error
     */
    @PostMapping("/add")
    public ResponseEntity<JobPosting> addPost(@RequestBody JobPosting post) {
        if (post.getRecruiterId() == null || post.getRecruiterId().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        post.setId(null);
        JobPosting savedPost = jobPostingRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    /**
     * Processes job application (JOB_SEEKER role required).
     * Fetches candidate profile and recruiter details, sends notification email.
     * @param jobId Job ID being applied for
     * @param applyingUserId Applicant user ID from header
     * @return Success or error message
     */
    @PostMapping("/apply/{jobId}")
    public ResponseEntity<String> applyForJob(@PathVariable String jobId,
                                               @RequestHeader("X-User-ID") String applyingUserId) {
        Optional<JobPosting> jobOpt = jobPostingRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found.");
        }
        JobPosting job = jobOpt.get();
        String recruiterId = job.getRecruiterId();
        CandidateProfile candidateProfile = null;
        try {
            ResponseEntity<CandidateProfile> profileResponse = candidateServiceClient
                    .getProfileByUserId(applyingUserId);
            if (profileResponse.getStatusCode() == HttpStatus.OK && profileResponse.getBody() != null) {
                candidateProfile = profileResponse.getBody();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Candidate profile not found or service error. Please ensure profile exists.");
            }
        } catch (FeignException.NotFound ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Candidate profile not found. Please create one first.");
        } catch (FeignException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error contacting candidate service.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred retrieving profile.");
        }
        if (candidateProfile == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve candidate profile details.");
        }
        String recruiterEmail = null;
        try {
            ResponseEntity<UserDTO> response = authServiceClient.getUserById(recruiterId);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                recruiterEmail = response.getBody().getEmail();
                if (recruiterEmail == null || recruiterEmail.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Recruiter email could not be determined from auth service response.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Could not retrieve recruiter details (Status: " + response.getStatusCode() + ")");
            }
        } catch (FeignException.NotFound ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Recruiter user associated with the job not found.");
        } catch (FeignException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error contacting authentication service.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred retrieving recruiter email.");
        }
        if (recruiterEmail == null || recruiterEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Recruiter email could not be determined.");
        }
        String subject = "New Application for " + job.getRole();
        String body = buildApplicationEmailBody(candidateProfile, job);
        try {
            emailService.sendEmail(recruiterEmail, subject, body);
        } catch (Exception e) {
        }
        return ResponseEntity.ok("Application submitted successfully!");
    }

    /**
     * Builds HTML email body for application notification.
     * @param profile Candidate profile
     * @param job Job posting details
     * @return HTML email body string
     */
    private String buildApplicationEmailBody(CandidateProfile profile, JobPosting job) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>New Application Received</h1>");
        sb.append("<p>A candidate has applied for the position: <strong>").append(job.getRole())
                .append("</strong> (Job ID: ").append(job.getId()).append(")</p>");
        sb.append("<hr>");
        sb.append("<h2>Candidate Details:</h2>");
        sb.append("<table border='0' cellpadding='5' style='border-collapse: collapse;'>");
        sb.append("<tr><td style='vertical-align: top;'><strong>Name:</strong></td><td>").append(profile.getFullName())
                .append("</td></tr>");
        sb.append("<tr><td style='vertical-align: top;'><strong>Email:</strong></td><td>").append(profile.getEmail())
                .append("</td></tr>");
        sb.append("<tr><td style='vertical-align: top;'><strong>Experience:</strong></td><td>")
                .append(profile.getTotalExperience()).append(" years</td></tr>");
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append("<tr><td style='vertical-align: top;'><strong>Skills:</strong></td><td>")
                    .append(String.join(", ", profile.getSkills())).append("</td></tr>");
        } else {
            sb.append("<tr><td style='vertical-align: top;'><strong>Skills:</strong></td><td>Not provided</td></tr>");
        }
        if (profile.getResumeUrl() != null && !profile.getResumeUrl().isEmpty()) {
            sb.append("<tr><td style='vertical-align: top;'><strong>Resume:</strong></td><td><a href='")
                    .append(profile.getResumeUrl()).append("' target='_blank'>View Resume</a></td></tr>");
        } else {
            sb.append("<tr><td style='vertical-align: top;'><strong>Resume:</strong></td><td>Not provided</td></tr>");
        }
        sb.append("</table>");
        sb.append("<hr>");
        sb.append("<p style='font-size: 0.9em; color: gray;'>This is an automated email from the Hiring Platform.</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
