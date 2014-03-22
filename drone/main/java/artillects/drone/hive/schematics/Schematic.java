package artillects.drone.hive.schematics;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import artillects.drone.Drone;
import artillects.drone.block.BlockSymbol;
import calclavia.lib.utility.nbt.ISaveObj;
import calclavia.lib.utility.nbt.NBTUtility;

/** File that represents all the data loaded from a schematic data file
 * 
 * @author DarkGuardsman */
public class Schematic implements ISaveObj
{
    //TODO save the schematics using block names, include a reference sheet to match block names to IDs instead of saving each block as a string
    
    public static final String BLOCK_LIST_SAVE_NAME = "BlockList";
    public static final String BLOCK_REF_SAVE_NAME = "BlockRef";
    public static final String BLOCK_MAP_SAVE_NAME = "BlockMap";

    private static final LinkedHashMap<String, Block> BLOCK_SAVE_MAP = new LinkedHashMap<String, Block>();
    private static final LinkedHashMap<Block, String> BLOCK_SAVE_MAP_REV = new LinkedHashMap<Block, String>();

    /** Manually saves a block by a set name, overrides vanilla block name saving */
    public static void registerSaveBlock(String name, Block block)
    {
        BLOCK_SAVE_MAP.put(name, block);
        BLOCK_SAVE_MAP_REV.put(block, name);
    }

    static
    {
        registerSaveBlock("wall1", Drone.blockHiveWalling);
        registerSaveBlock("wall2", Drone.blockHiveWalling);
        registerSaveBlock("symbol1", Drone.blockSymbol);
        registerSaveBlock("symbol2", Drone.blockSymbol);
        registerSaveBlock("symbol3", Drone.blockSymbol);
        registerSaveBlock("light", Drone.blockLight);
        registerSaveBlock("core", Drone.blockHiveCore);
        registerSaveBlock("teleporter", Drone.blockHiveTeleporterNode);
        registerSaveBlock("teleporterSymbol", Drone.blockGlyph);
    }

    protected String name;
    public Vector3 schematicSize;

    public Vector3 schematicCenter;

    public HashMap<Vector3, int[]> blocks = new HashMap<Vector3, int[]>();

    public boolean init = false;

    public String getFileName()
    {
        return this.name;
    }

    public void init()
    {
        if (this.schematicSize != null)
        {
            this.init = true;
            this.schematicCenter = new Vector3();
            this.schematicCenter.x = this.schematicSize.x / 2;
            // this.schematicCenter.y = this.schematicSize.y / 2;
            this.schematicCenter.z = this.schematicSize.z / 2;
        }
    }

    public void build(VectorWorld spot, boolean doWorldCheck)
    {
        if (this.blocks != null)
        {
            HashMap<Vector3, ItemStack> blocksToPlace = new HashMap<Vector3, ItemStack>();
            this.getBlocksToPlace(spot, blocksToPlace, doWorldCheck, doWorldCheck);
            for (Entry<Vector3, ItemStack> entry : blocksToPlace.entrySet())
            {
                entry.getKey().setBlock(spot.world, entry.getValue().itemID, entry.getValue().getItemDamage());
            }
        }
    }

