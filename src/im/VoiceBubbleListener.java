package im;

import android.view.View;


public interface VoiceBubbleListener {

	public void playFail(View messageBubble);

	public void playStoped(View messageBubble);

	public void playStart(View messageBubble);

	public void playDownload(View messageBubble);
	
	public void playCompletion(View messageBubble);
	
	
}
