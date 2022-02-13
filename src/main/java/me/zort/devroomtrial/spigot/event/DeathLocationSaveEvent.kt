package me.zort.devroomtrial.spigot.event

import com.j256.ormlite.dao.Dao
import me.zort.devroomtrial.spigot.data.DeathLocationEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class DeathLocationSaveEvent(val entity: DeathLocationEntity, val status: Dao.CreateOrUpdateStatus): Event(false) {

    companion object {
        private val handlerList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}