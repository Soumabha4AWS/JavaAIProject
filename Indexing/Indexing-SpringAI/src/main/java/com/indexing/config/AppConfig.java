package com.indexing.config;

import com.indexing.index.HybridIndex;
import com.indexing.index.KeywordIndex;
import com.indexing.index.SummaryIndex;
import com.indexing.index.VectorIndex;
import com.indexing.model.Document;
import com.indexing.util.OpenAIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public OpenAIClient openAIClient(ConfigurationManager config) {
        return new OpenAIClient(config);
    }

    @Bean
    public List<Document> documents() throws IOException {
        try {
            return DataLoader.loadDocumentsFromDefaultLocation();
        } catch (IOException e1) {
            logger.warn("Could not find synthetic_tickets.json in default locations, trying relative paths...");
            String[] paths = {
                    "synthetic_tickets.json",
                    "../synthetic_tickets.json",
                    "../../synthetic_tickets.json"
            };
            for (String path : paths) {
                try {
                    return DataLoader.loadDocuments(path);
                } catch (IOException ignored) {
                }
            }
            throw new IOException("Could not find synthetic_tickets.json.");
        }
    }

    @Bean
    public VectorIndex vectorIndex(OpenAIClient openAIClient, ConfigurationManager config) {
        // Index is created empty; buildIndex() is called by IndexInitializationService on startup
        return new VectorIndex(openAIClient, config.getEmbeddingModel());
    }

    @Bean
    public SummaryIndex summaryIndex(OpenAIClient openAIClient, ConfigurationManager config) {
        return new SummaryIndex(openAIClient, config.getLlmModel());
    }

    @Bean
    public KeywordIndex keywordIndex(OpenAIClient openAIClient, ConfigurationManager config) {
        return new KeywordIndex(openAIClient, config.getLlmModel());
    }

    @Bean
    public HybridIndex hybridIndex(VectorIndex vectorIndex, KeywordIndex keywordIndex) {
        return new HybridIndex(vectorIndex, keywordIndex);
    }
}
