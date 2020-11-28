import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The Event Manager class
 */
public class EventManager {


    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

    /**
     * Judge whether the new event can be created
     *
     * @param roomId the room id this new event will take place
     * @param start the start time of this new event
     * @param g the database
     * @return the boolean show whether the new event can be created
     */
    public boolean canCreateEvent(int roomId, LocalDateTime start, Gateway g){
        List<Event> allEvent = g.getEventList();
        for (Event event : allEvent) {
            if (roomId == event.getRoomId() && start.equals(event.getStartTime())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new event
     * @param start the start time
     * @param speakerId the speaker id of this event
     * @param title the title of this event
     * @param roomId the room id where this event will take place
     * @param g the database
     * @return the new event id
     */
    public int createEvent(LocalDateTime start, int speakerId, String title, int roomId, Gateway g){
        Event nEvent = new Event(start, g.getNextEventId(), speakerId, title, roomId);
        g.addEvent(nEvent);
        return nEvent.getEventId();
    }

    /**
     * Set a new speaker to the exist event
     * @param speakerId the new speaker id
     * @param eventId the event id
     * @param g the database
     */
    public void setSpeaker(int speakerId, int eventId, Gateway g){
        g.getEventById(eventId).setSpeakerId(speakerId);

    }

    /**
     * Judge whether a user can sign up to an event
     *
     * @param userid the userid
     * @param eventId the event id
     * @param g the database
     * @return the boolean shows whether a user can sign up to an event
     */
    public boolean canAddUserToEvent(int userid, int eventId, Gateway g) {
        if (!isExistingEvent(eventId, g)) {
            return false;
        }
        else {
            Event e = g.getEventById(eventId);
            if (e.getSingnedUserId().contains(userid)
                    | g.getRoomById(e.getRoomId()).getCapacity() <= e.getSingnedUserId().size()) {
                return false;
            }
            return true;
        }
    }

    /**
     * Add user to an event
     * @param userId the user id
     * @param eventId the event id
     * @param g the database
     */
    public void addUserToEvent(int userId, int eventId, Gateway g){
        g.getEventById(eventId).addUser(userId);
    }

    /**
     * Judge whether a user can be removed by an event
     * @param userid the user id
     * @param eventId the event id
     * @param g the database
     * @return the boolean whether a user can be removed by an event
     */
    public boolean canRemoveUser(int userid, int eventId, Gateway g) {
        if (isExistingEvent(eventId, g)) {
            return g.getEventById(eventId).getSingnedUserId().contains(userid);
        }
        return false;
    }

    /**
     * Remove the user from an event
     * @param userId the user id
     * @param eventId the event id
     * @param g the database
     */
    public void removeUser(int userId, int eventId, Gateway g) {
        g.getEventById(eventId).removeUser(userId);
    }

    /**
     * A getter for the all signed up user of and event
     *
     * @param eventID the event id
     * @param g the database
     * @return all signed up user of and event
     */
    public List<Integer> getUserList(int eventID, Gateway g){
        Event event = g.getEventById(eventID);
        return event.getSingnedUserId();
    }


    /**
     * A getter for ids of all events in the database
     * @param g the database
     * @return the list of ids of all events in the database
     */
    public List<Integer> getEventList(Gateway g){
        List<Integer> allEvents = new ArrayList<>();
        List<Event> events = g.getEventList();
        for (Event event : events){
            allEvents.add(event.getEventId());
        }
        return allEvents;
    }

    public String getStringOfEvent(int eventID, Gateway g){
        Event event = g.getEventById(eventID);
        return "The event " + event.getTitle() +
                " with ID " + event.getEventId() +
                " by " + g.getSpeakerById(event.getSpeakerId()).getUserName() +
                " starts at " + event.getStartTime().format(getStartTimeFormatter()) +
                " takes place in " + g.getRoomById(event.getRoomId()).getRoomNum();
    }

    /**
     * A getter for the event start time format
     *
     * @return the event start time format
     */
    public DateTimeFormatter getStartTimeFormatter(){
        return this.formatter;
    }

    /**
     * Judge whether the event is in the database
     * @param eventID event id
     * @param g the database
     * @return the boolean shows whether the event is in the database
     */
    public boolean isExistingEvent(int eventID, Gateway g){
        return g.getEventById(eventID) != null;
    }
}

