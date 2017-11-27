package ca.bcit.comp3717.guardian.controller;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.adapter.LinkedUsersAdapter;
import ca.bcit.comp3717.guardian.adapter.LinkedUserRequestsAdapter;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.model.LinkedUser;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.DialogBuilder;
import ca.bcit.comp3717.guardian.util.UserBuilder;
import ca.bcit.comp3717.guardian.util.UserValidation;

public class LinkedAccountActivity extends AppCompatActivity {

    private User user;
    private List<LinkedUser> linkedUsersList;
    private Dialog loadingDialog;
    private AlertDialog addLinkedUserDialog;
    private AlertDialog alertDialog;
    private String TAG = LinkedAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_account);

        if (savedInstanceState == null) {
            user = UserBuilder.constructUserFromIntent(getIntent());
            loadingDialog = DialogBuilder.constructLoadingDialog(LinkedAccountActivity.this,
                    R.layout.dialog_loading);
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

    public static void testMethod() {

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
        Button btnSendRequest = dialogView.findViewById(R.id.button_dialogAddLinkedUser_sendRequest);

        mBuilder.setView(dialogView);
        addLinkedUserDialog = mBuilder.create();
        addLinkedUserDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLinkedUserDialog.dismiss();
            }
        });

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLinkedUserRequest(addLinkedUserDialog, etTargetEmail);
            }
        });
    }

    private void addLinkedUserRequest(Dialog d, EditText email) {
        boolean validEmail = UserValidation.validateAddLinkedUserInputEmail(email);

        if (validEmail) {
            addLinkedUserDialog.dismiss();
            new AddLinkedUserTask(email.getText().toString()).execute();

        } else {
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "Invalid Email!", "Email must not be blank.");
            alertDialog.show();
            Log.e(TAG, "Invalid Email! Email must not be blank.");
        }
    }

    private void addLinkedUserResponse(boolean addSuccess) {
        if (addSuccess) {
            new GetLinkedUsersTask().execute();
            Toast.makeText(LinkedAccountActivity.this, "Request sent", Toast.LENGTH_SHORT).show();

        } else {
            addLinkedUserDialog.show();
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "No User Found!", "Cannot find a user with that email.");
            alertDialog.show();
            Log.e(TAG, "No User Found! Cannot find a user with that email.");
        }
    }

    private List<LinkedUser> constructLinkedUserRequestsGivenLinkedUserList(List<LinkedUser> luList) {
        List<LinkedUser> linkRequests = new ArrayList<>();

        for (LinkedUser lu : luList) {
            if (!lu.isAddedMe() && lu.isAddedTarget() && !lu.isDeleted()) {
                linkRequests.add(lu);
            }
        }
        return linkRequests.size() == 0 ? null : linkRequests;
    }

    private List<LinkedUser> constructLinkedUsersGivenLinkedUserList(List<LinkedUser> luList) {
        List<LinkedUser> linkedUsers = new ArrayList<>();

        for (LinkedUser lu : luList) {
            if (lu.isAddedMe() && lu.isAddedTarget() && !lu.isDeleted()) {
                linkedUsers.add(lu);
            }
        }
        return linkedUsers.size() == 0 ? null : linkedUsers;
    }

    private void displayLinkedUserLists(List<LinkedUser> luList) {
        if (luList != null) {
            List<LinkedUser> linkedUserRequests = constructLinkedUserRequestsGivenLinkedUserList(luList);
            List<LinkedUser> linkedUsers = constructLinkedUsersGivenLinkedUserList(luList);

            if (linkedUserRequests == null) {
                LinearLayout llLinkedUserRequests = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUserRequests);
                llLinkedUserRequests.setVisibility(View.GONE);

            } else {
                displayLinkedUserRequests(linkedUserRequests);
            }

            if (linkedUsers == null) {
                LinearLayout llLinkedUsers = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUsers);
                llLinkedUsers.setVisibility(View.GONE);

                TextView tvNoLinkedUsers = (TextView) findViewById(R.id.textView_linkedAccount_noLinkedUsers);
                tvNoLinkedUsers.setVisibility(View.VISIBLE);

            } else {
                displayLinkedUsers(linkedUsers);
            }
        }
    }

    private void displayLinkedUserRequests(List<LinkedUser> linkedUserRequests) {
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

    private void displayLinkedUsers(List<LinkedUser> linkedUsers) {
        Collections.sort(linkedUsers);

        LinearLayout llLinkedUsers = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUsers);
        llLinkedUsers.setVisibility(View.VISIBLE);
        TextView tvNoLinkedUsers = (TextView) findViewById(R.id.textView_linkedAccount_noLinkedUsers);
        tvNoLinkedUsers.setVisibility(View.GONE);

        ListView linkedUsersListView = (ListView) findViewById(R.id.listView_linkedAccount_linkedUsers);

        LinkedUsersAdapter linkedUsersAdapter = new LinkedUsersAdapter(
                LinkedAccountActivity.this, R.layout.listview_5column, R.drawable.ic_delete_red, linkedUsers);
        linkedUsersListView.setAdapter(linkedUsersAdapter);

        linkedUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "parent: " + parent.toString());
                Log.e(TAG, "view: " + view.toString());
                Log.e(TAG, "position: " + position);
                Log.e(TAG, "id: " + id);
            }
        });
    }

    private class GetLinkedUsersTask extends AsyncTask<Void, Void, List<LinkedUser>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected List<LinkedUser> doInBackground(Void... voidArgs) {
            return HttpHandler.LinkedUserController.getLinkedUsersById(user.getEmail(), user.getPassword(), user.getId());
        }

        @Override
        protected void onPostExecute(List<LinkedUser> luList) {
            super.onPostExecute(luList);
            linkedUsersList = luList;
            displayLinkedUserLists(luList);
            loadingDialog.dismiss();
        }
    }

    private class AddLinkedUserTask extends AsyncTask<Void, Void, Boolean> {
        private String targetEmail;

        public AddLinkedUserTask(String targetEmail) {
            this.targetEmail = targetEmail;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            User targetUser = HttpHandler.UserController.getUserByEmail(user.getEmail(),
                    user.getPassword(), this.targetEmail);

            if (targetUser != null) {
                return HttpHandler.LinkedUserController.addLinkedUser(user.getEmail(), user.getPassword(),
                        user.getId(), targetUser.getId());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean addSuccess) {
            super.onPostExecute(addSuccess);
            addLinkedUserResponse(addSuccess);
            loadingDialog.dismiss();
        }
    }
}
