package main.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataObject {

    private String site;

    private String siteName;

    private String uri;

    private String title;

    private String snippet;

    private double relevance;
}
