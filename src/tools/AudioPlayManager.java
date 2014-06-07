package tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import config.CommonValue;

public class AudioPlayManager {
	public static final String CACHE_VOICE_FILE_PATH = Environment.getExternalStorageDirectory() + "/donal/voice/";
	
	private static AudioPlayManager audioPlayManager = null;
	private static MediaPlayer mediaPlayer;
	
	private AudioPlayManager(Context context) {
		mediaPlayer = new MediaPlayer();
	}

	public static AudioPlayManager getInstance(Context context) {
		if (audioPlayManager == null) {
			audioPlayManager = new AudioPlayManager(context);
		}
		return audioPlayManager;
	}
	
	public static void destroy() {
		audioPlayManager = null;
		mediaPlayer = null;
	}
}
