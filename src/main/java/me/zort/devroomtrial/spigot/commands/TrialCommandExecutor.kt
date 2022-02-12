package me.zort.devroomtrial.spigot.commands

import me.zort.devroomtrial.DevRoomTrial
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TrialCommandExecutor(private val plugin: DevRoomTrial): CommandExecutor {

    override fun onCommand(s: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(s is Player) {
            if(args.size == 1 && args[0].equals("reload", true)) {
                s.sendMessage("§eReloading plugin...")
                val errors = plugin.bootstrap.reload()
                if(errors.isNotEmpty()) {
                    s.sendMessage("§c§lOH NO! §cThere was several erors while reloading:")
                    errors.forEach {
                        s.sendMessage("§4* §f$it")
                    }
                    s.sendMessage("§7§oPlugin cannot be loaded")
                } else s.sendMessage("§aPlugin succssfully reloaded")
            } else {
                s.sendMessage("§eUsage:")
                s.sendMessage("§6* §f/rip §areload §7(Reloads plugin)")
            }
        } else s.sendMessage("§lHEY! This command can be used only as player!")
        return true
    }

}