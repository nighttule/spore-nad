package logic;

import engine.Creature;
import engine.Food;
import engine.NetPlayer;
import logger.Logger;
import netParts.*;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class ServerGame extends Game implements IServerWorker, Serializable, IRunOver {
    private int onlinePlayers;
    private Server server;
    private boolean readyToWrite = false;
    private Logger logger = new Logger("Server_Client.log");
    private ArrayList<NetPlayer> players = new ArrayList<NetPlayer>();

    public ServerGame(int port){
        this.server = new Server(port, this, 0);
        this.curSectors = new NetSectorMap(4);
        this.players = ((NetSectorMap)curSectors).getPlayers();

        server.start();

    }
    public ArrayList<NetPlayer> getCreatures(){
        return players;
    }

    public void registerPlayer(NetPlayer player)
    {
        player.activate();
        players.get(onlinePlayers).setBody(player.getBody());
        players.get(onlinePlayers).setCreatureParts(player.getCreatureParts());
    }

    public void setPlayer(NetPlayer player)
    {
        player.activate();
        players.set(player.getId(), player);
    }

    @Override
    public void read(InputStream stream) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(stream);
        try {
            IMessage message = (IMessage) ois.readObject();
            if(message!=null) {
                message.run(this);
                for (var player : players){
                    if(player.isActive())
                        System.out.printf("player id: %d in x: %d y: %d\n", player.getId(),
                                player.sectorPosition.x, player.sectorPosition.y);
                }
                readyToWrite = true;
            }
        }catch(ClassNotFoundException ignored){
            System.out.println(ignored.toString());
        }
    }

    @Override
    public void onConnectRead(InputStream stream) throws IOException
    {
//        ObjectInputStream ois = new ObjectInputStream(stream);
//        try
//        {
//            IMessage message = (IMessage) ois.readObject();
//            if(message!=null) {
//                message.run(this);
//                for (var player : players){
//                    if(player.isActive())
//                        System.out.printf("player id: %d in x: %d y: %d\n", player.getId(),
//                                player.sectorPosition.x, player.sectorPosition.y);
//                }
//                readyToWrite = true;
//            }
//        }catch(ClassNotFoundException ignored){
//            System.out.println(ignored.toString());
//        }
    }

    @Override
    public void write(OutputStream stream) throws IOException {
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(new LevelMessage((NetSectorMap) curSectors));
            oos.flush();
            //readyToWrite = false;
    }

    @Override
    public void onConnectWrite(OutputStream stream) throws IOException {

        NetPlayer player = players.get(onlinePlayers);
        //System.out.printf("Registrating new player: id: %d x: %d y: %d\n", player.getId(),
        //        player.sectorPosition.x, player.sectorPosition.y);
        player.activate();
        onlinePlayers++;
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(new RegistrationMessage((NetSectorMap) curSectors, player));
        oos.flush();
    }

    public void run(){
        while(true){
            update();
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ie){
                System.out.println("Interrupted");
            }
            readyToWrite = true;
        }
    }

    @Override
    public void onConnectionReset() {

    }

    @Override
    public boolean isReady() {
        return readyToWrite;
    }

    @Override
    protected void observeSectorPosition(int curXNet, int curYNet, Creature creature)
    {
    }

    @Override
    public void observeCreatures()
    {
        ArrayList<Creature> deadCreatures = new ArrayList<>();
        for (var player : players)
        {
            if(player.isActive())
            {
                var sector = player.getSector((NetSectorMap) curSectors);
                eatFood(sector, player);
                eatCreatures(sector, player, deadCreatures);
                var maxWidth = NetSectorMap.netSize * curSectors.sectorSize.width - 1;
                var maxHeight = NetSectorMap.netSize * curSectors.sectorSize.height - 1;
                if(player.sectorPosition.x <= 1)
                    player.sectorPosition.x = maxWidth;
                if(player.sectorPosition.x >= maxWidth)
                    player.sectorPosition.x = 2;
                if(player.sectorPosition.y <= 1)
                    player.sectorPosition.y = maxHeight;
                if(player.sectorPosition.y >= maxHeight)
                    player.sectorPosition.y = 2;
                tick++;
            }
        }
        players.removeAll(deadCreatures);
    }

    @Override
    protected void eatFood(Sector curSec, Creature creature)
    {
        var removedFood = new ArrayList<Food>();
        var playerSector = ((NetPlayer)creature).getSector((NetSectorMap)curSectors);
        for (Food food : ((NetSectorMap)curSectors).getFoods())
        {
            if(playerSector.equals(food.getSector((NetSectorMap)curSectors))) {
                var keys = food.getPieces().keySet();
                for (Point piecePosition : keys) {
                    //System.out.printf("Creture: %d current weight: %d\n", ((NetPlayer)creature).getId(), creature.getFattiness());
                    //System.out.printf("creature pos : %d %d\n", ((NetPlayer)creature).sectorPosition.x, ((NetPlayer)creature).sectorPosition.y);
                    //System.out.printf("piece pos : %d %d\n", piecePosition.x, piecePosition.y);
                    if (dist(creature.sectorPosition, piecePosition) <= creature.getFattiness() - food.MaxSize) {
                        int nutrition = food.destroyPiece(piecePosition);
                        //System.out.printf("Creture: %d current weight: %d\n", ((NetPlayer)creature).getId(), creature.getFattiness());
                        creature.putOnWeight(nutrition);
                        //System.out.printf("Creture: %d weight after putOn: %d\n", ((NetPlayer)creature).getId(), creature.getFattiness());
                        if (food.isEmpty)
                            removedFood.add(food);
//                        if (creature instanceof Player) {
//                            progressBar += nutrition;
//                        }
                    }
                }
            }
        }
        curSec.removeFood(removedFood);
    }

    @Override
    protected void eatCreatures(Sector curSec, Creature creature, ArrayList<Creature> deadCreatures)
    {
        for(var preyCreature : players)
        {
            if(preyCreature.isActive()) {
                if (creature.getFattiness() > preyCreature.getFattiness() &&
                        dist(creature.sectorPosition, preyCreature.sectorPosition) <=
                                creature.getFattiness() - preyCreature.getFattiness()) {
                    creature.eat(preyCreature);
                }
            }
        }
    }

    @Override
    public boolean update(){
        //level.refreshPlayers();
        this.observeCreatures();
        return false;
    }
}