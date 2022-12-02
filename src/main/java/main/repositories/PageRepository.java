package main.repositories;

import main.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    Page findByPath(String path);
    long countBySiteId(int siteId);


    @Query(value = "SELECT * FROM page p " +
            "JOIN `index` i ON p.id = i.page_id " +
            "JOIN lemma l ON l.id = i.lemma_id " +
            "JOIN site s ON s.id = p.site_id " +
            "WHERE l.lemma IN (:lemmasList) " +
            "AND s.id = :id " +
            "GROUP BY p.path " +
            "HAVING COUNT(*) = :count " +
            "ORDER BY SUM(i.rank) DESC", nativeQuery = true)
    List<Page> findPagesByNativeQueryWithSiteId(@Param("lemmasList") Set<String> lemmasList,
                                      @Param("count") int count,
                                                @Param("id") int siteId);

}