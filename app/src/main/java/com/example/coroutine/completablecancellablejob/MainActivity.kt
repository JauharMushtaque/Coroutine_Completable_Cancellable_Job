package com.example.coroutine.completablecancellablejob

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {
    private lateinit var job:CompletableJob
    private val PROGRESS_START=0
    private val PROGRESS_MAX=100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job_button.setOnClickListener {
            if(!::job.isInitialized){
                initJob()
            }
            job_progress_bar.startJobOrCancelJob()

        }
    }

    private fun ProgressBar.startJobOrCancelJob(){
        if(this.progress>0){
            resetJob()
        }else{
            job_button.setText("Cancel Job #1")
            CoroutineScope(IO+job).launch {
                for(i in PROGRESS_START..PROGRESS_MAX){
                    delay(40)
                    this@startJobOrCancelJob.progress=i
                }
                updateJobCompleteTextView("Job is complete!")
            }
        }

    }

    private fun resetJob(){
        if(job.isActive || job.isCompleted){
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    private fun initJob(){
        job_button.setText("Start Job #1")
        updateJobCompleteTextView("")
        job= Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg=it
                if(msg.isNullOrBlank()){
                    msg="Unknown cancellation error."
                }
                showToast(msg)
            }
        }

        job_progress_bar.max=100
        job_progress_bar.progress=0
    }

    private fun updateJobCompleteTextView(text:String){
        GlobalScope.launch(Main) {
            job_complete_text.setText(text)
        }
    }

    private fun showToast(text:String){
        GlobalScope.launch(Main) {
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}