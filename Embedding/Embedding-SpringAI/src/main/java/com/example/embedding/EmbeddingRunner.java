package com.example.embedding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.knowm.xchart.*;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Orchestrates the full embedding-similarity pipeline on application startup.
 *
 * <ol>
 *   <li>Load tickets from JSON</li>
 *   <li>Generate embeddings via {@link EmbeddingService}</li>
 *   <li>Embed the query and compute cosine similarities</li>
 *   <li>Print the top-K results to the console</li>
 *   <li>Write {@code similarity_matrix.csv}</li>
 *   <li>Save {@code query_similarity.png}</li>
 * </ol>
 *
 * All tuneable parameters are read from {@code application.properties}.
 */
@Component
public class EmbeddingRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingRunner.class);

    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Value("${embedding.tickets-file:synthetic_tickets.json}")
    private String ticketsFile;

    @Value("${embedding.query:Users can't login after changing password}")
    private String query;

    @Value("${embedding.top-k:5}")
    private int topK;

    public EmbeddingRunner(EmbeddingService embeddingService, ObjectMapper objectMapper) {
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        // ── 1. Load tickets ──────────────────────────────────────────────────
        log.info("Loading tickets from '{}'…", ticketsFile);
        List<Ticket> tickets = objectMapper.readValue(
                new File(ticketsFile),
                new TypeReference<List<Ticket>>() {}
        );
        log.info("Loaded {} tickets.", tickets.size());

        // ── 2. Build text corpus ─────────────────────────────────────────────
        List<String> texts = new ArrayList<>();
        for (Ticket t : tickets) {
            texts.add(t.title + ". " + t.description);
        }

        // ── 3. Embed corpus ──────────────────────────────────────────────────
        double[][] embeddings = embeddingService.embed(texts);

        // ── 4. Embed query ───────────────────────────────────────────────────
        log.info("Embedding query: '{}'", query);
        double[][] queryResult = embeddingService.embed(Collections.singletonList(query));
        double[] queryEmbedding = queryResult[0];

        // ── 5. Compute cosine similarities ───────────────────────────────────
        double[] similarities = new double[tickets.size()];
        for (int i = 0; i < tickets.size(); i++) {
            similarities[i] = embeddingService.cosineSimilarity(queryEmbedding, embeddings[i]);
        }

        // ── 6. Top-K results ─────────────────────────────────────────────────
        int k = Math.min(topK, tickets.size());
        int[] indices = embeddingService.topKIndices(similarities, k);

        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.printf("  Top %d most similar tickets to query: \"%s\"%n", k, query);
        System.out.println("══════════════════════════════════════════════════════════\n");

        for (int rank = 0; rank < indices.length; rank++) {
            int idx = indices[rank];
            Ticket t = tickets.get(idx);
            String desc = t.description != null
                    ? t.description.substring(0, Math.min(150, t.description.length())) + "…"
                    : "";
            System.out.printf("#%d  [%.4f]  %s%n", rank + 1, similarities[idx], t.ticket_id);
            System.out.printf("    Title    : %s%n", t.title);
            System.out.printf("    Category : %-20s  Priority : %s%n", t.category, t.priority);
            System.out.printf("    Desc     : %s%n%n", desc);
        }

        // ── 7. Build selection list for visualisation (topK + up to 5 extras) ─
        int numExtra = Math.min(5, tickets.size() - k);
        List<Integer> selected = new ArrayList<>();
        for (int idx : indices) selected.add(idx);

        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < tickets.size(); i++) {
            if (!selected.contains(i)) pool.add(i);
        }
        Collections.shuffle(pool, new Random(123));
        for (int i = 0; i < Math.min(numExtra, pool.size()); i++) {
            selected.add(pool.get(i));
        }
        int nSel = selected.size();

        // ── 8. Similarity matrix CSV ─────────────────────────────────────────
        StringBuilder csv = new StringBuilder("id");
        for (int i : selected) csv.append(",").append(tickets.get(i).ticket_id);
        csv.append("\n");

        for (int i = 0; i < nSel; i++) {
            csv.append(tickets.get(selected.get(i)).ticket_id);
            for (int j = 0; j < nSel; j++) {
                double val = embeddingService.cosineSimilarity(
                        embeddings[selected.get(i)],
                        embeddings[selected.get(j)]
                );
                csv.append(",").append(String.format("%.6f", val));
            }
            csv.append("\n");
        }
        Files.write(Path.of("similarity_matrix.csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
        log.info("Saved similarity_matrix.csv");

        // ── 9. Query-similarity bar chart ────────────────────────────────────
        List<String> labels = new ArrayList<>();
        List<Double> querySims = new ArrayList<>();
        for (int i : selected) {
            labels.add(tickets.get(i).ticket_id);
            querySims.add(similarities[i]);
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(900).height(600)
                .title("Similarity to Query: \"" + query + "\"")
                .xAxisTitle("Ticket ID")
                .yAxisTitle("Cosine Similarity")
                .build();
        chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
        chart.addSeries("Similarity", labels, querySims);
        BitmapEncoder.saveBitmap(chart, "query_similarity", BitmapFormat.PNG);
        log.info("Saved query_similarity.png");

        System.out.println("Done. Artefacts written to working directory.");
    }
}
