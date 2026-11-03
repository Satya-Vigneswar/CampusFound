package com.campus.lostfound.repository;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ReportType;
import com.campus.lostfound.model.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
            select item from Item item
            where (:query is null or :query = ''
                or lower(item.title) like lower(concat('%', :query, '%'))
                or lower(item.description) like lower(concat('%', :query, '%'))
                or lower(item.category) like lower(concat('%', :query, '%'))
                or lower(item.location) like lower(concat('%', :query, '%')))
              and (:reportType is null or item.reportType = :reportType)
            order by item.createdAt desc
            """)
    List<Item> searchPublicItems(@Param("query") String query, @Param("reportType") ReportType reportType);

    List<Item> findByStudentOrderByCreatedAtDesc(Student student);

    Optional<Item> findByIdAndStudent(Long id, Student student);

    List<Item> findByReportTypeAndStatusAndIdNot(ReportType reportType, ItemStatus status, Long id);
}
