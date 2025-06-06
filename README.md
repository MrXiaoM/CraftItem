# CraftItem

[![Releases](https://img.shields.io/github/downloads/MrXiaoM/CraftItem/total)](https://plugins.mcio.dev/dl?repo=CraftItem)

本插件 Fork 自 https://gitee.com/zhibumiao/CraftItem

## 介绍

- 一款 Minecraft 管理起来方便又快捷的 Bukkit 锻造插件
- 此插件原本发布在 我的世界中文论坛 MCBBS
- [RPG|经济]CraftItem — 方便快捷又独特的锻造插件 [1.8-1.19]
- https://www.mcbbs.net/thread-1395391-1-1.html
- 支持全GUI配置锻造配方
- 详细介绍请见新帖 https://www.minebbs.com/resources/8269

## 安装教程

1. 于 [MCIO Plugins](https://plugins.mcio.dev/dl?repo=CraftItem) 下载插件
2. 放入服务器根目录 plugins 文件夹内
3. 重启以加载插件 开始享用吧

## 详情

以下为本 Fork 对于原插件的修改

* 使用 Gradle 管理依赖，便于贡献代码
* **使用 PluginBase 重构**
* 将硬编码的字符串提取到配置文件
* 添加事件，便于编写附属
* 修正一些严重bug
比如 监听命令方式不正确 锻造成果物品莫名减少 锻造配方名不能用`.`号 副手的锻造材料不会扣除 等等
* 添加更多可配置项
* 添加小游戏作为锻造难度挑战 (来自 [Custom-Fishing](https://github.com/Xiao-MoMi/Custom-Fishing)，感谢开源! 配套材质包可以在[这里](https://github.com/MrXiaoM/CraftItem/wiki/resourcepack.zip)下载)
* 添加**时长锻造**选项，玩家可以仅仅在提交材料后，等待一定时间即可领取锻造成品
* 添加**连击锻造**选项，允许玩家一键连续进行多次普通锻造
* 添加**锻造保底**选项，玩家失败达到保底次数后，失败将变为小成功
* 材料不足时提示玩家缺少哪些材料
* 移除了 XSeries 依赖
* 只需要依赖 Vault 和经济插件即可运行最基本的功能

## 开发者

[![](https://jitpack.io/v/MrXiaoM/CraftItem.svg)](https://jitpack.io/#MrXiaoM/CraftItem)

```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    compileOnly("com.github.MrXiaoM:CraftItem:$VERSION")
}
```
