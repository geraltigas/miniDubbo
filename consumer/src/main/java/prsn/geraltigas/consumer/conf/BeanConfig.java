package prsn.geraltigas.consumer.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
