package xyz.donit.domain.goal;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.DateSerializer;
import xyz.donit.domain.exception.ResourceErrCode;
import xyz.donit.domain.exception.ResourceException;
import xyz.donit.utils.CustomDateDeserializer;
import xyz.donit.utils.CustomDateSerializer;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by felix on 1/11/17.
 */
public class Task extends Doable{
    public Task(){}

    public Instant getRemindAt() {
        return remindAt;
    }
    public void setRemindAt(Instant remindAt) {
        this.remindAt = remindAt;
    }

    public Task(long id, String name, boolean done, long duration, Instant at) {
        super(id, name, done, duration);
        this.remindAt = at;
    }
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Instant remindAt;
    private static final String QUERY_STORE_TASK = "INSERT INTO tasks (user_id, goal_id, task_id, name, done, at, duration) VALUES " +
            "(?,?,?,?,?,?,?)";
    public void store(Connection conn, long userId, long goalId) throws ResourceException{
        try(PreparedStatement stmt = conn.prepareStatement(QUERY_STORE_TASK)){
            stmt.setLong(1, userId);
            stmt.setLong(2, goalId);
            stmt.setLong(3, this.getId());
            stmt.setString(4, this.getName());
            stmt.setBoolean(5, this.isDone());
            stmt.setTimestamp(6, Timestamp.from(this.remindAt));
            stmt.setLong(7, getDuration());
            int count = stmt.executeUpdate();
            if (count != 1){
                throw new ResourceException(ResourceErrCode.CANNOT_STORE);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static final String QUERY_GET_TASKS = "SELECT task_id, name, done, duration, at " +
            "FROM tasks WHERE user_id = ? AND goal_id = ?";
    public static ArrayList<Task> getTasks(Connection conn, long userId, long goalId){
        ArrayList<Task> tasks = new ArrayList<>();
        try(PreparedStatement stmt = conn.prepareStatement(QUERY_GET_TASKS)){
            stmt.setLong(1, userId);
            stmt.setLong(2, goalId);
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    Task task = new Task(rs.getLong(1), rs.getString(2),
                            rs.getBoolean(3),rs.getLong(4), rs.getTimestamp(5).toInstant());
                    tasks.add(task);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return tasks;
    }

}
