package ru.practicum.ewm.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.mapper.UserActionMapper;
import ru.practicum.ewm.producer.UserActionProducer;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionProcessorImpl implements UserActionProcessor {

    private final UserActionProducer producer;
    private final UserActionMapper mapper;

    @Override
    public void process(UserActionProto userActionProto) {
        UserActionAvro avro = mapper.mapToAvro(userActionProto);
        producer.sendUserAction(avro);
        log.debug("Действие пользователя обработано: userId={}", userActionProto.getUserId());
    }
}
