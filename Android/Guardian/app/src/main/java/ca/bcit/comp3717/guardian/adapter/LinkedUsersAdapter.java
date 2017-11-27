package ca.bcit.comp3717.guardian.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.controller.LinkedAccountActivity;
import ca.bcit.comp3717.guardian.model.LinkedUser;

public class LinkedUsersAdapter extends ArrayAdapter<LinkedUser> {

    private Context context;
    private List<LinkedUser> linkedUsersList;
    private int layoutResource;
    private int deleteIconResource;

    public LinkedUsersAdapter(Context context, int layoutResource, int deleteIconResource,
                              List<LinkedUser> linkedUsersList) {

        super(context, 0, linkedUsersList);
        this.context = context;
        this.layoutResource = layoutResource;
        this.deleteIconResource = deleteIconResource;
        this.linkedUsersList = linkedUsersList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutResource, null, false);

        TextView textViewUsername = view.findViewById(R.id.textView_listView5column_username);
        TextView textViewStatus = view.findViewById(R.id.textView_listView5column_status);
        CheckBox checkboxAlert = view.findViewById(R.id.checkbox_listView5column_alert);
        CheckBox checkboxMute = view.findViewById(R.id.checkbox_listView5column_mute);
        ImageView imageViewDelete = view.findViewById(R.id.imageView_listView5column_delete);

        LinkedUser linkedUser = linkedUsersList.get(position);

        textViewUsername.setText(linkedUser.getNameTarget());

        if (linkedUser.getStatusTarget() == 5) {
            textViewStatus.setText("Normal");
            textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.teal));

        } else {
            textViewStatus.setText("Alert");
            textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
        }

        checkboxAlert.setChecked(linkedUser.isAlertMe());
        checkboxMute.setChecked(linkedUser.isMuteMe());
        imageViewDelete.setImageResource(this.deleteIconResource);

//        checkboxAlert.setOnCheckedChangeListener(new MyCheckedChangeListener(textViewStatus));
//        checkboxMute.setOnCheckedChangeListener(new MyCheckedChangeListener(textViewStatus));
//        imageViewDelete.setOnClickListener(new MyClickHandler(imageViewDelete));
        return view;
    }

    private class MyCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        TextView textViewStatus;

        public MyCheckedChangeListener(TextView textViewStatus) {
            this.textViewStatus = textViewStatus;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch(buttonView.getId()) {

                case R.id.checkbox_listView5column_alert:
                    break;

                case R.id.checkbox_listView5column_mute:
                    break;

                default:
                    break;
            }
        }
    }

    private class MyClickHandler implements ImageView.OnClickListener {
        private ImageView imageViewDelete;

        public MyClickHandler(ImageView imageViewDelete) {
            this.imageViewDelete = imageViewDelete;
        }

        @Override
        public void onClick(View v) {

        }
    }
}
