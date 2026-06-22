package net.apertyotis.createandesiteabound.mixin.design_decor;

import com.mangomilk.design_decor.blocks.large_boiler.aluminum.AluminumBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.andesite.AndesiteBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.brass.BrassBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.capitalism.CapitalismBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.cast_iron.CastIronBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.copper.CopperBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.gold.GoldBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.industrial_iron.IndustrialIronBoilerStructure;
import com.mangomilk.design_decor.blocks.large_boiler.zinc.ZincBoilerStructure;
import net.apertyotis.createandesiteabound.compat.design_decor.LargeBoilerStructure;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
    AluminumBoilerStructure.class,
    AndesiteBoilerStructure.class,
    BrassBoilerStructure.class,
    CapitalismBoilerStructure.class,
    CastIronBoilerStructure.class,
    CopperBoilerStructure.class,
    GoldBoilerStructure.class,
    IndustrialIronBoilerStructure.class,
    ZincBoilerStructure.class
}, remap = false)
public abstract class LargeBoilerStructureMixin implements LargeBoilerStructure {}
