package com.example.touchpanel;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends Activity {

	private SignatureView signatureView;
	private ImageView image;
	private Button back_button;
	private Button save_button;
	private String filename="test.png";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
    	signatureView=(SignatureView)this.findViewById(R.id.SignatureView);
    	image=(ImageView)this.findViewById(R.id.imgview);
    	back_button=(Button)this.findViewById(R.id.back_button);
    	save_button=(Button)this.findViewById(R.id.save_button);
    	signatureView.setImageView(image);
    	signatureView.setContentResolver(getContentResolver());
    	signatureView.postDelayed(new Runnable() {
            @Override
            public void run() {
            	signatureView.invalidate();           
                signatureView.setSize(signatureView.getWidth(), signatureView.getHeight());
            }
        }, 1);
    	
    	
    	back_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				signatureView.goback();
			}
    	});
    	
    	save_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				signatureView.save(filename);
			}
    	});
    	
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
