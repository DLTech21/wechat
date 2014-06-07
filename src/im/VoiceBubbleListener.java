package im;


public interface VoiceBubbleListener {

	public void playFail(MessageBubble messageBubble);

	public void playStoped(MessageBubble messageBubble);

	public void playStart(MessageBubble messageBubble);

	public void playCompletion(MessageBubble messageBubble);
	
}
