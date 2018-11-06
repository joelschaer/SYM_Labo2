package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Scanner;

public class asynchrone extends Activity {

    private Button sendText;
    private TextInputEditText textToSend;
    private TextView resultInfoServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asynchrone);

        sendText = findViewById(R.id.sendText);
        textToSend = findViewById(R.id.textToSend);
        resultInfoServer = findViewById(R.id.resultInfoServer);

        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest(textToSend.getText().toString());
            }
        });
    }

    public ArrayList<String> ParseText(String text){
        ArrayList<String> lines = new ArrayList<>();
        Scanner scanner = new Scanner(text);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
        }
        scanner.close();
        return lines;
    }

    public void sendRequest(final String request){
        System.out.println("REQUEST : " + request);
        AsyncSendRequest requestAuteur = new AsyncSendRequest() ;
        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)
                        ArrayList<String> lines = ParseText(response);
                        System.out.println(response);
                        final String resultServer = lines.get(3) + "\n" + lines.get(4) + "\nText : " + lines.get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultInfoServer.setText(resultServer);
                            }
                        });

                        return true;
                    }
                }
        );
        requestAuteur.sendRequest(request,"http://sym.iict.ch/rest/txt");
    }

}
