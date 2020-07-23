package kdp.limelib.helper.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import kdp.limelib.util.GlobalBlockPos;
import kdp.limelib.util.StackWrapper;

@SuppressWarnings("unchecked")
public class NBTHelper {

    public static final String NULL_INDICES = "_n0lL";
    public static final String NULL_AMOUNT = "_n0lL+";
    public static final String TYPE = "_tYpE";
    public static final String COLLECTION = "_lIsT";
    public static final String KEYS = "_kEyS";
    public static final String VALUES = "_vAlUeS";
    private static Set<INBTConverter<?>> converters = new ReferenceOpenHashSet<>();
    private static Map<Class<?>, INBTConverter<?>> converterCache = new IdentityHashMap<>();
    private static boolean initd = false;

    static {
        init();
    }

    public static boolean hasTag(CompoundNBT nbt, String key) {
        return nbt != null && nbt.contains(key);
    }

    public static CompoundNBT removeTag(CompoundNBT nbt, String key) {
        if (nbt == null)
            return nbt;
        nbt.remove(key);
        return nbt;
    }

    public static <T extends INBT> Optional<T> getTagOptional(CompoundNBT nbt, String key) {
        return hasTag(nbt, key) ? (Optional<T>) Optional.of(nbt.get(key)) : Optional.empty();
    }

    public static <T extends INBT> Optional<T> getTagOptional(CompoundNBT nbt, String key, Class<T> clazz) {
        if (!hasTag(nbt, key))
            return Optional.empty();
        INBT n = nbt.get(key);
        if (clazz.isAssignableFrom(n.getClass()))
            return (Optional<T>) Optional.of(n);
        else
            return Optional.empty();
    }

    public static String toASCIIString(CompoundNBT nbt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            CompressedStreamTools.write(nbt, dos);
        } catch (IOException e) {
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static CompoundNBT fromASCIIString(String s) {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(s));
        DataInputStream dis = new DataInputStream(bais);
        try {
            return CompressedStreamTools.read(dis);
        } catch (IOException e) {
            return null;
        }
    }

