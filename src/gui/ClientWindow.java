package gui;

import engine.Creature;
import engine.NetPlayer;
import logic.ClientGame;
import logic.Game;
import logic.Sector;
import logic.ServerGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ClientWindow extends GameWindow
{
    //String color;
    protected ClientWindow()
    {

    }

    public ClientWindow(JFrame frame, Game game)
    {
//        BufferedReader reader;
//        try {
//            FileReader file = new FileReader("C:\\Users\\BigBird\\Desktop\\colors.txt");
//            reader = new BufferedReader(file);
//
//            color =  reader.readLine();
//            System.out.println(color);
//            //file.flush();
//        }catch(IOException ioe) {
//            System.err.println(ioe.toString());
//            //gui.invokeMainMenu();
//        }


        MapShift = new Point(frame.getWidth() / 2 - game.getPlayer().sectorPosition.x,
                frame.getHeight() / 2 - game.getPlayer().sectorPosition.y);
        this.frame = frame;
        this.game = game;
        this.keyAdapter = new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    var s = game.getPlayer().move(-5);
                    MapShift = new Point(frame.getWidth() / 2 - game.getPlayer().absPosition.x,
                            frame.getHeight() / 2 - game.getPlayer().absPosition.y);
                    MapShift.x -= s.x;
                    MapShift.y -= s.y;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    var s = game.getPlayer().move(5);
                    MapShift = new Point(frame.getWidth() / 2 - game.getPlayer().absPosition.x,
                            frame.getHeight() / 2 - game.getPlayer().absPosition.y);
                    MapShift.x -= s.x;
                    MapShift.y -= s.y;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                {
                    game.getPlayer().turn(Math.PI / 8);
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    game.getPlayer().turn(-Math.PI / 8);
                }
                repaint();
            }
        };
            this.frame.addKeyListener(keyAdapter);
    }

    @Override
    protected void drawGame(Graphics2D g)
    {
        AffineTransform origXform = g.getTransform();
        var sectors = new ArrayList<>(game.getSectorNet().getSectors());
        for (var sector: sectors)
        {
            drawSector(g, origXform, sector);
        }

        if (!(game instanceof ClientGame) &&!(game instanceof ServerGame))
            drawProgressBar(g);

        drawPlayer(g);
        //drawFlagella(g, origXform, game.getPlayer(), null);
        //drawEye(g, origXform, game.getPlayer(), null);

    }

    @Override
    protected void drawSector(Graphics2D g, AffineTransform oldForm, Sector sector)
    {

        AffineTransform mapAT = (AffineTransform) (oldForm.clone());
        mapAT.translate(MapShift.x, MapShift.y);
        g.setTransform(mapAT);
        drawBackground(g, sector);
        g.drawRect(sector.location.x, sector.location.y, game.getSectorNet().sectorSize.width,
                game.getSectorNet().sectorSize.height);
        drawFood(g, sector);
        var creatures = new ArrayList<Creature>(sector.creatures);
        for (Creature creature : creatures)
            drawCreature(g, creature, sector);

        g.setTransform(oldForm);
    }

    @Override
    protected void drawPlayer(Graphics2D g)
    {
        var player = game.getPlayer();
        var playerFattiness = player.getFattiness();
        int viewFattiness = (int)(playerFattiness * game.scale);
        //var pos = new Point(player.absPosition.x, player.absPosition.y);
        g.drawImage(player.getBody().getSkin().getImage(), frame.getWidth()  / 2 - viewFattiness,
                frame.getHeight() / 2 - viewFattiness,
                viewFattiness * 2,
                viewFattiness * 2, null);
        var creatureParts = player.getCreatureParts();
        for(var creaturePart:creatureParts)
        {
            var oldForm = g.getTransform();
            AffineTransform partsAT = (AffineTransform) (oldForm.clone());
            Point pos;
            Double angle;
            pos = new Point( player.absPosition.x,
                    player.absPosition.y);
            angle = player.getDirection();

            g.drawOval(pos.x, pos.y, 7, 7);
            g.setTransform(partsAT);
            Image bi = creaturePart.getSkin().getImage();
            g.drawImage(bi,
                    (int)(-bi.getWidth(null) * game.scale),
                    (int)(-player.getFattiness() * game.scale),
                    (int)(game.scale * player.getFattiness()),
                    (int)(game.scale * player.getFattiness()),
                    null);
        }


    }

    private void drawLevelCompletion(Graphics2D g)
    {
        g.setColor(new Color(0));
        g.drawRect(0, 0, frame.getWidth(), 500);
    }
}
