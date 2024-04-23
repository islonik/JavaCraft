package my.javacraft.elastic.model;

public enum SeekType {

    ALL,
    BOOKS,
    COMPANIES,
    MUSIC,
    MOVIES,
    PEOPLE;

    public static SeekType valueByName(String name) {
        SeekType[] values = values();
        for (SeekType seekType : values) {
            if (seekType.name().equalsIgnoreCase(name)) {
                return seekType;
            }
        }
        return ALL;
    }

}
