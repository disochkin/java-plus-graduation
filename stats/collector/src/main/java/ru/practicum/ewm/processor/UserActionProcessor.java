package ru.practicum.ewm.processor;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionProcessor {
    void process(UserActionProto userActionProto);
}
