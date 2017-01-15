package xyz.donit.domain.goal;

import xyz.donit.domain.exception.ResourceErrCode;
import xyz.donit.domain.exception.ResourceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by felix on 1/11/17.
 */
public class Habit extends Doable{
    public DaySet getDays() {
        return days;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setDays(DaySet days) {
        this.days = days;
    }

    private DaySet days = new DaySet();
    private long offset;

    public Habit(long id, String name, boolean done, long duration, DaySet days, long offset) {
        super(id, name, done, duration);
        this.days = days;
        this.offset = offset;
    }
    private static final String SQL_STORE_HABIT = "INSERT INTO " +
            "habits(user_id, goal_id, habit_id, name, done, days, time_offset, duration) " +
            "VALUES (?, ?,?, ?,?,?,?,?)";
    public void store(Connection conn, long userId, long goalId ) throws ResourceException{
        try(PreparedStatement stmt = conn.prepareStatement(SQL_STORE_HABIT)){
            stmt.setLong(1, userId);
            stmt.setLong(2, goalId);
            stmt.setLong(3, getId());
            stmt.setString(4, this.getName());
            stmt.setBoolean(5, this.isDone());
            stmt.setInt(6, this.getDays().toInt());
            stmt.setLong(7, offset);
            stmt.setLong(8, getDuration());
            if (stmt.executeUpdate() != 1) {
                throw new ResourceException(ResourceErrCode.CANNOT_STORE);
            }
        }catch (SQLException e){
            e.printStackTrace();
            throw new ResourceException(ResourceErrCode.CANNOT_STORE);
        }
    }
    public Habit(){}
    private static final String QUERY_GET_HABITS = "SELECT habit_id, name, done, duration, days, time_offset " +
            "FROM habits WHERE user_id = ? AND goal_id = ?";
    public static ArrayList<Habit> getHabits (Connection conn, long userId, long goalId){
        ArrayList<Habit> habits = new ArrayList<>();
        try(PreparedStatement stmt = conn.prepareStatement(QUERY_GET_HABITS)){
            stmt.setLong(1, userId);
            stmt.setLong(2, goalId);
            try(ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    Habit habit = new Habit(rs.getLong(1),
                            rs.getString(2), rs.getBoolean(3),
                            rs.getLong(4),new DaySet(rs.getInt(5)), rs.getLong(6));
                    habits.add(habit);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return habits;
    }

}
