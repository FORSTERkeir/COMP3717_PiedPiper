package ca.bcit.comp3717.guardian.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.adapter.LinkedUsersAdapter;
import ca.bcit.comp3717.guardian.adapter.LinkedUserRequestsAdapter;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.model.LinkedUser;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.UserBuilder;

public class LinkedAccountActivity extends AppCompatActivity {

    private User user;
    private AlertDialog addLinkedUserDialog;
//    private List<LinkedUser> linkedUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_account);

        if (savedInstanceState == null) {
            user = UserBuilder.constructUserFromIntent(getIntent());
            new GetLinkedUsersTask().execute();
            setFloatingActionButtonListener();
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

    private void setFloatingActionButtonListener() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FAB_linkedAccountActivity_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAddLinkedUserDialog();
            }
        });
    }

    public void displayAddLinkedUserDialog() {
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

    private List<LinkedUser> constructLinkedUserRequestsGivenLinkedUserList(List<LinkedUser> linkedUsersList) {
        List<LinkedUser> linkRequests = new ArrayList<>();

        for (LinkedUser lu : linkedUsersList) {
            if (!lu.isAddedMe() && lu.isAddedTarget() && !lu.isDeleted()) {
                linkRequests.add(lu);
            }
        }
        return linkRequests.size() == 0 ? null : linkRequests;
    }

    private List<LinkedUser> constructLinkedUsersGivenLinkedUserList(List<LinkedUser> linkedUsersList) {
        List<LinkedUser> linkedUsers = new ArrayList<>();

        for (LinkedUser lu : linkedUsersList) {
            if (lu.isAddedMe() && lu.isAddedTarget() && !lu.isDeleted()) {
                linkedUsers.add(lu);
            }
        }
        return linkedUsers.size() == 0 ? null : linkedUsers;
    }

    private void displayLinkedUsers(List<LinkedUser> linkedUsersList) {
        if (linkedUsersList != null) {
            List<LinkedUser> linkedUserRequests = constructLinkedUserRequestsGivenLinkedUserList(linkedUsersList);
            List<LinkedUser> linkedUsers = constructLinkedUsersGivenLinkedUserList(linkedUsersList);

            if (linkedUserRequests != null) {
                Collections.sort(linkedUserRequests);

                LinearLayout llLinkedUserRequests = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUserRequests);
                llLinkedUserRequests.setVisibility(View.VISIBLE);

                ListView linkedUserRequestsListView = (ListView) findViewById(R.id.listView_linkedAccount_linkedUserRequests);
                LinkedUserRequestsAdapter linkedUserRequestsAdapter = new LinkedUserRequestsAdapter(
                        LinkedAccountActivity.this, R.layout.listview_3column, linkedUserRequests);
                linkedUserRequestsListView.setAdapter(linkedUserRequestsAdapter);

                // adjust the list view height to half
                if (linkedUserRequests.size() > 7) {
                    LinearLayout llBody = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_body);
                    llLinkedUserRequests.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, llBody.getHeight() / 2));
                }
            }

            if (linkedUsers != null) {
                Collections.sort(linkedUsers);

                LinearLayout llLinkedUsers = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUsers);
                llLinkedUsers.setVisibility(View.VISIBLE);
                TextView tvNoLinkedUsers = (TextView) findViewById(R.id.textView_linkedAccount_noLinkedUsers);
                tvNoLinkedUsers.setVisibility(View.GONE);

                ListView linkedUsersListView = (ListView) findViewById(R.id.listView_linkedAccount_linkedUsers);

                LinkedUsersAdapter linkedUsersAdapter = new LinkedUsersAdapter(
                        LinkedAccountActivity.this, R.layout.listview_5column, R.drawable.ic_delete_red, linkedUsers);
                linkedUsersListView.setAdapter(linkedUsersAdapter);
            }
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
