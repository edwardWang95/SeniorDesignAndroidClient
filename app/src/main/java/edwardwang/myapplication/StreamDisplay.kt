package edwardwang.myapplication

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.VideoView

import kotlinx.android.synthetic.main.activity_stream_display.*
import kotlinx.android.synthetic.main.content_stream_display.*

class StreamDisplay : AppCompatActivity() {

    private var videoView: VideoView? = null;
/*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream_display)
        setSupportActionBar(toolbar)

        videoView = findViewById<VideoView>(R.id.videoView)
        videoView?.setVideoURI(Uri.pars)
        configureVideoView()
    }

    private fun configureVideoView()
    {
        videoView.setVideoPath()
        videoView.start()
    }
    */

}
