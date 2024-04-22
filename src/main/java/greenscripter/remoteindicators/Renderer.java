package greenscripter.remoteindicators;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class Renderer {

	public static int[] colorToRGBA(int color) {
		return new int[] { (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF };
	}

	public static void drawBox(Box box, float lineWidth, int color, boolean depthTest) {
		int[] rgba = colorToRGBA(color);
		drawBox(box, lineWidth, rgba[0], rgba[1], rgba[2], rgba[3], depthTest);
	}

	public static void drawBox(Box box, float lineWidth, int r, int g, int b, int a, boolean depthTest) {
		RenderSystem.enableBlend();
		if (!depthTest) RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.lineWidth(lineWidth);
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

		MatrixStack matrices = new MatrixStack();

		@SuppressWarnings("resource")
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		matrices.translate(box.minX - camera.getPos().x, box.minY - camera.getPos().y, box.minZ - camera.getPos().z);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

		vertexBoxLines(matrices, buffer, box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate()), r, g, b, a);

		tessellator.draw();

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		if (!depthTest) RenderSystem.enableDepthTest();
	}

	private static void vertexBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, int r, int g, int b, int a) {
		float minX = (float) box.minX;
		float minY = (float) box.minY;
		float minZ = (float) box.minZ;
		float maxX = (float) box.maxX;
		float maxY = (float) box.maxY;
		float maxZ = (float) box.maxZ;

		vertexLine(matrices, vertexConsumer, new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(maxX, minY, minZ), new Vector3f(maxX, minY, maxZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(maxX, minY, maxZ), new Vector3f(minX, minY, maxZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(minX, minY, maxZ), new Vector3f(minX, minY, minZ), r, g, b, a);

		vertexLine(matrices, vertexConsumer, new Vector3f(minX, minY, maxZ), new Vector3f(minX, maxY, maxZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(minX, minY, minZ), new Vector3f(minX, maxY, minZ), r, g, b, a);

		vertexLine(matrices, vertexConsumer, new Vector3f(maxX, minY, maxZ), new Vector3f(maxX, maxY, maxZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(maxX, minY, minZ), new Vector3f(maxX, maxY, minZ), r, g, b, a);

		vertexLine(matrices, vertexConsumer, new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(maxX, maxY, minZ), new Vector3f(maxX, maxY, maxZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(maxX, maxY, maxZ), new Vector3f(minX, maxY, maxZ), r, g, b, a);
		vertexLine(matrices, vertexConsumer, new Vector3f(minX, maxY, maxZ), new Vector3f(minX, maxY, minZ), r, g, b, a);
	}

	private static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, Vector3f start, Vector3f end, int r, int g, int b, int a) {
		Matrix4f model = matrices.peek().getPositionMatrix();
		Matrix3f normal = matrices.peek().getNormalMatrix();

		Vector3f normalVec = getNormal(start, end);

		vertexConsumer.vertex(model, start.x, start.y, start.z).color(r, g, b, a).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
		vertexConsumer.vertex(model, end.x, end.y, end.z).color(r, g, b, a).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
	}

	private static Vector3f getNormal(Vector3f v1, Vector3f v2) {
		float xNormal = v2.x - v1.x;
		float yNormal = v2.y - v1.y;
		float zNormal = v2.z - v1.z;
		float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

		return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
	}

	public static void drawLine(Vec3d from, Vec3d to, int color, float width, boolean depthTest) {
		int[] rgba = colorToRGBA(color);
		drawLine(from, to, rgba[0], rgba[1], rgba[2], rgba[3], width, depthTest);
	}

	public static void drawLine(Vec3d from, Vec3d to, int r, int g, int b, int a, float width, boolean depthTest) {
		RenderSystem.enableBlend();
		if (!depthTest) RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
		RenderSystem.lineWidth(width);

		MatrixStack matrices = new MatrixStack();

		@SuppressWarnings("resource")
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		matrices.translate(from.x - camera.getPos().x, from.y - camera.getPos().y, from.z - camera.getPos().z);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		vertexLine(matrices, buffer, new Vector3f(0f, 0f, 0f), new Vector3f((float) (to.x - from.x), (float) (to.y - from.y), (float) (to.z - from.z)), r, g, b, a);
		tessellator.draw();

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		if (!depthTest) RenderSystem.enableDepthTest();
	}
}
