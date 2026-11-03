package com.campus.lostfound.service;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemForm;
import com.campus.lostfound.model.ItemMatch;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ReportType;
import com.campus.lostfound.model.Student;
import com.campus.lostfound.repository.ItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMatchingService itemMatchingService;

    public ItemService(ItemRepository itemRepository, ItemMatchingService itemMatchingService) {
        this.itemRepository = itemRepository;
        this.itemMatchingService = itemMatchingService;
    }

    @Transactional(readOnly = true)
    public List<Item> search(String query, ReportType reportType) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return itemRepository.searchPublicItems(normalizedQuery, reportType);
    }

    @Transactional(readOnly = true)
    public Optional<Item> findPublicById(Long id) {
        return itemRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ItemMatch> findPotentialMatches(Item item) {
        if (item.getStatus() != ItemStatus.OPEN) {
            return List.of();
        }
        ReportType oppositeType = item.getReportType() == ReportType.LOST
                ? ReportType.FOUND
                : ReportType.LOST;
        List<Item> candidates = itemRepository.findByReportTypeAndStatusAndIdNot(
                oppositeType, ItemStatus.OPEN, item.getId());
        return itemMatchingService.rank(item, candidates);
    }

    @Transactional(readOnly = true)
    public List<Item> findOwnedBy(Student student) {
        return itemRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    @Transactional
    public Item create(ItemForm form, Student student) {
        Item item = new Item();
        item.setStudent(student);
        item.setStatus(ItemStatus.OPEN);
        item.setCreatedAt(LocalDateTime.now());
        applyForm(item, form);
        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public Item findOwnedById(Long id, Student student) {
        return itemRepository.findById(id)
                .map(item -> {
                    if (!item.getStudent().getId().equals(student.getId())) {
                        throw new AccessDeniedException("You can only manage your own reports.");
                    }
                    return item;
                })
                .orElseThrow(() -> new ItemNotFoundException());
    }

    @Transactional(readOnly = true)
    public boolean isOwnedBy(Long id, Student student) {
        return student != null && itemRepository.findByIdAndStudent(id, student).isPresent();
    }

    @Transactional
    public Item update(Long id, ItemForm form, Student student) {
        Item item = findOwnedById(id, student);
        applyForm(item, form);
        return itemRepository.save(item);
    }

    @Transactional
    public void delete(Long id, Student student) {
        itemRepository.delete(findOwnedById(id, student));
    }

    private void applyForm(Item item, ItemForm form) {
        item.setTitle(form.getTitle().trim());
        item.setDescription(form.getDescription().trim());
        item.setCategory(form.getCategory().trim());
        item.setLocation(form.getLocation().trim());
        item.setReportType(form.getReportType());
    }
}
