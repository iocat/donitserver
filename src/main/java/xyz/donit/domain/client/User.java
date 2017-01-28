package xyz.donit.domain.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.DateSerializer;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
import xyz.donit.domain.exception.ResourceErrCode;
import xyz.donit.domain.exception.ResourceException;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by felix on 1/11/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonSerialize(using = ToStringSerializer.class)
    private long id;

    private String username;
    private String email;
    @JsonSerialize(using = DateSerializer.class)
    private Timestamp joinAt;

    public User(){}
    public User(long id, String username, String email, Timestamp joinAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.joinAt = joinAt;
    }
    public User(String username, String email){
        this.username = username;
        this.email = email;
    }
    public User(long userId){
        this.id = userId;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Timestamp getJoinAt() {
        return joinAt;
    }
    public void setJoinAt(Timestamp joinAt) {
        this.joinAt = joinAt;
    }

    private static final String SQL_STORE_USER =
            "INSERT INTO users(user_id, username, email, join_at) " +
                    "VALUES (DEFAULT, ?, ?, DEFAULT) RETURNING user_id, join_at";
    public void store(DataSource ds) throws ResourceException {
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_STORE_USER)) {
            stmt.setString(1, this.username);
            stmt.setString(2, this.email);
            try(ResultSet rs = stmt.executeQuery()){
                if (!rs.next()){
                    throw new ResourceException(ResourceErrCode.CANNOT_STORE);
                }
                this.id = rs.getLong(1);
                this.joinAt = rs.getTimestamp(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static final String SQL_RETRIEVE_USER =
            "SELECT username, email, join_at FROM users WHERE user_id = ?";
    // get the user based on the user's id
    public void retrieve(DataSource ds) throws ResourceException{
        try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_RETRIEVE_USER)) {
            stmt.setLong(1, this.id);
            try(ResultSet rs = stmt.executeQuery()){
                if (!rs.next()){
                    throw new ResourceException(ResourceErrCode.DOES_NOT_EXIST);
                }
                this.email = rs.getString(2);
                this.username = rs.getString(1);
                this.joinAt = rs.getTimestamp(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
