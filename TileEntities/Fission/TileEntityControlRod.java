/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ReactorCraft.TileEntities.Fission;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.ReactorCraft.Auxiliary.ReactorCoreTE;
import Reika.ReactorCraft.Auxiliary.Temperatured;
import Reika.ReactorCraft.Base.TileEntityReactorBase;
import Reika.ReactorCraft.Entities.EntityNeutron;
import Reika.ReactorCraft.Registry.ReactorSounds;
import Reika.ReactorCraft.Registry.ReactorTiles;
import Reika.ReactorCraft.TileEntities.Fission.TileEntityWaterCell.LiquidStates;

public class TileEntityControlRod extends TileEntityReactorBase implements ReactorCoreTE, Temperatured {

	private boolean lowered = true;
	private Motions motion;

	private static final int MINOFFSET = -5;
	private static final int MAXOFFSET = 20;

	private int rodOffset = MINOFFSET;

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		this.moveRods();
		thermalTicker.update();
		if (thermalTicker.checkCap())
			this.updateTemperature(world, x, y, z);
	}

	private void moveRods() {
		if (motion != null) {
			rodOffset += motion.stepHeight;
		}
		if (rodOffset <= MINOFFSET || rodOffset >= MAXOFFSET) {
			motion = null;
			rodOffset = Math.max(MINOFFSET, rodOffset);
			rodOffset = Math.min(MAXOFFSET, rodOffset);
			lowered = rodOffset == MINOFFSET;
		}
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public int getIndex() {
		return ReactorTiles.CONTROL.ordinal();
	}

	public void toggle(boolean sound) {
		if (lowered) {
			motion = Motions.RAISING;
		}
		else {
			motion = Motions.LOWERING;
		}
		if (sound)
			ReactorSounds.CONTROL.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, 1, 1.3F);
	}

	public void setActive(boolean active, boolean sound) {
		motion = active ? Motions.LOWERING : Motions.RAISING;
		if (sound)
			ReactorSounds.CONTROL.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, 1, 1.3F);
	}

	public void drop(boolean sound) {
		if (sound)
			if (rodOffset > MINOFFSET && motion != Motions.SCRAM)
				ReactorSounds.SCRAM.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, 1, 1F);
		motion = Motions.SCRAM;
	}

	public boolean isActive() {
		return lowered && rodOffset == MINOFFSET;
	}

	@Override
	public boolean onNeutron(EntityNeutron e, World world, int x, int y, int z) {
		return this.isActive() ? ReikaRandomHelper.doWithChance(60) : false;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void setTemperature(int T) {
		temperature = T;
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT)
	{
		super.writeSyncTag(NBT);

		NBT.setBoolean("down", lowered);

		if (motion != null)
			NBT.setInteger("motion", motion.ordinal());

		NBT.setInteger("rodoffset", rodOffset);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT)
	{
		super.readSyncTag(NBT);

		lowered = NBT.getBoolean("down");

		if (NBT.hasKey("motion"))
			motion = Motions.values()[NBT.getInteger("motion")];

		rodOffset = NBT.getInteger("rodoffset");
	}

	@Override
	public int getMaxTemperature() {
		return 0;
	}

	private void onMeltdown(World world, int x, int y, int z) {

	}

	@Override
	public boolean canDumpHeatInto(LiquidStates liq) {
		return liq.isWater();
	}

	@Override
	public int getTextureState(ForgeDirection side) {
		return this.isActive() ? 1 : 0;
	}

	public int getRodPosition() {
		return rodOffset;
	}

	private static enum Motions {
		RAISING(1),
		LOWERING(-1),
		SCRAM(-7);

		public final int stepHeight;

		private Motions(int dh) {
			stepHeight = dh;
		}
	}

}
