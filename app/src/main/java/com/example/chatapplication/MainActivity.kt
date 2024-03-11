package com.example.chatapplication

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity() : AppCompatActivity() {

    private lateinit var userRecylerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        adapter = UserAdapter(this,userList)

        userRecylerView = findViewById(R.id.userRecyclerView)

        userRecylerView.layoutManager = LinearLayoutManager(this)
        userRecylerView.adapter = adapter


        mDbRef.child("user").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for(postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if(mAuth.currentUser?.uid != currentUser?.uid){
                        userList.add(currentUser!!)
                    }

                }
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }


        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
//            if (item.itemId == R.id.vibutton) {
//                val isChecked = item.isChecked
//                handleVisuallyImpairedSetting(isChecked)
//                return true
//            }
            // Handle other menu items
            if(item.itemId == R.id.logout){
                //write the logic for logout
                mAuth.signOut()
                val intent = Intent(this@MainActivity,Login::class.java)
                finish()
                startActivity(intent)
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun handleVisuallyImpairedSetting(isChecked: Boolean): Boolean {
            if (isChecked) {
                // Apply visually impaired settings
                // ... your code here ...

                // Show notification
                Toast.makeText(this@MainActivity, "Visually Impaired Setting is ON", Toast.LENGTH_SHORT).show()
                return true
            } else {
                // Disable visually impaired settings
                // ... your code here ...
                return false

            }

        }}


//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        return true
//    }
//}