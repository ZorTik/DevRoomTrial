package me.zort.devroomtrial.spigot.configuration

enum class Message(val key: String, val defValue: String) {

    CHEST_ANNOUNCE("chest-announce", "&a%player%'s &edeath chest &fis located at &eX: &a%x% &eY: &a%y% &eZ: &a%z%"),
    DEATH_KEY_TITLE("death-key-title", "&cDeath Key"),
    DEATH_KEY_LORE("death-key-lore", "§7With this key you can open an Death Chest from another player"),
    DEATH_KEY_NO_KEY_MESSAGE("no-death-key-message", "&c&lHEY! &cYou don't have death key in your hand, so you can't open this death chest")

}