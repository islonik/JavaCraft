package my.javacraft.ses.events;


/**
 * Created by nikilipa on 8/29/16.
 */
public enum EventStatus {
    CREATED("Created", 0),
    ACCEPTED("Accepted", 1),
    REJECTED("Rejected", 2),
    RUNNING("In running", 3),
    COMPLETED("Completed", 4);

    private String value;
    private int sort;

    EventStatus(String value, int sort) {
        this.value = value;
        this.sort = sort;
    }

    @Override
    public String toString() {
        return value;
    }

    public static EventStatus valueOf(int sort) {
        switch (sort) {
            case 0:
                return CREATED;
            case 1:
                return ACCEPTED;
            case 2:
                return REJECTED;
            case 3:
                return RUNNING;
            case 4:
                return COMPLETED;
            default:
                throw new RuntimeException(String.format("Unknown EventStatus sortId = %s", sort));
        }
    }

    public int getSort() {
        return sort;
    }
}
