package mrriegel.limelib.helper;

import java.util.EnumSet;
import java.util.List;

import mrriegel.limelib.LimeLib;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;

import com.google.common.collect.Lists;

public class NBTHelper {

	public static boolean hasTag(NBTTagCompound nbt, String keyName) {
		return nbt != null && nbt.hasKey(keyName);
	}

	public static void removeTag(NBTTagCompound nbt, String keyName) {
		if (nbt == null)
			return;
		{
			nbt.removeTag(keyName);
		}
	}

	public static NBTTagList createList(NBTBase... list) {
		return createList(Lists.newArrayList(list));
	}

	public static NBTTagList createList(List<NBTBase> list) {
		NBTTagList tagList = new NBTTagList();
		for (NBTBase b : list)
			tagList.appendTag(b);
		return tagList;
	}

	public static NBTTagCompound getTag(Object... os) {
		NBTTagCompound nbt = new NBTTagCompound();
		List<NBTBase> lis = getNBTs(os);
		NBTHelper.setInt(nbt, "size", lis.size());
		for (int i = 0; i < lis.size(); i++) {
			NBTBase o = lis.get(i);
			nbt.setTag("" + i, o);
		}
		return nbt;
	}

	public static List<NBTBase> getObjects(NBTTagCompound nbt) {
		List<NBTBase> lis = Lists.newArrayList();
		for (int i = 0; i < getInt(nbt, "size"); i++)
			lis.add(nbt.getTag("" + i));
		return lis;
	}

	public static List<NBTBase> getNBTs(Object... os) {
		List<NBTBase> lis = Lists.newArrayList();
		for (Object o : os)
			try {
				if (o.getClass().equals(Boolean.class))
					lis.add(new NBTTagByte(((Boolean) o).booleanValue() ? (byte) 1 : 0));
				else if (o.getClass().equals(Byte.class))
					lis.add(new NBTTagByte((Byte) o));
				else if (o.getClass().equals(Short.class))
					lis.add(new NBTTagShort((Short) o));
				else if (o.getClass().equals(Integer.class))
					lis.add(new NBTTagInt((Integer) o));
				else if (o.getClass().equals(Long.class))
					lis.add(new NBTTagLong((Long) o));
				else if (o.getClass().equals(Float.class))
					lis.add(new NBTTagFloat((Float) o));
				else if (o.getClass().equals(Double.class))
					lis.add(new NBTTagDouble((Double) o));
				else if (o.getClass().equals(String.class))
					lis.add(new NBTTagString((String) o));
				else if (o.getClass().equals(BlockPos.class))
					lis.add(new NBTTagLong(((BlockPos) o).toLong()));
				else if (o.getClass().equals(ItemStack.class)) {
					NBTTagCompound n1 = new NBTTagCompound();
					((ItemStack) o).writeToNBT(n1);
					lis.add(n1);
				} else
					LimeLib.log.warn("Unacceptable Class.");
			} catch (Exception e) {
				LimeLib.log.error("Couldn't construct NBTBase.");
			}
		return lis;
	}

	public static <T> T getObjectFrom(NBTBase nbt, Class<T> clazz) {
		try {
			if (clazz.equals(Boolean.class))
				return (T) (((NBTTagByte) nbt).getByte() == 1 ? Boolean.TRUE : Boolean.FALSE);
			else if (clazz.equals(Byte.class))
				return (T) new Byte(((NBTTagByte) nbt).getByte());
			else if (clazz.equals(Short.class))
				return (T) new Short(((NBTTagShort) nbt).getShort());
			else if (clazz.equals(Integer.class))
				return (T) new Integer(((NBTTagInt) nbt).getInt());
			else if (clazz.equals(Long.class))
				return (T) new Long(((NBTTagLong) nbt).getLong());
			else if (clazz.equals(Float.class))
				return (T) new Float(((NBTTagFloat) nbt).getFloat());
			else if (clazz.equals(Double.class))
				return (T) new Double(((NBTTagDouble) nbt).getDouble());
			else if (clazz.equals(String.class))
				return (T) new String(((NBTTagString) nbt).getString());
			else if (clazz.equals(BlockPos.class))
				return (T) BlockPos.fromLong(((NBTTagLong) nbt).getLong());
			else if (clazz.equals(ItemStack.class)) {
				return (T) ItemStack.loadItemStackFromNBT((NBTTagCompound) nbt);
			} else
				LimeLib.log.warn("Unacceptable Class.");
		} catch (ClassCastException e) {
			LimeLib.log.error("Wrong Class. Obtained: " + clazz.toString());
		}
		return null;
	}

