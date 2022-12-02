package main.repositories;

import main.model.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {

    List<Lemma> findAllByLemmaInAndSiteIdOrderByFrequencyAsc(Set<String> lemmasList, int siteId);
    long countBySiteId(int siteId);
}