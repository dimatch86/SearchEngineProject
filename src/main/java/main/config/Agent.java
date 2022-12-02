package main.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sitelist.agent")
public class Agent {

    private String userAgent;
    private String referrer;
}
