package com.example.silentwhisper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.silentwhisper.R
import com.example.silentwhisper.databinding.ActivityAddUsersBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

lateinit var abind:ActivityAddUsersBinding
class AddUsers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        abind=ActivityAddUsersBinding.inflate(layoutInflater)
        setContentView(abind.root)
        abind.backbtn.setOnClickListener {
            startActivity(Intent(this@AddUsers,FriendsPage::class.java))
            finish()
        }
        val adapter=GroupieAdapter()
        adapter.add(UserItem())
        adapter.add(UserItem())
        adapter.add(UserItem())
        abind.addRecyclerView.adapter=adapter
    }
}

class UserItem: Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //will implement
    }
    override fun getLayout(): Int {
        return R.layout.newuser_container
    }

}