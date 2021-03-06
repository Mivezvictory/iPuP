package comp3350.iPuP.presentation;

import comp3350.iPuP.R;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class ParkerMenuActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parker_menu);
    }

    public void buttonCreateOnClick(View v)
    {
        String name;
        Bundle extras = getIntent().getExtras();
        if(extras == null)
            name = null;
        else
            name = extras.getString(getResources().getString(R.string.extra_name));

        Intent parkerMenuIntent = new Intent(ParkerMenuActivity.this, ParkerSearchActivity.class);

        Bundle newBundle = new Bundle();
        newBundle.putString(getResources().getString(R.string.extra_name), name);
        parkerMenuIntent.putExtras(newBundle);
        ParkerMenuActivity.this.startActivity(parkerMenuIntent);
    }

    public void buttonParkerLogOnClick(View v)
    {
        String name;
        Bundle extras = getIntent().getExtras();
        if(extras == null)
            name = null;
        else
            name = extras.getString(getResources().getString(R.string.extra_name));

        Intent parkerLogIntent = new Intent(ParkerMenuActivity.this, ParkerLogViewActivity.class);
        Bundle newBundle = new Bundle();
        newBundle.putString(getResources().getString(R.string.extra_name), name);
        parkerLogIntent.putExtras(newBundle);
        ParkerMenuActivity.this.startActivity(parkerLogIntent);
    }
}
