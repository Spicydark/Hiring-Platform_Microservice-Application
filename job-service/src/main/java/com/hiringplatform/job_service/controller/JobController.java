package com.hiringplatform.job_service.controller;

import com.hiringplatform.job_service.client.AuthServiceClient;
import com.hiringplatform.job_service.client.CandidateServiceClient; // Import Candidate Feign Client
import com.hiringplatform.job_service.dto.UserDTO;
import com.hiringplatform.job_service.model.CandidateProfile; // Import CandidateProfile (used as DTO)
import com.hiringplatform.job_service.model.JobPosting;
// REMOVE: CandidateProfileRepository import
// import com.hiringplatform.jobservice.repository.CandidateProfileRepository;
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
 * REST Controller for managing Job Posting operations. Exposes endpoints for
 * creating, retrieving, searching, and applying to jobs.
 */
@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "*", maxAge = 3600) // Allow cross-origin requests (adjust in production)
public class JobController {

	// --- Autowired Repositories, Services, and Clients ---

	@Autowired
	private JobPostingRepository jobPostingRepository; // Handles direct JobPosting data access

	@Autowired
	private SearchRepository searchRepository; // Handles advanced job searching

	// REMOVE: No longer directly access Candidate Profile data
	// @Autowired
	// private CandidateProfileRepository candidateProfileRepository;

	@Autowired
	private EmailService emailService; // Handles sending emails

	@Autowired
	private AuthServiceClient authServiceClient; // Feign client for Auth Service

	@Autowired
	private CandidateServiceClient candidateServiceClient; // Feign client for Candidate Service

	// --- Public Endpoints ---

	/**
	 * Retrieves all job postings. Endpoint: GET /posts/all
	 *
	 * @return A list of all JobPosting objects.
	 */
	@GetMapping("/all")
	public List<JobPosting> getAllPosts() {
		return jobPostingRepository.findAll();
	}

	/**
	 * Retrieves a single job posting by its unique ID. Endpoint: GET /posts/{id}
	 *
	 * @param id The ID of the job posting.
	 * @return ResponseEntity containing the JobPosting if found, or 404 Not Found.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<JobPosting> getPostById(@PathVariable String id) {
		return jobPostingRepository.findById(id).map(ResponseEntity::ok) // If found, return 200 OK with the job
				.orElse(ResponseEntity.notFound().build()); // If not found, return 404
	}

	/**
	 * Searches for job postings based on a text query. Endpoint: GET
	 * /posts/search/{text}
	 *
	 * @param text The search keyword.
	 * @return A list of JobPostings matching the search text.
	 */
	@GetMapping("/search/{text}")
	public List<JobPosting> search(@PathVariable String text) {
		// Delegates to the SearchRepository implementation (e.g., using Atlas Search)
		return searchRepository.findByText(text);
	}

	// --- Protected Endpoints ---

	/**
	 * Creates a new job posting. Requires RECRUITER role (enforced by
	 * Gateway/Security). Endpoint: POST /posts/add Expects recruiterId to be set in
	 * the request body.
	 *
	 * @param post The JobPosting data from the request body.
	 * @return ResponseEntity containing the created JobPosting or 400 Bad Request.
	 */
	@PostMapping("/add")
	public ResponseEntity<JobPosting> addPost(@RequestBody JobPosting post) {
		// Basic validation: Ensure recruiterId is provided in the request
		// More robust validation can be added (e.g., check if recruiterId exists via
		// Auth service)
		if (post.getRecruiterId() == null || post.getRecruiterId().isEmpty()) {
			System.err.println("Recruiter ID missing in job post request.");
			// Consider returning a more informative error message body
			return ResponseEntity.badRequest().body(null); // Indicate missing required field
		}
		// Ensure MongoDB generates a new ID
		post.setId(null);
		JobPosting savedPost = jobPostingRepository.save(post);
		// Return 201 Created status with the newly created job posting
		return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
	}

