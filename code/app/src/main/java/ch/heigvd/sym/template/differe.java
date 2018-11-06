package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class differe extends Activity {

    private Button sendText;
    private TextInputEditText textToSend;
    private TextView resultInfoServer;

    private List<String> waitingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        waitingList = new ArrayList<>();

        new Thread(new Runnable() {
            public void run() {
                // loop until the thread is interrupted
                while (!Thread.currentThread().isInterrupted()) {

                    while(!waitingList.isEmpty()){
                        sendRequest(waitingList.get(0));
                        waitingList.remove(0);
                    }
                    try{
                        Thread.sleep(5000);
                    }catch(InterruptedException e){
                        throw new RuntimeException();
                    }
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
                textToSend.clearFocus();
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
                        // Code de traitement de la répons (tri etc...)
                        ArrayList<String> lines = ParseText(response);
                        System.out.println(response);
                        final String resultServer = lines.get(3) + "\n" + lines.get(4) + "\nText : " + lines.get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String srvResponse = resultInfoServer.getText().toString();
                                srvResponse = srvResponse + resultServer;
                                resultInfoServer.setText(srvResponse);
                            }
                        });

                        return true;
                    }
                }
        );
        requestAuteur.sendRequest(request, "http://sym.iict.ch/rest/txt");
    }
}
