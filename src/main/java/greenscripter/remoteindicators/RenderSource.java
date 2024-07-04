package greenscripter.remoteindicators;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.DataInputStream;
import java.net.Socket;

import net.minecraft.util.math.Vec3d;

public class RenderSource {

	Map<Integer, Shape> shapes = new ConcurrentHashMap<>();
	Config conf;
	int version = 1;

	private static final int ID_ADD_LINE = 0;
	private static final int ID_ADD_CUBOID = 1;
	private static final int ID_REMOVE_SHAPE = 2;

	public RenderSource(Config c) {
		this.conf = c;

		new Thread(() -> {
			Socket remote;
			while (true) {
				try {
					shapes.clear();
					remote = new Socket(conf.serverIp, conf.port);
					try {
						DataInputStream in = new DataInputStream(remote.getInputStream());
						int version = in.readInt();
						if (version != this.version) {
							throw new RuntimeException("Version missmatch for indicator server " + conf.serverIp + ":" + conf.port + ", " + this.version + " != " + version);
						}
						System.out.println("Connected to server on version " + version);
						while (true) {
							int type = in.readInt();
							switch (type) {
								case ID_ADD_LINE -> {
									Line next = new Line();
									int id = in.readInt();

									double x1 = in.readDouble();
									double y1 = in.readDouble();
									double z1 = in.readDouble();
									next.pos1 = new Vec3d(x1, y1, z1);

									double x2 = in.readDouble();
									double y2 = in.readDouble();
									double z2 = in.readDouble();
									next.pos2 = new Vec3d(x2, y2, z2);

									next.dimension = in.readUTF();
									next.color = in.readInt();
									next.depthTest = in.readBoolean();
									shapes.put(id, next);
									//									System.out.println(next);
								}
								case ID_ADD_CUBOID -> {
									Cuboid next = new Cuboid();
									int id = in.readInt();

									double x1 = in.readDouble();
									double y1 = in.readDouble();
									double z1 = in.readDouble();
									next.pos1 = new Vec3d(x1, y1, z1);

									double x2 = in.readDouble();
									double y2 = in.readDouble();
									double z2 = in.readDouble();
									next.pos2 = new Vec3d(x2, y2, z2);

									next.dimension = in.readUTF();
									next.color = in.readInt();
									next.depthTest = in.readBoolean();
									shapes.put(id, next);
									//									System.out.println(next);
								}
								case ID_REMOVE_SHAPE -> {
									int id = in.readInt();
									shapes.remove(id);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					remote.close();
				} catch (Exception e) {
					//					e.printStackTrace();
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