	public static Object getObjectFrom(NBTBase nbt) {
		if (nbt instanceof NBTTagByte)
			return new Byte(((NBTTagByte) nbt).getByte());
		else if (nbt instanceof NBTTagShort)
			return new Short(((NBTTagShort) nbt).getShort());
		else if (nbt instanceof NBTTagInt)
			return new Integer(((NBTTagInt) nbt).getInt());
		else if (nbt instanceof NBTTagLong)
			return new Long(((NBTTagLong) nbt).getLong());
		else if (nbt instanceof NBTTagFloat)
			return new Float(((NBTTagFloat) nbt).getFloat());
		else if (nbt instanceof NBTTagDouble)
			return new Double(((NBTTagDouble) nbt).getDouble());
		else if (nbt instanceof NBTTagString)
			return new String(((NBTTagString) nbt).getString());
		else
			LimeLib.log.warn("Unacceptable Class.");
		return null;
	}

	// list
	public static NBTTagList getList(NBTTagCompound nbt, String tag, int objtype, boolean nullifyOnFail) {
		return nbt != null && hasTag(nbt, tag) ? nbt.getTagList(tag, objtype) : nullifyOnFail ? null : new NBTTagList();
	}

	public static NBTTagCompound setList(NBTTagCompound nbt, String tag, NBTTagList list) {
		if (nbt == null)
			return nbt;
		nbt.setTag(tag, list);
		return nbt;
	}

