package mrriegel.limelib.gui.element;

import java.util.Random;

import mrriegel.limelib.gui.GuiDrawer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.config.GuiUtils;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.collect.Lists;

public class MCLabel extends GuiElement implements ITooltip, IClickable, IScrollable {

	public String text;
	public int color;

	public MCLabel(int x, int y, String text, int color, GuiDrawer drawer) {
		super(x, y, Minecraft.getMinecraft().fontRendererObj.getStringWidth(text), Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, drawer);
		this.text = text;
		this.color = color;
	}

	@Override
	public void drawForeground(int mouseX, int mouseY) {
		mc.fontRendererObj.drawString(text, x + getOffsetX(), y + getOffsetY(), color);
	}

	@Override
	public void drawBackground(int mouseX, int mouseY) {
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		width = mc.fontRendererObj.getStringWidth(text);
	}

	@Override
	public void drawTooltip(int mouseX, int mouseY) {
		ScaledResolution sr = new ScaledResolution(mc);
		GuiUtils.drawHoveringText(Lists.newArrayList("Neuer", "Lahm", "Hummels", "Martinez", "Alaba", "Alonso", "Sanches", "Thiago", "Ribery", "Lewandowski", "Müller"), mouseX, mouseY, sr.getScaledWidth(), sr.getScaledHeight(), -1, mc.fontRendererObj);
	}

	@Override
	public void onScrolled(int scroll) {
		if (scroll == 0)
			return;
		else if (scroll < 0) {
			if (text.length() > 1)
				text = text.substring(0, text.length() - 1);
		} else {
			text += RandomStringUtils.randomAlphabetic(1);
		}
	}

	@Override
	public void onClick(int button) {
		text = RandomStringUtils.randomAlphabetic(10);
	}

	@Override
	public void onRelease(int button) {
		color = new Random().nextInt() % 0xffffff;
	}

}