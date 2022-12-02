package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.persistence.Index;
import java.util.List;

@Entity
@Table(name = "page", indexes = {@Index(name = "path_index", columnList = "path")})
@Data
@NoArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Column(name = "path", columnDefinition = "varchar(255)", nullable = false)
    private String path;

    private int code;

    @Column(columnDefinition = "mediumtext", nullable = false)
    private String content;

    @Column(name = "site_id", nullable = false)
    private int siteId;


    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "Index", joinColumns = {@JoinColumn(name = "page_id")},
            inverseJoinColumns = {@JoinColumn(name = "lemma_id")})
    private List<Lemma> lemmas;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Site site;


    public Page(String path, int code, String content, int siteId) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.siteId = siteId;
    }
}