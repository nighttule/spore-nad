package netParts;

import engine.NetPlayer;
import logic.ClientGame;
import logic.IMessage;
import logic.IRunOver;
import logic.NetSectorMap;

public class RegistrationMessage implements IMessage {
    private NetSectorMap sectors;
    private int playerId;
    public RegistrationMessage(NetSectorMap sectors, NetPlayer player){
        this.sectors = sectors;
        this.playerId = player.getId();
    }
    @Override
    public void run(IRunOver runOver) {
        if (runOver instanceof ClientGame){
            ClientGame game = (ClientGame)runOver;
            game.registerSelf(sectors, playerId);
        }
    }
}
