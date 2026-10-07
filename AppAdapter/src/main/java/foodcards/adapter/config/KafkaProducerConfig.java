package foodcards.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.dto.KafkaSendDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, KafkaSendDTO> producerFactory(KafkaProperties kafkaProperties,
                                                                 ObjectMapper objectMapper) {

        Map<String, Object> configs = kafkaProperties.buildProducerProperties(null);
        DefaultKafkaProducerFactory<String, KafkaSendDTO> factory = new DefaultKafkaProducerFactory<>(configs);

        factory.setValueSerializerSupplier(() -> new JsonSerializer<>(objectMapper));
        factory.setKeySerializerSupplier(StringSerializer::new);

        return factory;

    }

    @Bean
    public KafkaTemplate<String, KafkaSendDTO> kafkaTemplate(ProducerFactory<String, KafkaSendDTO> factory) {
        return new KafkaTemplate<String, KafkaSendDTO>(factory);
    }
}
