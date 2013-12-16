package artillects.entity.ai;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import artillects.InventoryHelper;
import artillects.Vector3;
import artillects.entity.EntityFabricator;
import artillects.entity.IArtillect;
import artillects.hive.ArtillectTaskType;
import artillects.hive.Hive;
import artillects.hive.zone.ZoneProcessing;

public class EntityAIReproduce extends EntityAIBase
{
	private EntityFabricator entity;
	private World world;

	/** The speed the creature moves at during mining behavior. */
	private double moveSpeed;
	private int idleTime = 0;
	private final int maxIdleTime = 20 * 10;

	public EntityAIReproduce(EntityFabricator entity, double par2)
	{
		this.entity = entity;
		this.world = entity.worldObj;
		this.moveSpeed = par2;
	}

	@Override
	public void startExecuting()
	{

	}

	/** Returns whether the EntityAIBase should begin execution. */
	@Override
	public boolean shouldExecute()
	{
		return true;// this.entity.zone instanceof ZoneProcessing && !((ZoneProcessing)
					// entity.zone).chestPositions.isEmpty();
	}

	/** Returns whether an in-progress EntityAIBase should continue executing */
	@Override
	public boolean continueExecuting()
	{
		return this.shouldExecute();
	}

	/** Resets the task */
	@Override
	public void resetTask()
	{

	}

	/** Updates the task */
	@Override
	public void updateTask()
	{
		this.idleTime--;

		if (this.idleTime <= 0 && this.shouldExecute())
		{
			HashMap<ArtillectTaskType, Integer> artillectTypeCount = new HashMap<ArtillectTaskType, Integer>();

			for (IArtillect artillect : Hive.instance().getArtillects())
			{
				ArtillectTaskType type = artillect.getType();
				artillectTypeCount.put(artillect.getType(), (artillectTypeCount.containsKey(type) ? artillectTypeCount.get(type) : 0) + 1);
			}

			for (ArtillectTaskType type : ArtillectTaskType.values())
			{
				int amount = artillectTypeCount.containsKey(type) ? artillectTypeCount.get(type) : 0;

				if (amount < type.ratio)
				{
					this.tryProduce(type);
					return;
				}
			}

			this.tryProduce(ArtillectTaskType.FABRICATOR);
			this.idleTime = this.maxIdleTime;
		}
	}

	/**
	 * Attempts to produce the Artillect of such type.
	 * 
	 * @param type
	 * @return True if produced.
	 */
	private boolean tryProduce(ArtillectTaskType type)
	{
		if (this.hasResoucre(type))
		{
			try
			{
				Entity entity = type.entityClass.getConstructor(World.class).newInstance(this.world);
				entity.setPosition(this.entity.posX, this.entity.posY, this.entity.posZ);
				this.world.spawnEntityInWorld(entity);
				((IArtillect) entity).setType(type);
				return true;
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			this.lookForResource(type);
		}

		return false;
	}

	private void lookForResource(ArtillectTaskType type)
	{
		if (this.entity.zone instanceof ZoneProcessing)
		{
			ZoneProcessing zone = (ZoneProcessing) this.entity.zone;

			for (ItemStack stackRequired : type.getResourcesRequired())
			{
				for (Vector3 chestPosition : zone.chestPositions)
				{
					TileEntity tileEntity = this.world.getBlockTileEntity((int) chestPosition.x, (int) chestPosition.y, (int) chestPosition.z);

					if (tileEntity instanceof TileEntityChest)
					{
						TileEntityChest chest = (TileEntityChest) tileEntity;

						for (int i = 0; i < chest.getSizeInventory(); i++)
						{
							ItemStack stackInChest = chest.getStackInSlot(i);

						}
					}
				}
			}
		}
	}

	/**
	 * @param type
	 * @return If the Artillect has resource to fabricate this Artillect.
	 */
	private boolean hasResoucre(ArtillectTaskType type)
	{
		return InventoryHelper.hasItems(this.entity.getInventory(), type.getResourcesRequired().toArray(new ItemStack[0]));
	}
}