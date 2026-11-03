package com.campus.lostfound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemMatch;
import com.campus.lostfound.model.ReportType;
import com.campus.lostfound.service.ItemMatchingService;
import java.util.List;
import org.junit.jupiter.api.Test;

class ItemMatchingServiceTests {

    private final ItemMatchingService matchingService = new ItemMatchingService();

    @Test
    void normalizesCasePunctuationAndAccentsForAnExactMatch() {
        Item lost = item("Blue bottle", "Drinkware", "Blue glass water bottle", "Café north", ReportType.LOST);
        Item found = item("blue-bottle", "drinkware", "blue glass water bottle", "cafe north", ReportType.FOUND);

        List<ItemMatch> matches = matchingService.rank(lost, List.of(found));

        assertEquals(1, matches.size());
        assertEquals(100, matches.get(0).score());
        assertEquals(100, matches.get(0).titleSimilarity());
        assertEquals(100, matches.get(0).categorySimilarity());
        assertEquals(100, matches.get(0).descriptionSimilarity());
        assertEquals(100, matches.get(0).locationSimilarity());
    }

    @Test
    void combinesFieldSimilarityWithTheDocumentedWeights() {
        Item lost = item("blue bottle", "drinkware", "blue glass bottle", "north hall", ReportType.LOST);
        Item found = item("blue bottle", "accessories", "red wallet", "south field", ReportType.FOUND);

        List<ItemMatch> matches = matchingService.rank(lost, List.of(found));

        assertEquals(1, matches.size());
        assertEquals(35, matches.get(0).score());
        assertEquals(100, matches.get(0).titleSimilarity());
        assertEquals(0, matches.get(0).categorySimilarity());
        assertEquals(0, matches.get(0).descriptionSimilarity());
        assertEquals(0, matches.get(0).locationSimilarity());
    }

    @Test
    void excludesSameTypeAndLowSimilarityReports() {
        Item lost = item("blue bottle", "drinkware", "blue glass bottle", "north hall", ReportType.LOST);
        Item sameType = item("blue bottle", "drinkware", "blue glass bottle", "north hall", ReportType.LOST);
        Item unrelated = item("wallet", "accessories", "brown leather wallet", "south field", ReportType.FOUND);

        assertTrue(matchingService.rank(lost, List.of(sameType, unrelated)).isEmpty());
    }

    private Item item(String title, String category, String description, String location, ReportType type) {
        Item item = new Item();
        item.setTitle(title);
        item.setCategory(category);
        item.setDescription(description);
        item.setLocation(location);
        item.setReportType(type);
        return item;
    }
}
