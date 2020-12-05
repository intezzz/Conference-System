package usecase;

import entity.VipUser;
import entity.event.Event;
import entity.eventFactory.FactoryProducer;
import gateway.GatewayFacade;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The Event Manager class
 */
public class EventManager {


    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Judge whether the new event can be created
     *
     * @param roomId the room id this new event will take place
     * @param start the start time of this new event
     * @param g the database
     * @return the boolean show whether the new event can be created
     */
    public boolean canCreateEvent(int roomId, LocalDateTime start, LocalDateTime end, int capacity, GatewayFacade g){
        List<Event> allEvent = g.getEventList();
        for (Event event : allEvent) {
            if (roomId == event.getRoomId() &&
                    (event.getStartTime().isBefore(end) & !(event.getEndTime().isBefore(start))) &
                            capacity > g.getRoomById(roomId).getCapacity()) {
                return false;
            }
        }
        LocalDateTime today = LocalDateTime.now();
        return start.isBefore(end) && today.isBefore(start);
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
    public int createEvent(int type1, int type2, int speakerId, LocalDateTime start,
                           LocalDateTime end, String title, int roomId, int capacity, GatewayFacade g){
        try{
            Event nEvent = FactoryProducer.getFactory(type1).getEvent(type2, start, end,
                    g.getNextEventId(), title, roomId, capacity);
            g.addEvent(nEvent);
            g.getOneSpeakerEventById(nEvent.getEventId()).setSpeaker(speakerId);
            return nEvent.getEventId();
        }
        catch (NullPointerException npe){
            return -1;
        }

    }

    public int createEvent(int type1, int type2, LocalDateTime start, LocalDateTime end, String title,
                           int roomID, int capacity, GatewayFacade gw){
        Event nEvent = FactoryProducer.getFactory(type1).getEvent(type2, start, end, gw.getNextEventId(),
                title, roomID, capacity);
        gw.addEvent(nEvent);
        return nEvent.getEventId();
    }

    /**
     * Create a new event
     * @param start the start time
     * @param speakerList a list of speaker id of this event
     * @param title the title of this event
     * @param roomId the room id where this event will take place
     * @param g the database
     * @return the new event id
     */
    public int createEvent(int type1, int type2, @NotNull ArrayList<Integer> speakerList, LocalDateTime start,
                           LocalDateTime end, String title, int roomId, int capacity, GatewayFacade g){
        Event nEvent = FactoryProducer.getFactory(type1).getEvent(type2, start, end,
                g.getNextEventId(), title, roomId, capacity);
        g.addEvent(nEvent);
        g.getMultiSpeakerEventById(nEvent.getEventId()).setSpeaker(speakerList);
        return nEvent.getEventId();
    }

    /**
     * Set a new speaker to the exist event
     * @param speakerId the new speaker id
     * @param eventId the event id
     * @param g the database
     */
    public void setSpeakerToOneSpeakerEvent(int speakerId, int eventId, GatewayFacade g){
        g.getOneSpeakerEventById(eventId).setSpeaker(speakerId);
    }

    /**
     * Set a new speaker and add it to multiple speaker Event.
     * @param speakerId the new speaker id
     * @param eventId the event id
     * @param g the database
     */
    public void addSpeakerToMultiSpeakerEvent(int speakerId, int eventId, GatewayFacade g) {
        g.getMultiSpeakerEventById(eventId).addNewSpeaker(speakerId);
    }

    /**
     * Judge whether a user can sign up to an event
     *
     * @param userId the userid
     * @param eventId the event id
     * @param g the database
     * @return the boolean shows whether a user can sign up to an event
     */
    public boolean canAddUserToEvent(int userId, int eventId, GatewayFacade g) {
        Event e = g.getEventById(eventId);
        if (!isExistingEvent(eventId, g)) {
            return false;
        }
        else if (! (g.getUserById(userId) instanceof VipUser) & e.isVipEvent()) {
            return false;
        }
        else {
            if (e.getSignedUpUserList().contains(userId)
                    | e.getCapacity() >= e.getSignedUpUserList().size()) {
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
    public void addUserToEvent(int userId, int eventId, GatewayFacade g){
        g.getEventById(eventId).addUserToEvent(userId);
    }

    /**
     * Judge whether a user can be removed by an event
     * @param userId the user id
     * @param eventId the event id
     * @param g the database
     * @return the boolean whether a user can be removed by an event
     */
    public boolean canRemoveSignedUpUser(int userId, int eventId, GatewayFacade g) {
        if (isExistingEvent(eventId, g)) {
            return g.getEventById(eventId).getSignedUpUserList().contains(userId);
        }
        return false;
    }

    /**
     * Remove the user from an event
     * @param userId the user id
     * @param eventId the event id
     * @param g the database
     */
    public void removeSignedUpUser(int userId, int eventId, GatewayFacade g) {
        g.getEventById(eventId).removeUserFromEvent(userId);
    }


    /**
     * @param eventId the id of the event
     * @param g the gateway
     * @Description: add the first ranked waitlist user
     */
    public int add1stRankedWaitListUser(int eventId, GatewayFacade g) {
        Event e = g.getEventById(eventId);
        int userId = e.getWaitList().get(0);
        e.addUserToEvent(userId);
        e.removeUserFromWaitList(userId);
        g.updateEvent(e);
        return userId;
    }

    /**
     * A getter for the all signed up user of and event
     *
     * @param eventID the event id
     * @param g the database
     * @return all signed up user of and event
     */
    public List<Integer> getUserList(int eventID, GatewayFacade g){
        Event event = g.getEventById(eventID);
        return event.getSignedUpUserList();
    }

    /**
     * A getter for ids of all events in the database
     * @param g the database
     * @return the list of ids of all events in the database
     */
    public List<Integer> getEventList(GatewayFacade g){
        List<Integer> allEvents = new ArrayList<>();
        List<Event> events = g.getEventList();
        for (Event event : events){
            allEvents.add(event.getEventId());
        }
        return allEvents;
    }

//    public List<Integer> getNormalEventList(GatewayFacade g){
//        List<Integer> normalEvents = new ArrayList<>();
//        List<Event> events = g.getEventList();
//        for (Event event : events){
//            if(event.isVipEvent() == false) {
//                normalEvents.add(event.getEventId());
//            }
//        }
//        return normalEvents;
//    }

    // modified getStringOfEvent
    // return list of string in format [title, eventID, startTime, endTime, duration, room, VIPstatus]
    // VIP status: "VIP event" for VIP events, "not VIP event" for non-VIP events
    public List<String> getInfoOfEvent(int eventID, GatewayFacade g){
        Event event = g.getEventById(eventID);
        List<String> eventInfo = new ArrayList<String>(){
            {
                add(event.getClass().getName());
                add(event.getTitle());
                add(String.valueOf(event.getEventId()));
                add(event.getStartTime().format(formatter));
                add(event.getEndTime().format(formatter));
                add(String.valueOf(event.getDuration().toHours()));
                add(g.getRoomById(event.getRoomId()).getRoomNum());
            }
        };
        if (event.isVipEvent()){
            eventInfo.add("VIP event");
        }
        else {
            eventInfo.add("not VIP event");
        }
        return eventInfo;

    }

    // no speaker = "No Speaker"
    // 1 speaker = "speakername"
    // 2 or more speaker = "speakername1, speakername2..."
    public String getStringOfSpeakerOfEvent(int eventID, GatewayFacade gw){
        int type = determineEventType(eventID, gw);
        String sSpeaker = null;
        switch (type){
            case 0:
                sSpeaker = "No Speaker";
                break;
            case 1:
                int speakerID = gw.getOneSpeakerEventById(eventID).getSpeakerId();
                sSpeaker = gw.getUserById(speakerID).getUserName();
                break;
            case 2:
                List<Integer> ids = gw.getMultiSpeakerEventById(eventID).getSpeakerId();
                List<String> speakers = new ArrayList<>();
                StringBuilder sbSpeakers = new StringBuilder();
                for (int id : ids){
                    String name = gw.getUserById(id).getUserName();
                    speakers.add(name);
                }
                int i = 0;
                while (i < speakers.size() - 1){
                    sbSpeakers.append(speakers.get(i)).append(", ");
                    i += 1;
                }
                sbSpeakers.append(speakers.get(speakers.size() - 1));
                sSpeaker = sbSpeakers.toString();
                break;

        }
        return sSpeaker;
    }

    public int getCapacity(int eventID, GatewayFacade gw){
        return gw.getEventById(eventID).getCapacity();
    }

    // 0: no speaker event, 1: 1speaker event, 2: multi speaker event
    public int determineEventType(int eventID, GatewayFacade gw){
        if (gw.getOneSpeakerEventById(eventID) != null){
            return 1;
        }
        else if (gw.getMultiSpeakerEventById(eventID) != null){
            return 2;
        }
        else{
            return 0;
        }
    }

    /**
     * A getter for the event start time format
     *
     * @return the event start time format
     */
    public DateTimeFormatter getTimeFormatter(){
        return this.formatter;
    }

    /**
     * Judge whether the event is in the database
     * @param eventID event id
     * @param g the database
     * @return the boolean shows whether the event is in the database
     */
    public boolean isExistingEvent(int eventID, GatewayFacade g){
        return g.getEventById(eventID) != null;
    }

    /**
     * Determine whether we can cancel an event or not
     * @param eventID event id
     * @param gw the database
     * @return the boolean shows whether the event has been canceled.
     */
    public boolean canCancelEvent(int eventID, GatewayFacade gw) {
        return isExistingEvent(eventID, gw);
    }

    /**
     * cancel the event
     * @param eventID event id
     * @param gw the database
     */
    public void cancelEvent(int eventID, GatewayFacade gw) {
        gw.deleteEvent(gw.getEventById(eventID));
    }

    /**
     *
     * @param eventId event id
     * @param g the database
     * @return the event duration
     */
    public Duration getEventDuration(int eventId, GatewayFacade g) {return g.getEventById(eventId).getDuration();}

    /**
     * Judge whether we can change the capacity of an Event
     * @param eventId event id
     * @param g the database
     * @return the boolean shows whether the capacity of a certain event can be changed or not
     */
    public boolean canChangeEventCapacity(int eventId,int newCapacity ,GatewayFacade g) {
        if (newCapacity > g.getRoomById(g.getEventById(eventId).getRoomId()).getCapacity() |
                newCapacity < g.getEventById(eventId).getSignedUpUserList().size()) {
            return false;
        }
        return true;
    }

    /**
     * change the capacity of the event and add waitlist users to signed up list.
     * @param eventId event id
     * @param g the database
     */
    public List<Integer> changeEventCapacity(int eventId, int newCapacity, GatewayFacade g) {
        Event event = g.getEventById(eventId);
        event.setCapacity(newCapacity);
        g.updateEvent(event);
        List<Integer> offWaitlistUsers = new ArrayList<>();
        while (newCapacity > g.getEventById(eventId).getSignedUpUserList().size() &&
                g.getEventById(eventId).getWaitList().size() != 0){
            int userID = add1stRankedWaitListUser(eventId, g);
            offWaitlistUsers.add(userID);
        }
        return offWaitlistUsers;
    }

    /**
     * change a event to VIP, and remove all non-vip users
     * @param eventId event id
     * @param type event type
     * @param g the database
     * @return
     */
    public boolean changeVipStatusOfEvent(int eventId, Boolean type, GatewayFacade g){
        Event event = g.getEventById(eventId);
        event.setVipEvent(type);

        return true;
    }

    /**
     * return the event type, true means event is VIP, false means event is not VIP
     * @param eventId event id
     * @return the type of event
     */
    public Boolean getVipStatusOfEvent(int eventId, GatewayFacade g){
        return g.getEventById(eventId).isVipEvent();
    }


    /**
     *
     * @Description check if a user can be added to the waitlist
     */
    public boolean canAddUserToWaitList(int eventId, int userId, GatewayFacade g) {
        Event e = g.getEventById(eventId);
        if (e == null || e.getSignedUpUserList().size() <= e.getCapacity()
                || (e.isVipEvent() & !(g.getUserById(userId) instanceof VipUser))) {
            return false;
        }
        return true;
    }


    /**
     *
     * @Description add users to the wait list
     */
    public void addUserToWaitList(int eventId, int userId, GatewayFacade g) {
        Event event = g.getEventById(eventId);
        if (!(g.getUserById(userId) instanceof VipUser)){
            event.addUserToWaitList(userId);
        }
        else {
            for (int i = 0; i < event.getWaitList().size(); i++) {
                if (!((g.getUserById(event.getWaitList().get(i))) instanceof VipUser)) {
                    event.getWaitList().add(i, userId);
                    break;
                }
            }
        }
        g.updateEvent(event);
    }


    /**
     *
     * @Description check if the waiting users can be removed.
     */
    public boolean canRemoveWaitingUser(int eventId, int userId, GatewayFacade g) {
        if (isExistingEvent(eventId, g)) {
            return g.getEventById(eventId).getWaitList().contains(userId);
        }
        return false;
    }


    /**
     *
     * @Description remove the waiting users
     */
    public void removeWaitingUser(int eventId, int userId, GatewayFacade g) {
        Event event = g.getEventById(eventId);
        event.removeUserFromWaitList(userId);
        g.updateEvent(event);
    }

    public int getWaitlistLength(int eventID, GatewayFacade gw){
        return gw.getEventById(eventID).getWaitList().size();
    }
}

