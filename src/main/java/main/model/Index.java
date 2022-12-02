package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
@Data
@NoArgsConstructor
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(name = "lemma_id", nullable = false)
    private Long lemmaId;

    @Column(name = "`rank`", nullable = false)
    private double rank;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Page page;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Lemma lemma;


    public Index(Long pageId, Long lemmaId, double rank) {

        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.rank = rank;
    }
}