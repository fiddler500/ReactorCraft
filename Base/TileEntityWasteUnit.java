/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ReactorCraft.Base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.ChromatiCraft.API.Accelerator;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.Isotopes;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaNuclearHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaTimeHelper;
import Reika.ReactorCraft.Auxiliary.WasteManager;
import Reika.ReactorCraft.Entities.EntityNeutron;
import Reika.ReactorCraft.Entities.EntityNeutron.NeutronType;
import Reika.ReactorCraft.Registry.ReactorItems;

public abstract class TileEntityWasteUnit extends TileEntityInventoriedReactorBase {

	protected void fill() {
		for (int i = 0; i < this.getSizeInventory(); i++) {
			if (this.getStackInSlot(i) == null) {
				ItemStack is = WasteManager.getFullyRandomWasteItem();
				this.setInventorySlotContents(i, is);
			}
		}
	}

	public abstract boolean leaksRadiation();

	public abstract boolean isValidIsotope(Isotopes i);

	protected final int getAccelerationFactor(World world, int x, int y, int z) {
		int mult = 1;
		for (int i = 0; i < 6; i++) {
			TileEntity te = this.getAdjacentTileEntity(dirs[i]);
			if (te instanceof Accelerator) {
				mult *= ReikaMathLibrary.logbase2(((Accelerator)te).getAccelerationFactor());
			}
		}
		return mult;
	}

	protected final void decayWaste() {
		this.decayWaste(1);
	}

	protected final void decayWaste(int mult) {
		for (int i = 0; i < this.getSizeInventory(); i++) {
			if (inv[i] != null && inv[i].getItem() == ReactorItems.WASTE.getItemInstance()) {
				List<Isotopes> iso = WasteManager.getWasteList();
				Isotopes atom = iso.get(inv[i].getItemDamage());
				if (ReikaRandomHelper.doWithChance(mult*0.5*ReikaNuclearHelper.getDecayChanceFromHalflife(Math.log(atom.getMCHalfLife())))) {
					//ReikaJavaLibrary.pConsole("Radiating from "+atom);
					if (this.leaksRadiation() && rand.nextBoolean())
						this.leakRadiation(worldObj, xCoord, yCoord, zCoord);
				}
				//ReikaJavaLibrary.pConsole(ReikaNuclearHelper.getDecayChanceFromHalflife(atom.getMCHalfLife()));
				if (ReikaNuclearHelper.shouldDecay(atom)) {
					ReikaInventoryHelper.decrStack(i, inv);
					this.onDecayWaste(i);
				}
			}
		}
	}

	protected void onDecayWaste(int i) {

	}

	protected void leakRadiation(World world, int x, int y, int z) {
		ForgeDirection dir = dirs[rand.nextInt(dirs.length)];
		if (!world.isRemote)
			world.spawnEntityInWorld(new EntityNeutron(world, x, y, z, dir, NeutronType.WASTE));
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return itemstack.getItem() == ReactorItems.WASTE.getItemInstance();
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public final boolean canEnterFromSide(ForgeDirection dir) {
		return true;
	}

	@Override
	public final boolean canExitToSide(ForgeDirection dir) {
		return true;
	}

	public final int countWaste() {
		int count = 0;
		for (int i = 0; i < this.getSizeInventory(); i++) {
			if (inv[i] != null) {
				if (inv[i].getItem() == ReactorItems.WASTE.getItemInstance()) {
					count += inv[i].stackSize;
				}
			}
		}
		return count;
	}

	public final boolean hasWaste() {
		return this.countWaste() > 0;
	}

	public final double getHalfLife(ItemStack is) {
		if (is.getItem() != ReactorItems.WASTE.getItemInstance())
			return 0;
		return WasteManager.getWasteList().get(is.getItemDamage()).getMCHalfLife();
	}

	protected final boolean isLongLivedWaste(ItemStack is) {
		return is.getItem() == ReactorItems.WASTE.getItemInstance() && this.getHalfLife(is) > 6*ReikaTimeHelper.YEAR.getMinecraftDuration();
	}

	protected final boolean isLongLivedWaste(Isotopes i) {
		return i.getMCHalfLife() > 6*ReikaTimeHelper.YEAR.getMinecraftDuration();
	}
}
