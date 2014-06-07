package im;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import tools.AudioRecoderManager;
import tools.Logger;
import tools.MD5Util;
import im.model.IMMessage;
import bean.JsonMessage;

import com.donal.wechat.R;

import config.CommonValue;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MessageBubble extends LinearLayout implements OnClickListener{
	private Context context;
	
	private IMMessage message;
	private int messageType;
	private int mediaType;

	public TextView timeTV;
	
	public RelativeLayout leftLayout;
	public ImageView leftAvatar;
	public TextView leftNickname;
	public TextView leftText;
	public ImageView leftPhoto;
	public TextView leftVoice;
	
	public RelativeLayout rightLayout;
	public RelativeLayout rightFrame;
	public ImageView rightAvatar;
	public TextView rightNickname;
	public TextView rightText;
	public ImageView rightPhoto;
	public TextView photoProgress;
	public ProgressBar rightProgress;
	public TextView rightVoice;
	
	//语音相关
	private AnimationDrawable mAnimationDrawable;// 播放时的波纹动画
	private VoiceBubbleListener voiceBubbleListener;
	private MediaPlayer mMediaPlayer;
	private String mUrl;
	private boolean mIsprepared;// 是否准备好了，（文件已经下载下来，且MediaPlayer可以播放）
	private int mId;
	private boolean isDownloading;// 如果正在下载，则不再触发下载
	private boolean playAfterDownload;// 播放时本地缓存不存在，触发下载，下载完成后是否播放(可能下载过程中，触发了其它语音的播放，则此标志置为false)
	private Handler bubbleHandler;
	private static final int PREPARE_VIEW = 110;
	private static final int START_VIEW = 111;
	private static final int STOP_VIEW = 112;
	private static final int ERROR_VIEW = 113;

	
	public MessageBubble(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public MessageBubble(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public MessageBubble(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		Logger.i("init");
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.listviewcell_chat_normal, null);
		timeTV = (TextView) convertView.findViewById(R.id.textview_time);
		leftLayout = (RelativeLayout) convertView.findViewById(R.id.layout_left);
		leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
		leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
		leftText = (TextView) convertView.findViewById(R.id.textview_content_l);
		leftPhoto = (ImageView) convertView.findViewById(R.id.photo_content_l);
		leftVoice = (TextView) convertView.findViewById(R.id.receiverVoiceNode);		
		
		rightLayout = (RelativeLayout) convertView.findViewById(R.id.layout_right);
		rightFrame = (RelativeLayout) convertView.findViewById(R.id.layout_content_r);
		rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
		rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
		rightText = (TextView) convertView.findViewById(R.id.textview_content_r);
		rightPhoto = (ImageView) convertView.findViewById(R.id.photo_content_r);
		photoProgress = (TextView) convertView.findViewById(R.id.photo_content_progress);
		rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
		rightVoice = (TextView) convertView.findViewById(R.id.senderVoiceNode);
		
		leftLayout.setOnClickListener(this);
		rightLayout.setOnClickListener(this);

		bubbleHandler = new BubbleHandler();
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mIsprepared) {
					bubbleHandler.sendEmptyMessage(STOP_VIEW);
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playCompletion(MessageBubble.this);
					}
				} else {
					bubbleHandler.sendEmptyMessage(ERROR_VIEW);
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playFail(MessageBubble.this);
					}
				}
			}
		});
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				bubbleHandler.sendEmptyMessage(ERROR_VIEW);
				if (voiceBubbleListener != null) {
					voiceBubbleListener.playFail(MessageBubble.this);
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
					bubbleHandler.sendEmptyMessage(STOP_VIEW);
				}
			}
		});
		this.addView(convertView);
	}
	
	public void setVoiceBubbleListener(VoiceBubbleListener voiceBubbleListener) {
		this.voiceBubbleListener = voiceBubbleListener;
	}

	public void setIMMessage(final IMMessage message) {
		this.message = message;
		this.messageType = message.getMsgType();
		try {
			final JsonMessage msg = JsonMessage.parse(message.getContent());
			this.mediaType = msg.messageType;
			if (msg.messageType == CommonValue.kWCMessageTypePlain) {
			}
			else if (msg.messageType == CommonValue.kWCMessageTypeImage) {
			}
			else if (msg.messageType == CommonValue.kWCMessageTypeVoice) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						File soundFile;
						if (message.getType() == 0) {
							soundFile = new File(AudioRecoderManager.CACHE_VOICE_FILE_PATH + MD5Util.getMD5String(msg.file));
						}
						else {
							soundFile = new File(msg.file);
						}
						if (soundFile.exists()) {
							doPrepare(soundFile);
						}
						else {
							mUrl = msg.file;
						}
					}
				}).start();
			}
		} catch (Exception e) {
			this.mediaType = CommonValue.kWCMessageTypePlain;
		}
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
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			doStop();
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
			bubbleHandler.sendEmptyMessage(ERROR_VIEW);
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playFail(MessageBubble.this);
			}
		}
		return false;
	}
	
	/**
	 * 开始或暂停播放
	 */
	private void startStopPlay() {
		if (mMediaPlayer.isPlaying()) {
			Logger.i("a");
			doStop();
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playStoped(MessageBubble.this);
			}
		} else {
			Logger.i("b");
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playStart(MessageBubble.this);
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
					bubbleHandler.sendEmptyMessage(ERROR_VIEW);
					if (voiceBubbleListener != null) {
						voiceBubbleListener.playFail(MessageBubble.this);
					}
				}
			}
		}
	}
	
	private void doPlay() {
		bubbleHandler.sendEmptyMessage(START_VIEW);
		try {
			Logger.i("play");
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
			bubbleHandler.sendEmptyMessage(ERROR_VIEW);
			if (voiceBubbleListener != null) {
				voiceBubbleListener.playFail(MessageBubble.this);
			}
		}
	}

	private void doStop() {
		bubbleHandler.sendEmptyMessage(STOP_VIEW);
		mMediaPlayer.pause();
		mMediaPlayer.seekTo(0);
	}
	
	private class BubbleHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);

			switch (msg.what) {
			case START_VIEW:
				setPlayingView();
				break;
			case STOP_VIEW:
				setStopView();
				break;
			case PREPARE_VIEW:
				setLoadingView();
				break;
			case ERROR_VIEW:
				setErrorView();
				break;
			default:
				break;
			}
		}
	}

	private void setPlayingView() {
//		imgWave.setImageResource(R.anim.bubble_anim);
//		mAnimationDrawable = (AnimationDrawable) imgWave.getDrawable();
//		mAnimationDrawable.start();
//		progressBar.setVisibility(View.GONE);
//		layoutMain.setEnabled(true);
	}

	private void setStopView() {
//		if (mAnimationDrawable != null && mAnimationDrawable.isRunning()) {
//			mAnimationDrawable.stop();
//		}
//		imgWave.setImageResource(R.drawable.ic_bubble_normal);
//		layoutMain.setBackgroundResource(R.drawable.bg_bubble);
//		layoutMain.setEnabled(true);
//		progressBar.setVisibility(View.GONE);
//		imgError.setVisibility(View.GONE);
	}

	private void setErrorView() {
//		imgError.setVisibility(View.VISIBLE);
//		progressBar.setVisibility(View.GONE);
//		if (mAnimationDrawable != null && mAnimationDrawable.isRunning()) {
//			mAnimationDrawable.stop();
//		}
//		imgWave.setImageResource(R.drawable.ic_bubble_normal);
//		layoutMain.setBackgroundResource(R.drawable.bg_bubble);
//		layoutMain.setEnabled(true);
	}

	private void setLoadingView() {
//		imgError.setVisibility(View.GONE);
//		progressBar.setVisibility(View.VISIBLE);
//		layoutMain.setEnabled(false);
	}
	
	private void downLoadPrepareFile(String url) {
		final File soundFile = new File(AudioRecoderManager.CACHE_VOICE_FILE_PATH + MD5Util.getMD5String(url) + ".amr");
		if (!soundFile.exists()) {
			if (!isDownloading) {
				bubbleHandler.sendEmptyMessage(PREPARE_VIEW);
				isDownloading = true;
				Logger.i(url);
//				ApiClent.downVoiceFromQiniu(context, url, ".amr", new ClientCallback() {
//					@Override
//					public void onSuccess(Object data) {
//						Logger.i("aaa");
//						doPrepare(new File((String)data));
//						isDownloading = false;
//					}
//					
//					@Override
//					public void onFailure(String message) {
//						Logger.i("aaa");
//						bubbleHandler.sendEmptyMessage(ERROR_VIEW);
//						isDownloading = false;
//					}
//					
//					@Override
//					public void onError(Exception e) {
//						Logger.i("aaa");
//						bubbleHandler.sendEmptyMessage(ERROR_VIEW);
//						isDownloading = false;
//					}
//				});
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
						bubbleHandler.sendEmptyMessage(ERROR_VIEW);
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
					bubbleHandler.sendEmptyMessage(ERROR_VIEW);
					isDownloading = false;
				}
			}
		} else {
			doPrepare(soundFile);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_left:
		case R.id.layout_right:
			switch (mediaType) {
			case CommonValue.kWCMessageTypePlain:
				break;
			case CommonValue.kWCMessageTypeImage:
				break;
			case CommonValue.kWCMessageTypeVoice:
				Logger.i("a");
				startStopPlay();
				break;
			default:
				break;
			}
			break;
		}
	}
}
