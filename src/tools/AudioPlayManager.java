package tools;

import im.VoiceBubbleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import config.CommonValue;

public class AudioPlayManager {
	public static final String CACHE_VOICE_FILE_PATH = Environment.getExternalStorageDirectory() + "/donal/voice/";
	
	private View convertView;
	private static AudioPlayManager audioPlayManager = null;
	private static MediaPlayer mMediaPlayer;
	private static VoiceBubbleListener voiceBubbleListener;
	private String mUrl;
	private boolean mIsprepared;// 是否准备好了，（文件已经下载下来，且MediaPlayer可以播放）
	private int mId;
	private boolean isDownloading;// 如果正在下载，则不再触发下载
	private boolean playAfterDownload;// 播放时本地缓存不存在，触发下载，下载完成后是否播放(可能下载过程中，触发了其它语音的播放，则此标志置为false)
	
	private AudioPlayManager(Context context) {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mIsprepared) {
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playCompletion(convertView);
					}
				} else {
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playFail(convertView);
					}
				}
			}
		});
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				if (voiceBubbleListener != null) {
					voiceBubbleListener.playFail(convertView);
				}
				return false;
			}
		});
		mMediaPlayer.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
				return false;
			}
		});
		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				mIsprepared = true;
				if (playAfterDownload) {
					startStopPlay();
				} else {
				}
			}
		});
	}

	public static AudioPlayManager getInstance(Context c, VoiceBubbleListener listener) {
		if (audioPlayManager == null) {
			audioPlayManager = new AudioPlayManager(c);
			voiceBubbleListener = listener;
		}
		return audioPlayManager;
	}
	
	public static void destroy() {
		audioPlayManager.stopPlay();
		audioPlayManager = null;
		mMediaPlayer = null;
	}
	
	public void setConvertView(View v) {
		this.convertView = v;
	}
	
	public void setURL(String url) {
		mIsprepared = false;
		this.mUrl = url;
	}
	
	/**
	 * 判断是否正在播放
	 */
	public boolean isPlaying() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 停止播放
	 */
	public void stopPlay() {
		if (mMediaPlayer != null ) {
			if (mMediaPlayer.isPlaying()) {
				doStop();
			}
		} else {
			playAfterDownload = false;
		}
	}
	
	/**
	 * 准备播放
	 */
	private boolean doPrepare(File soundFile) {
		try {
			mMediaPlayer.reset();
			Logger.i(soundFile.getAbsolutePath());
			mMediaPlayer.setDataSource(soundFile.getAbsolutePath());
			mMediaPlayer.prepare();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playFail(convertView);
			}
		}
		return false;
	}
	
	/**
	 * 开始或暂停播放
	 */
	public void startStopPlay() {
		if (mMediaPlayer.isPlaying()) {
			Logger.i("a");
			doStop();
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playStoped(convertView);
			}
		} else {
			Logger.i("b");
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playStart(convertView);
			}
			if (mIsprepared) {
				Logger.i("c");
				doPlay();
			} else {
				Logger.i("d");
				if (mUrl != null && mUrl.length() > 0) {
					Logger.i("e");
					new Thread(new Runnable() {
						@Override
						public void run() {
							playAfterDownload = true;
							downLoadPrepareFile(mUrl);
						}
					}).start();

				} else {
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playFail(convertView);
					}
				}
			}
		}
	}
	
	private void doPlay() {
		try {
			Logger.i("play");
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playFail(convertView);
			}
		}
	}

	private void doStop() {
		mMediaPlayer.pause();
		mMediaPlayer.seekTo(0);
	}
	
	private void downLoadPrepareFile(String url) {
		final File soundFile = new File(AudioRecoderManager.CACHE_VOICE_FILE_PATH + MD5Util.getMD5String(url) + ".amr");
		if (!soundFile.exists()) {
			if (!isDownloading) {
				if (voiceBubbleListener != null) {
					voiceBubbleListener.playDownload(convertView);
				}
				isDownloading = true;
				Logger.i(url);
				try {
					isDownloading = true;
					if (!(new File(AudioRecoderManager.CACHE_VOICE_FILE_PATH)).exists()) {
						(new File(AudioRecoderManager.CACHE_VOICE_FILE_PATH)).mkdirs();
					}
					System.setProperty("http.keepAlive", "false");// 解决经常报此异常问题，at
																	// java.util.zip.GZIPInputStream.readFully(GZIPInputStream.java:214)
					URL Url = new URL(url);
					URLConnection conn = Url.openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					// this.fileSize = conn.getContentLength();// 根据响应获取文件大小
					// if (this.fileSize <= 0) { // 获取内容长度为0
					// throw new RuntimeException("无法获知文件大小 ");
					// }
					if (is == null) { // 没有下载流
						if (voiceBubbleListener != null) {
							voiceBubbleListener.playFail(convertView);
						}
					}
					FileOutputStream FOS = new FileOutputStream(soundFile); // 创建写入文件内存流，通过此流向目标写文件

					byte buf[] = new byte[1024];
					// downLoadFilePosition = 0;
					int numread;
					while ((numread = is.read(buf)) != -1) {
						FOS.write(buf, 0, numread);
						// downLoadFilePosition += numread
					}
					is.close();

					doPrepare(soundFile);
					isDownloading = false;
					FOS.flush();
					if (FOS != null) {
						FOS.close();
					}

				} catch (Exception e) {
					e.printStackTrace();
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playFail(convertView);
					}
					isDownloading = false;
				}
			}
		} else {
			doPrepare(soundFile);
		}
	}
	
}
