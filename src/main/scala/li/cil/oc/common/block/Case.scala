package li.cil.oc.common.block

import java.util

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import li.cil.oc.common.GuiType
import li.cil.oc.common.tileentity
import li.cil.oc.integration.util.BuildCraft
import li.cil.oc.util.Color
import li.cil.oc.util.Tooltip
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.EnumRarity
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

class Case(val tier: Int) extends RedstoneAware with traits.PowerAcceptor {
  private val iconsOn = new Array[IIcon](6)

  // ----------------------------------------------------------------------- //

  override protected def customTextures = Array(
    Some("CaseTop"),
    Some("CaseTop"),
    Some("CaseBack"),
    Some("CaseFront"),
    Some("CaseSide"),
    Some("CaseSide")
  )

  override def registerBlockIcons(iconRegister: IIconRegister) = {
    super.registerBlockIcons(iconRegister)
    System.arraycopy(icons, 0, iconsOn, 0, icons.length)
    iconsOn(ForgeDirection.NORTH.ordinal) = iconRegister.registerIcon(Settings.resourceDomain + ":CaseBackOn")
    iconsOn(ForgeDirection.WEST.ordinal) = iconRegister.registerIcon(Settings.resourceDomain + ":CaseSideOn")
    iconsOn(ForgeDirection.EAST.ordinal) = iconsOn(ForgeDirection.WEST.ordinal)
  }

  override def getIcon(world: IBlockAccess, x: Int, y: Int, z: Int, worldSide: ForgeDirection, localSide: ForgeDirection) = {
    if (world.getTileEntity(x, y, z) match {
      case computer: tileentity.Case => computer.isRunning
      case _ => false
    }) iconsOn(localSide.ordinal)
    else getIcon(localSide.ordinal(), 0)
  }

  @SideOnly(Side.CLIENT)
  override def getRenderColor(metadata: Int) = Color.byTier(tier)

  // ----------------------------------------------------------------------- //

  override def rarity = Array(EnumRarity.common, EnumRarity.uncommon, EnumRarity.rare, EnumRarity.epic).apply(tier)

  override protected def tooltipBody(metadata: Int, stack: ItemStack, player: EntityPlayer, tooltip: util.List[String], advanced: Boolean) {
    tooltip.addAll(Tooltip.get(getClass.getSimpleName, slots))
  }

  private def slots = tier match {
    case 0 => "2/1/1"
    case 1 => "2/2/2"
    case 2 | 3 => "3/2/3"
    case _ => "0/0/0"
  }

  // ----------------------------------------------------------------------- //

  override def energyThroughput = Settings.get.caseRate(tier)

  override def createTileEntity(world: World, metadata: Int) = new tileentity.Case(tier)

  // ----------------------------------------------------------------------- //

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer,
                                side: ForgeDirection, hitX: Float, hitY: Float, hitZ: Float) = {
    if (!player.isSneaking && !BuildCraft.holdsApplicableWrench(player, x, y, z)) {
      if (!world.isRemote) {
        player.openGui(OpenComputers, GuiType.Case.id, world, x, y, z)
      }
      true
    }
    else if (player.getCurrentEquippedItem == null) {
      if (!world.isRemote) {
        world.getTileEntity(x, y, z) match {
          case computer: tileentity.Case if !computer.machine.isRunning => computer.machine.start()
          case _ =>
        }
      }
      true
    }
    else false
  }

  override def removedByPlayer(world: World, player: EntityPlayer, x: Int, y: Int, z: Int, willHarvest: Boolean) =
    world.getTileEntity(x, y, z) match {
      case c: tileentity.Case => c.canInteract(player.getCommandSenderName) && super.removedByPlayer(world, player, x, y, z, willHarvest)
      case _ => super.removedByPlayer(world, player, x, y, z, willHarvest)
    }
}
