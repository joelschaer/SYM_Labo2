package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class differe extends Activity {

    private Button sendText;
    private TextInputEditText textToSend;
    private TextView resultInfoServer;

    private List<String> waitingList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        waitingList = Collections.synchronizedList(new ArrayList<String>());

        new Thread(new Runnable() {
            public void run() {
                // loop until the thread is interrupted
                while (!Thread.currentThread().isInterrupted()) {

                    while(!waitingList.isEmpty()){
                        sendRequest(waitingList.get(0));
                        waitingList.remove(0);
                    }
                    try{
                        Thread.sleep(10000);
                    }catch(InterruptedException e){ /*do nothin */ }

                }
            }
        }).start();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_differe);

        sendText = findViewById(R.id.sendText);
        textToSend = findViewById(R.id.textToSend);
        resultInfoServer = findViewById(R.id.resultInfoServer);

        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitingList.add(textToSend.getText().toString());
                textToSend.getText().clear();
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

    public void sendRequest(final String request) {
        System.out.println("REQUEST : " + request);
        AsyncSendRequest requestAuteur = new AsyncSendRequest();
        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener() {
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la réponse (tri etc...)
                        System.err.print(response);
                        ArrayList<String> lines = ParseText(response);
                        System.err.println(response);
                        final String resultServer = lines.get(3) + "\n" + lines.get(4) + "\nText : " + lines.get(0) +"\n";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String resultText = resultServer + resultInfoServer.getText();
                                resultInfoServer.setText(resultText);
                            }
                        });

                        return true;
                    }
                }
        );
        requestAuteur.sendRequest(request, "http://sym.iict.ch/rest/txt", MedType.TEXT);
    }
}
