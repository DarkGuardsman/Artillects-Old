package artillects.content.tool.surveyor;

import artillects.content.tool.TilePlaceableTool;
import artillects.core.Artillects;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import resonant.api.IRemovable.ISneakPickup;
import resonant.engine.ResonantEngine;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketIDReceiver;
import resonant.lib.transform.vector.Vector3;

import java.awt.*;

/** Small camera looking block that can deploy laser lines, gauge distances, and do other utilities.
 * 
 * @author Darkguardsman */
public class TileSurveyor extends TilePlaceableTool implements IPacketIDReceiver, ISneakPickup
{
    protected Color beamColor = Color.cyan;

    public TileSurveyor()
    {
        super(Material.anvil);
        rayDistance = 1000;
        doRayTrace = true;
        isOpaqueCube(false);
        normalRender(false);
        customItemRender(true);
        itemBlock(ItemSurveyor.class);
        //textureName("material_steel");
    }

    @Override
    public void update()
    {
        super.update();
        if (this.ticks() % 3 == 0)
        {
            //TODO render distance above tile
            if (lastRayHit != null && world().isRemote && enabled)
                Artillects.proxy.renderLaser(world(), loc, lastRayHit, beamColor, 3);
        }
    }

    @Override
    public boolean use(EntityPlayer player, int side, Vector3 hit)
    {
        if (!player.isSneaking())
        {
            player.openGui(Artillects.INSTANCE, 0, world(), xi(), yi(), zi());
            return true;
        }
        return super.use(player, side, hit);
    }

    public double distance()
    {
        if (lastRayHit != null)
        {
            return lastRayHit.distance(new Vector3(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5));
        }
        return -1;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("beamColor"))
        {
            NBTTagCompound tag = nbt.getCompoundTag("beamColor");
            this.beamColor = new Color(tag.getInteger("red"), tag.getInteger("green"), tag.getInteger("blue"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        NBTTagCompound colorBeamTag = new NBTTagCompound();
        colorBeamTag.setInteger("red", beamColor.getRed());
        colorBeamTag.setInteger("blue", beamColor.getBlue());
        colorBeamTag.setInteger("green", beamColor.getGreen());
        nbt.setTag("beamColor", colorBeamTag);
    }

    @Override
    public boolean read(ByteBuf data, int id,  EntityPlayer player, PacketType type)
    {
        if (id == DESC_ID)
        {
            this.angle.yaw_$eq(data.readDouble());
            this.angle.pitch_$eq(data.readDouble());
            this.enabled = data.readBoolean();
            this.beamColor = new Color(data.readInt(), data.readInt(), data.readInt());
            return true;
        }
        else if (super.read(data, id, player, type))
        {
            return true;
        }
        else if (id == 3)
        {
            this.beamColor = new Color(data.readInt(), data.readInt(), data.readInt());
            return true;
        }
        return false;
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, DESC_ID, this.angle.yaw(), this.angle.pitch(), this.enabled, this.beamColor.getRed(), this.beamColor.getGreen(), this.beamColor.getBlue());
    }

    public void setColor(Color color)
    {
        this.beamColor = color;
        PacketTile packet = new PacketTile(this, 3, this.beamColor.getRed(), this.beamColor.getGreen(), this.beamColor.getBlue());
        if (world().isRemote)
            ResonantEngine.instance.packetHandler.sendToServer(packet);
    }

    public void setColor(String color)
    {
        try
        {
            setColor(hex2Rgb(color));
        }
        catch (Exception e)
        {

        }
    }

    public static Color hex2Rgb(String colorStr)
    {
        return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    public static String rgb2hex(Color color)
    {
        String hex = Integer.toHexString(color.getRGB() & 0xffffff);
        if (hex.length() < 6)
        {
            hex = "0" + hex;
        }
        return hex;
    }
}
