package cn.jrmcdp.craftitem.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public enum Message {
    prefix("&4&lCraftItem &8>> &e"),
    reload("&a配置文件重载成功."),
    no_permission("&c缺少权限 &e%s&c."),
    no_player("&e此命令只有玩家能执行."),
    help(
            "&4&lCraftItem",
            "  &8➥ &b/ci Category <TypeID> [Player] &7给 自己/别人 配方分类界面",
            "  &8➥ &b/ci Open <ItemID> [Player] &7给 自己/别人 打开锻造界面",
            "  &8➥ &b/ci Get <ItemID> [Player] &7给 自己/别人 锻造成功后的奖励",
            "  &8➥ &b/ci Create <ItemID> &7创建锻造配方",
            "  &8➥ &b/ci Delete <ItemID> &7删除锻造配方",
            "  &8➥ &b/ci Edit <ItemID> &7编辑锻造配方",
            "  &8➥ &b/ci Reload &7重载配置文件"
    ),
    not_online("&c玩家未在线或不存在."),
    category__not_found("&c未找到 &e%s&c."),
    delete__done("&a成功删除 &e%s&c."),
    create__found("&c已存在 &e%s&c."),
    full_inventory("&c背包已满, &d%s&ex%d &c掉了出来."),

    craft__not_found("&c未找到 &e%s&c."),
    craft__success("&a成功锻造出了 &e%s&a."),
    craft__not_enough_money("&e没有足够的金币来锻造."),
    craft__not_enough_material("&e身上没有足够的材料."),
    craft__process_success_small("&a锻造 小成功 ！！！ &f&l[ &a+ &e%d%% &f&l]"),
    craft__process_success_medium("&a锻造 成功 ！！！ &f&l[ &a+ &e%d%% &f&l]"),
    craft__process_success_big("&a锻造 大成功 ！！！ &f&l[ &a+ &e%d%% &f&l]"),
    craft__process_fail_small("&c锻造 小失败 ！！！ &f&l[ &c- &e%d%% &f&l]"),
    craft__process_fail_medium("&c锻造 失败 ！！！ &f&l[ &c- &e%d%% &f&l]"),
    craft__process_fail_big("&c锻造 大失败 ！！！ &f&l[ &c- &e%d%% &f&l]"),
    craft__process_fail_lost_item("&c并且还损坏了 &e%d &c个 &e%s&c."),

    page__already_first("&e当前已是首页."),
    page__already_last("&e当前已是尾页."),

    gui__edit_title("编辑 %s"),
    gui__edit__item__material__name("&a材料"),
    gui__edit__item__material__lore(
            "&7点击 查看/编辑",
            "",
            "&7当前:",
            "%s"
    ),
    gui__edit__item__successful_rate__name("&a成功率"),
    gui__edit__item__successful_rate__lore(
            "§7点击 编辑",
            "§7使用正整数",
            "",
            "§7当前: §e%d"
    ),
    gui__edit__item__multiple__name("&a倍数"),
    gui__edit__item__multiple__lore(
            "§7点击 编辑",
            "§7格式 \"5 10 20\"",
            "§7对应 小/中/大 失败/成功 的 涨幅/跌幅",
            "",
            "§7当前: §e%s"
    ),
    gui__edit__item__cost__name("&a价格"),
    gui__edit__item__cost__lore(
            "§7点击 查看/编辑",
            "§7使用正整数",
            "",
            "§7当前: §e%d"
    ),
    gui__edit__item__display__name("&a显示物品"),
    gui__edit__item__display__lore(
            "§7点击 查看/编辑",
            "§7对外显示的物品外貌",
            "",
            "§7当前: §e%s"
    ),
    gui__edit__item__item__name("&a奖励物品"),
    gui__edit__item__item__lore(
            "§7点击 查看/编辑",
            "§7锻造成功后给予的物品",
            "",
            "§7当前:",
            "%s"
    ),
    gui__edit__item__command__name("&a奖励命令"),
    gui__edit__item__command__lore(
            "§7点击 查看/编辑",
            "§7格式 \"say 这个插件太棒了||服务器说这个插件太棒了\"",
            "§7用 || 分割，左边是命令 右边是显示出来的介绍",
            "§7此处支持 PAPI 变量",
            "§7锻造成功后执行的命令",
            "",
            "§7当前:",
            "%s"
    ),


    gui__edit_material_title("材料"),
    gui__edit_input_chance("&a请输入概率 正整数"),
    gui__edit_input_multiple("&a请按照格式填写倍率 \"5 10 20\" (小 中 大)"),
    gui__edit_input_cost("&a请输入金额 正整数"),
    gui__edit_display_title("将要展示的物品放在第一格"),
    gui__edit_display_not_found("&c未找到第一格的物品"),
    gui__edit_item_title("奖励物品"),
    gui__edit_command_title("奖励命令"),
    not_integer("&a请输入整数"),

    ;
    private static final Map<Message, String> config = new HashMap<>();
    public final String defValue;
    public final String key = name().replace("__", ".").replace("_", "-").toLowerCase();
    Message(String defValue) {
        this.defValue = ChatColor.translateAlternateColorCodes('&', defValue);
    }
    Message(String... defValue) {
        this.defValue = ChatColor.translateAlternateColorCodes('&', String.join("\n&r", defValue));
    }

    @Override
    public String toString() {
        return get();
    }
    public List<String> list(Object... args) {
        return Lists.newArrayList(get(args).split("\n"));
    }
    public String get(Object... args) {
        return String.format(config.getOrDefault(this, defValue), args);
    }
    public boolean msg0(CommandSender sender, Object... args) {
        sender.sendMessage(get(args));
        return true;
    }
    public boolean msg(CommandSender sender, Object... args) {
        sender.sendMessage(prefix.get() + get(args));
        return true;
    }
    
    public static void reload() {
        FileConfiguration config = FileConfig.Message.getConfig();
        Message.config.clear();
        for (Message m : values()) {
            List<String> list = config.getStringList(m.key);
            String str = !list.isEmpty() ? String.join("\n&r", list) : config.getString(m.key);
            if (str != null) {
                Message.config.put(m, ChatColor.translateAlternateColorCodes('&', str));
            }
        }
    }
}
