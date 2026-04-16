package ru.practicum.ewm.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.mapper.UserActionMapper;
import ru.practicum.ewm.producer.UserActionProducer;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class CollectorController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final UserActionMapper userActionMapper;
    private final UserActionProducer userActionProducer;

    @Override
    public void collectEvent(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.info("Received user action: userId={}, eventId={}, actionType={}, timestamp={}",
                request.getUserId(),
                request.getEventId(),
                request.getActionType(),
                request.getTimestamp());

        try {
            var avroMessage = userActionMapper.mapToAvro(request);
            userActionProducer.send(avroMessage);

            log.info("Successfully sent to Kafka: {}", avroMessage);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error processing user action", e);
            responseObserver.onError(e);
        }
    }
    }
