package me.jetp250.goapimpl.utilities;

import org.bukkit.Bukkit;

public class Debug {

	private static final boolean ENABLED = true;

	private static boolean isEnabled() {
		return Debug.ENABLED;
	}

	public static void s(final Object... toPrint) {
		if (!Debug.isEnabled() || toPrint.length == 0) {
			return;
		}
		if (toPrint.length == 1) {
			System.out.println(toPrint[0].toString());
			return;
		}
		final StringBuilder builder = new StringBuilder(toPrint[0].toString());
		for (int i = 1; i < toPrint.length; ++i) {
			builder.append(toPrint[i].toString());
		}
		System.out.println(builder.toString());
	}

	public static void a(final Object... toPrint) {
		if (!Debug.isEnabled() || toPrint.length == 0) {
			return;
		}
		if (toPrint.length == 1) {
			Bukkit.broadcastMessage(toPrint[0].toString());
			System.out.println(toPrint[0].toString());
			return;
		}
		final StringBuilder builder = new StringBuilder(toPrint[0].toString());
		for (int i = 1; i < toPrint.length; ++i) {
			builder.append(toPrint[i].toString());
		}
		Bukkit.broadcastMessage(builder.toString());
		System.out.println(builder.toString());
	}

	public static void b(final Object... toPrint) {
		if (!Debug.isEnabled() || toPrint.length == 0) {
			return;
		}
		if (toPrint.length == 1) {
			Bukkit.broadcastMessage(toPrint[0].toString());
			return;
		}
		final StringBuilder builder = new StringBuilder(toPrint[0].toString());
		for (int i = 1; i < toPrint.length; ++i) {
			builder.append(toPrint[i].toString());
		}
		Bukkit.broadcastMessage(builder.toString());
	}

}
