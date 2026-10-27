package com.example.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Spring AI-backed embedding service.
 *
 * <p>Uses the auto-configured {@link EmbeddingModel} bean (backed by OpenAI) to produce
 * dense vector embeddings for a list of text inputs.  If the API key is absent or the call
 * fails the service falls back to deterministic random embeddings so the rest of the
 * pipeline can still be exercised without a live key.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final int EMBEDDING_DIM = 1536;

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Embed a batch of texts.  Returns an {@code n × EMBEDDING_DIM} matrix.
     *
     * @param texts list of strings to embed
     * @return 2-D array of float embeddings (one row per input)
     */
    public double[][] embed(List<String> texts) {
        try {
            log.info("Requesting embeddings for {} texts via Spring AI / OpenAI…", texts.size());
            EmbeddingResponse response = embeddingModel.embedForResponse(texts);
            double[][] result = new double[texts.size()][EMBEDDING_DIM];
            for (int i = 0; i < texts.size(); i++) {
                float[] floats = response.getResults().get(i).getOutput();
                for (int j = 0; j < floats.length && j < EMBEDDING_DIM; j++) {
                    result[i][j] = floats[j];
                }
            }
            log.info("Received {} embeddings (dim={})", result.length, EMBEDDING_DIM);
            return result;
        } catch (Exception ex) {
            log.warn("Embedding API call failed ({}). Falling back to random embeddings.", ex.getMessage());
            return generateRandomEmbeddings(texts.size(), EMBEDDING_DIM, 42);
        }
    }

    /**
     * Compute cosine similarity between two vectors.
     *
     * @param a first vector
     * @param b second vector
     * @return cosine similarity in [-1, 1]
     */
    public double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-12);
    }

    /**
     * Return the indices of the top-K highest values in {@code scores}, sorted descending.
     */
    public int[] topKIndices(double[] scores, int k) {
        return IntStream.range(0, scores.length)
                .boxed()
                .sorted((a, b) -> Double.compare(scores[b], scores[a]))
                .mapToInt(Integer::intValue)
                .limit(k)
                .toArray();
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /**
     * Generate deterministic unit-normalised random embeddings.
     * Used as a fallback when no OpenAI API key is configured.
     */
    public double[][] generateRandomEmbeddings(int n, int dim, long seed) {
        Random rnd = new Random(seed);
        double[][] out = new double[n][dim];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < dim; j++) {
                out[i][j] = rnd.nextDouble();
            }
            double norm = norm(out[i]);
            for (int j = 0; j < dim; j++) out[i][j] /= (norm + 1e-12);
        }
        return out;
    }

    private double norm(double[] a) {
        double s = 0.0;
        for (double v : a) s += v * v;
        return Math.sqrt(s);
    }

    public int getEmbeddingDim() {
        return EMBEDDING_DIM;
    }
}
