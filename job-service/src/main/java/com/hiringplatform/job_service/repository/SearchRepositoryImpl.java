package com.hiringplatform.job_service.repository;

import com.hiringplatform.job_service.model.JobPosting;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Repository; // Use Repository for implementation

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the SearchRepository using MongoDB aggregation pipeline with $search (Atlas Search).
 * Requires a MongoDB Atlas Search index named 'default' on the 'JobPostings' collection
 * covering fields like 'role', 'description', and 'skillSet'.
 */
@Repository // Mark this as a Spring component
public class SearchRepositoryImpl implements SearchRepository {

    @Autowired
    private MongoClient client; // Injects the configured MongoDB client

    @Autowired
    private MongoConverter converter; // Used to convert BSON Documents back to JobPosting objects

    @Value("${spring.data.mongodb.database}") // Inject database name from properties
    private String databaseName;

    private static final String COLLECTION_NAME = "JobPostings";

    /**
     * Executes the Atlas Search aggregation pipeline.
     *
     * @param text The search query string provided by the user.
     * @return A list of JobPostings matching the search criteria.
     */
    @Override
    public List<JobPosting> findByText(String text) {

        final List<JobPosting> posts = new ArrayList<>();

        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        // Define the Atlas Search aggregation pipeline stages
        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
                // Stage 1: $search - Performs the full-text search using the 'default' index
                new Document("$search",
                        new Document("index", "default") // Assumes an Atlas Search index named 'default'
                                .append("text",
                                        new Document("query", text)
                                                // Search across these fields
                                                .append("path", Arrays.asList("role", "description", "skillSet"))
                                )
                ),
                // Stage 2: $sort (Optional) - Sort results, e.g., by experience or relevance score
                new Document("$sort",
                        new Document("experience", 1L) // Example: Sort by experience ascending
                        // For relevance sorting: new Document("score", new Document("$meta", "searchScore"))
                ),
                // Stage 3: $limit (Optional) - Limit the number of results returned
                new Document("$limit", 10L) // Example: Limit to top 10 results
        ));

        // Convert the resulting BSON Documents back into JobPosting Java objects
        result.forEach(doc -> posts.add(converter.read(JobPosting.class, doc)));

        return posts;
    }
}
