package cn.jrmcdp.craftitem.config;

import cn.jrmcdp.craftitem.CraftItem;
import cn.jrmcdp.craftitem.minigames.utils.AdventureManagerImpl;
import cn.jrmcdp.craftitem.utils.Utils;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "")
public enum Message implements IHolderAccessor {
    prefix("&4&lCraftItem &8>> &e"),
    reload("&a配置文件重载成功."),
    reload_no_database("&a当前使用 YAML 储存数据，无需重连数据库."),
    reload_database("&a已重新连接数据库."),
    no_permission("&c缺少权限 &e%s&c."),
    no_player("&e此命令只有玩家能执行."),
    no_protocollib("&e服务器未安装依赖 ProtocolLib."),
    no_minigames("&e未配置困难锻造小游戏列表 &7(config.yml:RandomGames)&e，请联系服务器管理员"),
    not_expected("&e参数异常&7(%s)&e，请联系服务器管理员"),
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
    craft__edit_not_confirm(
            "",
            "  &e&l警告&f 你正在尝试编辑与其它服务端共用的配置文件。",
            "  &f请确保在其它服务端没有其它管理员在编辑，否则你的修改",
            "  &f将覆盖其他人的修改。",
            "  &f请在编辑之前执行一次重载，以免覆盖来自其它服务端的修改。",
            "  &b在阅读并理解上述警告后，使用以下命令确认编辑",
            "  &e/craftitem %command% confirm"
    ),
    craft__time_start("&a已开启时长锻造，时间结束后再次打开锻造书即可锻造成功。你也可以在锻造书中查看进度和剩余时间。"),
    craft__success("&a成功锻造出了 &e%s&a."),
    craft__not_enough_money("&e没有足够的金币来锻造."),
    craft__not_enough_level("&e没有足够的经验等级来锻造."),
    craft__not_enough_material("&e身上没有足够的材料."),
    craft__not_enough_material_details("&7需要 %s&7 (&f%d &7/ &f%d&7)."),
    craft__forge_limit("&e当前已到达锻造次数上限&7 (%d)"),
    craft__time_forge_limit("&e当前已到达时长锻造次数上限&7 (%d)"),
    craft__process_success_small("&a锻造 小成功 ！！！ &f&l[ &a+ &e%d%% &f&l]"),
    craft__process_success_medium("&a锻造 成功 ！！！ &f&l[ &a+ &e%d%% &f&l]"),
    craft__process_success_big("&a锻造 大成功 ！！！ &f&l[ &a+ &e%d%% &f&l]"),
    craft__process_fail_small("&c锻造 小失败 ！！！ &f&l[ &c- &e%d%% &f&l]"),
    craft__process_fail_medium("&c锻造 失败 ！！！ &f&l[ &c- &e%d%% &f&l]"),
    craft__process_fail_big("&c锻造 大失败 ！！！ &f&l[ &c- &e%d%% &f&l]"),
    craft__process_fail_lost_item("&c并且还损坏了 &e%d &c个 &e%s&c."),
    craft__unlimited("&b无限制"),
    craft__limited("&e%d &7/ &e%d"),

    page__already_first("&e当前已是首页."),
    page__already_last("&e当前已是尾页."),

