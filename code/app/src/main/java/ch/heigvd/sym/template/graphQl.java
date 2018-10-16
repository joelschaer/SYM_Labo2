package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class graphQl extends Activity {

    private TextView textView  = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_ql);

        textView = findViewById(R.id.textView);

        AsyncSendRequest asr = new AsyncSendRequest() ;
        asr.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Code dans le UI-Thread pour l'affichage graphique
                                textView.setText(response);
                            }
                        });

                        return true;
                    }
                }
        );
        asr.sendRequest("{ \"query\": \"{allAuthors{id}}\" }","http://sym.iict.ch/api/graphql");

    }

}
