package ca.bcit.comp3717.guardian.controller;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.adapter.LinkedAccountAdapter;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.model.LinkedUser;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.UserBuilder;

public class LinkedAccountActivity extends AppCompatActivity {

    private User user;
    private AlertDialog addLinkedUserDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_account);

        if (savedInstanceState == null) {
            user = UserBuilder.constructUserFromIntent(getIntent());
            new GetLinkedUsersTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("userId", user.getId());
        outState.putString("userName", user.getUserName());
        outState.putString("email", user.getEmail());
        outState.putString("password", user.getPassword());
        outState.putString("phoneNumber", user.getPhone());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        user = new User();
        user.setId(savedInstanceState.getInt("userId"));
        user.setUserName(savedInstanceState.getString("userName"));
        user.setEmail(savedInstanceState.getString("email"));
        user.setPassword(savedInstanceState.getString("password"));
        user.setPhone(savedInstanceState.getString("phoneNumber"));
        new GetLinkedUsersTask().execute();
    }

    public void back (View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("userName", user.getUserName());
        i.putExtra("password", user.getPassword());
        i.putExtra("email", user.getEmail());
        i.putExtra("phoneNumber", user.getPhone());
        startActivity(i);
    }

//    public void addLinkedUser (View view) {
//        // Create custom dialog object
//        final Dialog dialog = new Dialog(LinkedAccountActivity.this);
//        // Include dialog.xml file
//        dialog.setContentView(R.layout.add_linked_user);
//        // Set dialog title
//        dialog.setTitle("Add User");
//
//        dialog.show();
//        Button addUser = (Button) dialog.findViewById(R.id.sendRequestBtn);
//        addUser.setOnClickListener(new Button.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                addUser(dialog);
//            }
//        });
//
//    }

    private void displayAddLinkedUserDialog(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(LinkedAccountActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_linked_user, null);

        final EditText etTargetEmail = dialogView.findViewById(R.id.editText_dialogAddLinkedUser_email);
        Button btnCancel = dialogView.findViewById(R.id.button_dialogAddLinkedUser_cancel);
        Button btnAdd = dialogView.findViewById(R.id.button_dialogAddLinkedUser_add);

        mBuilder.setView(dialogView);
        addLinkedUserDialog = mBuilder.create();
        addLinkedUserDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLinkedUserDialog.dismiss();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new AddLinkedUserTask(etTargetEmail.getText().toString()).execute();
            }
        });
    }

    private void displayLinkedUsers(List<LinkedUser> linkedUsersList) {
        if (linkedUsersList != null) {
            ListView listView = (ListView) findViewById(R.id.listView_linkedAccount);
            LinkedAccountAdapter adapter = new LinkedAccountAdapter(
                    LinkedAccountActivity.this, R.layout.listview_5column, R.drawable.ic_close_black_24dp, linkedUsersList);
            listView.setAdapter(adapter);
        }
    }

    private class GetLinkedUsersTask extends AsyncTask<Void, Void, List<LinkedUser>> {
        @Override
        protected List<LinkedUser> doInBackground(Void... voidArgs) {
            return HttpHandler.LinkedUserController.getLinkedUsersById(user.getEmail(), user.getPassword(), user.getId());
        }

        @Override
        protected void onPostExecute(List<LinkedUser> linkedUsersList) {
            super.onPostExecute(linkedUsersList);
            displayLinkedUsers(linkedUsersList);
        }
    }

    private class AddLinkedUserTask extends AsyncTask<Void, Void, Void> {
        private String targetUserEmail;

        public AddLinkedUserTask(String targetUserEmail) {
            this.targetUserEmail = targetUserEmail;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