	/**
	 * Handles a job application request. Requires JOB_SEEKER role (enforced by
	 * Gateway/Security). Endpoint: POST /posts/apply/{jobId} Fetches details via
	 * Feign clients and sends an email notification. Expects the applying user's ID
	 * in the 'X-User-ID' header (added by Gateway).
	 *
	 * @param jobId          The ID of the job being applied for.
	 * @param applyingUserId The ID of the user applying (from header).
	 * @return ResponseEntity indicating success or failure.
	 */
	@PostMapping("/apply/{jobId}")
	public ResponseEntity<String> applyForJob(@PathVariable String jobId,
			@RequestHeader("X-User-ID") String applyingUserId) {

		// --- Step 1: Validate Job Posting ---
		Optional<JobPosting> jobOpt = jobPostingRepository.findById(jobId);
		if (jobOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found.");
		}
		JobPosting job = jobOpt.get();
		String recruiterId = job.getRecruiterId();

		// --- Step 2: Fetch Candidate Profile via Feign ---
		CandidateProfile candidateProfile = null;
		try {
			// Call the Candidate Service using the Feign client
			ResponseEntity<CandidateProfile> profileResponse = candidateServiceClient
					.getProfileByUserId(applyingUserId);

			// Check if the call was successful and data is present
			if (profileResponse.getStatusCode() == HttpStatus.OK && profileResponse.getBody() != null) {
				candidateProfile = profileResponse.getBody();
			} else {
				// Handle non-OK status from Candidate Service
				System.err.println(
						"Could not retrieve candidate profile (Status: " + profileResponse.getStatusCode() + ")");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Candidate profile not found or service error. Please ensure profile exists.");
			}
		} catch (FeignException.NotFound ex) {
			// Handle 404 specifically - profile doesn't exist for the user
			System.err.println(
					"Feign Client Error (NotFound): Candidate profile for user " + applyingUserId + " not found.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Candidate profile not found. Please create one first.");
		} catch (FeignException ex) {
			// Handle other Feign errors (network issues, service down, etc.)
			System.err.println("Feign Client Error calling Candidate Service: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error contacting candidate service.");
		} catch (Exception e) {
			// Catch any other unexpected exceptions during the call
			System.err.println("Unexpected error during candidate service call: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred retrieving profile.");
		}

		// Defensive check, although handled in try-catch
		if (candidateProfile == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to retrieve candidate profile details.");
		}

		// --- Step 3: Fetch Recruiter Email via Feign ---
		String recruiterEmail = null;
		try {
			// Call the Auth Service using the Feign client
			ResponseEntity<UserDTO> response = authServiceClient.getUserById(recruiterId);

			// Check if the call was successful and data is present
			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				recruiterEmail = response.getBody().getEmail();
				// Check specifically if the email field itself is missing/empty in the response
				if (recruiterEmail == null || recruiterEmail.isEmpty()) {
					System.err.println("Recruiter email is null or empty in UserDTO for user ID: " + recruiterId);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body("Recruiter email could not be determined from auth service response.");
				}
			} else {
				// Handle non-OK status from Auth Service
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Could not retrieve recruiter details (Status: " + response.getStatusCode() + ")");
			}
		} catch (FeignException.NotFound ex) {
			// Handle 404 specifically - recruiter user doesn't exist
			System.err.println(
					"Feign Client Error (NotFound): Recruiter user " + recruiterId + " not found in Auth Service.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Recruiter user associated with the job not found.");
		} catch (FeignException ex) {
			// Handle other Feign errors
			System.err.println("Feign Client Error calling Auth Service: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error contacting authentication service.");
		} catch (Exception e) {
			// Catch any other unexpected exceptions
			System.err.println("Unexpected error during auth service call: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred retrieving recruiter email.");
		}

		// Defensive check, also handled above
		if (recruiterEmail == null || recruiterEmail.isEmpty()) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Recruiter email could not be determined.");
		}

		// --- Step 4: Send Email Notification ---
		String subject = "New Application for " + job.getRole();
		String body = buildApplicationEmailBody(candidateProfile, job);
		try {
			// Use the injected EmailService
			emailService.sendEmail(recruiterEmail, subject, body);
		} catch (Exception e) {
			// Log email sending failure but potentially still return success to the user
			System.err.println(
					"Failed to send application notification email to " + recruiterEmail + ": " + e.getMessage());
			// Depending on requirements, you might return an error here instead.
			// For now, we'll log the error but still confirm application receipt.
			// return
			// ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Application
			// received but failed to send notification email.");
		}

		// Return success response to the applying user
		return ResponseEntity.ok("Application submitted successfully!");
	}

	/**
	 * Helper method to construct the HTML email body for the application
	 * notification.
	 *
	 * @param profile The profile of the applying candidate.
	 * @param job     The job posting being applied for.
	 * @return An HTML string representing the email body.
	 */
	private String buildApplicationEmailBody(CandidateProfile profile, JobPosting job) {
		// Using StringBuilder for efficient string concatenation
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>"); // Start HTML email
		sb.append("<h1>New Application Received</h1>");
		sb.append("<p>A candidate has applied for the position: <strong>").append(job.getRole())
				.append("</strong> (Job ID: ").append(job.getId()).append(")</p>");
		sb.append("<hr>"); // Add a separator

		sb.append("<h2>Candidate Details:</h2>");
		sb.append("<table border='0' cellpadding='5' style='border-collapse: collapse;'>"); // Use a simple table for
																							// layout
		sb.append("<tr><td style='vertical-align: top;'><strong>Name:</strong></td><td>").append(profile.getFullName())
				.append("</td></tr>");
		sb.append("<tr><td style='vertical-align: top;'><strong>Email:</strong></td><td>").append(profile.getEmail())
				.append("</td></tr>");
		sb.append("<tr><td style='vertical-align: top;'><strong>Experience:</strong></td><td>")
				.append(profile.getTotalExperience()).append(" years</td></tr>");

		// Check if skills list is available and not empty
		if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
			sb.append("<tr><td style='vertical-align: top;'><strong>Skills:</strong></td><td>")
					.append(String.join(", ", profile.getSkills())).append("</td></tr>");
		} else {
			sb.append("<tr><td style='vertical-align: top;'><strong>Skills:</strong></td><td>Not provided</td></tr>");
		}

		// Check if resume URL is available
		if (profile.getResumeUrl() != null && !profile.getResumeUrl().isEmpty()) {
			// Make the link clickable
			sb.append("<tr><td style='vertical-align: top;'><strong>Resume:</strong></td><td><a href=\"")
					.append(profile.getResumeUrl()).append("\">View Resume</a></td></tr>");
		} else {
			sb.append("<tr><td style='vertical-align: top;'><strong>Resume:</strong></td><td>Not provided</td></tr>");
		}

		sb.append("</table>"); // End table
		sb.append("<hr>"); // Add another separator

		sb.append(
				"<p style='font-size: smaller; color: #555;'>Please review the candidate's details. You can contact them directly via their email address.</p>");
		sb.append("</body></html>"); // End HTML email
		return sb.toString();
	}
}