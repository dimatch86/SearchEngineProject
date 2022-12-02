package main.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import main.model.Status;

import java.util.Date;

@Data
@AllArgsConstructor
public class Detailed {
    private String url;
    private String name;
    private Status status;
    private Date statusTime;
    private String error;
    private long pages;
    private long lemmas;
}
