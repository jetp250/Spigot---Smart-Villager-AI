package me.jetp250.goapimpl.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public final class FileUtility {

	private static final String[] IGNORED_FILES;

	public static World copyWorld(final World originalWorld, final String newWorldName) {
		return FileUtility.copyWorld(originalWorld.getWorldFolder(), newWorldName);
	}

	public static World copyWorld(final File originalWorldFolder, final String newWorldName) {
		if (FileUtility.copyFileStructure(originalWorldFolder, new File(Bukkit.getWorldContainer(), newWorldName)) != null) {
			return null;
		}
		return new WorldCreator(newWorldName).createWorld();
	}

	private static final Throwable copyFileStructure(final File source, final File target) {
		try {
			if (!ArrayUtils.contains(FileUtility.IGNORED_FILES, source.getName())) {
				if (source.isDirectory()) {
					if (!target.exists()) {
						if (!target.mkdirs()) {
							return new IOException("unable to create directory");
						}
					}
					final String files[] = source.list();
					for (final String file : files) {
						final File srcFile = new File(source, file);
						final File destFile = new File(target, file);
						FileUtility.copyFileStructure(srcFile, destFile);
					}
				} else {
					final InputStream in = new FileInputStream(source);
					final OutputStream out = new FileOutputStream(target);
					final byte[] buffer = new byte[1024];
					int length;
					while ((length = in.read(buffer)) > 0) {
						out.write(buffer, 0, length);
					}
					in.close();
					out.close();
				}
			}
		} catch (final Exception e) {
			return e;
		}
		return null;
	}

	public static boolean deleteWorld(final World world) {
		return FileUtility.deleteWorld(world.getWorldFolder());
	}

	public static boolean deleteWorld(final File path) {
		if (path.exists()) {
			final File[] files = path.listFiles();
			for (final File file : files) {
				if (file.isDirectory()) {
					FileUtility.deleteWorld(file);
				}
				file.delete();
			}
		}
		return path.delete();
	}

	static {
		IGNORED_FILES = new String[] { "uid.dat", "session.lock" };
	}

}
