package com.indexing.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Vector utility functions for similarity calculations.
 * Used by VectorIndex for semantic search.
 */
public class VectorUtils {

    /**
     * Computes cosine similarity between two vectors.
     * @param vec1 first vector
     * @param vec2 second vector
     * @return cosine similarity score (0 to 1, higher = more similar)
     */
    public static double cosineSimilarity(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }

        RealVector v1 = new ArrayRealVector(vec1);
        RealVector v2 = new ArrayRealVector(vec2);

        double dotProduct = v1.dotProduct(v2);
        double norm1 = v1.getNorm();
        double norm2 = v2.getNorm();

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    /**
     * Computes Euclidean distance between two vectors.
     * @param vec1 first vector
     * @param vec2 second vector
     * @return Euclidean distance
     */
    public static double euclideanDistance(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }

        double sum = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            double diff = vec1[i] - vec2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Normalizes a vector to unit length.
     * @param vector vector to normalize
     * @return normalized vector
     */
    public static double[] normalize(double[] vector) {
        RealVector v = new ArrayRealVector(vector);
        RealVector normalized = v.mapDivide(v.getNorm());
        return normalized.toArray();
    }

    /**
     * Computes L2 norm of a vector.
     * @param vector vector to compute norm for
     * @return L2 norm
     */
    public static double norm(double[] vector) {
        return new ArrayRealVector(vector).getNorm();
    }

    /**
     * Checks if vectors have valid dimensions (non-empty, same size).
     * @param vectors vectors to check
     * @return true if all vectors are valid and same dimension
     */
    public static boolean validateVectors(double[]... vectors) {
        if (vectors.length == 0) {
            return false;
        }

        int dim = vectors[0].length;
        if (dim == 0) {
            return false;
        }

        for (double[] vec : vectors) {
            if (vec.length != dim) {
                return false;
            }
        }

        return true;
    }
}
