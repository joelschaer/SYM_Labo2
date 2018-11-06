package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class compresse extends Activity {

    private Button compress;
    private ImageView picToSend;
    private ImageView picRcvd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compresse);

        compress = findViewById(R.id.B_compress);
        picToSend = findViewById(R.id.pic_to_send);
        picRcvd= findViewById(R.id.pic_to_rcv);

        compress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCompress();
            }
        });


    }

    public void sendCompress(){

    }

}
