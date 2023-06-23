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

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length > 0) {
            String perm = command.getName() + ".command." + args[0];
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runReload(sender, args);
                case "category":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runCategory(sender, args);
                case "open":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runOpen(sender, args);
                case "get":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runGet(sender, args);
                case "edit":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runEdit(sender, args);
                case "create":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runCreate(sender, args);
                case "delete":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.msg(sender, perm);
                    }
                    return runDelete(sender, args);
                default:
                    break;
            }
        }
        return Message.help.msg0(sender);
    }

    private boolean runDelete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Message.no_player.msg(sender);
        }
        Player player = (Player)sender;
        CraftData craftData = Craft.getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.msg(player, args[1]);
        }
        Craft.delete(args[1]);
        return Message.delete__done.msg(player, args[1]);
    }

    private boolean runCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Message.no_player.msg(sender);
        }
        Player player = (Player)sender;
        CraftData craftData = Craft.getCraftData(args[1]);
        if (craftData != null) {
            return Message.create__found.msg(player, args[1]);
        }
        craftData = new CraftData(new ArrayList<>(), 75, Arrays.asList(5, 10, 20), 188, new ItemStack(Material.COBBLESTONE), new ArrayList<>(), new ArrayList<>());
        Craft.save(args[1], craftData);
        player.openInventory(EditHolder.buildGui(args[1], craftData));
        return true;
    }

    private boolean runEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Message.no_player.msg(sender);
        }
        Player player = (Player)sender;
        CraftData craftData = Craft.getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.msg(player, args[1]);
        }
        player.openInventory(EditHolder.buildGui(args[1], craftData));
        return true;
    }

    private boolean runOpen(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                return Message.not_online.msg(sender);
            }
        } else {
            if (!(sender instanceof Player)) {
                return Message.no_player.msg(sender);
            }
            player = (Player)sender;
        }
        CraftData craftData = Craft.getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.msg(player, args[1]);
        }
        Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> player.openInventory(Gui.buildGui(DataManager.getOrCreatePlayerData(player), args[1], craftData)), 1);
        return false;
    }

    private boolean runCategory(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                return Message.not_online.msg(sender);
            }
        } else {
            if (!(sender instanceof Player)) {
                return Message.no_player.msg(sender);
            }
            player = (Player)sender;
        }
        List<String> list = Config.getCategory().get(args[1]);
        if (list == null) {
            return Message.category__not_found.msg(player, args[1]);
        }
        Bukkit.getScheduler().runTaskLater(CraftItem.getPlugin(), () -> Category.openGui(DataManager.getOrCreatePlayerData(player), args[1], list, 0), 1);
        return true;
    }

    private boolean runGet(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                return Message.not_online.msg(sender);
            }
        } else {
            if (!(sender instanceof Player)) {
                return Message.no_player.msg(sender);
            }
            player = (Player)sender;
        }
        CraftData craftData = Craft.getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.msg(player, args[1]);
        }
        Message.craft__success.msg(player, Utils.getItemName(craftData.getDisplayItem()));
        for (ItemStack item : craftData.getItems()) {
            for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                player.getWorld().dropItem(player.getLocation(), add);
                Message.full_inventory.msg(player, Utils.getItemName(add), add.getAmount());
            }
        }
        for (String str : craftData.getCommands()) {
            String cmd = str.split("\\|\\|")[0];
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
        }
        return true;
    }

    private boolean runReload(CommandSender sender, String[] args) {
        Message.reload();
        cn.jrmcdp.craftitem.config.Material.reload();
        Config.reload();
        Craft.reload();
        Gui.reload();
        Category.reload();
        return Message.reload.msg(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
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
                if (args[0].equalsIgnoreCase("category")) {
                    List<String> list = new ArrayList<>(Config.getCategory().keySet());
                    list.removeIf(next -> !next.toLowerCase().startsWith(args[1].toLowerCase()));
                    return list;
                }
                List<String> list = new ArrayList<>(Craft.getCraftDataMap().keySet());
                list.removeIf(next -> !next.toLowerCase().startsWith(args[1].toLowerCase()));
                return list;
            }
        }
        return null;
    }
}
