package cz.lukesmith.automaticsorter.datagen;

import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                createShaped(RecipeCategory.MISC, ModBlocks.PIPE_BLOCK, 2)
                        .pattern("111")
                        .pattern(" 1 ")
                        .pattern("111")
                        .input('1', Items.COPPER_INGOT)
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .offerTo(exporter);

                createShaped(RecipeCategory.MISC, ModBlocks.FILTER_BLOCK)
                        .pattern(" 1 ")
                        .pattern("121")
                        .pattern(" 3 ")
                        .input('1', Items.IRON_INGOT)
                        .input('2', Items.DROPPER)
                        .input('3', ModBlocks.PIPE_BLOCK)
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .offerTo(exporter);

                createShaped(RecipeCategory.MISC, ModBlocks.SORTER_CONTROLLER_BLOCK)
                        .pattern("121")
                        .pattern("131")
                        .pattern(" 4 ")
                        .input('1', Items.IRON_INGOT)
                        .input('2', Items.COMPARATOR)
                        .input('3', Items.HOPPER)
                        .input('4', ModBlocks.PIPE_BLOCK)
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Automatic Sorter Recipes";
    }
}
