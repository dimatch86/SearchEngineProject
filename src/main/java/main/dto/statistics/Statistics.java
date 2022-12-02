package main.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Statistics {

    private Total total;

    private List<Detailed> detailed;
}
