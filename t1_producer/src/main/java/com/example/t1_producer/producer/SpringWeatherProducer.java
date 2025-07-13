package com.example.t1_producer.producer;

import com.example.t1_producer.dto.WeatherDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Setter
//@Component
public class SpringWeatherProducer implements WeatherProducerInterface {

    @Value("${topic.name}")
    private String topicName;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    //@Autowired
    public SpringWeatherProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMessage(WeatherDto weatherDto) {

        try {
            kafkaTemplate.send(topicName, objectMapper.writeValueAsString(weatherDto));
            log.info("Send message {}", weatherDto);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