    public static int getSize(INBT nbt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            nbt.write(dos);
            dos.close();
        } catch (IOException e) {
        }
        return baos.size();
    }

    public static void init() {
        if (initd) {
            return;
        }
        initd = true;
        // Primitives + wrappers
        register(new INBTConverter<Object>() {
            private Object2ByteOpenHashMap<Object> class2id = new Object2ByteOpenHashMap<>();

            {
                byte id = 1;
                class2id.put(boolean.class, id++);
                class2id.put(Boolean.class, id++);
                class2id.put(byte.class, id++);
                class2id.put(Byte.class, id++);
                class2id.put(char.class, id++);
                class2id.put(Character.class, id++);
                class2id.put(short.class, id++);
                class2id.put(Short.class, id++);
                class2id.put(int.class, id++);
                class2id.put(Integer.class, id++);
                class2id.put(float.class, id++);
                class2id.put(Float.class, id++);
                class2id.put(long.class, id++);
                class2id.put(Long.class, id++);
                class2id.put(double.class, id++);
                class2id.put(Double.class, id++);
            }

            @Override
            public boolean classValid(Class<?> clazz) {
                return class2id.containsKey(clazz);
            }

            @Override
            public INBT toNBT(Object value) {
                switch (class2id.getByte(value.getClass())) {
                case 1:
                case 2:
                    return new ByteNBT((byte) (((boolean) value) ? 1 : 0));
                case 3:
                case 4:
                    return new ByteNBT((byte) value);
                case 5:
                case 6:
                    return new ShortNBT((short) ((((char) value)) + Short.MIN_VALUE));
                case 7:
                case 8:
                    return new ShortNBT((short) value);
                case 9:
                case 10:
                    return new IntNBT((int) value);
                case 11:
                case 12:
                    return new FloatNBT((float) value);
                case 13:
                case 14:
                    return new LongNBT((long) value);
                case 15:
                case 16:
                    return new DoubleNBT((double) value);
                default:
                    throw new RuntimeException("primitives2");
                }
            }

            @Override
            public Object toValue(INBT nbt, Class<?> clazz) {
                switch (class2id.getByte(clazz)) {
                case 1:
                case 2:
                    return ((ByteNBT) nbt).getByte() != 0;
                case 3:
                case 4:
                    return ((ByteNBT) nbt).getByte();
                case 5:
                case 6:
                    return (char) ((((ShortNBT) nbt).getShort()) - Short.MIN_VALUE);
                case 7:
                case 8:
                    return ((ShortNBT) nbt).getShort();
                case 9:
                case 10:
                    return ((IntNBT) nbt).getInt();
                case 11:
                case 12:
                    return ((FloatNBT) nbt).getFloat();
                case 13:
                case 14:
                    return ((LongNBT) nbt).getLong();
                case 15:
                case 16:
                    return ((DoubleNBT) nbt).getDouble();
                default:
                    throw new RuntimeException("primitives2");
                }
            }

            @Override
            public Supplier<Object> defaultValue(Class<?> clazz) {
                switch (class2id.getByte(clazz)) {
                case 1:
                case 2:
                    return () -> false;
                case 3:
                case 4:
                    return () -> (byte) 0;
                case 5:
                case 6:
                    return () -> (char) 0;
                case 7:
                case 8:
                    return () -> (short) 0;
                case 9:
                case 10:
                    return () -> 0;
                case 11:
                case 12:
                    return () -> 0F;
                case 13:
                case 14:
                    return () -> 0L;
                case 15:
                case 16:
                    return () -> 0D;
                default:
                    throw new RuntimeException("primitives2");
                }
            }

        });
        // Primitive arrays
        register(new INBTConverter<Object>() {
            private Object2ByteOpenHashMap<Object> class2id = new Object2ByteOpenHashMap<>();

            {
                byte id = 1;
                class2id.put(boolean[].class, id++);
                class2id.put(byte[].class, id++);
                class2id.put(char[].class, id++);
                class2id.put(short[].class, id++);
                class2id.put(int[].class, id++);
                class2id.put(float[].class, id++);
                class2id.put(long[].class, id++);
                class2id.put(double[].class, id++);
            }

            @Override
            public boolean classValid(Class<?> clazz) {
                return class2id.containsKey(clazz);
            }

            @Override
            public INBT toNBT(Object value) {
                switch (class2id.getByte(value.getClass())) {
                case 1:
                    boolean[] arrB = (boolean[]) value;
                    BitSet bs = new BitSet(arrB.length);
                    for (int i = 0; i < arrB.length; i++)
                        bs.set(i, arrB[i]);
                    CompoundNBT nbt = new CompoundNBT();
                    nbt.putByteArray("v", bs.toByteArray());
                    if (arrB.length % 8 != 0)
                        if (arrB.length <= Byte.MAX_VALUE) {
                            nbt.putByte("s", (byte) arrB.length);
                        } else if (arrB.length <= Short.MAX_VALUE) {
                            nbt.putShort("s", (short) arrB.length);
                        } else {
                            nbt.putInt("s", arrB.length);
                        }
                    return nbt;
                case 2:
                    return new ByteArrayNBT((byte[]) value);
                case 3:
                    return new StringNBT(new String((char[]) value));
                case 4:
                    short[] arrS = (short[]) value;
                    ByteBuffer bb = ByteBuffer.allocate(arrS.length * 2);
                    for (short s : arrS)
                        bb.putShort(s);
                    CompoundNBT nbtt = new CompoundNBT();
                    nbtt.putByteArray("v", bb.array());
                    if (arrS.length % 8 != 0)
                        if (arrS.length <= Byte.MAX_VALUE) {
                            nbtt.putByte("s", (byte) arrS.length);
                        } else if (arrS.length <= Short.MAX_VALUE) {
                            nbtt.putShort("s", (short) arrS.length);
                        } else {
                            nbtt.putInt("s", arrS.length);
                        }
                    return nbtt;
                case 5:
                    return new IntArrayNBT((int[]) value);
                case 6:
                    float[] arrF = (float[]) value;
                    int[] retF = new int[arrF.length];
                    for (int i = 0; i < arrF.length; i++)
                        retF[i] = Float.floatToRawIntBits(arrF[i]);
                    return new IntArrayNBT(retF);
                case 7:
                    return new LongArrayNBT((long[]) value);
                case 8:
                    double[] arrD = (double[]) value;
                    long[] retD = new long[arrD.length];
                    for (int i = 0; i < arrD.length; i++)
                        retD[i] = Double.doubleToRawLongBits(arrD[i]);
                    return new LongArrayNBT(retD);
                default:
                    throw new RuntimeException("arrays2");
                }
            }

            @Override
            public Object toValue(INBT nbt, Class<?> clazz) {
                switch (class2id.getByte(clazz)) {
                case 1:
                    CompoundNBT tag = (CompoundNBT) nbt;
                    byte[] arr = tag.getByteArray("v");
                    int num = tag.contains("s") ? tag.getInt("s") : arr.length << 3;
                    BitSet bs = BitSet.valueOf(arr);
                    boolean[] ret = new boolean[num];
                    for (int i = 0; i < num; i++) {
                        ret[i] = bs.get(i);
                    }
                    return ret;
                case 2:
                    return ((ByteArrayNBT) nbt).getByteArray();
                case 3:
                    return ((StringNBT) nbt).getString().toCharArray();
                case 4:
                    CompoundNBT tag2 = (CompoundNBT) nbt;
                    byte[] arrS = tag2.getByteArray("v");
                    int num2 = tag2.contains("s") ? tag2.getInt("s") : arrS.length >> 1;
                    ByteBuffer bb = ByteBuffer.wrap(arrS);
                    short[] ret2 = new short[num2];
                    for (int i = 0; i < num2; i++) {
                        ret2[i] = bb.getShort();
                    }
                    return ret2;
                case 5:
                    return ((IntArrayNBT) nbt).getIntArray();
                case 6:
                    int[] arrF = ((IntArrayNBT) nbt).getIntArray();
                    float[] retF = new float[arrF.length];
                    for (int i = 0; i < arrF.length; i++)
                        retF[i] = Float.intBitsToFloat(arrF[i]);
                    return retF;
                case 7:
                    return ((LongArrayNBT) nbt).getAsLongArray();
                case 8:
                    long[] arrD = ((LongArrayNBT) nbt).getAsLongArray();
                    double[] retD = new double[arrD.length];
                    for (int i = 0; i < arrD.length; i++)
                        retD[i] = Double.longBitsToDouble(arrD[i]);
                    return retD;
                default:
                    throw new RuntimeException("arrays2");
                }
            }

            @Override
            public Supplier<Object> defaultValue(Class<?> clazz) {
                switch (class2id.getByte(clazz)) {
                case 1:
                    return () -> new boolean[0];
                case 2:
                    return () -> new byte[0];
                case 3:
                    return () -> new char[0];
                case 4:
                    return () -> new short[0];
                case 5:
                    return () -> new int[0];
                case 6:
                    return () -> new float[0];
                case 7:
                    return () -> new long[0];
                case 8:
                    return () -> new double[0];
                default:
                    throw new RuntimeException("arrays2");
                }
            }
        });
        // Enum
        register(of(null, (n, c) -> {
            Object[] enums = c.getEnumConstants();
            int index = ((ShortNBT) n).getShort();
            if (index < 0 || index >= enums.length) {
                return null;
            }
            return enums[index];
        }, v -> new ShortNBT((short) ((Enum<?>) v).ordinal()), Class::isEnum));
        // IForgeRegistryEntry
        register(of(null, (n, c) -> {
            CompoundNBT entry = (CompoundNBT) n;
            String id = entry.getString("id"), clas = entry.getString("class");
            try {
                @SuppressWarnings("rawtypes")
                IForgeRegistry<?> reg = RegistryManager.ACTIVE
                        .getRegistry((Class<IForgeRegistryEntry>) Class.forName(clas));
                if (reg != null) {
                    return reg.getValue(new ResourceLocation(id));
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
            return null;
        }, v -> {
            CompoundNBT entry = new CompoundNBT();
            entry.putString("id", v.getRegistryName().toString());
            entry.putString("class", v.getRegistryType().getCanonicalName());
            return entry;
        }, IForgeRegistryEntry.class::isAssignableFrom));
        // INBT
        register(of(null, (n, c) -> n, v -> v, INBT.class::isAssignableFrom));
        // BlockPos
        register(of(null, (n, c) -> BlockPos.fromLong(((LongNBT) n).getLong()), v -> new LongNBT(v.toLong()),
                BlockPos.class, MutableBlockPos.class));
        // GlobalBlockPos
        register(of(null, (n, c) -> {
            long[] arr = ((LongArrayNBT) n).getAsLongArray();
            return new GlobalBlockPos(BlockPos.fromLong(arr[0]), (int) arr[1]);
        }, v -> new LongArrayNBT(new long[] { v.getPos().toLong(), v.getDimension() }), GlobalBlockPos.class));
        // ItemStack
        register(of(c -> ItemStack.EMPTY, (n, c) -> ItemStack.read((CompoundNBT) n), v -> v.write(new CompoundNBT()),
                ItemStack.class));
        // UUID
        register(of(null, (n, c) -> {
            long[] l = ((LongArrayNBT) n).getAsLongArray();
            return new UUID(l[0], l[1]);
        }, v -> new LongArrayNBT(new long[] { v.getMostSignificantBits(), v.getLeastSignificantBits()
        }), UUID.class));
        // ResourceLocation
        register(of(null, (n, c) -> new ResourceLocation(((StringNBT) n).getString()), v -> new StringNBT(v.toString()),
                ResourceLocation.class));
        // FluidStack
        register(of(null, (n, c) -> FluidStack.loadFluidStackFromNBT((CompoundNBT) n),
                v -> v.writeToNBT(new CompoundNBT()), FluidStack.class));
        // String
        register(of(c -> "", (n, c) -> ((StringNBT) n).getString(), StringNBT::new, String.class));
        // StackWrapper
        register(of(null, (n, c) -> StackWrapper.loadStackWrapperFromNBT((CompoundNBT) n),
                v -> v.writeToNBT(new CompoundNBT()), StackWrapper.class));
    }

    public static <T> INBTConverter<T> of(@Nullable Function<Class<? extends T>, T> defaultValue,
            BiFunction<INBT, Class<?>, T> getter, Function<T, INBT> setter, Class<?>... classes) {
        return of(defaultValue, getter, setter, clazz -> ArrayUtils.contains(classes, clazz));
    }

    public static <T> INBTConverter<T> of(@Nullable Function<Class<? extends T>, T> defaultValue,
            BiFunction<INBT, Class<?>, T> getter, Function<T, INBT> setter, Predicate<Class<?>> pred) {
        return new INBTConverter<T>() {

            @Override
            public boolean classValid(Class<?> clazz) {
                return pred.test(clazz);
            }

            @Override
            public Supplier<T> defaultValue(Class<? extends T> clazz) {
                return () -> defaultValue != null ? defaultValue.apply(clazz) : null;
            }

            @Override
            public INBT toNBT(T value) {
                return setter.apply(value);
            }

            @Override
            public T toValue(INBT nbt, Class<? extends T> clazz) {
                try {
                    return getter.apply(nbt, clazz);
                } catch (ClassCastException e) {
                    return defaultValue(clazz).get();
                }
            }

        };
    }

    public static void register(INBTConverter<?> converter) {
        Validate.isTrue(
                ModLoadingContext.get().getActiveContainer().getCurrentState().ordinal() < ModLoadingStage.COMPLETE
                        .ordinal(), "Register the converter earlier.");
        if (!converters.contains(Objects.requireNonNull(converter))) {
            converters.add(converter);
        }
    }

    @Nonnull
    private static INBTConverter<?> getConverter(Class<?> clazz) {
        if (converterCache.containsKey(clazz))
            return converterCache.get(clazz);
        List<INBTConverter<?>> cons = converters.stream().filter(c -> c.classValid(clazz)).collect(Collectors.toList());
        Validate.isTrue(cons.size() < 2, "More than one converter found for " + clazz.getName());
        if (cons.isEmpty())
            throw new IllegalArgumentException("No converter found for " + clazz.getName());
        INBTConverter<?> con = cons.get(0);
        converterCache.put(clazz, con);
        return con;
    }

    public static <T> T get(CompoundNBT nbt, String key, Class<T> clazz) {
        if (nbt == null)
            return null;
        if (clazz == UUID.class)
            return (T) nbt.getUniqueId(key);
        INBTConverter<T> converter = (INBTConverter<T>) getConverter(clazz);
        if (!nbt.contains(key))
            return converter.defaultValue(clazz).get();
        return converter.toValue(nbt.get(key), clazz);
    }

    public static <T> Optional<T> getOptional(CompoundNBT nbt, String key, Class<T> clazz) {
        if (nbt == null)
            return Optional.empty();
        if (clazz == UUID.class && nbt.contains(key + "Most", 99) && nbt.contains(key + "Least", 99))
            return (Optional<T>) Optional.of(nbt.getUniqueId(key));
        if (!nbt.contains(key))
            return Optional.empty();
        INBTConverter<T> converter = (INBTConverter<T>) getConverter(clazz);
        return Optional.of(converter.toValue(nbt.get(key), clazz));
    }

    public static <T> CompoundNBT set(CompoundNBT nbt, String key, T value) {
        if (nbt == null)
            return null;
        if (Objects.requireNonNull(value, () -> "No null value allowed. key: " + key).getClass() == UUID.class) {
            nbt.putUniqueId(key, (UUID) value);
            return nbt;
        }
        INBTConverter<T> converter = (INBTConverter<T>) getConverter(value.getClass());
        nbt.put(key, converter.toNBT(value));
        return nbt;
    }

    public static <T, C extends Collection<T>> C getCollection(CompoundNBT nbt, String key, Class<T> clazz) {
        Supplier<C> supplier;
        if (clazz == boolean.class || clazz == Boolean.class) {
            supplier = () -> (C) new BooleanArrayList();
        } else if (clazz == byte.class || clazz == Byte.class) {
            supplier = () -> (C) new ByteArrayList();
        } else if (clazz == char.class || clazz == Character.class) {
            supplier = () -> (C) new CharArrayList();
        } else if (clazz == short.class || clazz == Short.class) {
            supplier = () -> (C) new ShortArrayList();
        } else if (clazz == int.class || clazz == Integer.class) {
            supplier = () -> (C) new IntArrayList();
        } else if (clazz == float.class || clazz == Float.class) {
            supplier = () -> (C) new FloatArrayList();
        } else if (clazz == long.class || clazz == Long.class) {
            supplier = () -> (C) new LongArrayList();
        } else if (clazz == double.class || clazz == Double.class) {
            supplier = () -> (C) new DoubleArrayList();
        } else {
            supplier = () -> (C) new ArrayList<>();
        }
        return getCollection(nbt, key, clazz, supplier);
    }

    public static <T, C extends Collection<T>> C getCollection(CompoundNBT nbt, String key, Class<T> clazz,
            Supplier<C> supplier) {
        C result = supplier.get();
        if (nbt == null)
            return result;
        CompoundNBT collectionTag = nbt.getCompound(key);
        if (collectionTag == null || collectionTag.isEmpty())
            return result;
        if (collectionTag.contains(NULL_AMOUNT, 3)) {
            result.addAll(Collections.nCopies(collectionTag.getInt(NULL_AMOUNT), null));
            return result;
        }
        INBTConverter<T> converter = (INBTConverter<T>) getConverter(clazz);
        INBT n = collectionTag.get(COLLECTION);
        List<T> tmpResults = new ArrayList<>();
        if (n instanceof ByteArrayNBT) {
            byte[] array = ((ByteArrayNBT) n).getByteArray();
            for (byte b : array) {
                tmpResults.add(converter.toValue(new ByteNBT(b), clazz));
            }
        } else if (n instanceof IntArrayNBT) {
            int[] array = ((IntArrayNBT) n).getIntArray();
            byte type = collectionTag.getByte(TYPE);
            IntFunction<INBT> func = null;
            switch (type) {
            case 2:
                func = i -> new ShortNBT((short) i);
                break;
            case 3:
                func = i -> new IntNBT(i);
                break;
            case 5:
                func = i -> new FloatNBT(Float.intBitsToFloat(i));
                break;
            default:
                throw new IllegalStateException();
            }
            for (int i : array) {
                tmpResults.add(converter.toValue(func.apply(i), clazz));
            }
        } else if (n instanceof LongArrayNBT) {
            long[] array = ((LongArrayNBT) n).getAsLongArray();
            byte type = collectionTag.getByte(TYPE);
            LongFunction<INBT> func = null;
            switch (type) {
            case 4:
                func = l -> new LongNBT(l);
                break;
            case 6:
                func = l -> new DoubleNBT(Double.longBitsToDouble(l));
                break;
            default:
                throw new IllegalStateException();
            }
            for (long l : array) {
                tmpResults.add(converter.toValue(func.apply(l), clazz));
            }
        } else if (n instanceof ListNBT) {
            for (INBT nn : (ListNBT) n) {
                tmpResults.add(converter.toValue(nn, clazz));
            }
        } else {
            throw new IllegalStateException("unexpected value: " + n);
        }
        int[] nullIndices = collectionTag.getIntArray(NULL_INDICES);
        for (int i : nullIndices) {
            tmpResults.add(i, null);
        }
        result.addAll(tmpResults);
        return result;
    }

    public static <T> CompoundNBT setCollection(CompoundNBT nbt, String key, Collection<T> values) {
        if (nbt == null)
            return null;
        CompoundNBT collectionTag = new CompoundNBT();
        nbt.put(key, collectionTag);
        int nullAmount = Collections.frequency(values, null);
        if (values.isEmpty() || values.size() == nullAmount) {
            collectionTag.putInt(NULL_AMOUNT, nullAmount);
            return nbt;
        }
        Set<INBTConverter<T>> converters = new ReferenceOpenHashSet<>();
        IntArrayList nullIndices = new IntArrayList();
        int tmp = 0;
        for (Object o : values) {
            if (o != null) {
                INBTConverter<T> converter = (INBTConverter<T>) getConverter(o.getClass());
                converters.add(converter);
            } else {
                nullIndices.add(tmp);
            }
            tmp++;
        }
        if (converters.size() != 1)
            throw new IllegalArgumentException(
                    "Found " + converters.size() + " converters for " + values.stream().filter(Objects::nonNull)
                            .map(Object::getClass).distinct().collect(Collectors.toList()) + ". Only 1 allowed.");
        INBTConverter<T> converter = converters.iterator().next();
        if (!nullIndices.isEmpty()) {
            collectionTag.putIntArray(NULL_INDICES, nullIndices.toIntArray());
        }
        CollectionNBT<? extends INBT> collection;
        Collection<T> nonNullValues = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
        INBT base = converter.toNBT(nonNullValues.iterator().next());
        if (base instanceof ByteNBT) {
            ByteArrayList list = new ByteArrayList();
            for (T o : nonNullValues) {
                list.add(((ByteNBT) converter.toNBT(o)).getByte());
            }
            collection = new ByteArrayNBT(list.toByteArray());
        } else if (base instanceof ShortNBT || base instanceof IntNBT || base instanceof FloatNBT) {
            IntArrayList list = new IntArrayList();
            for (T o : nonNullValues) {
                if (base instanceof ShortNBT || base instanceof IntNBT) {
                    list.add(((NumberNBT) converter.toNBT(o)).getInt());
                } else {
                    list.add(Float.floatToRawIntBits(((FloatNBT) converter.toNBT(o)).getFloat()));
                }
            }
            collection = new IntArrayNBT(list.toIntArray());
            collectionTag.putByte(TYPE, base.getId());
        } else if (base instanceof LongNBT || base instanceof DoubleNBT) {
            LongArrayList list = new LongArrayList();
            for (T o : nonNullValues) {
                if (base instanceof LongNBT) {
                    list.add(((LongNBT) converter.toNBT(o)).getLong());
                } else {
                    list.add(Double.doubleToRawLongBits(((DoubleNBT) converter.toNBT(o)).getDouble()));
                }
            }
            collection = new LongArrayNBT(list.toLongArray());
            collectionTag.putByte(TYPE, base.getId());
        } else {
            ListNBT list = new ListNBT();
            for (T o : nonNullValues) {
                list.add(converter.toNBT(o));
            }
            collection = list;
        }
        collectionTag.put(COLLECTION, collection);
        return nbt;
    }

    public static <K, V> Map<K, V> getMap(CompoundNBT nbt, String key, Class<K> keyClazz, Class<V> valClazz) {
        Supplier<Map<K, V>> supplier;
        if (keyClazz == boolean.class || keyClazz == Boolean.class) {
            supplier = () -> new Object2ObjectArrayMap<>(2);
        } else if (keyClazz == byte.class || keyClazz == Byte.class) {
            supplier = () -> (Map<K, V>) new Byte2ObjectOpenHashMap<>();
        } else if (keyClazz == char.class || keyClazz == Character.class) {
            supplier = () -> (Map<K, V>) new Char2ObjectOpenHashMap<>();
        } else if (keyClazz == short.class || keyClazz == Short.class) {
            supplier = () -> (Map<K, V>) new Short2ObjectOpenHashMap<>();
        } else if (keyClazz == int.class || keyClazz == Integer.class) {
            supplier = () -> (Map<K, V>) new Int2ObjectOpenHashMap<>();
        } else if (keyClazz == float.class || keyClazz == Float.class) {
            supplier = () -> (Map<K, V>) new Float2ObjectOpenHashMap<>();
        } else if (keyClazz == long.class || keyClazz == Long.class) {
            supplier = () -> (Map<K, V>) new Long2ObjectOpenHashMap<>();
        } else if (keyClazz == double.class || keyClazz == Double.class) {
            supplier = () -> (Map<K, V>) new Double2ObjectOpenHashMap<>();
        } else
            supplier = HashMap::new;
        return getMap(nbt, key, keyClazz, valClazz, supplier);
    }

    public static <K, V> Map<K, V> getMap(CompoundNBT nbt, String key, Class<K> keyClazz, Class<V> valClazz,
            Supplier<Map<K, V>> supplier) {
        Map<K, V> result = supplier.get();
        if (nbt == null)
            return result;
        CompoundNBT mapTag = nbt.getCompound(key);
        List<K> keys = getCollection(mapTag, KEYS, keyClazz);
        List<V> values = getCollection(mapTag, VALUES, valClazz);
        Validate.isTrue(keys.size() == values.size(),
                "No equal amount of keys and values." + System.lineSeparator() + keys + System
                        .lineSeparator() + values);
        IntStream.range(0, keys.size()).forEach(i -> result.put(keys.get(i), values.get(i)));
        return result;
    }

    public static <K, V> CompoundNBT setMap(CompoundNBT nbt, String key, Map<K, V> values) {
        if (nbt == null || values.isEmpty())
            return null;
        CompoundNBT mapTag = new CompoundNBT();
        nbt.put(key, mapTag);
        setCollection(mapTag, KEYS, values.keySet());
        setCollection(mapTag, VALUES, values.values());
        return nbt;
    }
}
