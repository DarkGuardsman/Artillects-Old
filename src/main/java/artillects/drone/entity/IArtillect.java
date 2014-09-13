package artillects.drone.entity;

import net.minecraft.inventory.IInventory;
import artillects.core.interfaces.IEntity;
import artillects.drone.hive.zone.Zone;

/** Applied to any Entity that is considered to be a drone for the hive
 * 
 * @author Darkguardsman */
public interface IArtillect extends IEntity
{
    /** Sets the drone's owner */
    public void setOwner(Object hive);

    /** Gets the drone's owner */
    public Object getOwner();

    /** Sets the working area of the drone */
    public void setZone(Zone zone);

    /** Gets the drone's working area */
    public Zone getZone();

    /** Gets the drone's working inventory */
    public IInventory getInventory();
}
