package ru.mealcard.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@ApplicationScoped
public class KafkaProucerConfig {
    private KafkaProducer<String, String> producer;

    @Inject
    KafkaConfig kafkaConfig;

    public void init(@Observes StartupEvent ev) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConfig.bootstrapServers());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all");
        props.put("retries", "3");

        this.producer = new KafkaProducer<>(props);
    }

    @Produces
    @ApplicationScoped
    public KafkaProducer<String, String> getProducer() {
        return producer;
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

}
