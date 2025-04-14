package cn.jrmcdp.craftitem.commands;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.config.*;
import cn.jrmcdp.craftitem.data.CraftData;
import cn.jrmcdp.craftitem.data.PlayerData;
import cn.jrmcdp.craftitem.func.AbstractModule;
import cn.jrmcdp.craftitem.gui.GuiEdit;
import cn.jrmcdp.craftitem.manager.CraftDataManager;
import cn.jrmcdp.craftitem.manager.PlayerDataManager;
import cn.jrmcdp.craftitem.utils.ConfigUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.PAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter {
    public CommandMain(CraftItem plugin) {
        super(plugin);
        registerCommand("CraftItem", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            String perm = command.getName() + ".command." + args[0];
            switch (args[0].toLowerCase()) {
                case "reload":
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runReload(sender, args);
                case "category":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runCategory(sender, args);
                case "open":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runOpen(sender, args);
                case "get":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runGet(sender, args);
                case "edit":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runEdit(sender, args);
                case "create":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runCreate(sender, args);
                case "delete":
                    if (args.length < 2) break;
                    if (!sender.hasPermission(perm)) {
                        return Message.no_permission.tm(sender, perm);
                    }
                    return runDelete(sender, args);
                default:
                    break;
            }
        }
        return Message.help.tm(sender);
    }

    private boolean runDelete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Message.no_player.tm(sender);
        }
        Player player = (Player)sender;
        CraftDataManager manager = CraftDataManager.inst();
        CraftData craftData = manager.getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.tm(player, args[1]);
        }
        manager.delete(args[1]);
        return Message.delete__done.tm(player, args[1]);
    }

    private boolean runCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Message.no_player.tm(sender);
        }
        Player player = (Player)sender;
        CraftDataManager manager = CraftDataManager.inst();
        CraftData craftData = manager.getCraftData(args[1]);
        if (craftData != null) {
            return Message.create__found.tm(player, args[1]);
        }
        craftData = new CraftData();
        manager.save(args[1], craftData);

        GuiEdit.openGui(player, args[1], craftData);
        return true;
    }

    private boolean runEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Message.no_player.tm(sender);
        }
        Player player = (Player)sender;
        CraftData craftData = CraftDataManager.inst().getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.tm(player, args[1]);
        }
        GuiEdit.openGui(player, args[1], craftData);
        return true;
    }

    private boolean runOpen(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                return Message.not_online.tm(sender);
            }
        } else {
            if (!(sender instanceof Player)) {
                return Message.no_player.tm(sender);
            }
            player = (Player)sender;
        }
        CraftData craftData = CraftDataManager.inst().getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.tm(player, args[1]);
        }
        PlayerData playerData = PlayerDataManager.inst().getOrCreatePlayerData(player);
        ConfigForgeGui.inst().openGui(playerData, args[1], craftData);
        return false;
    }

    private boolean runCategory(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                return Message.not_online.tm(sender);
            }
        } else {
            if (!(sender instanceof Player)) {
                return Message.no_player.tm(sender);
            }
            player = (Player)sender;
        }
        List<String> list = plugin.config().getCategory().get(args[1]);
        if (list == null) {
            return Message.category__not_found.tm(player, args[1]);
        }
        PlayerData playerData = PlayerDataManager.inst().getOrCreatePlayerData(player);
        ConfigCategoryGui.inst().openGui(playerData, args[1], list, 0);
        return true;
    }

    private boolean runGet(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                return Message.not_online.tm(sender);
            }
        } else {
            if (!(sender instanceof Player)) {
                return Message.no_player.tm(sender);
            }
            player = (Player)sender;
        }
        CraftData craftData = CraftDataManager.inst().getCraftData(args[1]);
        if (craftData == null) {
            return Message.craft__not_found.tm(player, args[1]);
        }
        Message.craft__success.tm(player, craftData.getDisplayItem());
        for (ItemStack item : craftData.getItems()) {
            for (ItemStack add : player.getInventory().addItem(new ItemStack[] { item }).values()) {
                player.getWorld().dropItem(player.getLocation(), add);
                Message.full_inventory.tm(player, add, add.getAmount());
            }
        }
        for (String str : craftData.getCommands()) {
            String cmd = str.split("\\|\\|")[0];
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PAPI.setPlaceholders(player, cmd));
        }
        return true;
    }

    private boolean runReload(CommandSender sender, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("messages")) {
            YamlConfiguration cfg = new YamlConfiguration();
            for (Message message : Message.values()) {
                Object defValue = message.holder().defaultValue;
                cfg.set(message.holder().key, defValue);
            }
            File file = plugin.resolve("Message.yml");
            ConfigUtils.savePluginConfig(plugin, "Message.yml", cfg);
        }
        plugin.reloadConfig();
        return Message.reload.tm(sender);
    }

    private List<String> args0 = Lists.newArrayList(
            "category",
            "open",
            "get",
            "create",
            "delete",
            "edit",
            "reload"
    );
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        switch (args.length) {
            case 1 : {
                String arg0 = args[0].toLowerCase();
                List<String> list = new ArrayList<>();
                for (String s : args0) {
                    if (s.startsWith(arg0)) {
                        list.add(s);
                    }
                }
                return list;
            }
            case 2 : {
                String arg1 = args[1].toLowerCase();
                if (args[0].equalsIgnoreCase("category")) {
                    List<String> list = new ArrayList<>();
                    for (String s : plugin.config().getCategory().keySet()) {
                        if (s.startsWith(arg1)) {
                            list.add(s);
                        }
                    }
                    return list;
                }
                List<String> list = new ArrayList<>();
                for (String s : CraftDataManager.inst().getCraftDataMap().keySet()) {
                    if (s.startsWith(arg1)) {
                        list.add(s);
                    }
                }
                return list;
            }
        }
        return null;
    }
}
