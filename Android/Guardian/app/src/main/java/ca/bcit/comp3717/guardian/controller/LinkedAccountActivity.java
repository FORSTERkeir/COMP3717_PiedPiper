package ca.bcit.comp3717.guardian.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.model.LinkedUser;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.DialogBuilder;
import ca.bcit.comp3717.guardian.util.LinkedUserComparator;
import ca.bcit.comp3717.guardian.util.UserBuilder;
import ca.bcit.comp3717.guardian.util.UserValidation;

public class LinkedAccountActivity extends AppCompatActivity {

    private User user;
    private List<LinkedUser> deleteLinkedUserList;
    private List<LinkedUser> linkedUserRequestsDisplayList;
    private List<LinkedUser> linkedUsersDisplayList;
    private Dialog loadingDialog;
    private AlertDialog addLinkedUserDialog;
    private AlertDialog alertDialog;
    private LinkedUserRequestsAdapter linkedUserRequestsAdapter;
    private LinkedUsersAdapter linkedUsersAdapter;
    private String TAG = LinkedAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_account);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Guardians.ttf");

        Button tx = (Button) findViewById(R.id.backBtn);
        tx.setTypeface(custom_font);

        if (savedInstanceState == null) {
            user = UserBuilder.constructUserFromIntent(getIntent());
            loadingDialog = DialogBuilder.constructLoadingDialog(LinkedAccountActivity.this,
                    R.layout.dialog_loading);
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

    public void saveChanges(View view) {
        if (deleteLinkedUserList == null) {
            confirmLinkedUserRequests();
            updateGuiModifiedLinkedUsers();

        } else {
            deleteLinkedUserRequests();
            confirmLinkedUserRequests();
            updateGuiModifiedLinkedUsers();
        }
        Toast.makeText(LinkedAccountActivity.this, "Changes saved", Toast.LENGTH_SHORT).show();
    }

    private void confirmLinkedUserRequests() {
        for (LinkedUser linkedUser : linkedUsersDisplayList) {
            if (linkedUser.isConfirmedByGui()) {
                new AddLinkedUserByIdTask(linkedUser.getUserIdTarget()).execute();
            }
        }
    }

    private void deleteLinkedUserRequests() {
        for (LinkedUser linkedUser : deleteLinkedUserList) {
            new RemoveLinkedUserByIdTask(linkedUser.getUserIdTarget()).execute();
        }
    }

    private void updateGuiModifiedLinkedUsers() {
        for (LinkedUser linkedUser : linkedUsersDisplayList) {
            if (linkedUser.isAlertModifiedByGui()) {
                new SetLinkedUserAlertTask(linkedUser.getUserIdTarget(), linkedUser.isAlertMe()).execute();
                Log.e(TAG, linkedUser.getNameTarget() + " is modified by alert");
            }
            if (linkedUser.isMuteModifiedByGui()) {
                new SetLinkedUserMuteTask(linkedUser.getUserIdTarget(), linkedUser.isMuteMe()).execute();
                Log.e(TAG, linkedUser.getNameTarget() + " is modified by mute");
            }
        }
    }

    public void displayAddLinkedUserDialog(View v) {
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
                addLinkedUserByEmailRequest(addLinkedUserDialog, etTargetEmail);
            }
        });
    }

    private void addLinkedUserByEmailRequest(Dialog d, EditText email) {
        boolean validEmail = UserValidation.validateAddLinkedUserInputEmail(email);

        if (validEmail) {
            addLinkedUserDialog.dismiss();
            new AddLinkedUserByEmailTask(email.getText().toString()).execute();

        } else {
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "Invalid Email!", "Email must not be blank.");
            alertDialog.show();
            Log.e(TAG, "Invalid Email! Email must not be blank.");
        }
    }

    private void addLinkedUserByEmailResponse(boolean addSuccess) {
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

    private void addLinkedUserByIdResponse(boolean addSuccess) {
        if (!addSuccess) {
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "Failed to Confirm Linked User!", "Unable to save changes.");
            alertDialog.show();
            Log.e(TAG, "Failed to Confirm Linked User! Unable to save changes.");
        }
    }

    private void removeLinkedUserByIdResponse(boolean removeSuccess) {
        if (!removeSuccess) {
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "Failed to Remove Linked User!", "Unable to save changes.");
            alertDialog.show();
            Log.e(TAG, "Failed to Remove Linked User! Unable to save changes.");
        }
    }

    private void setLinkedUserAlertResponse(boolean setSuccess) {
        if (!setSuccess) {
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "Failed to set Linked User alert!", "Unable to save changes.");
            alertDialog.show();
            Log.e(TAG, "Failed to set Linked User alert! Unable to save changes.");
        }
    }

    private void setLinkedUserMuteResponse(boolean setSuccess) {
        if (!setSuccess) {
            alertDialog = DialogBuilder.constructAlertDialog(LinkedAccountActivity.this,
                    "Failed to set Linked User mute!", "Unable to save changes.");
            alertDialog.show();
            Log.e(TAG, "Failed to set Linked User mute! Unable to save changes.");
        }
    }










    private void getLinkedUsersResponse(List<LinkedUser> luList) {
        if (luList != null) {
            displayLinkedUserLists(luList);
        }
    }

    private void displayLinkedUserLists(List<LinkedUser> luList) {
        if (luList != null) {
            deleteLinkedUserList = new ArrayList<>();
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

    private void displayLinkedUserRequests(List<LinkedUser> linkedUserRequests) {
        Collections.sort(linkedUserRequests);
        linkedUserRequestsDisplayList = linkedUserRequests;
        removeDeletedLinkedUsersFromDisplayList();

        LinearLayout llLinkedUserRequests = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUserRequests);
        llLinkedUserRequests.setVisibility(View.VISIBLE);

        ListView linkedUserRequestsListView = (ListView) findViewById(R.id.listView_linkedAccount_linkedUserRequests);
        linkedUserRequestsAdapter = new LinkedUserRequestsAdapter(
                LinkedAccountActivity.this, R.layout.listview_3column, linkedUserRequestsDisplayList);
        linkedUserRequestsListView.setAdapter(linkedUserRequestsAdapter);

        // adjust the list view height to half
        if (linkedUserRequests.size() > 6) {
            LinearLayout llBody = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_body);
            llLinkedUserRequests.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (llBody.getHeight() / 2) - 100));
        }
    }

    private void removeDeletedLinkedUsersFromDisplayList() {
        if (deleteLinkedUserList != null) {

            for (int i = 0; i < deleteLinkedUserList.size(); i++) {
                int targetId = deleteLinkedUserList.get(i).getUserIdTarget();

                for (int j = 0; j < linkedUserRequestsDisplayList.size(); j++) {

                    if (linkedUserRequestsDisplayList.get(j).getUserIdTarget() == targetId) {
                        linkedUserRequestsDisplayList.remove(j);
                    }
                }
            }
        }
    }

    private void updatedLinkedUserRequestsLayout() {
        LinearLayout llLinkedUserRequests = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUserRequests);

        if (linkedUserRequestsDisplayList == null || linkedUserRequestsDisplayList.size() == 0) {
            llLinkedUserRequests.setVisibility(View.GONE);

        } else if (linkedUserRequestsDisplayList.size() < 6) {
            llLinkedUserRequests.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    private void displayLinkedUsers(List<LinkedUser> linkedUsers) {
        Collections.sort(linkedUsers);
        linkedUsersDisplayList = linkedUsers;

        LinearLayout llLinkedUsers = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUsers);
        llLinkedUsers.setVisibility(View.VISIBLE);
        TextView tvNoLinkedUsers = (TextView) findViewById(R.id.textView_linkedAccount_noLinkedUsers);
        tvNoLinkedUsers.setVisibility(View.GONE);

        ListView linkedUsersListView = (ListView) findViewById(R.id.listView_linkedAccount_linkedUsers);

        linkedUsersAdapter = new LinkedUsersAdapter(
                LinkedAccountActivity.this, R.layout.listview_5column, R.drawable.ic_delete_red, linkedUsersDisplayList);
        linkedUsersListView.setAdapter(linkedUsersAdapter);
    }

    private void updatedLinkedUsersLayout() {
        LinearLayout llLinkedUsers = (LinearLayout) findViewById(R.id.linearLayout_linkedAccount_linkedUsers);
        TextView tvNoLinkedUsers = (TextView) findViewById(R.id.textView_linkedAccount_noLinkedUsers);

        if (linkedUsersDisplayList == null || linkedUsersDisplayList.size() == 0) {
            llLinkedUsers.setVisibility(View.GONE);
            tvNoLinkedUsers.setVisibility(View.VISIBLE);

        } else {
            llLinkedUsers.setVisibility(View.VISIBLE);
            tvNoLinkedUsers.setVisibility(View.GONE);
        }
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
            getLinkedUsersResponse(luList);
            loadingDialog.dismiss();
        }
    }

    private class AddLinkedUserByEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String targetEmail;

        public AddLinkedUserByEmailTask(String targetEmail) {
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
            addLinkedUserByEmailResponse(addSuccess);
            loadingDialog.dismiss();
        }
    }

    private class AddLinkedUserByIdTask extends AsyncTask<Void, Void, Boolean> {
        private int targetId;

        public AddLinkedUserByIdTask(int targetId) {
            this.targetId = targetId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return HttpHandler.LinkedUserController.addLinkedUser(user.getEmail(), user.getPassword(),
                    user.getId(), this.targetId);
        }

        @Override
        protected void onPostExecute(Boolean addSuccess) {
            super.onPostExecute(addSuccess);
            addLinkedUserByIdResponse(addSuccess);
            loadingDialog.dismiss();
        }
    }

    private class RemoveLinkedUserByIdTask extends AsyncTask<Void, Void, Boolean> {
        private int targetId;

        public RemoveLinkedUserByIdTask(int targetId) {
            this.targetId = targetId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return HttpHandler.LinkedUserController.removeLinkedUser(user.getEmail(), user.getPassword(),
                    user.getId(), this.targetId);
        }

        @Override
        protected void onPostExecute(Boolean addSuccess) {
            super.onPostExecute(addSuccess);
            removeLinkedUserByIdResponse(addSuccess);
            loadingDialog.dismiss();
        }
    }

    private class SetLinkedUserAlertTask extends AsyncTask<Void, Void, Boolean> {
        private int targetId;
        private boolean alertMe;

        public SetLinkedUserAlertTask(int targetId, boolean alertMe) {
            this.targetId = targetId;
            this.alertMe = alertMe;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return HttpHandler.LinkedUserController.setLinkedUserAlert(user.getEmail(),
                    user.getPassword(), user.getId(), this.targetId, this.alertMe);
        }

        @Override
        protected void onPostExecute(Boolean setSuccess) {
            super.onPostExecute(setSuccess);
            setLinkedUserAlertResponse(setSuccess);
            loadingDialog.dismiss();
        }
    }

    private class SetLinkedUserMuteTask extends AsyncTask<Void, Void, Boolean> {
        private int targetId;
        private boolean muteMe;

        public SetLinkedUserMuteTask(int targetId, boolean muteMe) {
            this.targetId = targetId;
            this.muteMe = muteMe;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return HttpHandler.LinkedUserController.setLinkedUserMute(user.getEmail(),
                    user.getPassword(), user.getId(), this.targetId, this.muteMe);
        }

        @Override
        protected void onPostExecute(Boolean setSuccess) {
            super.onPostExecute(setSuccess);
            setLinkedUserMuteResponse(setSuccess);
            loadingDialog.dismiss();
        }
    }










    private class LinkedUserRequestsAdapter extends ArrayAdapter<LinkedUser> {
        private int layoutResource;
        private Context context;

        public LinkedUserRequestsAdapter(Context context, int layoutResource,
                                         List<LinkedUser> linkedUserRequests) {

            super(context, 0, linkedUserRequests);
            this.layoutResource = layoutResource;
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinkedUser linkedUser = getItem(position);
            LayoutInflater layoutInflater = LayoutInflater.from(this.context);
            View view = layoutInflater.inflate(this.layoutResource, null, false);

            TextView userName = view.findViewById(R.id.textView_listView3column_username);
            Button confirm = view.findViewById(R.id.button_listView3column_confirm);
            Button delete = view.findViewById(R.id.button_listView3column_delete);

            userName.setText(linkedUser.getNameTarget());
            confirm.setOnClickListener(new MyButtonClickHandler(linkedUser));
            delete.setOnClickListener(new MyButtonClickHandler(linkedUser));
            return view;


















//            ViewHolder viewHolder;
//            LinkedUser linkedUser = getItem(position);
//
//            // Check if an existing view is being reused, otherwise inflate the view
//            if (convertView == null) {
//                // If there's no view to re-use, inflate a brand new view for row
//                viewHolder = new ViewHolder();
//
//                convertView = LayoutInflater.from(LinkedAccountActivity.this).inflate(layoutResource, null, false);
//                viewHolder.userName = convertView.findViewById(R.id.textView_listView3column_username);
//                viewHolder.confirm = convertView.findViewById(R.id.button_listView3column_confirm);
//                viewHolder.delete = convertView.findViewById(R.id.button_listView3column_delete);
//
//                // Set click handlers
//                viewHolder.confirm.setOnClickListener(null);
//                viewHolder.delete.setOnClickListener(null);
//                viewHolder.confirm.setOnClickListener(new MyButtonClickHandler(linkedUser));
//                viewHolder.delete.setOnClickListener(new MyButtonClickHandler(linkedUser));
//
//                // Cache the viewHolder object inside the fresh view
//                convertView.setTag(viewHolder);
//
//            // View is being recycled, retrieve the viewHolder object from tag
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//
//                // Set click handlers
//                viewHolder.confirm.setOnClickListener(null);
//                viewHolder.delete.setOnClickListener(null);
//                viewHolder.confirm.setOnClickListener(new MyButtonClickHandler(linkedUser));
//                viewHolder.delete.setOnClickListener(new MyButtonClickHandler(linkedUser));
//            }
//            // Populate the data from the data object via the viewHolder object into the template view
//            viewHolder.userName.setText(linkedUser.getNameTarget());
//
//            return convertView;
        }

        private class MyButtonClickHandler implements ImageView.OnClickListener {
            private LinkedUser linkedUser;

            public MyButtonClickHandler(LinkedUser linkedUser) {
                this.linkedUser = linkedUser;
                Log.d(MyButtonClickHandler.class.getSimpleName(), "LinkRequestButton constructor for ------------------------------> " + linkedUser.getNameTarget());
            }

            @Override
            public void onClick(View v) {
                switch(v.getId()) {

                    case R.id.button_listView3column_confirm:
                        linkedUserRequestsAdapter.remove(this.linkedUser); // Remove from linkedUserRequests gui list
                        this.linkedUser.setConfirmedByGui(true); // Set as confirmed
                        linkedUsersAdapter.add(this.linkedUser); // Add to linkedUsers gui list
                        linkedUsersAdapter.sort(new LinkedUserComparator.Ascending());
                        linkedUserRequestsAdapter.notifyDataSetChanged(); // Update linkedUserRequests gui list
                        linkedUsersAdapter.notifyDataSetChanged(); // Update linkedUsers gui list
                        updatedLinkedUserRequestsLayout(); // Update the linkedUserRequests layout (if needed)
                        updatedLinkedUsersLayout(); // Update the linkedUsers layout (if needed)
                        Log.e(LinkedUserRequestsAdapter.class.getSimpleName(), "confirm: " + this.linkedUser.getNameTarget());
                        break;

                    case R.id.button_listView3column_delete:
                        linkedUserRequestsAdapter.remove(this.linkedUser); // Remove from linkedUserRequests gui list
                        linkedUserRequestsAdapter.notifyDataSetChanged(); // Update linkedUserRequests gui list
                        deleteLinkedUserList.add(this.linkedUser); // Add to deletedList
                        updatedLinkedUserRequestsLayout(); // Update the linkedUserRequests layout (if needed)
                        Log.e(LinkedUserRequestsAdapter.class.getSimpleName(), "delete: " + this.linkedUser.getNameTarget());
                        break;

                    default:
                        break;
                }
            }
        }

        private class ViewHolder {
            TextView userName;
            Button confirm;
            Button delete;
        }
    }










    private class LinkedUsersAdapter extends ArrayAdapter<LinkedUser> {
        private int deleteIconResource;
        private int layoutResource;
        private Context context;

        public LinkedUsersAdapter(Context context, int layoutResource, int deleteIconResource,
                                  List<LinkedUser> luList) {

            super(context, layoutResource, luList);
            this.layoutResource = layoutResource;
            this.deleteIconResource = deleteIconResource;
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinkedUser linkedUser = getItem(position);

            LayoutInflater layoutInflater = LayoutInflater.from(this.context);
            View view = layoutInflater.inflate(this.layoutResource, null, false);

            TextView userName = view.findViewById(R.id.textView_listView5column_username);
            TextView status = view.findViewById(R.id.textView_listView5column_status);
            CheckBox checkboxAlert = view.findViewById(R.id.checkbox_listView5column_alert);
            CheckBox checkboxMute = view.findViewById(R.id.checkbox_listView5column_mute);
            ImageView imageViewDelete = view.findViewById(R.id.imageView_listView5column_delete);

            userName.setText(linkedUser.getNameTarget());
            if (linkedUser.getStatusTarget() == 5) {
                status.setText("Normal");
                status.setTextColor(ContextCompat.getColor(LinkedAccountActivity.this, R.color.teal));

            } else {
                status.setText("Alert");
                status.setTextColor(ContextCompat.getColor(LinkedAccountActivity.this, R.color.darkRed));
            }

            if (linkedUser.isAlertMe()) {
                checkboxAlert.setChecked(linkedUser.isAlertMe());
            }

            if (linkedUser.isMuteMe()) {
                checkboxMute.setChecked(linkedUser.isMuteMe());
            }
            checkboxAlert.setOnCheckedChangeListener(new MyAlertCheckedChangeListener(linkedUser, linkedUser.isAlertMe()));
            checkboxMute.setOnCheckedChangeListener(new MyMuteCheckedChangeListener(linkedUser, linkedUser.isMuteMe()));
            imageViewDelete.setOnClickListener(new MyClickHandler(linkedUser));
            imageViewDelete.setImageResource(this.deleteIconResource);
            return view;





//            ViewHolder viewHolder;
//            LinkedUser linkedUser = getItem(position);
//
//            // Check if an existing view is being reused, otherwise inflate the view
//            if (convertView == null) {
//                // If there's no view to re-use, inflate a brand new view for row
//                viewHolder = new ViewHolder();
//                convertView = LayoutInflater.from(LinkedAccountActivity.this).inflate(this.layoutResource, parent, false);
//
//                viewHolder.userName = convertView.findViewById(R.id.textView_listView5column_username);
//                viewHolder.status = convertView.findViewById(R.id.textView_listView5column_status);
//                viewHolder.checkboxAlert = convertView.findViewById(R.id.checkbox_listView5column_alert);
//                viewHolder.checkboxMute = convertView.findViewById(R.id.checkbox_listView5column_mute);
//                viewHolder.imageViewDelete = convertView.findViewById(R.id.imageView_listView5column_delete);
//
//                // Cache the viewHolder object inside the fresh view
//                convertView.setTag(viewHolder);
//
//            // View is being recycled, retrieve the viewHolder object from tag
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//            // Populate the data from the data object via the viewHolder object into the template view
//            viewHolder.userName.setText(linkedUser.getNameTarget());
//
//            if (linkedUser.getStatusTarget() == 5) {
//                viewHolder.status.setText("Normal");
//                viewHolder.status.setTextColor(ContextCompat.getColor(LinkedAccountActivity.this, R.color.teal));
//
//            } else {
//                viewHolder.status.setText("Alert");
//                viewHolder.status.setTextColor(ContextCompat.getColor(LinkedAccountActivity.this, R.color.darkRed));
//            }
//
//            if (linkedUser.isAlertMe()) {
//                viewHolder.checkboxAlert.setChecked(linkedUser.isAlertMe());
//            }
//
//            if (linkedUser.isMuteMe()) {
//                viewHolder.checkboxMute.setChecked(linkedUser.isMuteMe());
//            }
//            // Set click handlers
//            viewHolder.checkboxAlert.setOnCheckedChangeListener(new MyAlertCheckedChangeListener(linkedUser, linkedUser.isAlertMe()));
//            viewHolder.checkboxMute.setOnCheckedChangeListener(new MyMuteCheckedChangeListener(linkedUser, linkedUser.isMuteMe()));
//            viewHolder.imageViewDelete.setOnClickListener(new MyClickHandler(linkedUser));
//            viewHolder.imageViewDelete.setImageResource(this.deleteIconResource);
//            return convertView;
        }

        private class ViewHolder {
            TextView userName;
            TextView status;
            CheckBox checkboxAlert;
            CheckBox checkboxMute;
            ImageView imageViewDelete;
        }

        private class MyAlertCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
            private LinkedUser linkedUser;
            private boolean initialSetChecked;

            public MyAlertCheckedChangeListener(LinkedUser linkedUser, boolean initialSetChecked) {
                this.linkedUser = linkedUser;
                this.initialSetChecked = initialSetChecked;
                Log.d(MyAlertCheckedChangeListener.class.getSimpleName(), "Alert constructor for: " + linkedUser.getNameTarget());
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(MyAlertCheckedChangeListener.class.getSimpleName(), "Alert ENTERED for ------------------------------> " + linkedUser.getNameTarget());
               if (!this.initialSetChecked) {
                   this.linkedUser.setAlertModifiedByGui(!this.linkedUser.isAlertModifiedByGui());
                   this.linkedUser.setAlertMe(!this.linkedUser.isAlertMe());
                   Log.d(MyAlertCheckedChangeListener.class.getSimpleName(), "Alert MODIFIED for ------------------------------> " + linkedUser.getNameTarget());

               } else {
                   this.initialSetChecked = false;
                   Log.d(MyAlertCheckedChangeListener.class.getSimpleName(), "Alert CHECKED for ------------------------------> " + linkedUser.getNameTarget());
               }
            }
        }

        private class MyMuteCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
            private LinkedUser linkedUser;
            private boolean initialSetChecked;

            public MyMuteCheckedChangeListener(LinkedUser linkedUser, boolean initialSetChecked) {
                this.linkedUser = linkedUser;
                this.initialSetChecked = initialSetChecked;
                Log.d(MyMuteCheckedChangeListener.class.getSimpleName(), "Mute constructor for: " + linkedUser.getNameTarget());
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(MyMuteCheckedChangeListener.class.getSimpleName(), "Mute ENTERED for ------------------------------> " + linkedUser.getNameTarget());
                if (!this.initialSetChecked) {
                    this.linkedUser.setMuteModifiedByGui(!this.linkedUser.isMuteModifiedByGui());
                    this.linkedUser.setMuteMe(!this.linkedUser.isMuteMe());
                    Log.d(MyMuteCheckedChangeListener.class.getSimpleName(), "Mute MODIFIED for ------------------------------> " + linkedUser.getNameTarget());

                } else {
                    this.initialSetChecked = false;
                    Log.d(MyMuteCheckedChangeListener.class.getSimpleName(), "Mute CHECKED for ------------------------------> " + linkedUser.getNameTarget());
                }
            }
        }

        private class MyClickHandler implements ImageView.OnClickListener {
            private LinkedUser linkedUser;

            public MyClickHandler(LinkedUser linkedUser) {
                this.linkedUser = linkedUser;
                Log.d(MyClickHandler.class.getSimpleName(), "X constructor for ------------------------------> " + linkedUser.getNameTarget());
            }

            @Override
            public void onClick(View v) {
                deleteLinkedUserList.add(this.linkedUser); // Add to deletedList
                linkedUsersAdapter.remove(this.linkedUser); // Remove from linkedUsers gui list
                linkedUsersAdapter.notifyDataSetChanged(); // Update linkedUsers gui list
                updatedLinkedUsersLayout(); // Update the linkedUsers layout (if needed)
                Log.e(LinkedUsersAdapter.class.getSimpleName(), "delete: " + this.linkedUser.getNameTarget());
            }
        }
    }
}
