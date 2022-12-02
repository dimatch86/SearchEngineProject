package main.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QueryObject {

    private boolean result;

    private int count;

    private List<DataObject> data;
}
