package me.zort.devroomtrial.spigot

object BukkitUtilities {

    @JvmStatic
    fun millisToTicks(millis: Long): Long {
        return millis / 50L
    }

    @JvmStatic
    fun tickToMillis(ticks: Long): Long {
        return ticks * 50L
    }

}