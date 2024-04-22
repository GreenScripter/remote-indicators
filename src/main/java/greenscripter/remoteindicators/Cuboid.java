package greenscripter.remoteindicators;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Cuboid extends Shape {

	public Vec3d pos1;
	public Vec3d pos2;
	public int color;
	public boolean depthTest;

	public void render() {
		Renderer.drawBox(new Box(pos1, pos2), 1, color, false);
	}

	public String toString() {
		return "Cuboid [" + (pos1 != null ? "pos1=" + pos1 + ", " : "") + (pos2 != null ? "pos2=" + pos2 + ", " : "") + "color=" + color + ", depthTest=" + depthTest + ", " + (dimension != null ? "dimension=" + dimension : "") + "]";
	}

}
