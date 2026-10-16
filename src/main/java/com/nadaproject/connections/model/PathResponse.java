package com.nadaproject.connections.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathResponse {
    private boolean connected;
    private Integer hops;
    private List<Person> path;

    public PathResponse(boolean connected) {
        this.connected = connected;
    }

    public PathResponse(boolean connected, int hops, List<Person> path) {
        this.connected = connected;
        this.hops = hops;
        this.path = path;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Integer getHops() {
        return hops;
    }

    public void setHops(Integer hops) {
        this.hops = hops;
    }

    public List<Person> getPath() {
        return path;
    }

    public void setPath(List<Person> path) {
        this.path = path;
    }
}
