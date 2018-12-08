package yassine.com.devchatapp;

/**
 * Created by Mohammed on 01/12/2018.
 */

public class Messages {
    private String message;
    private boolean seen;
    private String type;
    private Long time;
    private String from;
    public Messages(){

    }

    public Messages(String message, boolean seen, String type, Long time, String from) {

        this.message = message;
        this.seen = seen;
        this.type = type;
        this.time = time;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
