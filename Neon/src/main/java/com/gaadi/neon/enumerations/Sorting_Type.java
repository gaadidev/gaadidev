package com.gaadi.neon.enumerations;

public enum Sorting_Type {
    Date_Ascending("date_asc", "Sort by Oldest"),
    Date_Descending("date_desc", "Sort by Latest"),
    Alphabetical_Ascending("alphabetical_asc", "Sort by Name"),
    Alphabetical_Descending("alphabetical_desc", "Reverse Sort by Name");


    private final String key;
    private final String name;

    Sorting_Type(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static Sorting_Type fromKey(String key) {
        for (Sorting_Type type : Sorting_Type.values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return Sorting_Type.Date_Descending;
    }
}
