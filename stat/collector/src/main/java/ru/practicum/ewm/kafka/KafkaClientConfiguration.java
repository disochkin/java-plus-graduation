package ru.practicum.ewm.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class KafkaClientConfiguration {
    private final AtomicInteger counter = new AtomicInteger(0);
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    @Scope("prototype")
    KafkaClient getClient() {
        return new KafkaClient() {

            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    initProducer();
                }
                return producer;
            }

            private void initProducer() {
                Properties config = new Properties();
                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "ru.practicum.collector.serializer.AvroSerializer");
                producer = new KafkaProducer<>(config);
            }

            @Override
            public void stop() {
                if (producer != null) {
                    producer.close();
                }
            }
        };
    }
}
