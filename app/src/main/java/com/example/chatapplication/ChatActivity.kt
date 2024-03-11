package com.example.chatapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.ml.Model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.FloatBuffer
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    //private lateinit var speechToText: Button
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var sendButton: ImageView
    private lateinit var emotion: TextView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference


    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val name = intent.getStringExtra("name")
        val receiverUid= intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        //speechToText = findViewById(R.id.speechToText)
        //textToSpeech = findViewById(R.id.textToSpeech)
        //emotion = findViewById(R.id.emotion)
        sendButton = findViewById(R.id.sentButton)
        messageList = ArrayList()
        messageAdapter= MessageAdapter(this,messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        //logic for adding data to recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("message")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for(postSnapshot in snapshot.children){

                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)

                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        //adding the message to database
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val senderMessageObject = Message(message, senderUid)
            val receiverMessageObject = Message(message, senderUid) // Assuming senderUid should be receiverUid here

            mDbRef.child("chats").child(senderRoom!!).child("message").push()
                .setValue(senderMessageObject).addOnSuccessListener {
                    // Once sender's message is sent successfully, send the message to the receiver
                    mDbRef.child("chats").child(receiverRoom!!).child("message").push()
                        .setValue(receiverMessageObject).addOnSuccessListener {
                            // Clear the message box once message is sent to both sender and receiver
                            messageBox.setText("")
                        }
                }
        }

//        val speechToText = findViewById<Button>(R.id.speechToText)
//        speechToText.setOnClickListener{
//            editText.text = null
//
//        }
        val textToSpeechBtn = findViewById<Button>(R.id.textToSpeechBtn)

        textToSpeech = TextToSpeech(this){
                status ->
            if(status== TextToSpeech.SUCCESS){
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Toast.makeText(this, "language is not supported", Toast.LENGTH_LONG).show()
                }
            }
        }

        textToSpeechBtn.setOnClickListener {
            // Assuming encodedInput is a FloatArray or FloatList containing the encoded text






//            val model = Model.newInstance(this)
//
//            // Creates inputs for reference.
//            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 66), DataType.FLOAT32)
//            inputFeature0.loadBuffer(byteBuffer)
//
//            // Runs model inference and gets result.
//            val outputs = model.process(inputFeature0)
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//            // Releases model resources if no longer used.
//            model.close()



            val latestMessage = messageList.lastOrNull()


            if (latestMessage != null ) {
                textToSpeech.speak(
                    latestMessage.message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            } else {
                Toast.makeText(this, "No message to read", Toast.LENGTH_SHORT).show()
            }
        }

        val speechToTextBtn = findViewById<Button>(R.id.speechToTextBtn)
        //val message = messageBox.text.toString()
        speechToTextBtn.setOnClickListener {
            messageBox.text = null
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something")
                result.launch(intent)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }


    }



    val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if(result.resultCode == Activity.RESULT_OK){
            val results = result.data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            ) as ArrayList<String>

            messageBox.setText(results[0])
        }
    }
}