	// String
	public static String getString(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 8)) {
			return null;
		}
		return nbt.getString(keyName);
	}

	public static NBTTagCompound setString(NBTTagCompound nbt, String keyName, String keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null)
			nbt.setString(keyName, keyValue);
		return nbt;
	}

	// boolean
	public static boolean getBoolean(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setBoolean(nbt, keyName, false);
			return false;
		}
		return nbt.getBoolean(keyName);
	}

	public static NBTTagCompound setBoolean(NBTTagCompound nbt, String keyName, boolean keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setBoolean(keyName, keyValue);
		return nbt;
	}

	// byte
	public static byte getByte(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setByte(nbt, keyName, (byte) 0);
			return (byte) 0;
		}
		return nbt.getByte(keyName);
	}

	public static NBTTagCompound setByte(NBTTagCompound nbt, String keyName, byte keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setByte(keyName, keyValue);
		return nbt;
	}

	// short
	public static short getShort(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setShort(nbt, keyName, (short) 0);
			return (short) 0;
		}
		return nbt.getShort(keyName);
	}

	public static NBTTagCompound setShort(NBTTagCompound nbt, String keyName, short keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setShort(keyName, keyValue);
		return nbt;
	}

	// int
	public static int getInt(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setInt(nbt, keyName, 0);
			return 0;
		}
		return nbt.getInteger(keyName);
	}

	public static NBTTagCompound setInt(NBTTagCompound nbt, String keyName, int keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setInteger(keyName, keyValue);
		return nbt;
	}

	// long
	public static long getLong(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setLong(nbt, keyName, 0L);
			return 0L;
		}
		return nbt.getLong(keyName);
	}

	public static NBTTagCompound setLong(NBTTagCompound nbt, String keyName, long keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setLong(keyName, keyValue);
		return nbt;
	}

	// float
	public static float getFloat(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setFloat(nbt, keyName, 0F);
			return 0F;
		}
		return nbt.getFloat(keyName);
	}

	public static NBTTagCompound setFloat(NBTTagCompound nbt, String keyName, float keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setFloat(keyName, keyValue);
		return nbt;
	}

	// double
	public static double getDouble(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName, 99)) {
			//			setDouble(nbt, keyName, 0D);
			return 0D;
		}
		return nbt.getDouble(keyName);
	}

	public static NBTTagCompound setDouble(NBTTagCompound nbt, String keyName, double keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setDouble(keyName, keyValue);
		return nbt;
	}

	// tag
	public static NBTTagCompound getTag(NBTTagCompound nbt, String keyName) {
		if (nbt == null)
			return null;
		return (NBTTagCompound) nbt.getTag(keyName);
	}

	public static NBTTagCompound setTag(NBTTagCompound nbt, String keyName, NBTTagCompound keyValue) {
		if (nbt == null)
			return nbt;
		nbt.setTag(keyName, keyValue);
		return nbt;
	}

	// itemstack
	public static ItemStack getItemStack(NBTTagCompound nbt, String keyName) {
		if (nbt == null || !nbt.hasKey(keyName)) {
			return null;
		}
		NBTTagCompound res = (NBTTagCompound) nbt.getTag(keyName);
		return ItemStack.loadItemStackFromNBT(res);

	}

	public static NBTTagCompound setItemStack(NBTTagCompound nbt, String keyName, ItemStack keyValue) {
		if (nbt == null)
			return nbt;
		NBTTagCompound res = new NBTTagCompound();
		if (keyValue != null) {
			keyValue.writeToNBT(res);
		}
		nbt.setTag(keyName, res);
		return nbt;
	}

	// enum
	public static <E extends Enum<E>> E getEnum(NBTTagCompound nbt, String keyName, Class<E> clazz) {
		if (nbt == null || !nbt.hasKey(keyName)) {
			return null;
		}
		String s = getString(nbt, keyName);
		for (E e : EnumSet.allOf(clazz)) {
			if (e.name().equals(s))
				return e;
		}
		return null;

	}

	public static <E extends Enum<E>> NBTTagCompound setEnum(NBTTagCompound nbt, String keyName, E keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null)
			setString(nbt, keyName, keyValue.name());
		return nbt;
	}

	private static final String SIZE = "§Size";

	// Stringlist
	public static List<String> getStringList(NBTTagCompound nbt, String keyName) {
		List<String> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getString(nbt, keyName + ":" + i));
			else
				lis.add(getString(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setStringList(NBTTagCompound nbt, String keyName, List<String> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				String s = keyValue.get(i);
				if (s != null)
					setString(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Booleanlist
	public static List<Boolean> getBooleanList(NBTTagCompound nbt, String keyName) {
		List<Boolean> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getBoolean(nbt, keyName + ":" + i));
			else
				lis.add(getBoolean(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setBooleanList(NBTTagCompound nbt, String keyName, List<Boolean> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Boolean s = keyValue.get(i);
				if (s != null)
					setBoolean(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Bytelist
	public static List<Byte> getByteList(NBTTagCompound nbt, String keyName) {
		List<Byte> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getByte(nbt, keyName + ":" + i));
			else
				lis.add(getByte(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setByteList(NBTTagCompound nbt, String keyName, List<Byte> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Byte s = keyValue.get(i);
				if (s != null)
					setByte(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Shortlist
	public static List<Short> getShortList(NBTTagCompound nbt, String keyName) {
		List<Short> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getShort(nbt, keyName + ":" + i));
			else
				lis.add(getShort(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setShortList(NBTTagCompound nbt, String keyName, List<Short> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Short s = keyValue.get(i);
				if (s != null)
					setShort(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Integerlist
	public static List<Integer> getIntList(NBTTagCompound nbt, String keyName) {
		List<Integer> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getInt(nbt, keyName + ":" + i));
			else
				lis.add(getInt(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setIntList(NBTTagCompound nbt, String keyName, List<Integer> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Integer s = keyValue.get(i);
				if (s != null)
					setInt(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Longlist
	public static List<Long> getLongList(NBTTagCompound nbt, String keyName) {
		List<Long> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getLong(nbt, keyName + ":" + i));
			else
				lis.add(getLong(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setLongList(NBTTagCompound nbt, String keyName, List<Long> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Long s = keyValue.get(i);
				if (s != null)
					setLong(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Floatlist
	public static List<Float> getFloatList(NBTTagCompound nbt, String keyName) {
		List<Float> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getFloat(nbt, keyName + ":" + i));
			else
				lis.add(getFloat(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setFloatList(NBTTagCompound nbt, String keyName, List<Float> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Float s = keyValue.get(i);
				if (s != null)
					setFloat(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Doublelist
	public static List<Double> getDoubleList(NBTTagCompound nbt, String keyName) {
		List<Double> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getDouble(nbt, keyName + ":" + i));
			else
				lis.add(getDouble(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setDoubleList(NBTTagCompound nbt, String keyName, List<Double> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				Double s = keyValue.get(i);
				if (s != null)
					setDouble(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Taglist
	public static List<NBTTagCompound> getTagList(NBTTagCompound nbt, String keyName) {
		List<NBTTagCompound> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getTag(nbt, keyName + ":" + i));
			else
				lis.add(getTag(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setTagList(NBTTagCompound nbt, String keyName, List<NBTTagCompound> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				NBTTagCompound s = keyValue.get(i);
				//				if (s != null)
				setTag(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

	// Stacklist
	public static List<ItemStack> getItemStackList(NBTTagCompound nbt, String keyName) {
		List<ItemStack> lis = Lists.newArrayList();
		int size = getInt(nbt, keyName + SIZE);
		for (int i = 0; i < size; i++)
			if (hasTag(nbt, keyName + ":" + i))
				lis.add(getItemStack(nbt, keyName + ":" + i));
			else
				lis.add(getItemStack(nbt, keyName + "-" + i));
		return lis;
	}

	public static NBTTagCompound setItemStackList(NBTTagCompound nbt, String keyName, List<ItemStack> keyValue) {
		if (nbt == null)
			return nbt;
		if (keyValue != null && !keyValue.isEmpty()) {
			setInt(nbt, keyName + SIZE, keyValue.size());
			for (int i = 0; i < keyValue.size(); i++) {
				ItemStack s = keyValue.get(i);
				setItemStack(nbt, keyName + "-" + i, s);
			}
		}
		return nbt;
	}

}