    gui__edit_title("&0编辑 %s"),
    gui__edit__item__material__name("&a材料"),
    gui__edit__item__material__lore(
            "&7点击 查看/编辑",
            "",
            "&7当前:",
            "%s"
    ),
    gui__edit__item__material__too_much("&e你放的材料太多了! 超出了界面能够显示的范围，玩家将无法从界面中看到完整材料列表。&b请调整材料列表，或调整界面配置。"),
    gui__edit__item__successful_rate__name("&a成功率"),
    gui__edit__item__successful_rate__lore(
            "&7点击 编辑",
            "&7使用正整数",
            "",
            "&7当前: &e%d"
    ),
    gui__edit__item__multiple__name("&a倍数"),
    gui__edit__item__multiple__lore(
            "&7点击 编辑",
            "&7格式 \"5 10 20\"",
            "&7对应 小/中/大 失败/成功 的 涨幅/跌幅",
            "",
            "&7当前: &e%s"
    ),
    gui__edit__item__cost__name("&a价格"),
    gui__edit__item__cost__lore(
            "&7点击 查看/编辑",
            "&7使用正整数",
            "",
            "&7当前: &e%d"
    ),
    gui__edit__item__cost_level__name("&a花费经验等级"),
    gui__edit__item__cost_level__lore(
            "&7点击 查看/编辑",
            "&7使用正整数",
            "",
            "&7当前: &e%d"
    ),
    gui__edit__item__display__name("&a显示物品"),
    gui__edit__item__display__lore(
            "&7点击 查看/编辑",
            "&7对外显示的物品外貌",
            "",
            "&7当前: &e%s"
    ),
    gui__edit__item__item__name("&a奖励物品"),
    gui__edit__item__item__lore(
            "&7点击 查看/编辑",
            "&7锻造成功后给予的物品",
            "",
            "&7当前:",
            "%s"
    ),
    gui__edit__item__command__name("&a奖励命令"),
    gui__edit__item__command__lore(
            "&7点击 查看/编辑",
            "&7格式 \"say 这个插件太棒了||服务器说这个插件太棒了\"",
            "&7用 || 分割，左边是命令 右边是显示出来的介绍",
            "&7此处支持 PAPI 变量",
            "&7锻造成功后执行的命令",
            "",
            "&7当前:",
            "%s"
    ),
    gui__edit__item__time__name("&a锻造时长"),
    gui__edit__item__time__lore(
            "&7左键点击 增加1分钟",
            "&7右键点击 减少1分钟",
            "&7Shift+左键点击 增加10分钟",
            "&7Shift+右键点击 减少10分钟",
            "&7鼠标悬停按Q键 设置所需金币",
            "",
            "&7当时间大于0时，在锻造界面增加“时长锻造”选项",
            "&7玩家可以选择进行“时长锻造”操作，花费一定金币",
            "&7等待一段时间即可获得奖励，无需敲打锻造。",
            "",
            "&7当前时长: &f%s",
            "&7花费金币: &f%s"
    ),
    gui__edit__item__time_count_limit__name("&a锻造次数限制"),
    gui__edit__item__time_count_limit__lore(
            "&7左键点击 选择限制组 普通/困难锻造",
            "&7右键点击 移除限制组 普通/困难锻造",
            "",
            "&7Shift+左键点击 选择限制组 时长锻造",
            "&7Shift+右键点击 移除限制组 时长锻造",
            "",
            "&7设置锻造次数限制后，玩家被限制只能进行指定次数",
            "&7的指定类型锻造，超过次数无法再进行该类型锻造。",
            "&7限制组的详细设定详见配置文件 config.yml",
            "",
            "&7普通/困难锻造 限制组: &e%s",
            "&7时长锻造 限制组: &e%s"
    ),
    gui__edit__item__difficult__name("&a困难锻造"),
    gui__edit__item__difficult__lore(
            "&7左键点击 切换状态",
            "",
            "&7开启困难锻造之后，普通的敲打锻造将会",
            "&7被替换为 CustomFishing 小游戏锻造。",
            "&7小游戏失败则敲打失败，小游戏成功再按概率",
            "&7计算是否成功。",
            "",
            "&7状态: %s"
    ),
    gui__edit__item__fail_times__name("&a保底次数"),
    gui__edit__item__fail_times__lore(
            "&7左键点击 增加1",
            "&7右键点击 减少1",
            "&7Shift+左键点击 增加10",
            "&7Shift+右键点击 减少10",
            "",
            "&7设置保底次数后，当失败次数超过保底次数时，",
            "&7所有类型的失败将变为小成功",
            "",
            "&7保底次数: &e%s"
    ),
    gui__edit__item__combo__name("&a连击次数"),
    gui__edit__item__combo__lore(
            "&7左键点击 增加1",
            "&7右键点击 减少1",
            "&7Shift+左键点击 增加10",
            "&7Shift+右键点击 减少10",
            "",
            "&7设置连击次数后，玩家在普通锻造中",
            "&7可以鼠标右键进行锻造连击，即进行数次锻造判定，",
            "&7当判定时因大失败导致材料不足，将终止连击，保留进度。",
            "",
            "&7单次连击次数: &e%s"
    ),


    gui__edit_material_title("&0材料"),
    gui__edit_input_chance("&a请输入概率 正整数"),
    gui__edit_input_multiple("&a请按照格式填写倍率 \"5 10 20\" (小 中 大)"),
    gui__edit_input_cost("&a请输入金额 正整数"),
    gui__edit_input_cost_level("&a请输入经验等级 正整数"),
    gui__edit_display_title("将要展示的物品放在第一格"),
    gui__edit_display_not_found("&c未找到第一格的物品"),
    gui__edit_item_title("奖励物品"),
    gui__edit_command_title("奖励命令 (点击空格子添加)"),
    gui__edit_command_tips(
            "&a请在聊天栏发送命令 &7(无需/)&a，支持PAPI变量",
            "  &a格式: &e命令||显示内容",
            "  &a示例: &esay 这个插件太棒了||服务器说这个插件太棒了"),
    gui__edit_command_lore("", "&4点击删除"),
    gui__edit_time_limit_count_title_normal("&0选择 普通/困难锻造限制组"),
    gui__edit_time_limit_count_title_time("&0选择 时长锻造限制组"),
    gui__edit_time_cost_sum__tips(
            "&a按以下格式修改数值 正整数",
            "&e  M+数值 &f修改时长锻造价格，如&e M100 &f消耗100金币",
            "&e  L+数值 &f修改时长锻造消耗经验等级，如&e L3 &f消耗3级经验"),
    gui__edit_time_cost_sum__wrong_type("&e时长锻造消耗输入格式有误"),

    gui__category__not_found("&c未找到 &e%s"),
    gui__craft_info__lore__header(
            "",
            "&a包含:"
    ),
    gui__craft_info__lore__item(" &8➥ &e%s&fx%d"),
    gui__craft_info__lore__command(" &8➥ &e%s"),

    gui__edit__status__on("&a开启"),
    gui__edit__status__off("&c关闭"),
    gui__edit__unset("&f未设置"),

    not_integer("&a请输入整数"),

    ;
    Message(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Message(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Message(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    // 4. 添加字段 holder 以及它的 getter
    private final LanguageEnumAutoHolder<Message> holder;
    public LanguageEnumAutoHolder<Message> holder() {
        return holder;
    }
    @Override
    public String toString() {
        return str();
    }
}
