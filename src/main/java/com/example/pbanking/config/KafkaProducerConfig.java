package com.example.pbanking.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.example.pbanking.transaction.dto.response.TransactionsSummaryResponse.TransactionDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.producer.client-id:pbanking-backend}")
    private String producerClientId;

    @Bean
    public ProducerFactory<String, TransactionDto> producerFactory(ObjectMapper mapper) {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProperties.put(ProducerConfig.CLIENT_ID_CONFIG, producerClientId);
        configProperties.put(ProducerConfig.ACKS_CONFIG, "all");
        JsonSerializer<TransactionDto> serializer = new JsonSerializer<>(mapper);
        serializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(configProperties, new StringSerializer(), serializer);
    }

    @Bean
    public KafkaTemplate<String, TransactionDto> kafkaTemplate(ProducerFactory<String, TransactionDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
