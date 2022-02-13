package me.zort.devroomtrial.spigot

import com.j256.ormlite.dao.Dao
import de.tr7zw.changeme.nbtapi.NBTItem
import me.zort.devroomtrial.DevRoomTrial
import me.zort.devroomtrial.data.MysqlDataBridge
import me.zort.devroomtrial.spigot.configuration.Message
import me.zort.devroomtrial.spigot.data.DeathLocationEntity
import me.zort.devroomtrial.spigot.event.DeathLocationSaveEvent
import org.apache.commons.lang.RandomStringUtils
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getScheduler
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

internal class DeathLocationsService(private val plugin: DevRoomTrial, private val dataBridge: MysqlDataBridge): Listener, Runnable {

    companion object {
        private val DLS_BASE_INTERVAL = BukkitUtilities.millisToTicks(
            TimeUnit.SECONDS.toMillis(1)
        )
        private const val NBT_KEY = "dev-room-trial-item"
    }

    init {
        rep(DLS_BASE_INTERVAL, this)
    }

    private val dao: Dao<DeathLocationEntity, String> = dataBridge.getDao(DeathLocationEntity::class.java, String::class.java)
    private val locsCache: MutableList<DeathLocationEntity> = dao.queryForAll()
    private val tickWorkers: MutableList<DLSTickWorker> = mutableListOf<DLSTickWorker>(
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
    )
    private var uptime: Long = 0L
        get

    fun saveLocationAsync(nick: String, loc: Location, content: Array<out ItemStack>): CompletableFuture<Pair<DeathLocationEntity, Dao.CreateOrUpdateStatus>> {
        return cfEx(CompletableFuture.supplyAsync { saveLocation(nick, loc, content) })
    }

    fun forceRemoveCurrentAsync(nick: String): CompletableFuture<DeathLocationEntity?> {
        return cfEx(CompletableFuture.supplyAsync { forceRemoveCurrent(nick) })
    }

    fun saveLocation(nick: String, loc: Location, content: Array<out ItemStack>): Pair<DeathLocationEntity, Dao.CreateOrUpdateStatus> {
        forceRemoveCurrent(nick)
        val e = DeathLocationEntity().also {
            it.name = nick
            it.loc = loc
            it.replacement = DeathLocationEntity.Replacement
                .fromBlock(loc)
        }
        val saveStatus: Dao.CreateOrUpdateStatus = dao.createOrUpdate(e)
        locsCache.add(e)
        sync {
            loc.block.type = Material.CHEST
            val chest = loc.block.state as Chest
            chest.blockInventory.addItem(*(content.filterNotNull().toTypedArray()))
            val g: () -> Block = {
                loc.block.location.clone().subtract(0.0, 0.0, 1.0).block
            }
            val signBlock = g.invoke()
            signBlock.type = Material.OAK_WALL_SIGN
            val state = g.invoke().state as Sign
            state.setLine(0, nick)
            state.update()
            e(DeathLocationSaveEvent(e, saveStatus))
        }
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

    fun isDeathChest(block: Block): Boolean {
        return getChestOwner(block) != null
    }

    fun isDeathChestSign(block: Block): Boolean {
        return block.type.name.contains("SIGN") && BlockFace.values().any {
            isDeathChest(block.getRelative(it))
        }
    }

    fun getChestOwner(block: Block): DeathLocationEntity? {
        return locsCache.firstOrNull {
            it.loc.block == block
        }
    }

    fun getDeathChestLocation(nick: String): DeathLocationEntity? {
        return locsCache.firstOrNull {
            it.name == nick
        }
    }

    fun buildDeathKey(): ItemStack {
        val item = ItemStack(Material.STICK, 1)
        val meta = item.itemMeta!!
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
        meta.setDisplayName(plugin.getConfigMessage(Message.DEATH_KEY_TITLE))
        meta.lore = mutableListOf(plugin.getConfigMessage(Message.DEATH_KEY_LORE))
        item.itemMeta = meta
        val nbtItem = NBTItem(item)
        nbtItem.setString(NBT_KEY, "death-key")
        return nbtItem.item
    }

    fun isDeathKey(item: ItemStack?): Boolean {
        if(item == null) return false
        val nbtItem = NBTItem(item)
        return nbtItem.hasKey(NBT_KEY) && nbtItem.getString(NBT_KEY).equals("death-key", true)
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

    private fun rep(i: Long, r: Runnable) {
        getScheduler().runTaskTimer(plugin, r, 0L, i)
    }

    private fun lat(d: Long, r: Runnable) {
        getScheduler().runTaskLater(plugin, r, d)
    }

    private fun e(e: Event) {
        Bukkit.getServer().pluginManager.callEvent(e)
    }

    private fun <T> cfEx(f: CompletableFuture<T>): CompletableFuture<T> {
        return f.whenComplete { _, ex -> ex?.printStackTrace() }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val p = e.entity
        val content = arrayOf(*p.inventory.contents)
        p.inventory.clear()
        e.drops.clear()
        saveLocationAsync(p.name, p.location, content).thenRun {
            val config = plugin.config
            val d = config.getLong("times.chest-announce-delay")
            lat(d) {
                val chestLoc = getDeathChestLocation(p.name)
                if(chestLoc != null) {
                    plugin.getConfigMessage(Message.CHEST_ANNOUNCE)
                        .replace("%player%", p.name)
                        .replace("%x%", chestLoc.loc.blockX.toString())
                        .replace("%y%", chestLoc.loc.blockY.toString())
                        .replace("%z%", chestLoc.loc.blockZ.toString())
                }
            }
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        val p = e.player
        val clickedBlock = e.clickedBlock
        if(e.action == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
            val owner = getChestOwner(clickedBlock)
            val isDeathChestSign = isDeathChestSign(clickedBlock)
            val item = e.item
            if(owner != null || isDeathChestSign) {
                e.isCancelled = if(item != null && isDeathKey(item) && !isDeathChestSign) {
                    p.inventory.removeItem(item)
                    false
                } else {
                    p.sendMessage(plugin.getConfigMessage(Message.DEATH_KEY_NO_KEY_MESSAGE))
                    val vec = p.location.direction.clone()
                        .multiply(-1.0)
                    p.velocity = vec
                    true
                }
            }
        }
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        val b = e.block
        if(isDeathChest(b) || isDeathChestSign(b)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val p = e.player
        val inv = e.inventory
        val holder = inv.holder
        if(holder != null && holder is Chest) {
            val block = holder.block
            if(isDeathChest(block)) {
                val content = inv.contents
                forceRemoveCurrentAsync(p.name).thenRun {
                    sync {
                        content.filterNotNull().forEach { item ->
                            block.world.dropItem(block.location.clone().add(0.5, 0.0, 0.5), item)
                        }
                    }
                }
            }
        }
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