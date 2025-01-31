package plugin.videotrimmingeditor.widget;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * author : J.Chou
 * e-mail : who_know_me@163.com
 * time   : 2018/10/20 11:22 AM
 * version: 1.0
 * description:
 */
public class ZVideoView extends VideoView {
  private int mVideoWidth = 480;
  private int mVideoHeight = 480;
  private int videoRealW = 1;
  private int videoRealH = 1;

  public ZVideoView(Context context) {
    super(context);
  }

  public ZVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void setVideoURI(Uri uri) {
    super.setVideoURI(uri);
    MediaMetadataRetriever retr = new MediaMetadataRetriever();
    retr.setDataSource(uri.getPath());
    String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
    String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
    try {
      videoRealH = Integer.parseInt(height);
      videoRealW = Integer.parseInt(width);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getDefaultSize(0, widthMeasureSpec);
    int height = getDefaultSize(0, heightMeasureSpec);

    Log.i("[VideoSize]", "Initial width: " + width + ", height: " + height);

    if (height > width) {
      Log.i("[VideoSize]", "Orientation: Portrait");
      if (videoRealH > videoRealW) {
        mVideoHeight = height;
        float r = (float) videoRealW / videoRealH;
        mVideoWidth = (int) (mVideoHeight * r);
        Log.i("[VideoSize]", "Video is portrait, height fill, width adjust. mVideoWidth: " + mVideoWidth + ", mVideoHeight: " + mVideoHeight);
      } else {
        mVideoWidth = width;
        float r = videoRealH / (float) videoRealW;
        mVideoHeight = (int) (mVideoWidth * r);
        Log.i("[VideoSize]", "Video is landscape, width fill, height adjust. mVideoWidth: " + mVideoWidth + ", mVideoHeight: " + mVideoHeight);
      }
    } else {
      // 横屏
      Log.i("[VideoSize]", "Orientation: Landscape");
      if (videoRealH > videoRealW) {
        mVideoHeight = height;
        float r = videoRealW / (float) videoRealH;
        mVideoWidth = (int) (mVideoHeight * r);
        Log.i("[VideoSize]", "Video is portrait, height fill, width adjust. mVideoWidth: " + mVideoWidth + ", mVideoHeight: " + mVideoHeight);
      } else {
        mVideoWidth = width;
        float r = (float) videoRealH / videoRealW;
        mVideoHeight = (int) (mVideoWidth * r);
        Log.w("[VideoSize]", "Video is landscape, width fill, height adjust. mVideoWidth: " + mVideoWidth + ", mVideoHeight: " + mVideoHeight);
      }
    }

    if (videoRealH == videoRealW && videoRealH == 1) {
      // 没能获取到视频真实的宽高，自适应就可以了，什么也不用做
      Log.i("[VideoSize]", "Could not get video real size, use default.");
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    } else {
      setMeasuredDimension(mVideoWidth, mVideoHeight);
      Log.i("[VideoSize]", "Final dimensions: mVideoWidth: " + mVideoWidth + ", mVideoHeight: " + mVideoHeight);
    }

  }

}
