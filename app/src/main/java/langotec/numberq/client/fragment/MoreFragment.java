package langotec.numberq.client.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import langotec.numberq.client.login.LoginActivity;
import langotec.numberq.client.R;
/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment {
    public Context context = null;
    private Button loginButton;

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        loginButton = view.findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),LoginActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }
}
