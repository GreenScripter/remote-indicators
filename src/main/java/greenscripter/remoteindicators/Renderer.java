package greenscripter.remoteindicators;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("resource")
public class Renderer {

	public static int[] colorToRGBA(int color) {
		return new int[] { (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF };
	}

	public static Renderer startDraw(float lineWidth, boolean depthTest) {
		RenderSystem.enableBlend();
		if (!depthTest) RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.lineWidth(lineWidth);
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

		MatrixStack matrices = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

		Renderer r = new Renderer();
		r.matrices = matrices;
		r.buffer = buffer;
		r.tessellator = tessellator;
		r.camX = camera.getPos().x;
		r.camY = camera.getPos().y;
		r.camZ = camera.getPos().z;
		return r;
	}

	Tessellator tessellator;
	MatrixStack matrices;
	double camX;
	double camY;
	double camZ;
	BufferBuilder buffer;
	boolean rendered;

	public void drawBoxPart(Box box, int color) {
		if (!rendered) rendered = true;
		matrices.push();
		matrices.translate(box.minX - camX, box.minY - camY, box.minZ - camZ);

		vertexBoxLines(matrices, buffer, box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate()), color);
		matrices.pop();
	}

	public void drawLinePart(Vec3d from, Vec3d to, int color) {
		if (!rendered) rendered = true;
		matrices.push();

		matrices.translate(from.x - camX, from.y - camY, from.z - camZ);

		Matrix4f model = matrices.peek().getPositionMatrix();
		Matrix3f normal = matrices.peek().getNormalMatrix();

		vertexLine(matrices, model, normal, buffer, 0, 0, 0, (float) (to.x - from.x), (float) (to.y - from.y), (float) (to.z - from.z), color);

		matrices.pop();
	}

	public void finishDraw(boolean depthTest) {
		if (rendered) {
			// tessellator.draw();
			BufferRenderer.drawWithGlobalProgram(buffer.end());
		}

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		if (!depthTest) RenderSystem.enableDepthTest();
	}

	public static void drawBox(Box box, float lineWidth, int color, boolean depthTest) {
		RenderSystem.enableBlend();
		if (!depthTest) RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.lineWidth(lineWidth);
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

		MatrixStack matrices = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

		// start
		matrices.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);

		vertexBoxLines(matrices, buffer, box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate()), color);

		//end
		// tessellator.draw();
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		if (!depthTest) RenderSystem.enableDepthTest();
	}

	private static void vertexBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int color) {
		float minX = (float) box.minX;
		float minY = (float) box.minY;
		float minZ = (float) box.minZ;
		float maxX = (float) box.maxX;
		float maxY = (float) box.maxY;
		float maxZ = (float) box.maxZ;
		Matrix4f model = matrices.peek().getPositionMatrix();
		Matrix3f normal = matrices.peek().getNormalMatrix();

		vertexLine(matrices, model, normal, vertexConsumer, minX, minY, minZ, maxX, minY, minZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, maxX, minY, minZ, maxX, minY, maxZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, maxX, minY, maxZ, minX, minY, maxZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, minX, minY, maxZ, minX, minY, minZ, color);

		vertexLine(matrices, model, normal, vertexConsumer, minX, minY, maxZ, minX, maxY, maxZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, minX, minY, minZ, minX, maxY, minZ, color);

		vertexLine(matrices, model, normal, vertexConsumer, maxX, minY, maxZ, maxX, maxY, maxZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, maxX, minY, minZ, maxX, maxY, minZ, color);

		vertexLine(matrices, model, normal, vertexConsumer, minX, maxY, minZ, maxX, maxY, minZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, maxX, maxY, minZ, maxX, maxY, maxZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, maxX, maxY, maxZ, minX, maxY, maxZ, color);
		vertexLine(matrices, model, normal, vertexConsumer, minX, maxY, maxZ, minX, maxY, minZ, color);
	}

	private static void vertexLine(MatrixStack matrices, Matrix4f model, Matrix3f normal, VertexConsumer vertexConsumer, float startx, float starty, float startz, float endx, float endy, float endz, int color) {

		float xNormal = startx - endx;
		float yNormal = starty - endy;
		float zNormal = startz - endz;

		vertexConsumer.vertex(model, startx, starty, startz).color(color).normal(matrices.peek(), xNormal, yNormal, zNormal);
		vertexConsumer.vertex(model, endx, endy, endz).color(color).normal(matrices.peek(), xNormal, yNormal, zNormal);
	}

	public static void drawLine(Vec3d from, Vec3d to, int color, float width, boolean depthTest) {
		RenderSystem.enableBlend();
		if (!depthTest) RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
		RenderSystem.lineWidth(width);

		MatrixStack matrices = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		matrices.translate(from.x - camera.getPos().x, from.y - camera.getPos().y, from.z - camera.getPos().z);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

		Matrix4f model = matrices.peek().getPositionMatrix();
		Matrix3f normal = matrices.peek().getNormalMatrix();
		vertexLine(matrices, model, normal, buffer, 0f, 0f, 0f, (float) (to.x - from.x), (float) (to.y - from.y), (float) (to.z - from.z), color);
		//tessellator.draw();
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		if (!depthTest) RenderSystem.enableDepthTest();
	}
}
