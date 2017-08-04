package me.alex.SecretChests;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexm on 8/4/2017.
 */
public class SecretChests extends JavaPlugin implements Listener,CommandExecutor {

    List<String> waitingclose;
    List<String> waitingclick;

    public void onEnable() {
        saveDefaultConfig();
        getCommand("secretchests").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        waitingclose = new ArrayList<String>();
        waitingclick = new ArrayList<String>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (command.getName().equalsIgnoreCase("secretchests"))
            {
                if (p.hasPermission("secretchests.use") || p.hasPermission("secretchests.*"))
                {
                    if(args.length == 0)
                    {

                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.Help")));

                        p.sendMessage(waitingclick.toString());
                    return false;
                    }

                    if(args.length == 1)
                    {
                        if(args[0].equalsIgnoreCase("create"))
                        {
                            if(p.hasPermission("secretchests.create") || p.hasPermission("secretchests.*"))
                            {
                                waitingclick.add(p.getName());
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', name(prefix(getConfig().getString("Messages.WaitingCreation")), p)));
                                return false;

                            } else
                            {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', name(prefix(getConfig().getString("Messages.NoPermission")),p)));
                                return false;
                            }


                        } else
                        {
                            p.sendMessage(name(prefix(getConfig().getString("Messages.IncorrectArgs")), p));
                            return false;
                        }


                    }

                    if(args.length > 1)
                    {
                        p.sendMessage(name(prefix(getConfig().getString("Messages.IncorrectArgs")), p));
                        return false;
                    }

                }


            }


        }

        return false;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e)
    {

            if(isSecretChest(e.getInventory()))
            {
                this.waitingclose.add(e.getPlayer().getName());
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',xyz(prefix(name(getConfig().getString("Messages.OnOpen"),Bukkit.getPlayer(e.getPlayer().getName()))), ((BlockState)e.getInventory().getHolder()).getLocation())));
            }


    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e)
    {
     
        Block placed = e.getBlock();
        World w  = e.getBlock().getWorld();
        Block plusx =  w.getBlockAt(placed.getX()+ 1, placed.getY(), placed.getZ());
        Block minusx =  w.getBlockAt(placed.getX()+ -1, placed.getY(), placed.getZ());
        Block plusz =  w.getBlockAt(placed.getX(), placed.getY(), placed.getZ()+1);
        Block minusz =  w.getBlockAt(placed.getX(), placed.getY(), placed.getZ()-1);

        List<Block> touching = new ArrayList<Block>();
        touching.add(plusx);
        touching.add(minusx);
        touching.add(plusz);
        touching.add(minusz);

        for(Block b : touching)
        {

            if(b.getType() == Material.CHEST)
            {

                if(isSecretChest(b.getLocation()))
                {

                    e.setCancelled(true);


                }

            }


        }



    }

    @EventHandler
    public void onClose(InventoryCloseEvent e)
    {
        if(waitingclose.contains(e.getPlayer().getName()))
        {
            if(isSecretChest(e.getInventory()))
            {
                removeSecretChest(((BlockState)e.getInventory().getHolder()).getLocation());
                Block b = (Block)((BlockState) e.getInventory().getHolder()).getBlock();
                b.setType(Material.AIR);
                b.getLocation().getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.CHEST.getId());


            }


        }


    }

    @EventHandler
    public void onTnt(EntityExplodeEvent e)
    {
        for(String s : getConfig().getStringList("Saved"))
        {
            for(Block b : e.blockList())
            {
                if(deserializeLocation(s).equals(b.getLocation()))
                {
                    e.blockList().remove(b);
                }


            }


        }


    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e)
    {

        if(this.waitingclick.contains(e.getPlayer().getName()))
        {

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK)
            {

                if(e.getClickedBlock() != null)
                {

                    if(e.getClickedBlock().getType() == Material.CHEST)
                    {

                        if(!isSecretChest(((Chest)e.getClickedBlock().getState()).getInventory()))
                        {

                            createSecretChest(e.getClickedBlock().getLocation());
                            this.waitingclick.remove(e.getPlayer().getName());
                            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xyz(prefix(name(getConfig().getString("Messages.OnCreate"), e.getPlayer())), e.getClickedBlock().getLocation())));
                            e.setCancelled(true);
                            return;
                        } else
                        {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', xyz(prefix(name(getConfig().getString("Messages.AlreadyMade"), e.getPlayer())), e.getClickedBlock().getLocation())));
                        }


                    }


                }


            }


        }

        if(e.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            if(e.getClickedBlock().getType() == Material.CHEST)
            {
                if(isSecretChest(((Chest)e.getClickedBlock().getState()).getInventory()))
                {
                    e.setCancelled(true);


                }

            }



        }


    }

    public String prefix(String s)
    {
       return s.replace("{Prefix}", getConfig().getString("Messages.Prefix"));


    }

    public String name(String s, Player p)
    {
        return s.replace("{PlayerName}", p.getName());


    }

    public String xyz(String s, Location l)
    {
        return s.replace("{x}", l.getBlockX() + "").replace("{y}", l.getBlockY() + "").replace("{z}", l.getBlockZ() + "");


    }

    public boolean isSecretChest(Inventory e)
    {
     if(e.getHolder() instanceof BlockState)
     {
         Location loc = ((BlockState)e.getHolder()).getLocation();

         if(getConfig().getStringList("Saved").contains(serializeLocation(loc)))
         {
             return true;
         }

     }

       return false;
    }

    public boolean isSecretChest(Location loc)
    {

        if(getConfig().getStringList("Saved").contains(serializeLocation(loc))) return true;
        return false;


    }

    public String createSecretChest(Location loc)
    {
        String saveString = serializeLocation(loc);
        List<String> list = getConfig().getStringList("Saved");
        list.add(saveString.toString());
        getConfig().set("Saved", list);
        saveConfig();


        return saveString;
    }

    public void removeSecretChest(Location loc)
    {
        if(getConfig().getStringList("Saved").contains(serializeLocation(loc)))
        {
            List<String> list = getConfig().getStringList("Saved");
            list.remove(serializeLocation(loc));
            getConfig().set("Saved", list);
            saveConfig();


        }


    }

    public String serializeLocation(Location loc)
    {


        StringBuilder saveString = new StringBuilder();
        int x,y,z;
        String world;
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        world = loc.getWorld().getName();

        saveString.append(x).append(',').append(y).append(',').append(z).append(',').append(world);
        return saveString.toString();
    }

    public Location deserializeLocation(String s)
    {
        String[] split = s.split(",");
        Double x,y,z;
        World w;
        x = Double.parseDouble(split[0]);
        y = Double.parseDouble(split[1]);
        z = Double.parseDouble(split[2]);
        w = Bukkit.getWorld(split[3]);

        return new Location(w,x,y,z);


    }
}
