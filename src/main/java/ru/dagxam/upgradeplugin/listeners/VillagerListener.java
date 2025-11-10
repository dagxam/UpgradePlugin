// ИЗМЕНЕНО
package ru.dagxam.upgradeplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
// ИЗМЕНЕНО
import ru.dagxam.upgradeplugin.UpgradePlugin;
import ru.dagxam.upgradeplugin.items.ItemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillagerListener implements Listener {
    
    private final UpgradePlugin plugin;
    private final Random random = new Random();

    public VillagerListener(UpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVillagerTrade(VillagerAcquireTradeEvent event) {
        // Проверяем, что это Библиотекарь
        if (event.getEntity().getProfession() == Villager.Profession.LIBRARIAN) {
            
            // Даем 50% шанс на добавление сделки (чтобы не было у каждого)
            if (random.nextInt(100) < 50) { 
                ItemStack book = ItemManager.createUpgradeBook();
                // 20 изумрудов -> 1 Книга. Макс. сделок = 3. Опыт за сделку = 15.
                MerchantRecipe recipe = new MerchantRecipe(book, 0, 3, true, 15, 0.05f);
                
                recipe.addIngredient(new ItemStack(Material.EMERALD, 20));

                // Получаем текущие рецепты и добавляем наш
                List<MerchantRecipe> recipes = new ArrayList<>(event.getEntity().getRecipes());
                recipes.add(recipe);
                
                // Устанавливаем обновленный список
                // (Примечание: в новых версиях Spigot может потребоваться event.setRecipe)
                // Этот способ самый надежный:
                event.getEntity().setRecipes(recipes);
            }
        }
    }
}
