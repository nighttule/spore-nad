package engine;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import creatureParts.Body;
import creatureParts.CreaturePart;
import logic.IRunOver;

public class NetPlayer extends Player implements Serializable, IRunOver {
    private static int ttl = 60000;
    private int id;
    private boolean active = false;
    private long live;
    public NetPlayer(Point position, int speed, int agility, int fattiness, int id) {
        super(position, speed, agility, fattiness);
        this.id = id;
        this.absPosition = new Point();
    }
    public void setFattines(int nFattines)
    {
        Fattiness = nFattines;
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
    public void setBody(Body body)
    {
        this.body = body;
    }
    public void setCreatureParts(ArrayList<CreaturePart> ceatureParrts)
    {
        this.creatureParts = creatureParts;
    }
}