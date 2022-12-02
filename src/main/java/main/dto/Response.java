package main.dto;

import lombok.Data;

@Data
public class Response {

    private boolean result;
    private String error;

    public Response(boolean result) {
        this.result = result;
    }

    public Response(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
