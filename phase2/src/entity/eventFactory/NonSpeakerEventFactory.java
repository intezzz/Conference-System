package entity.eventFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import entity.event.*;

public class NonSpeakerEventFactory extends AbstractEventFactory{
    @Override
    public Event getEvent(int type, LocalDateTime startTime,
                                    LocalDateTime endTime, int eventId, String title, int roomId, int capacity) {
        if (type == 0) {
            return new Party(startTime, endTime, eventId, title, roomId, capacity);
        }
        return null;
    }
}
