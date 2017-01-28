package xyz.donit.domain.goal;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
import xyz.donit.domain.client.VisibilityEnum;
import xyz.donit.domain.exception.ResourceErrCode;
import xyz.donit.domain.exception.ResourceException;
import xyz.donit.utils.CustomDateSerializer;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;

public class Goal {

    private static final String QUERY_RETRIEVE_GOAL =
            "SELECT name, description, last_updated, img_url, done, visibility " +
                    "FROM goals WHERE user_id = ? AND goal_id = ?";
    @JsonSerialize(using = ToStringSerializer.class)
    private long id = 0;
    private String name = "";
    private String description = "";

    @JsonSerialize(using = CustomDateSerializer.class)
    private Instant lastUpdated;

    private boolean done = false;
    private String img = "";
    private ArrayList<Habit> habits = new ArrayList<>();
    private ArrayList<Task> tasks = new ArrayList<>();

    public Goal(long id, String name, String description, Instant lastUpdated, boolean done, String img, ArrayList<Habit> habits, ArrayList<Task> tasks, VisibilityEnum visibility) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.done = done;
        this.img = img;
        this.habits = habits;
        this.tasks = tasks;
        this.visibility = visibility;
    }

    public ArrayList<Habit> getHabits() {
        return habits;
    }

    public void setHabits(ArrayList<Habit> habits) {
        this.habits = habits;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public Goal(long id, String name, String description, Instant lastUpdated, boolean done, String img, VisibilityEnum visibility) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastUpdated = lastUpdated;
        this.done = done;
        this.img = img;
        this.visibility = visibility;
    }

    private VisibilityEnum visibility = VisibilityEnum.PUBLIC;

    public Goal(){}
    public Goal(long id){
        this.id = id;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }
    public String getImg() {
        return img;
    }
    public void setImg(String img) {
        this.img = img;
    }
    public VisibilityEnum getVisibility() {
        return visibility;
    }
    public void setVisibility(VisibilityEnum visibility) {
        this.visibility = visibility;
    }

    private void initIds(){
        int i = 0;
        for (Task task: this.tasks){
            task.setId(i);
            i++;
        }
        i = 0;
        for (Habit habit: this.habits){
            habit.setId(i);
            i++;
        }
    }
    private static final String QUERY_STORE_GOAL =
            "INSERT INTO goals(user_id, goal_id, name, description, last_updated, img_url, done, visibility) " +
                    "VALUES(?,DEFAULT,?,?,DEFAULT,?,?,?) RETURNING goal_id, last_updated";
    public void store(DataSource ds, long userId) throws ResourceException {
        this.initIds();
        try(Connection conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(QUERY_STORE_GOAL)){
            conn.setAutoCommit(false);
            stmt.setLong(1, userId);
            stmt.setString(2,name);
            stmt.setString(3, description);
            stmt.setString(4, img);
            stmt.setBoolean(5, done);
            stmt.setInt(6, visibility.ordinal());
            try(ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    conn.rollback();
                    throw new ResourceException(ResourceErrCode.CANNOT_STORE,"no generated keys");
                }
                this.id = rs.getLong(1);
                this.lastUpdated = rs.getTimestamp(2).toInstant();
            }
            try {
                for (Task task : this.tasks) {
                    task.store(conn, userId, this.id);
                }
                for (Habit habit : this.habits) {
                    habit.store(conn, userId, this.id);
                }
            }catch (ResourceException e){
                conn.rollback();
                throw e;
            }
            conn.commit();
        }catch (SQLException e){
            e.printStackTrace();
            throw new ResourceException(ResourceErrCode.CANNOT_STORE, e.getMessage());
        }
    }

    public void retrieve(DataSource ds, long userId) throws ResourceException{
        try( Connection conn = ds.getConnection()){
            conn.setAutoCommit(false);
            try{
                retrieve(conn, userId);
            }catch (ResourceException e){
                conn.rollback();
                throw e;
            }
            conn.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    // Transactional version of retrieve
    private void retrieve(Connection conn, long userId) throws ResourceException{
        try(PreparedStatement stmt = conn.prepareStatement(QUERY_RETRIEVE_GOAL)){
            stmt.setLong(1, userId);
            stmt.setLong(2, this.id);
            try (ResultSet rs = stmt.executeQuery()){
                if (!rs.next()){
                    throw new ResourceException(ResourceErrCode.DOES_NOT_EXIST);
                }
                this.name =  rs.getString(1);
                this.description = rs.getString(2);
                this.lastUpdated = rs.getTimestamp(3).toInstant();
                this.img = rs.getString(4);
                this.done = rs.getBoolean(5);
                this.visibility = VisibilityEnum.values()[rs.getInt(6)];
            }
            this.habits = Habit.getHabits(conn, userId, this.id);
            this.tasks = Task.getTasks(conn, userId, this.id);
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    private static final String QUERY_ALL_GOALIDS_PAGING = "SELECT goal_id FROM goals WHERE user_id = ? " +
            "ORDER BY last_updated " +
            "LIMIT ? OFFSET ?";
    public static ArrayList<Goal> getGoals(DataSource ds, long userId, int limit, int offset) throws  ResourceException{
        ArrayList<Long> ids = new ArrayList<>();
        ArrayList<Goal> goals = null;
        try(Connection conn = ds.getConnection()){
            conn.setAutoCommit(false);
            try(PreparedStatement stmt = conn.prepareStatement(QUERY_ALL_GOALIDS_PAGING)){
                stmt.setLong(1, userId);
                stmt.setInt(2, limit);
                stmt.setInt(3, offset);
                try(ResultSet rs = stmt.executeQuery()){
                    while (rs.next()){
                        ids.add(rs.getLong(1));
                    }
                }
            }
            try{
                goals = getGoalsByIds(conn, ids, userId);
            }catch (ResourceException e){
                conn.rollback();
                throw e;
            }
            conn.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return goals;
    }

    private static ArrayList<Goal> getGoalsByIds(Connection conn, ArrayList<Long> ids, long userId) throws ResourceException{
        ArrayList<Goal> goals = new ArrayList<>();
        for(long id: ids){
            Goal goal = new Goal(id);
            goal.retrieve(conn, userId);
            goals.add(goal);
        }
        return goals;
    }

    private static final String QUERY_ALL_GOALIDS = "SELECT goal_id FROM goals WHERE user_id = ? " +
            "ORDER BY last_updated ";
    public static ArrayList<Goal> getGoals(DataSource ds, long userId) throws  ResourceException{
        ArrayList<Long> ids = new ArrayList<>();
        ArrayList<Goal> goals = null;
        try(Connection conn = ds.getConnection()){
            conn.setAutoCommit(false);
            try(PreparedStatement stmt = conn.prepareStatement(QUERY_ALL_GOALIDS)){
                stmt.setLong(1, userId);
                try(ResultSet rs = stmt.executeQuery()){
                    while (rs.next()){
                        ids.add(rs.getLong(1));
                    }
                }
            }
            try{
                goals = getGoalsByIds(conn, ids, userId);
            }catch (ResourceException e){
                conn.rollback();
                throw e;
            }
            conn.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return goals;
    }
}
