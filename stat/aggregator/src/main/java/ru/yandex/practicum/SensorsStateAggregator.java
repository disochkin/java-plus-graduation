package ru.yandex.practicum;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SensorsStateAggregator {

    private static final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public static Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        // 1. Получаем или создаём снапшот по hubId
        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(
                event.getHubId(),
                hubId -> SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(event.getTimestamp())
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        // 2. Достаём текущее состояние сенсора из снапшота
        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        // 3. Если состояние уже есть — проверяем, нужно ли обновление
        if (oldState != null) {
            boolean isOlderEvent =
                    oldState.getTimestamp().isAfter(event.getTimestamp());

            boolean sameData =
                    Objects.equals(oldState.getData(), event.getPayload());

            if (isOlderEvent || sameData) {
                return Optional.empty();
            }
        }

        // 4. Создаём новое состояние сенсора
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        // 5. Обновляем снапшот
        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);


    }
}
