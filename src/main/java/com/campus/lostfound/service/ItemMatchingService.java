package com.campus.lostfound.service;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemMatch;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ItemMatchingService {

    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final int MINIMUM_SCORE = 15;
    private static final int MAX_MATCHES = 5;

    public List<ItemMatch> rank(Item report, List<Item> candidates) {
        List<ItemMatch> matches = new ArrayList<>();
        for (Item candidate : candidates) {
            if ((report.getId() != null && Objects.equals(candidate.getId(), report.getId()))
                    || candidate.getReportType() == report.getReportType()) {
                continue;
            }

            int title = similarity(report.getTitle(), candidate.getTitle());
            int category = similarity(report.getCategory(), candidate.getCategory());
            int description = similarity(report.getDescription(), candidate.getDescription());
            int location = similarity(report.getLocation(), candidate.getLocation());
            int score = Math.round(title * 0.35f + category * 0.20f
                    + description * 0.25f + location * 0.20f);

            if (score >= MINIMUM_SCORE) {
                matches.add(new ItemMatch(candidate, score, title, category, description, location));
            }
        }

        return matches.stream()
                .sorted(Comparator.comparingInt(ItemMatch::score).reversed()
                        .thenComparing(match -> match.item().getCreatedAt(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_MATCHES)
                .toList();
    }

    private int similarity(String left, String right) {
        Set<String> leftWords = words(left);
        Set<String> rightWords = words(right);
        if (leftWords.isEmpty() || rightWords.isEmpty()) {
            return 0;
        }

        Set<String> intersection = new HashSet<>(leftWords);
        intersection.retainAll(rightWords);
        Set<String> union = new HashSet<>(leftWords);
        union.addAll(rightWords);
        return Math.round(100.0f * intersection.size() / union.size());
    }

    private Set<String> words(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        Set<String> words = new HashSet<>();
        for (String word : WORD_SEPARATOR.split(normalized)) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }
        return words;
    }
}
