package ch.heigvd.sym.template;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsyncSendRequest {

    private OkHttpClient client = new OkHttpClient();

    private List<CommunicationEventListener> theListeners = new LinkedList<>();

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    // Envoie la requete voulue, lance un Thread afin de ne pas bloquer le déroulement du thread principal
    public void sendRequest(final String request, final String myUrl){
        new Thread(){
            public void run(){
                RequestBody body = RequestBody.create(JSON, request);
                Request SendingRequest = new Request.Builder().url(myUrl).post(body).build();
                try {
                    Response response = client.newCall(SendingRequest).execute();

                    // Regarde si des listener écoute pour la réponse. Si oui on leur envoie la réponse via la méthode handleServerResponse
                    for(CommunicationEventListener cel : theListeners){
                        if(cel.handleServerResponse(response.body().string())) break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        //return response.body().string();
        //Cette méthode retourne rien car elle va appeler une méthode qui elle va retourner quelque chsoe
    }

    public void addCommunicationEventListener(CommunicationEventListener listener){
        if(!theListeners.contains(listener))
            theListeners.add(listener);
    }
}
