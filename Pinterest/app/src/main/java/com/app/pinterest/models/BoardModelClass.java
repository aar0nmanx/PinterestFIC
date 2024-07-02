package com.app.pinterest.models;

import java.util.List;

public class BoardModelClass {
    private String id;
    private String name;
    private String visibility;
    private List<String> pinsId;

    public BoardModelClass() {}

    public BoardModelClass(String id, String name, String visibility, List<String> pinsId) {
        this.id = id;
        this.name = name;
        this.visibility = visibility;
        this.pinsId = pinsId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVisibility() {
        return visibility;
    }

    public List<String> getPinsId() {
        return pinsId;
    }
}
