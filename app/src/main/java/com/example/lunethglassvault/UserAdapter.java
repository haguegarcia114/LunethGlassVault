package com.example.lunethglassvault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.realm.Realm;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        if (user == null || !user.isValid()) return;

        String uuid = user.getUuid();
        String fullName = user.getFullName();
        String role = user.getRole();
        String imageUri = user.getImageUri();

        holder.tvUserName.setText(fullName);
        holder.tvUserRole.setText("Role: " + role);
        holder.tvUserUuid.setText("ID: " + uuid);

        if (imageUri != null && !imageUri.isEmpty()) {
            holder.imgUserAvatar.setImageURI(Uri.parse(imageUri));
        }

        // edits the user details
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, RegisterUserActivity.class);
            intent.putExtra("userUuid", uuid);
            context.startActivity(intent);
        });

        // delete user
        holder.btnDelete.setOnClickListener(v -> {
            Realm realm = Realm.getDefaultInstance();

            realm.executeTransaction(r -> {
                User userToDelete = r.where(User.class).equalTo("uuid", uuid).findFirst();
                if (userToDelete != null) {
                    userToDelete.deleteFromRealm();
                }
            });

            realm.close();

            Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();
        });

        // open photo local var
        holder.imgUserAvatar.setOnClickListener(v -> {
            if (imageUri != null && !imageUri.isEmpty()) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra(ImageActivity.EXTRA_IMAGE_URI, imageUri);
                intent.putExtra(ImageActivity.EXTRA_IMAGE_TITLE, fullName);
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRole, tvUserUuid;
        ImageView imgUserAvatar;
        Button btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserUuid = itemView.findViewById(R.id.tvUserUuid);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}