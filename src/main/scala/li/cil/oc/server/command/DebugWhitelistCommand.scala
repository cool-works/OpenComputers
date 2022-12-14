package li.cil.oc.server.command

import li.cil.oc.Settings
import li.cil.oc.Settings.DebugCardAccess
import li.cil.oc.common.command.SimpleCommand
import net.minecraft.command.{ICommandSender, WrongUsageException}

object DebugWhitelistCommand extends SimpleCommand("oc_debugWhitelist") {
  // Required OP levels:
  //  to revoke your cards - 0
  //  to do other whitelist manipulation - 2

  override def getRequiredPermissionLevel = 0
  def isOp(sender: ICommandSender) = getOpLevel(sender) >= 2

  override def getCommandUsage(sender: ICommandSender): String =
    if (isOp(sender)) name + " [revoke|add|remove] <player> OR " + name + " [revoke|list]"
    else name + " revoke"

  override def processCommand(sender: ICommandSender, args: Array[String]): Unit = {
    val wl = Settings.get.debugCardAccess match {
      case w: DebugCardAccess.Whitelist => w
      case _ => throw new WrongUsageException("§cDebug card whitelisting is not enabled.")
    }

    def revokeUser(player: String): Unit = {
      if (wl.isWhitelisted(player)) {
        wl.invalidate(player)
        sender.addChatMessage("§aAll your debug cards were invalidated.")
      } else sender.addChatMessage("§cYou are not whitelisted to use debug card.")
    }

    args match {
      case Array("revoke") => revokeUser(sender.getCommandSenderName)
      case Array("revoke", player) if isOp(sender) => revokeUser(player)
      case Array("list") if isOp(sender) =>
        val players = wl.whitelist
        if (players.nonEmpty)
          sender.addChatMessage("§aCurrently whitelisted players: §e" + players.mkString(", "))
        else
          sender.addChatMessage("§cThere is no currently whitelisted players.")
      case Array("add", player) if isOp(sender) =>
        wl.add(player)
        sender.addChatMessage("§aPlayer was added to whitelist.")
      case Array("remove", player) if isOp(sender) =>
        wl.remove(player)
        sender.addChatMessage("§aPlayer was removed from whitelist")
      case _ =>
        sender.addChatMessage("§e" + getCommandUsage(sender))
    }
  }
}
