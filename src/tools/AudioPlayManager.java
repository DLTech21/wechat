package tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Environment;
import config.CommonValue;

public class AudioPlayManager {
	public static final String CACHE_VOICE_FILE_PATH = Environment.getExternalStorageDirectory() + "/donal/voice/";
	
	private static AudioPlayManager audioPlayManager = null;
	private static MediaPlayer mediaPlayer;
	
	private AudioPlayManager(Context context) {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				
			}
		});
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
	
	public static void startOrStop() {
		
	}
}
