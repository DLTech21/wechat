package tools;

import java.io.File;
import java.io.IOException;

import config.CommonValue;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;

public class AudioRecoderManager {

	public static final String CACHE_VOICE_FILE_PATH = Environment.getExternalStorageDirectory() + "/donal/voice/";
	
	private static int SAMPLE_RATE_IN_HZ = 8000; 
	private static AudioRecoderManager audioRecoderManager = null;
	private static MediaRecorder mediaRecorder = null;
	private String voicePath = null;
	private String userId;
	
	private AudioRecoderManager(Context context) {
		mediaRecorder = new MediaRecorder();
		SharedPreferences sharedPre = context.getSharedPreferences(
				CommonValue.LOGIN_SET, Context.MODE_PRIVATE);
		userId = sharedPre.getString(CommonValue.USERID, null);
	}

	public static AudioRecoderManager getInstance(Context context) {
		if (audioRecoderManager == null) {
			audioRecoderManager = new AudioRecoderManager(context);
		}
		return audioRecoderManager;
	}
	
	public static void destroy() {
		audioRecoderManager = null;
		mediaRecorder = null;
	}
	
	public void start() throws IllegalStateException, IOException {
		File dir = new File(CACHE_VOICE_FILE_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		voicePath = CACHE_VOICE_FILE_PATH + userId + "-" +System.currentTimeMillis() + ".amr";
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mediaRecorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
		mediaRecorder.setOutputFile(voicePath);
		mediaRecorder.prepare();
		mediaRecorder.start();
	}
	
	public String stop() throws IOException {
		mediaRecorder.stop();
		mediaRecorder.release();
		return voicePath;
	}
	
	public int getAmplitude() {		
		if (mediaRecorder != null){			
			return mediaRecorder.getMaxAmplitude();		
		}		
		else {		
			return 0;
		}
	}
}
