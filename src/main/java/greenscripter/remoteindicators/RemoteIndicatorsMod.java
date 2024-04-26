package greenscripter.remoteindicators;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;

public class RemoteIndicatorsMod implements ModInitializer {

	static Map<Integer, Shape> shapes = new HashMap<>();
	Config conf = new Config();
	RenderSource source;

	@Override
	public void onInitialize() {
		File configFolder = new File("config", "remoteindicators");
		configFolder.mkdirs();
		File config = new File(configFolder, "server.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		try {
			String fileData = Files.readString(config.toPath());
			conf = gson.fromJson(fileData, Config.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Files.writeString(config.toPath(), gson.toJson(conf));
		} catch (IOException e) {
			e.printStackTrace();
		}
		source = new RenderSource(conf);
		shapes = source.shapes;

	}

	public static void render(MatrixStack matrices, float tickDelta, Camera camera, Immediate immediate) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.world == null) return;

		String id = mc.world.getRegistryKey().getValue().toString();
		Renderer rend = Renderer.startDraw(1, false);
		for (Shape s : shapes.values()) {
			if (id.equals(s.dimension)) {
				s.render(rend);
			}
		}
		rend.finishDraw(false);
	}

}
