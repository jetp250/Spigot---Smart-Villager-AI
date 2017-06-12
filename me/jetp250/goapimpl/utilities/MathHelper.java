package me.jetp250.goapimpl.utilities;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class MathHelper {

	public static final FastRandom RANDOM = new FastRandom();
	public static final float FLOAT_PI;
	public static final int ZERO;
	private static final float[] SIN;
	private static final float[] ATAN2;

	public static final float atan2(float y, float x) {
		final float add, mul;
		if (x < 0.0f) {
			if (y < 0.0f) {
				x = -x;
				y = -y;
				mul = 1.0f;
			} else {
				x = -x;
				mul = -1.0f;
			}
			add = -MathHelper.FLOAT_PI;
		} else {
			if (y < 0.0f) {
				y = -y;
				mul = -1.0f;
			} else {
				mul = 1.0f;
			}
			add = 0.0f;
		}
		final float invDiv = 1.0f / ((x < y ? y : x) * 0.007874016F);
		final int xi = (int) (x * invDiv);
		final int yi = (int) (y * invDiv);
		return (MathHelper.ATAN2[yi * 128 + xi] + add) * mul;
	}

	public static int roundToNearest(final double n, final int stack) {
		return MathHelper.round(n / stack) / 2 * stack;
	}

	public static int roundToNearest(final float n, final int stack) {
		return MathHelper.round(n / stack) / 2 * stack;
	}

	public static float sin(final float n) {
		return MathHelper.SIN[(int) (n * 10430.378f) & 0xFFFF];
	}

	public static float cos(final float n) {
		return MathHelper.SIN[(int) (n * 10430.378f + 16384.0f) & 0xFFFF];
	}

	public static int round(final float f) {
		return (int) (f > 0 ? f + 0.5F : f - 0.5F);
	}

	public static int floor(final float f) {
		final int i = (int) f;
		return i > f ? i - 1 : i;
	}

	public static int ceil(final float f) {
		final int i = (int) f;
		return i > f ? i : i - 1;
	}

	public static float sin(final double n) {
		return MathHelper.SIN[(int) (n * 10430.378f) & 0xFFFF];
	}

	public static float cos(final double n) {
		return MathHelper.SIN[(int) (n * 10430.378f + 16384.0f) & 0xFFFF];
	}

	public static int round(final double n) {
		return (int) (n > 0 ? n + 0.5D : n - 0.5D);
	}

	public static int floor(final double n) {
		final int i = (int) n;
		return i > n ? i - 1 : i;
	}

	public static int ceil(final double n) {
		final int i = (int) n;
		return i > n ? i : i - 1;
	}

	public static int distAbs(final int a, final int b) {
		return a > b ? a - b : b - a;
	}

	public static float distAbs(final float a, final float b) {
		return a > b ? a - b : b - a;
	}

	public static int distSqr(int x, int y, int z, final int x2, final int y2, final int z2) {
		x -= x2;
		y -= y2;
		z -= z2;
		return x * x + y * y + z * z;
	}

	public static float distSqr(float x, float y, float z, final float x2, final float y2, final float z2) {
		x -= x2;
		y -= y2;
		z -= z2;
		return x * x + y * y + z * z;
	}

	public static double distSqr(double x, double y, double z, final double x2, final double y2, final double z2) {
		x -= x2;
		y -= y2;
		z -= z2;
		return x * x + y * y + z * z;
	}

	public static int distSqr(int x, int z, final int x2, final int z2) {
		x -= x2;
		z -= z2;
		return x * x + z * z;
	}

	public static float distSqr(float x, float z, final float x2, final float z2) {
		x -= x2;
		z -= z2;
		return x * x + z * z;
	}

	public static double distSqr(double x, double z, final double x2, final double z2) {
		x -= x2;
		z -= z2;
		return x * x + z * z;
	}

	public static Vector getDirection(float pitch, float yaw, final Vector vector) {
		pitch = MathHelper.toRadians(pitch);
		yaw = MathHelper.toRadians(yaw);
		vector.setY(-MathHelper.sin(pitch));
		final float xz = MathHelper.cos(pitch);
		vector.setX(-xz * MathHelper.sin(yaw));
		vector.setZ(xz * MathHelper.cos(yaw));
		return vector;
	}

	public static Vector getHorizontalDirectionWithPitch(final Location source, final Vector target) {
		final float yaw = MathHelper.toRadians(source.getYaw());
		final float zx = MathHelper.cos(MathHelper.toRadians(source.getPitch()));
		target.setX(-zx * MathHelper.sin(yaw));
		target.setZ(zx * MathHelper.cos(yaw));
		return target;
	}

	public static Vector rotatedDirection(final Location location, final float xRot, final float yRot) {
		location.setPitch(location.getPitch() + yRot);
		location.setYaw(location.getYaw() + xRot);
		final float rotY = MathHelper.toRadians(location.getPitch());
		final float rotX = MathHelper.toRadians(location.getYaw());
		final float zx = MathHelper.cos(rotY);
		return new Vector(-zx * MathHelper.sin(rotX), -MathHelper.sin(rotY), zx * MathHelper.cos(rotX));
	}

	public static Vector rotatedDirection(final float pitch_, final float yaw_, final float xRot, final float yRot) {
		final float pitch = MathHelper.toRadians(pitch_ + 90 + xRot);
		final float yaw = MathHelper.toRadians(yaw_ + 90 + yRot);
		final float z_axis = MathHelper.sin(pitch);
		return new Vector(z_axis * MathHelper.cos(yaw), MathHelper.cos(pitch), z_axis * MathHelper.sin(yaw));
	}

	public static float toRadians(final float degrees) {
		return degrees * 0.01745329251994329576923516235563F;
	}

	public static float toDegrees(final float radians) {
		return radians * 57.295779513082320876798154814105F;
	}

	public static double toRadians(final double degrees) {
		return degrees * 0.01745329251994329576923516235563;
	}

	public static double toDegrees(final double radians) {
		return radians * 57.295779513082320876798154814105;
	}

	public static float sqrt(final float f) {
		final float xhalf = f * 0.5F;
		float y = Float.intBitsToFloat(0x5f375a86 - (Float.floatToIntBits(f) >> 1));
		y = y * (1.5F - xhalf * y * y);
		y = y * (1.5F - xhalf * y * y);
		return f * y;
	}

	public static float sqrt(final float f, final float half) {
		float y = Float.intBitsToFloat(0x5f375a86 - (Float.floatToIntBits(f) >> 1));
		y = y * (1.5F - half * y * y);
		y = y * (1.5F - half * y * y);
		return f * y;
	}

	public static float sqrt(final float f, final float half, final int repeats) {
		float y = Float.intBitsToFloat(0x5f375a86 - (Float.floatToIntBits(f) >> 1));
		for (int i = 0; i < repeats; ++i) {
			y = y * (1.5F - half * y * y);
		}
		return f * y;
	}

	public static int sqr(final int x) {
		return x * x;
	}

	public static float sqr(final float f) {
		return f * f;
	}

	public static int[] parseIntegers(final char[] input, final char separator, final int expLen) {
		final int[] res = new int[expLen];
		int temp = 0;
		int index = 0;
		boolean n = input[0] == '-';
		if (n) {
			index++;
		}
		for (int i = 0; i < input.length; ++i) {
			final char c = input[i];
			if (c == separator) {
				res[index++] = n ? -temp : temp;
				temp = 0;
				n = input[i + 1] == '-';
				continue;
			}
			temp *= 10;
			temp += c - '0';
		}
		return res;
	}

	public static int fastParseInt(final String input) {
		if (input == null || input.length() == 0) {
			return 0;
		}
		final char[] charArray = input.toCharArray();
		final boolean n = charArray[0] == '-';
		int res = 0;
		for (int i = n ? 1 : 0; i < charArray.length; ++i) {
			final char c = charArray[i];
			if (c < '0' || c > '9') {
				break;
			}
			res *= 10;
			res += c - '0';
		}
		return n ? -res : res;
	}

	public static float fastParseFloat(final char[] input, int index, final int end) {
		if (input.length == 0) {
			return 0;
		}
		final boolean n = input[0] == '-';
		if (n) {
			++index;
		}
		float res = 0;
		for (; index < end; ++index) {
			final char c = input[index];
			if ('.' == c) {
				float f = 1F;
				for (; ++index < end;) {
					final char d = input[index];
					if (d < '0' || d > '9') {
						break;
					}
					f *= 0.1F;
					res += f * (d - '0');
				}
				break;
			} else if (c < '0' || c > '9') {
				break;
			}
			res *= 10;
			res += c - '0';
		}
		return n ? -res : res;
	}

	static {
		SIN = new float[65536];
		for (int i = 0; i < 65536; ++i) {
			MathHelper.SIN[i] = (float) Math.sin(i * 3.14159265358979323846D * 2.0 / 65536.0);
		}
		ZERO = '0';
		FLOAT_PI = 3.14159265358979323846F;
		ATAN2 = new float[16384];
		for (int i = 0; i < 128; i++) {
			for (int j = 0; j < 128; j++) {
				final float x0 = i / (128 + .0F);
				final float y0 = j / (128 + .0F);
				MathHelper.ATAN2[j * 128 + i] = (float) Math.atan2(y0, x0);
			}
		}
	}
}