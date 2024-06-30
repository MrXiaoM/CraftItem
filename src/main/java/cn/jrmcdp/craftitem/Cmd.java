package cn.jrmcdp.craftitem;

import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.manager.DataManager;
import cn.jrmcdp.craftitem.holder.EditHolder;

import java.util.ArrayList;
import java.util.List;

import cn.jrmcdp.craftitem.utils.PlaceholderSupport;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Cmd implements CommandExecutor, TabCompleter {

    public static void register(JavaPlugin plugin, String name) {
        PluginCommand command = plugin.getCommand(name);
        if (command != null) {
            Cmd cmd = new Cmd();
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        } else {
            plugin.getLogger().warning("无法注册命令 /" + name);
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
        craftData = new CraftData();
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
        Message.craft__success.msg(player, craftData.getDisplayItem());
        for (ItemStack item : craftData.getItems()) {
            for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                player.getWorld().dropItem(player.getLocation(), add);
                Message.full_inventory.msg(player, add, add.getAmount());
            }
        }
        for (String str : craftData.getCommands()) {
            String cmd = str.split("\\|\\|")[0];
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderSupport.setPlaceholders(player, cmd));
        }
        return true;
    }

    private boolean runReload(CommandSender sender, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("messages")) {
            YamlConfiguration cfg = new YamlConfiguration();
            for (Message message : Message.values()) {
                String defValue = message.defValue.replace("§", "&");
                if (defValue.contains("\n")) {
                    List<String> list = Lists.newArrayList(defValue.split("\n"));
                    cfg.set(message.key, list);
                } else {
                    cfg.set(message.key, defValue);
                }
            }
            FileConfig.Message.saveConfig(cfg);
        }
        Message.reload();
        cn.jrmcdp.craftitem.config.Material.reload();
        Config.reload();
        Craft.reload();
        Gui.reload();
        Category.reload();
        return Message.reload.msg(sender);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        switch (args.length) {
            case 1 : {
                String arg0 = args[0].toLowerCase();
                List<String> list = Lists.newArrayList(
                    "category",
                    "open",
                    "get",
                    "create",
                    "delete",
                    "edit",
                    "reload"
                );
                list.removeIf(next -> !next.startsWith(arg0));
                return list;
            }
            case 2 : {
                String arg1 = args[1].toLowerCase();
                if (args[0].equalsIgnoreCase("category")) {
                    List<String> list = new ArrayList<>(Config.getCategory().keySet());
                    list.removeIf(next -> !next.toLowerCase().startsWith(arg1));
                    return list;
                }
                List<String> list = new ArrayList<>(Craft.getCraftDataMap().keySet());
                list.removeIf(next -> !next.toLowerCase().startsWith(arg1));
                return list;
            }
        }
        return null;
    }
}
