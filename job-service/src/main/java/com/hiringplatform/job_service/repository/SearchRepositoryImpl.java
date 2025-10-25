package com.hiringplatform.job_service.repository;

import com.hiringplatform.job_service.model.JobPosting;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Search repository implementation using MongoDB Atlas Search.
 * Requires Atlas Search index named 'default' on JobPostings collection.
 */
@Repository
public class SearchRepositoryImpl implements SearchRepository {

    @Autowired
    private MongoClient client;

    @Autowired
    private MongoConverter converter;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private static final String COLLECTION_NAME = "JobPostings";

    /**
     * Executes Atlas Search aggregation pipeline for job search.
     * @param text Search query text
     * @return List of matching job postings
     */
    @Override
    public List<JobPosting> findByText(String text) {

        final List<JobPosting> posts = new ArrayList<>();

        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
                new Document("$search",
                        new Document("index", "default")
                                .append("text",
                                        new Document("query", text)
                                                .append("path", Arrays.asList("role", "description", "skillSet"))
                                )
                ),
                new Document("$sort",
                        new Document("experience", 1L)
                ),
                new Document("$limit", 10L)
        ));

        result.forEach(doc -> posts.add(converter.read(JobPosting.class, doc)));

        return posts;
    }
}
