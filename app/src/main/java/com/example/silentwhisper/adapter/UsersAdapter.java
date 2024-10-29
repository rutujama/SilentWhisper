package com.example.silentwhisper.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silentwhisper.databinding.ItemContainerUsersBinding;
import com.example.silentwhisper.models.Users;

import java.util.List;




public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private final List<Users> users;

    public UsersAdapter(List<Users> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       ItemContainerUsersBinding itemContainerUsersBinding=ItemContainerUsersBinding.inflate(
               LayoutInflater.from(parent.getContext()),parent,false);

        return new UserViewHolder(itemContainerUsersBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
             holder.setUserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUsersBinding binding;
        UserViewHolder(ItemContainerUsersBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());  // Correct usage of getRoot() method
       binding = itemContainerUserBinding;
        }
        void setUserData(Users users)
        {
            binding.name.setText(users.name);
            binding.email.setText(users.email);
            binding.image.setImageBitmap(getUserimage(users.image));


        }


    }
    private Bitmap getUserimage(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes
        ,0,bytes.length);
    }


}
