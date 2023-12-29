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

    private final String value;
    private final int sort;

    EventStatus(String value, int sort) {
        this.value = value;
        this.sort = sort;
    }

    @Override
    public String toString() {
        return value;
    }

    public static EventStatus valueOf(int sort) {
        return switch (sort) {
            case 0 -> CREATED;
            case 1 -> ACCEPTED;
            case 2 -> REJECTED;
            case 3 -> RUNNING;
            case 4 -> COMPLETED;
            default -> throw new RuntimeException(String.format("Unknown EventStatus sortId = %s", sort));
        };
    }

    public int getSort() {
        return sort;
    }
}
