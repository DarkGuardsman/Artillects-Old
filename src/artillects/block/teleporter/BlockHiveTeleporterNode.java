package artillects.block.teleporter;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import artillects.Artillects;
import artillects.block.BlockBase;
import artillects.block.teleporter.tile.TileHiveTNode;

public class BlockHiveTeleporterNode extends BlockBase
{
	public Icon iconTop, iconSide, iconBot;

	public BlockHiveTeleporterNode()
	{
		super("teleporterNode", Material.iron);
	}

	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float f1, float f2, float f3)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileHiveTNode)
		{
			System.out.println(((TileHiveTNode) tile).shapeCode);
		}

		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileHiveTNode();
	}

	public Icon getIcon(int side, int metadata)
	{
		if (side == 0)
		{
			return iconBot;
		}
		if (side == 1)
		{
			return iconTop;
		}

		return iconSide;
	}

	public void registerIcons(IconRegister ir)
	{
		this.iconTop = ir.registerIcon(Artillects.PREFIX + "teleporterNode_top");
		this.iconSide = ir.registerIcon(Artillects.PREFIX + "teleporterNode_side");
		this.iconBot = ir.registerIcon(Artillects.PREFIX + "decorWall1");
	}
}
