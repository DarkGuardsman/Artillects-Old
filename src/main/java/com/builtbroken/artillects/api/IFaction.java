package com.builtbroken.artillects.api;


import com.builtbroken.mc.api.IVirtualObject;
import com.builtbroken.mc.framework.access.api.IProfileContainer;

/**
 * Interface applied to objects that will become a faction
 *
 * @author Darkgardsman
 */
public interface IFaction extends IVirtualObject, IProfileContainer
{
    /** Checks if the object is a memember of the faction */
    boolean isMember(Object obj);

    /**
     * Gets the faction id
     *
     * @return string value of faction id
     */
    String getID();
}
