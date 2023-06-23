package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.manager.DataManager;
import cn.jrmcdp.craftitem.holder.EditHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Cmd implements CommandExecutor, TabCompleter {
    private CommandSender sender;

    private String[] args;

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        this.sender = sender;
        this.args = args;
        if (args.length == 0) {
            Message.getHelp().forEach(sender::sendMessage);
            return false;
        }
        String prem = command.getName() + ".command." + args[0];
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runReload();
            case "category":
                if (args.length < 2) break;
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runCategory();
            case "open":
                if (args.length < 2) break;
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runOpen();
            case "get":
                if (args.length < 2) break;
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runGet();
            case "edit":
                if (args.length < 2) break;
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runEdit();
            case "create":
                if (args.length < 2) break;
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runCreate();
            case "delete":
                if (args.length < 2) break;
                if (!sender.hasPermission(prem)) {
                    sender.sendMessage(Message.getPrefix() + Message.getNoPermission()
                            .replace("<Prem>", prem)
                    );
                    return false;
                }
                return runDelete();
            default:
                for (String line : Message.getHelp())
                    sender.sendMessage(line);
                return false;
        }
        Message.getHelp().forEach(sender::sendMessage);
        return false;
    }

    private boolean runDelete() {
        if (!(this.sender instanceof Player)) {
            this.sender.sendMessage(Message.getPrefix() + Message.getNoPlayer());
            return false;
        }
        Player player = (Player)this.sender;
        CraftData craftData = Craft.getCraftData(this.args[1]);
        if (craftData == null) {
            player.sendMessage(Message.getPrefix() + "§c未找到 §e" + args[1]);
            return false;
        }
        Craft.delete(args[1]);
        player.sendMessage(Message.getPrefix() + "§a成功删除 §e" + args[1]);
        return true;
    }

    private boolean runCreate() {
        if (!(this.sender instanceof Player)) {
            this.sender.sendMessage(Message.getPrefix() + Message.getNoPlayer());
            return false;
        }
        Player player = (Player)this.sender;
        CraftData craftData = Craft.getCraftData(this.args[1]);
        if (craftData != null) {
            player.sendMessage(Message.getPrefix() + "§c已存在 §e" + args[1]);
            return false;
        }
        craftData = new CraftData(new ArrayList(), 75, Arrays.asList(5, 10, 20), 188, new ItemStack(Material.COBBLESTONE), new ArrayList(), new ArrayList());
        Craft.save(args[1], craftData);
        player.openInventory(new EditHolder(this.args[1], craftData).buildGui());
        return true;
    }

    private boolean runEdit() {
        if (!(this.sender instanceof Player)) {
            this.sender.sendMessage(Message.getPrefix() + Message.getNoPlayer());
            return false;
        }
        Player player = (Player)this.sender;
        CraftData craftData = Craft.getCraftData(this.args[1]);
        if (craftData == null) {
            player.sendMessage(Message.getPrefix() + "§c未找到 §e" + args[1]);
            return false;
        }
        player.openInventory(new EditHolder(this.args[1], craftData).buildGui());
        return true;
    }

    private boolean runOpen() {
        Player player;
        if (this.args.length >= 3) {
            player = Bukkit.getPlayer(this.args[2]);
            if (player == null || !player.isOnline()) {
                this.sender.sendMessage(Message.getPrefix() + "§c玩家未在线或不存在");
                return false;
            }
        } else {
            if (!(this.sender instanceof Player)) {
                this.sender.sendMessage(Message.getPrefix() + Message.getNoPlayer());
                return false;
            }
            player = (Player)this.sender;
        }
        CraftData craftData = Craft.getCraftData(this.args[1]);
        if (craftData == null) {
            player.sendMessage(Message.getPrefix() + "§c未找到 §e" + args[1]);
            return false;
        }
        Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> player.openInventory(Gui.buildGui(DataManager.getOrCreatePlayerData(player), args[1], craftData)), 1);
        return false;
    }

    private boolean runCategory() {
        Player player;
        if (this.args.length >= 3) {
            player = Bukkit.getPlayer(this.args[2]);
            if (player == null || !player.isOnline()) {
                this.sender.sendMessage(Message.getPrefix() + "§c玩家未在线或不存在");
                return false;
            }
        } else {
            if (!(this.sender instanceof Player)) {
                this.sender.sendMessage(Message.getPrefix() + Message.getNoPlayer());
                return false;
            }
            player = (Player)this.sender;
        }
        List<String> list = Config.getCategory().get(args[1]);
        if (list == null) {
            player.sendMessage(Message.getPrefix() + "§c未找到 §e" + args[1]);
            return false;
        }
        Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> Category.openGui(DataManager.getOrCreatePlayerData(player), args[1], list, 0), 1);
        return true;
    }

    private boolean runGet() {
        Player player;
        if (this.args.length >= 3) {
            player = Bukkit.getPlayer(this.args[2]);
            if (player == null || !player.isOnline()) {
                this.sender.sendMessage(Message.getPrefix() + "§c玩家未在线或不存在");
                return false;
            }
        } else {
            if (!(this.sender instanceof Player)) {
                this.sender.sendMessage(Message.getPrefix() + Message.getNoPlayer());
                return false;
            }
            player = (Player)this.sender;
        }
        CraftData craftData = Craft.getCraftData(this.args[1]);
        if (craftData == null) {
            player.sendMessage(Message.getPrefix() + "§c未找到 §e" + args[1]);
            return false;
        }
        player.sendMessage(Message.getPrefix() + "§a成功锻造出了 §e" + cn.jrmcdp.craftitem.config.Material.getItemName(craftData.getDisplayItem()));
        for (ItemStack item : craftData.getItems()) {
            for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                player.getWorld().dropItem(player.getLocation(), add);
                player.sendMessage(Message.getPrefix() + "§c背包已满 §d" + cn.jrmcdp.craftitem.config.Material.getItemName(add) + "§ex" + add.getAmount() + " §c掉了出来");
            }
        }
        for (String str : craftData.getCommands()) {
            String cmd = str.split("\\|\\|")[0];
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
        }
        return true;
    }

    private boolean runReload() {
        Message.reload();
        cn.jrmcdp.craftitem.config.Material.reload();
        Config.reload();
        Craft.reload();
        Gui.reload();
        Category.reload();
        this.sender.sendMessage(Message.getPrefix() + Message.getReload());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        switch (args.length) {
            case 1 : {
                List<String> list = new ArrayList<String>() {{
                    add("Category");
                    add("Open");
                    add("Get");
                    add("Create");
                    add("Delete");
                    add("Edit");
                    add("Reload");
                }};
                list.removeIf(next -> !next.toLowerCase().startsWith(args[0].toLowerCase()));
                return list;
            }
            case 2 : {
                switch (args[0].toLowerCase()) {
                    case "category" : {
                        List<String> list = new ArrayList<>(Config.getCategory().keySet());
                        list.removeIf(next -> !next.toLowerCase().startsWith(args[1].toLowerCase()));
                        return list;
                    }
                    default : {
                        List<String> list = new ArrayList<>(Craft.getCraftDataMap().keySet());
                        list.removeIf(next -> !next.toLowerCase().startsWith(args[1].toLowerCase()));
                        return list;
                    }
                }
            }
        }
        return null;
    }
}
