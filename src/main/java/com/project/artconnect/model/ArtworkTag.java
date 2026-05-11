package com.project.artconnect.model;

public class ArtworkTag {
    private Integer id;
    private String name;

    public ArtworkTag() {
    }

    public ArtworkTag(String name) {
        this.name = name;
    }

    public ArtworkTag(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
