package xmpp;

public class TransferUtils {
	public static String calculateSpeed(long bytediff, long timediff) {
		double kB = calculateSpeedLong(bytediff, timediff);

		if ((bytediff == 0L) && (timediff == 0L)) {
			return "";
		}
		if (kB < 1024.0D) {
			String KB = Double.toString(kB);

			KB = splitAtDot(KB, 1);

			return KB + "kB/s";
		}
		String MB = Double.toString(kB / 1024.0D);

		MB = splitAtDot(MB, 1);

		return MB + "MB/s";
	}

	public static double calculateSpeedLong(long bytediff, long timediff) {
		timediff = timediff == 0L ? 1L : timediff;
		double kB = bytediff / timediff * 1000.0D / 1024.0D;
		return kB;
	}

	public static String calculateEstimate(long currentsize, long totalsize,
			long timestart, long timenow) {
		long timediff = timenow - timestart;
		long sizeleft = totalsize - currentsize;

		currentsize = currentsize == 0L ? 1L : currentsize;
		long x = sizeleft * timediff / currentsize;

		x /= 1000L;

		return convertSecondstoHHMMSS(Math.round((float) x));
	}

	public static String convertSecondstoHHMMSS(int second) {
		int hours = Math.round(second / 3600);
		int minutes = Math.round(second / 60 % 60);
		int seconds = Math.round(second % 60);
		String hh = "" + hours;
		String mm = "" + minutes;
		String ss = "" + seconds;

		return "(" + hh + ":" + mm + ":" + ss + ")";
	}

	public static String getAppropriateByteWithSuffix(long bytes) {
		if (bytes >= 1099511627776L) {
			String x = splitAtDot("" + bytes / 1099511627776L, 2);
			return x + " TB";
		}
		if (bytes >= 1073741824L) {
			String x = splitAtDot("" + bytes / 1073741824L, 2);
			return x + " GB";
		}
		if (bytes >= 1048576L) {
			String x = splitAtDot("" + bytes / 1048576L, 2);
			return x + " MB";
		}
		if (bytes >= 1024L) {
			String x = splitAtDot("" + bytes / 1024L, 2);
			return x + " KB";
		}
		return bytes + " B";
	}

	private static String splitAtDot(String string, int significantdigits) {
		if (string.contains(".")) {
			String s = string.replace(".", "T").split("T")[1];

			if (s.length() >= significantdigits) {
				return string.substring(0, string.indexOf(".") + 1
						+ significantdigits);
			}

			return string.substring(0, string.indexOf(".") + 1 + s.length());
		}

		return string;
	}
}