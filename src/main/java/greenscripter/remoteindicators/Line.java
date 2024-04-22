package greenscripter.remoteindicators;

import net.minecraft.util.math.Vec3d;

public class Line extends Shape {

	public Vec3d pos1;
	public Vec3d pos2;
	public int color;
	public boolean depthTest;

	public void render() {
		Renderer.drawLine(pos1, pos2, color, 1, depthTest);
	}

	public String toString() {
		return "Line [" + (pos1 != null ? "pos1=" + pos1 + ", " : "") + (pos2 != null ? "pos2=" + pos2 + ", " : "") + "color=" + color + ", depthTest=" + depthTest + ", " + (dimension != null ? "dimension=" + dimension : "") + "]";
	}

}
