package com.owentech.devdrawer.activities;

import com.owentech.devdrawer.R;
import com.owentech.devdrawer.utils.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditDialog extends Activity {

    EditText editText;
    Button changeButton;

    String originalText;
    String id;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_dialog);

        editText = findViewById(R.id.editDialogEditText);
        changeButton = findViewById(R.id.changeButton);

        final Bundle bundle = getIntent().getExtras();

        originalText = bundle.getString("text");
        id = bundle.getString("id");

        editText.setText(originalText);

        // Change button sends a result back to MainActivity
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final Intent intent = new Intent();
                final Bundle bundle = new Bundle();
                bundle.putString("newText", editText.getText().toString());
                bundle.putString("id", id);
                intent.putExtras(bundle);

                setResult(Constants.EDIT_DIALOG_CHANGE, intent);
                finish();

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
