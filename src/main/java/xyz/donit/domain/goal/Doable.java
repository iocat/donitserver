package xyz.donit.domain.goal;

/**
 * Created by felix on 1/11/17.
 */
public class Doable {
    private long id;
    private String name;
    private boolean done;
    private long duration;
    public Doable(){}

    public Doable(long id, String name, boolean done, long duration) {
        this.id = id;
        this.name = name;
        this.done = done;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isDone() {
        return done;
    }
    public void setDone(boolean done) {
        this.done = done;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

}
