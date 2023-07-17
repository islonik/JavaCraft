package my.javacraft.ses.events;

/**
 * Created by nikilipa on 7/25/16.
 */
public enum Priority {
    BLOCKER(0),
    CRITICAL(1),
    MAJOR(2),
    NORMAL(3),
    MINOR(4);

    private int sort;

    Priority(final int sort) {
        this.sort = sort;
    }

    public static Priority valueOf(int sort) {
        switch (sort) {
            case 0:
                return BLOCKER;
            case 1:
                return CRITICAL;
            case 2:
                return MAJOR;
            case 3:
                return NORMAL;
            case 4:
                return MINOR;
            default:
                throw new RuntimeException(String.format("Unknown Priority status = %s", sort));
        }
    }

    public int getSort() {
        return sort;
    }
}
