package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;

@Entity
@Table(name = "field", uniqueConstraints = {@UniqueConstraint(columnNames = "selector")})
@Data
@NoArgsConstructor
@SQLInsert(sql = "INSERT INTO field (name, selector, weight, id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE selector = selector")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String selector;

    @Column(nullable = false)
    private double weight;

    public Field(String name, String selector, double weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

}