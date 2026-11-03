package com.campus.lostfound.model;

/**
 * An explainable similarity score between opposite LOST and FOUND reports.
 * The score is a review aid, not a probability.
 */
public record ItemMatch(
        Item item,
        int score,
        int titleSimilarity,
        int categorySimilarity,
        int descriptionSimilarity,
        int locationSimilarity
) {
}
