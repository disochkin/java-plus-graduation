package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.serializer.AvroSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Класс AggregationStarter, ответственный за запуск агрегации данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    // ... объявление полей и конструктора ...
    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */

    private KafkaConsumer<String, SensorEventAvro> consumer;
    private KafkaProducer<String, SensorsSnapshotAvro> producer;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers; // localhost:9092
    @Value("${kafka.group-id}")
    private String consumerGroupId; // aggregator
    @Value("${kafka.input-topic}")
    private String inputTopic; // telemetry.sensors.v1
    @Value("${kafka.output-topic}")
    private String outputTopic; // telemetry.snapshots.v1


    public void start() {
        try {
            // ... подготовка к обработке данных ...
            // ... например, подписка на топик ...
            Properties consumerConfig = new Properties();
            consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventAvroDeserializer.class);
            consumer = new KafkaConsumer<>(consumerConfig);
            consumer.subscribe(Collections.singleton(inputTopic));

            Properties producerConfig = new Properties();
            producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
            producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
            producer = new KafkaProducer<>(producerConfig);

            // Цикл обработки событий
            while (true) {
                // ... реализация цикла опроса ...
                // ... и обработка полученных данных ...
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.info("Получено сообщение из партиции {}, со смещением {}:\n{}\n",
                            record.partition(), record.offset(), record.value());

                    SensorEventAvro event = record.value();

                    SensorsStateAggregator.updateState(event)
                            .ifPresent(snapshot -> {
                                ProducerRecord<String, SensorsSnapshotAvro> out =
                                        new ProducerRecord<>(
                                                outputTopic,
                                                snapshot.getHubId(),
                                                snapshot
                                        );
                                log.info("Отправлен снапшот {}\n", snapshot);
                                producer.send(out);
                            });
                }
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {


//            }
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                // здесь нужно вызвать метод продюсера для сброса данных в буффере
                // здесь нужно вызвать метод консьюмера для фиксации смещений

            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}