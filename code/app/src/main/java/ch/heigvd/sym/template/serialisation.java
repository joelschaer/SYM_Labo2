package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class serialisation extends Activity {

    private TextView resultSerialisation2;
    private TextView resultSerialisation1;
    private TextView resultSerialisationKey2;
    private TextView resultSerialisationKey1;
    private TextView resultFrom;
    private EditText EditValue2;
    private EditText EditValue1;
    private EditText EditKey2;
    private EditText EditKey1;
    private Button sendRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialisation);

        resultSerialisation2 = findViewById(R.id.resultSerialisation2);
        resultSerialisation1 = findViewById(R.id.resultSerialisation1);
        resultSerialisationKey2 = findViewById(R.id.resultSerialisationKey2);
        resultSerialisationKey1 = findViewById(R.id.resultSerialisationKey1);
        resultFrom = findViewById(R.id.resultFrom);
        EditValue2 = findViewById(R.id.EditValue2);
        EditValue1 = findViewById(R.id.EditValue1);
        EditKey2 = findViewById(R.id.EditKey2);
        EditKey1 = findViewById(R.id.EditKey1);
        sendRequest = findViewById(R.id.sendRequest);

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDataThenSend();
            }
        });
    }

    public void sendRequest(String request){
        AsyncSendRequest requestAuteur = new AsyncSendRequest() ;
        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)
                        String value1 = null;
                        String value2 = null;
                        String from = null;
                        JSONObject jo;
                        try {
                            jo = new JSONObject(response);
                            JSONObject data = jo.getJSONObject("query");
                            value1 = data.getString(EditKey1.getText().toString());
                            value2 = data.getString(EditKey2.getText().toString());

                            JSONObject infos = jo.getJSONObject("infos");
                            from = infos.getString("SERVER_NAME");
                            from  += " : " + infos.getString("SERVER_ADDR");
                        } catch (JSONException e) {
                            e.printStackTrace();
                       }

                        final String finalValue1 = value1;
                        final String finalValue2 = value2;
                        final String finalFrom = from;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Code dans le UI-Thread pour l'affichage graphique

                                resultSerialisation1.setText(finalValue1);
                                resultSerialisation2.setText(finalValue2);
                                resultSerialisationKey1.setText(EditKey1.getText().toString());
                                resultSerialisationKey2.setText(EditKey2.getText().toString());
                                resultFrom.setText(finalFrom);
                            }
                        });

                        return true;
                    }
                }
        );
        requestAuteur.sendRequest(request,"http://sym.iict.ch/rest/json", MedType.JSON);
    }

    public void GetDataThenSend(){
        JSONObject json = new JSONObject();
        JSONObject query = new JSONObject();
        try {
            query.put(EditKey1.getText().toString(), EditValue1.getText().toString());
            query.put(EditKey2.getText().toString(), EditValue2.getText().toString());
            json.put("query", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendRequest(json.toString());
    }

}
