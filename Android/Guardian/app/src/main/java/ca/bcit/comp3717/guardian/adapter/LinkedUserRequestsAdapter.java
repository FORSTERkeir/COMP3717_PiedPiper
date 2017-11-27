package ca.bcit.comp3717.guardian.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.model.LinkedUser;

public class LinkedUserRequestsAdapter extends ArrayAdapter<LinkedUser> {
    private Context context;
    private List<LinkedUser> linkedUserRequests;
    private int layoutResource;

    public LinkedUserRequestsAdapter(Context context, int layoutResource,
                                     List<LinkedUser> linkedUserRequests) {

        super(context, 0, linkedUserRequests);
        this.context = context;
        this.layoutResource = layoutResource;
        this.linkedUserRequests = linkedUserRequests;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(layoutResource, null, false);

        TextView textViewUsername = view.findViewById(R.id.textView_listView3column_username);
        Button btnConfirm = view.findViewById(R.id.button_listView3column_confirm);
        Button btnDelete = view.findViewById(R.id.button_listView3column_delete);

        LinkedUser linkedUser = this.linkedUserRequests.get(position);

        textViewUsername.setText(linkedUser.getNameTarget());

//        btnConfirm.setOnClickListener(new MyButtonClickHandler(btnConfirm, linkedUser));
//        btnDelete.setOnClickListener(new MyButtonClickHandler(btnDelete, linkedUser));
        return view;
    }

    private class MyButtonClickHandler implements ImageView.OnClickListener {
        private Button btn;
        private LinkedUser lu;

        public MyButtonClickHandler(Button btn, LinkedUser lu) {
            this.btn = btn;
            this.lu = lu;
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()) {

                case R.id.button_listView3column_confirm:
                    Log.e(LinkedUserRequestsAdapter.class.getSimpleName(), "confirm " + this.lu.getNameTarget());
                    break;

                case R.id.button_listView3column_delete:
                    Log.e(LinkedUserRequestsAdapter.class.getSimpleName(), "delete: " + this.lu.getNameTarget());
                    break;

                default:
                    break;
            }
        }
    }
}
