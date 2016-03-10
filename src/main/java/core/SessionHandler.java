package core; /**
 * Created by jcber on 2016-03-01.
 */

import com.google.gson.Gson;
import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.spi.JsonProvider;

@ApplicationScoped
public class SessionHandler {
    private final Set<UserSession> sessions = new HashSet<>();
    private static final JsonProvider provider = JsonProvider.provider();

    public void addSession(UserSession session) {
        sessions.add(session);
    }

    public void removeSession(UserSession session) {
        sessions.remove(session);
    }
    
    public List<UserIdentity> getUsers() {
        List<UserIdentity> users = new ArrayList<>();
        for (UserSession session : sessions)
            users.add(session.getUserIdentity());
        return users;
    }
    
    public UserIdentity findUserByName(String name){
        for (UserSession session : sessions){
            if (session.getUserIdentity().getUser().getName().equals(name))
                return session.getUserIdentity();
        }
        return null;
    }

    public UserSession getUserSession(String sessionId) {
        for (UserSession session : sessions) {
            if (session.getSession().getId().equals(sessionId)) {
                return session;
            }
        }
        return null;
    }

    public UserSession getUserSessionById(Long id){
        for (UserSession session : sessions) {
            System.out.println("User uuid: " + session.getUserIdentity().getId());
            if (session.getUserIdentity().getId().equals(id)) {
                return session;
            }
        }
        return null;
    }

    public void sendToAllConnectedSessions(String action, String data) {
        JsonObject response = createJson(action, data);
        for (UserSession session : sessions) {
            sendToSession(session, response);
        }
    }

    public void sendToSessions(Quiz quiz, String action, String data){
        JsonObject response = createJson(action, data);
        for (Long user: quiz.getPlayerIds()){
            this.sendToSession(this.getUserSessionById(user), response);
        }
    }
    
    private JsonObject createJson(String action, String data){
        return provider.createObjectBuilder().add("action", action).add("data", data).build();
    }

    public void sendToSession(UserSession session, String message) {
        try {
            session.getSession().getBasicRemote().sendText(message);
        } catch (IOException ex) {
            sessions.remove(session);
            Logger.getLogger(SessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendToSession(UserSession session, JsonObject data) {
        try {
            session.getSession().getBasicRemote().sendText(data.toString());
        } catch (IOException ex) {
            sessions.remove(session);
            Logger.getLogger(SessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (UserSession session : sessions) {
            sb.append("SessionId: ").append(session.getSession().getId()).append(", UserUUID: ").append(session.getUserIdentity().getId());
        }
        return sb.toString();
    }

}
