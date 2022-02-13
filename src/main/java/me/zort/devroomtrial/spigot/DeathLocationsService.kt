package me.zort.devroomtrial.spigot

import com.j256.ormlite.dao.Dao
import me.zort.devroomtrial.DevRoomTrial
import me.zort.devroomtrial.data.MysqlDataBridge
import me.zort.devroomtrial.spigot.data.DeathLocationEntity
import org.apache.commons.lang.RandomStringUtils
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Location
import org.bukkit.event.Listener
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

internal class DeathLocationsService(private val plugin: DevRoomTrial, private val dataBridge: MysqlDataBridge): Listener, Runnable {

    companion object {
        private val DLS_BASE_INTERVAL = BukkitUtilities.millisToTicks(
            TimeUnit.SECONDS.toMillis(1)
        )
    }

    init {
        rep(this, DLS_BASE_INTERVAL)
    }

    private val dao: Dao<DeathLocationEntity, String> = dataBridge.getDao(DeathLocationEntity::class.java, String::class.java)
    private val locsCache: MutableList<DeathLocationEntity> = dao.queryForAll()
    private val tickWorkers: MutableList<DLSTickWorker> = mutableListOf<DLSTickWorker>().also {
        // TODO: Defaults
        it.addAll(mutableListOf(
                DLSTickWorker(action = {
                    val remCandidates = locsCache.filter(DeathLocationEntity::hasExpired)
                    async {
                        remCandidates.forEach { e ->
                            forceRemoveCurrent(e.name)
                        }
                    }
                }).withApproval { service ->
                    service.locsCache.any(DeathLocationEntity::hasExpired)
                }
            ))
    }
    private var uptime: Long = 0L
        get

    fun saveLocationAsync(nick: String, loc: Location): CompletableFuture<Pair<DeathLocationEntity, Dao.CreateOrUpdateStatus>> {
        return CompletableFuture.supplyAsync { saveLocation(nick, loc) }
    }

    fun forceRemoveCurrentAsync(nick: String): CompletableFuture<DeathLocationEntity?> {
        return CompletableFuture.supplyAsync { forceRemoveCurrent(nick) }
    }

    fun saveLocation(nick: String, loc: Location): Pair<DeathLocationEntity, Dao.CreateOrUpdateStatus> {
        forceRemoveCurrent(nick)
        val e = DeathLocationEntity().also {
            it.name = nick
            it.loc = loc
            it.replacement = DeathLocationEntity.Replacement
                .fromBlock(loc)
        }
        val saveStatus: Dao.CreateOrUpdateStatus = dao.createOrUpdate(e)
        return Pair(e, saveStatus)
    }

    fun forceRemoveCurrent(nick: String): DeathLocationEntity? {
        val e = dao.queryForId(nick)
        return if(e != null) {
            val loc = e.loc
            val replacement = e.replacement
            dao.delete(e)
            locsCache.removeIf {
                it.name == nick
            }
            sync {
                replacement.apply(loc)
            }
            e
        } else null
    }

    fun registerWorker(action: (DeathLocationsService) -> Unit, interval: Long): String? {
        if(interval < 20L) {
            // Interval should not be lower than the base default worker interval.
            return null
        }
        val tw = DLSTickWorker(interval, action)
        tickWorkers.add(tw)
        return tw.sessionCode
    }

    override fun run() {
        val tickCandidates = tickCandidates()
        tickCandidates.forEach {
            it.action.invoke(this)
        }
        uptime += DLS_BASE_INTERVAL
    }

    private fun tickCandidates(): List<DLSTickWorker> {
        return tickWorkers.filter {
            it.preCheck(this)
        }
    }

    private fun sync(r: Runnable) {
        getScheduler().runTask(plugin, r)
    }

    private fun async(r: Runnable) {
        getScheduler().runTaskAsynchronously(plugin, r)
    }

    private fun rep(r: Runnable, i: Long) {
        getScheduler().runTaskTimer(plugin, r, 0L, i)
    }

    internal class DLSTickWorker(val interval: Long = DLS_BASE_INTERVAL, val action: (DeathLocationsService) -> Unit, var approval: DLSTickApproval? = null) {

        private val idI: String = RandomStringUtils.randomAlphanumeric(8)

        internal fun preCheck(service: DeathLocationsService): Boolean {
            return service.uptime % interval == 0L && approval?.approve(service)?: true
        }

        fun withApproval(approval: DLSTickApproval): DLSTickWorker {
            this.approval = approval
            return this
        }

        val sessionCode: String
            get() = idI

    }

    fun interface DLSTickApproval {

        fun approve(service: DeathLocationsService): Boolean

    }

}