    /** Gets all blocks needed to build the schematic at the given location
     * 
     * @param spot - center point of the schematic
     * @param blockMap - map of blocks from the schematic file
     * @param checkWorld - check if blocks are already placed
     * @param checkIfWorldIsLoaded - check if the area is loaded, make sure this is true if
     * checkWorld boolean is true as it effects the results if the chunk is unloaded. Setting this
     * true will not load the area and is designed to prevent wasting blocks when generating
     * buildings using actual blocks */
    public void getBlocksToPlace(VectorWorld spot, HashMap<Vector3, ItemStack> blockMap, boolean checkWorld, boolean checkIfWorldIsLoaded)
    {
        if (this.blocks != null)
        {
            for (Entry<Vector3, int[]> entry : this.blocks.entrySet())
            {
                int blockID = entry.getValue()[0];
                int meta = entry.getValue()[1];
                Block block = Block.blocksList[blockID];

                if (block == null || block != Block.sponge)
                {
                    if (blockID == Drone.blockSymbol.blockID)
                    {
                        meta = spot.world.rand.nextInt(BlockSymbol.SymbolType.values().length - 1);
                    }
                    if (meta > 15)
                    {
                        meta = 15;
                    }
                    else if (meta < 0)
                    {
                        meta = 0;
                    }
                    Vector3 setPos = spot.clone().subtract(this.schematicCenter).add(entry.getKey());
                    if (checkWorld)
                    {
                        if (checkIfWorldIsLoaded)
                        {
                            Chunk chunk = spot.world.getChunkFromBlockCoords(setPos.intX(), setPos.intZ());
                            if (!chunk.isChunkLoaded)
                            {
                                continue;
                            }
                        }
                        int checkID = setPos.getBlockID(spot.world);
                        int checkMeta = setPos.getBlockID(spot.world);
                        if (checkID == blockID && checkMeta == meta)
                        {
                            continue;
                        }
                    }

                    blockMap.put(setPos, new ItemStack(blockID, 1, meta));
                }
            }
        }
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        if (!init)
        {
            this.init();
        }
        NBTTagCompound blockNBT = nbt.getCompoundTag(BLOCK_LIST_SAVE_NAME);
        if (this.schematicSize != null)
        {
            nbt.setInteger("sizeX", (int) this.schematicSize.x);
            nbt.setInteger("sizeY", (int) this.schematicSize.y);
            nbt.setInteger("sizeZ", (int) this.schematicSize.z);
        }
        if (this.schematicCenter != null)
        {
            nbt.setInteger("centerX", (int) this.schematicCenter.x);
            nbt.setInteger("centerY", (int) this.schematicCenter.y);
            nbt.setInteger("centerZ", (int) this.schematicCenter.z);
        }
        int i = 0;

        for (Entry<Vector3, int[]> entry : blocks.entrySet())
        {
            String output = "";
            Block block = Block.blocksList[entry.getValue()[0]];
            if (block != null && Schematic.BLOCK_SAVE_MAP_REV.containsKey(block))
            {
                output += Schematic.BLOCK_SAVE_MAP_REV.get(block);
            }
            else
            {
                output += entry.getValue()[0];
            }
            output += ":" + entry.getValue()[1];
            output += ":" + ((int) entry.getKey().x) + ":" + ((int) entry.getKey().y) + ":" + ((int) entry.getKey().z);
            blockNBT.setString("Block" + i, output);
            i++;
        }
        blockNBT.setInteger("count", i);
        nbt.setCompoundTag(BLOCK_LIST_SAVE_NAME, blockNBT);

    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        schematicSize = new Vector3(nbt.getInteger("sizeX"), nbt.getInteger("sizeY"), nbt.getInteger("sizeZ"));
        schematicCenter = new Vector3(nbt.getInteger("centerX"), nbt.getInteger("centerY"), nbt.getInteger("centerZ"));
        NBTTagCompound blockDataSave = nbt.getCompoundTag(BLOCK_LIST_SAVE_NAME);

        for (int blockCount = 0; blockCount < blockDataSave.getInteger("count"); blockCount++)
        {
            String blockString = blockDataSave.getString("Block" + blockCount);
            String[] blockData = blockString.split(":");
            int blockID = 0;
            int blockMeta = 0;
            Vector3 blockPostion = new Vector3();
            if (blockData != null)
            {
                try
                {
                    if (blockData.length > 0)
                    {
                        if (Schematic.BLOCK_SAVE_MAP.containsKey(blockData[0]))
                        {
                            blockID = Schematic.BLOCK_SAVE_MAP.get(blockData[0]).blockID;
                        }
                        else
                        {
                            blockID = Integer.parseInt(blockData[0]);
                        }

                    }
                    if (blockData.length > 1)
                    {
                        blockMeta = Integer.parseInt(blockData[1]);
                    }
                    if (blockData.length > 2)
                    {
                        blockPostion.x = Integer.parseInt(blockData[2]);
                    }
                    if (blockData.length > 3)
                    {
                        blockPostion.y = Integer.parseInt(blockData[3]);
                    }
                    if (blockData.length > 4)
                    {
                        blockPostion.z = Integer.parseInt(blockData[4]);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                this.blocks.put(blockPostion, new int[] { blockID, blockMeta });
            }
        }

        if (!init)
        {
            this.init();
        }

    }

    public void getFromResourceFolder(String fileName)
    {
        try
        {
            this.load(CompressedStreamTools.readCompressed(Schematic.class.getResource("/assets/artillects/schematics/" + fileName + ".dat").openStream()));
            this.name = fileName;
            this.init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void saveToBaseDirectory(String fileName)
    {
        try
        {
            NBTTagCompound nbt = new NBTTagCompound();
            this.save(nbt);
            NBTUtility.saveData(new File(NBTUtility.getBaseDirectory(), "schematics"), fileName, nbt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Schematic loadWorldSelection(World world, Vector3 pos, Vector3 pos2)
    {
        int deltaX, deltaY, deltaZ;
        Vector3 start = new Vector3(pos.x > pos2.x ? pos2.x : pos.x, pos.y > pos2.y ? pos2.y : pos.y, pos.z > pos2.z ? pos2.z : pos.z);

        Schematic sch = new Schematic();
        if (pos.x < pos2.x)
        {
            deltaX = (int) (pos2.x - pos.x + 1);
        }
        else
        {
            deltaX = (int) (pos.x - pos2.x + 1);
        }
        if (pos.y < pos2.y)
        {
            deltaY = (int) (pos2.y - pos.y + 1);
        }
        else
        {
            deltaY = (int) (pos.y - pos2.y + 1);
        }
        if (pos.z < pos2.z)
        {
            deltaZ = (int) (pos2.z - pos.z + 1);
        }
        else
        {
            deltaZ = (int) (pos.z - pos2.z + 1);
        }
        sch.schematicSize = new Vector3(deltaX, deltaY, deltaZ);
        for (int x = 0; x < deltaX; ++x)
        {
            for (int y = 0; y < deltaY; ++y)
            {
                for (int z = 0; z < deltaZ; ++z)
                {
                    int blockID = world.getBlockId((int) start.x + x, (int) start.y + y, (int) start.z + z);
                    int blockMeta = world.getBlockMetadata((int) start.x + x, (int) start.y + y, (int) start.z + z);
                    sch.blocks.put(new Vector3(x, y, z), new int[] { blockID, blockMeta });
                }
            }
        }
        return sch;
    }
}