package com.example.embedding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.Styler.LegendPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class App {
    private static final int EMBEDDING_DIM = 1536;
    private static final String EMBEDDING_MODEL = "text-embedding-3-small";

    public static void main(String[] args) throws Exception {
        System.out.println("Loading tickets...");
        ObjectMapper mapper = new ObjectMapper();
        List<Ticket> tickets = mapper.readValue(new File("synthetic_tickets.json"), new TypeReference<List<Ticket>>(){});
        System.out.println("Loaded " + tickets.size() + " tickets");

        List<String> texts = new ArrayList<>();
        for (Ticket t : tickets) {
            texts.add(t.title + ". " + t.description);
        }

        double[][] embeddings;
        String apiKey = "";
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("OPENAI_API_KEY found — requesting embeddings from OpenAI...");
            embeddings = fetchEmbeddingsFromOpenAI(apiKey, texts, mapper);
        } else {
            System.out.println("No OPENAI_API_KEY found — generating deterministic random embeddings for demo");
            embeddings = generateRandomEmbeddings(texts.size(), EMBEDDING_DIM, 42);
        }

        // Create a query and its embedding
        String query = "Users can't login after changing password";
        double[] queryEmbedding;
        if (apiKey != null && !apiKey.isEmpty()) {
            List<String> q = Collections.singletonList(query);
            double[][] qEmb = fetchEmbeddingsFromOpenAI(apiKey, q, mapper);
            queryEmbedding = qEmb[0];
        } else {
            queryEmbedding = generateRandomEmbeddings(1, EMBEDDING_DIM, 999)[0];
        }

        // Compute similarities
        double[] similarities = new double[tickets.size()];
        for (int i = 0; i < tickets.size(); i++) {
            similarities[i] = cosineSimilarity(queryEmbedding, embeddings[i]);
        }

        // Get top K
        int topK = Math.min(5, tickets.size());
        int[] indices = args.length > 0 ? parseTopKArg(args[0], tickets.size()) : argsTopKIndices(similarities, topK);

        System.out.println("\nTop " + topK + " most similar tickets to query: '" + query + "'\n");
        for (int rank = 0; rank < indices.length; rank++) {
            int idx = indices[rank];
            System.out.println("#" + (rank+1) + " - Similarity: " + String.format("%.4f", similarities[idx]));
            System.out.println("Ticket ID: " + tickets.get(idx).ticket_id);
            System.out.println("Title: " + tickets.get(idx).title);
            System.out.println("Category: " + tickets.get(idx).category + " | Priority: " + tickets.get(idx).priority);
            System.out.println("Description: " + (tickets.get(idx).description != null ? tickets.get(idx).description.substring(0, Math.min(150, tickets.get(idx).description.length())) : "") + "...\n");
        }

        // Prepare selected indices for visualization (topK + up to 5 random others)
        int numExtra = Math.min(5, tickets.size() - topK);
        List<Integer> selectedList = new ArrayList<>();
        for (int idx : indices) selectedList.add(idx);
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < tickets.size(); i++) if (!selectedList.contains(i)) pool.add(i);
        Collections.shuffle(pool, new Random(123));
        for (int i = 0; i < Math.min(numExtra, pool.size()); i++) selectedList.add(pool.get(i));
        int nSel = selectedList.size();

        // Compute similarity matrix for selected tickets and write CSV
        double[][] similarityMatrix = new double[nSel][nSel];
        StringBuilder sb = new StringBuilder();
        sb.append("id");
        for (int i = 0; i < nSel; i++) sb.append(",").append(tickets.get(selectedList.get(i)).ticket_id);
        sb.append("\n");
        for (int i = 0; i < nSel; i++) {
            sb.append(tickets.get(selectedList.get(i)).ticket_id);
            for (int j = 0; j < nSel; j++) {
                double sVal = cosineSimilarity(embeddings[selectedList.get(i)], embeddings[selectedList.get(j)]);
                similarityMatrix[i][j] = sVal;
                sb.append(",").append(String.format("%.6f", sVal));
            }
            sb.append("\n");
        }
        java.nio.file.Files.write(java.nio.file.Path.of("similarity_matrix.csv"), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        System.out.println("Saved similarity_matrix.csv");

        // Create bar chart for query similarities of selected tickets
        java.util.List<String> labels = new ArrayList<>();
        double[] querySims = new double[nSel];
        for (int i = 0; i < nSel; i++) {
            labels.add(tickets.get(selectedList.get(i)).ticket_id);
            querySims[i] = similarities[selectedList.get(i)];
        }

        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title("Similarity to Query").xAxisTitle("Ticket").yAxisTitle("Similarity").build();
        chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
        chart.addSeries("Similarity", labels, java.util.Arrays.stream(querySims).boxed().toList());
        BitmapEncoder.saveBitmap(chart, "query_similarity", BitmapFormat.PNG);
        System.out.println("Saved query_similarity.png");

        System.out.println("Done.");
    }

    private static int[] argsTopKIndices(double[] similarities, int topK) {
        return IntStream.range(0, similarities.length)
                .boxed()
                .sorted((a,b) -> Double.compare(similarities[b], similarities[a]))
                .mapToInt(Integer::intValue)
                .limit(topK)
                .toArray();
    }

    private static int[] parseTopKArg(String arg, int size) {
        try {
            int k = Integer.parseInt(arg);
            k = Math.max(1, Math.min(k, size));
            // Return indices of top k
            int[] idxs = new int[k];
            for (int i = 0; i < k; i++) idxs[i] = i;
            return idxs;
        } catch (Exception e) {
            return new int[0];
        }
    }

    private static double[][] generateRandomEmbeddings(int n, int dim, long seed) {
        Random rnd = new Random(seed);
        double[][] out = new double[n][dim];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < dim; j++) {
                out[i][j] = rnd.nextDouble();
            }
            // normalize
            double norm = norm(out[i]);
            for (int j = 0; j < dim; j++) out[i][j] /= (norm + 1e-12);
        }
        return out;
    }

    private static double[][] fetchEmbeddingsFromOpenAI(String apiKey, List<String> inputs, ObjectMapper mapper) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        // Build payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", EMBEDDING_MODEL);
        payload.put("input", inputs);
        String json = mapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/embeddings")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String respBody = response.body().string();
            JsonNode root = mapper.readTree(respBody);
            JsonNode data = root.get("data");
            int n = data.size();
            double[][] embeddings = new double[n][EMBEDDING_DIM];
            for (int i = 0; i < n; i++) {
                JsonNode emb = data.get(i).get("embedding");
                for (int j = 0; j < EMBEDDING_DIM; j++) {
                    embeddings[i][j] = emb.get(j).asDouble();
                }
            }
            return embeddings;
        }
    }

    private static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-12);
    }

    private static double norm(double[] a) {
        double s = 0.0;
        for (double v : a) s += v * v;
        return Math.sqrt(s);
    }
}
