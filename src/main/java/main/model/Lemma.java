package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLInsert;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lemma", uniqueConstraints = {@UniqueConstraint(columnNames = {"lemma", "site_id"})})
@SQLInsert(sql = "INSERT INTO lemma (id, lemma, site_id, frequency) VALUES (?, ?, ?, 1) ON DUPLICATE KEY UPDATE frequency = frequency + 1")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String lemma;

    @Column(name = "site_id", nullable = false)
    private int siteId;

    @Column(nullable = false)
    private int frequency;

    @ManyToMany(mappedBy = "lemmas", fetch = FetchType.LAZY)
    private List<Page> pages;

    public Lemma(String lemma, int siteId) {
        this.lemma = lemma;
        this.siteId = siteId;
    }
}
