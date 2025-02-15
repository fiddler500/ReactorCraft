/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ReactorCraft.Entities;

import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import Reika.DragonAPI.Base.InertEntity;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.ReactorCraft.Auxiliary.RadiationEffects;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRadiation extends InertEntity implements IEntityAdditionalSpawnData {

	private int effectRange;

	public EntityRadiation(World par1World) {
		super(par1World);
	}

	public EntityRadiation(World world, int range) {
		super(world);
		effectRange = range;
	}

	@Override
	protected void entityInit() {

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound NBT) {
		effectRange = NBT.getInteger("effrange");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound NBT) {
		NBT.setInteger("effrange", effectRange);
	}

	@Override
	public void onUpdate()
	{
		this.onEntityUpdate();
		this.applyRadiation();

		if (effectRange <= 0)
			this.setDead();

		if (rand.nextInt(360000) == 0) {
			this.clean();
		}
		if (worldObj.isRaining()) {
			if (rand.nextInt(36000) == 0) {
				if (worldObj.getBiomeGenForCoords(this.getBlockX(), this.getBlockZ()).canSpawnLightningBolt()) {
					this.clean();
				}
			}
		}
	}

	public final int getBlockX() {
		return (int)Math.floor(posX);
	}

	public final int getBlockY() {
		return (int)Math.floor(posY);
	}

	public final int getBlockZ() {
		return (int)Math.floor(posZ);
	}

	private void applyRadiation() {
		World world = worldObj;
		double x = posX;
		double y = posY;
		double z = posZ;
		Random r = new Random();
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x, y, z).expand(effectRange, effectRange, effectRange);
		List<EntityLivingBase> inbox = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (EntityLivingBase e : inbox) {
			double dd = ReikaMathLibrary.py3d(e.posX-x, e.posY-y, e.posZ-z);
			if (dd <= effectRange) {
				RadiationEffects.instance.applyEffects(e);
			}
		}

		int c = 1;//20
		if (r.nextInt(c) == 0) {
			int dx = (int)x-effectRange+r.nextInt(effectRange*2+1);
			int dy = (int)y-effectRange+r.nextInt(effectRange*2+1);
			int dz = (int)z-effectRange+r.nextInt(effectRange*2+1);
			RadiationEffects.instance.transformBlock(world, dx, dy, dz);
		}
	}

	public void clean() {
		if (effectRange > 0)
			effectRange--;
		else
			this.setDead();
	}

	public int getRange() {
		return effectRange;
	}

	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(effectRange);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		effectRange = data.readInt();
	}

	@Override
	public boolean attackEntityFrom(DamageSource src, float par2)
	{
		if (src.isExplosion()) {
			RadiationEffects.instance.contaminateArea(worldObj, this.getBlockX(), this.getBlockY(), this.getBlockZ(), effectRange, 0.65F);
			this.setDead();
			return true;
		}
		return super.attackEntityFrom(src, par2);
	}

}
