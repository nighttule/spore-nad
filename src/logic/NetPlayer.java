package logic;

import engine.Player;

import java.awt.*;
import java.io.Serializable;

public class NetPlayer extends Player implements Serializable {
    private static int ttl = 60000;
    private int id;
    private boolean active = false;
    private long live;
    public NetPlayer(Point position, int speed, int agility, int fattiness, int id) {
        super(position, speed, agility, fattiness);
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public boolean isActive(){
        return active;
    }
    public void activate(){
        active = true;
        live = System.currentTimeMillis();
    }
    public void deactivate(){
        active = false;
    }
    public void checkLive(){
        if(live >= ttl)
            deactivate();
    }
}