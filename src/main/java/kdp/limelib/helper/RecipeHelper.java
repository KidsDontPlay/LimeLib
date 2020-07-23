package kdp.limelib.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.ModLoadingContext;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import kdp.limelib.LimeLib;

public class RecipeHelper {

    private static final Map<String, List<Pair<String, Map<String, Object>>>> recipes = new HashMap<>();
    private static final Map<String, List<Pair<String, Map<String, Object>>>> blockLoottables = new HashMap<>();
    private static final Map<String, List<Pair<String, Map<String, Object>>>> entityLoottables = new HashMap<>();
    private static final Map<String, List<Pair<String, Map<String, Object>>>> tags = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String modIDCurrentFolder;

    static {
        File folder = new File("").toPath().resolve("../src/main/java/kdp/").toFile();
        modIDCurrentFolder = folder.list()[0];
    }

    public static void generateFiles() {
        if (!LimeLib.DEV)
            return;
        try {
            generateData(recipes.get(modIDCurrentFolder), "recipes");
            generateData(blockLoottables.get(modIDCurrentFolder), "loot_tables/blocks");
            generateData(entityLoottables.get(modIDCurrentFolder), "loot_tables/entities");
            generateData(tags.get(modIDCurrentFolder), "tags");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateData(List<Pair<String, Map<String, Object>>> pairs, String folder) throws IOException {
        List<String> names = new ArrayList<>();
        for (Pair<String, Map<String, Object>> p : pairs) {
            String name = p.getLeft();
            int i = 1;
            while (names.contains(name)) {
                name = p.getLeft() + i++;
            }
            names.add(name);
        }
        File dir = new File("").toPath()
                .resolve("../src/main/resources/data/" + modIDCurrentFolder + "/" + folder + "/").toFile();
        if (!dir.exists())
            dir.mkdirs();
        for (int i = 0; i < names.size(); i++) {
            Files.write(new File(dir, names.get(i) + ".json").toPath(),
                    gson.toJson(pairs.get(i).getRight()).getBytes());
        }
    }

    private static Object serializeItem(Object o, boolean count) {
        Objects.requireNonNull(o);
        if (o instanceof String) {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("tag", new ResourceLocation((String) o).toString());
            return ret;
        }
        if (o instanceof ResourceLocation) {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("tag", o.toString());
            return ret;
        }
        if (o instanceof Item) {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("item", ((Item) o).getRegistryName().toString());
            return ret;
        }
        if (o instanceof Block) {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("item", ((Block) o).getRegistryName().toString());
            return ret;
        }
        if (o instanceof ItemStack) {
            ItemStack s = (ItemStack) o;
            Validate.isTrue(!s.isEmpty(), "ItemStack is empty.");
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put("item", s.getItem().getRegistryName().toString());
            if (count || s.getCount() > 1)
                ret.put("count", s.getCount());
            return ret;
        }
        if (o instanceof Collection) {
            return ((Collection<?>) o).stream().map(oo -> serializeItem(oo, count)).collect(Collectors.toList());
        }
        if (o instanceof Object[]) {
            return Arrays.stream((Object[]) o).map(oo -> serializeItem(oo, count)).collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Argument of type " + o.getClass().getName() + " is invalid.");

    }

    private static void validate(ItemStack stack) {
        Validate.isTrue(!stack.isEmpty(), "result must not be empty");
    }

    public static void addBlockLootTable(Block block) {
        if (!LimeLib.DEV)
            return;

        Map<String, Object> json = new LinkedHashMap<>();
    }

    public static void addTag(ResourceLocation tag, ResourceLocation... values) {
        /*tags.compute(id, (k, v) -> {
            Pair<String, Map<String, Object>> p = Pair.of(name, json);
            if (v == null) {
                return new ArrayList<>(Collections.singletonList(p));
            } else {
                v.add(p);
                return v;
            }
        });*/
    }

    public static void addCraftingRecipe(ItemStack result, @Nullable String group, boolean shaped, Object... input) {
        if (!LimeLib.DEV)
            return;
        validate(result);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", shaped ? "minecraft:crafting_shaped" : "minecraft:crafting_shapeless");
        if (!StringUtils.isNullOrEmpty(group))
            json.put("group", group);
        if (shaped) {
            List<String> pattern = new ArrayList<>();
            int i = 0;
            while (i < input.length && input[i] instanceof String) {
                pattern.add((String) input[i]);
                i++;
            }
            json.put("pattern", pattern);

            Map<String, Object> key = new LinkedHashMap<>();
            Character curKey = null;
            for (; i < input.length; i++) {
                Object o = input[i];
                if (o instanceof Character) {
                    if (curKey != null)
                        throw new IllegalArgumentException("Provided two char keys in a row");
                    curKey = (Character) o;
                } else {
                    if (curKey == null)
                        throw new IllegalArgumentException("Providing object without a char key");
                    key.put(Character.toString(curKey), serializeItem(o, false));
                    curKey = null;
                }
            }
            json.put("key", key);
        } else {
            json.put("ingredients",
                    Arrays.stream(input).map(o -> serializeItem(o, false)).collect(Collectors.toList()));
        }
        json.put("result", serializeItem(result, true));
        addRecipe(result.getItem().getRegistryName().getPath(), json);
    }

    public static void addSmeltingRecipe(ItemStack result, Object input, double exp, int time) {
        if (!LimeLib.DEV)
            return;
        validate(result);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("type", "smelting");
        json.put("ingredient", serializeItem(input, false));
        json.put("result", result.getItem().getRegistryName().toString());
        json.put("experience", exp);
        json.put("cookingtime", time);
        addRecipe(result.getItem().getRegistryName().getPath(), json);
    }

    public static void addRecipe(String name, Map<String, Object> json) {
        String id = ModLoadingContext.get().getActiveContainer().getNamespace();
        recipes.compute(id, (k, v) -> {
            Pair<String, Map<String, Object>> p = Pair.of(name, json);
            if (v == null) {
                return new ArrayList<>(Collections.singletonList(p));
            } else {
                v.add(p);
                return v;
            }
        });
    }

}
