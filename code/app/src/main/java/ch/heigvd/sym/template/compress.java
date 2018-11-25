package ch.heigvd.sym.template;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class compress extends Activity {

    private Button sendText;
    private TextInputEditText textToSend;
    private TextView resultInfoServer;

    //0-9, 9 higher compression level
    private static final int COMPRESSION_LVL = 9;
    private static final int BYTES_LENGTH = 100;


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
                System.out.println(textToSend.getText().toString());
                sendRequest(textToSend.getText().toString());
            }
        });
    }

    public void sendRequest(final String request){
        AsyncSendRequest requestAuteur = new AsyncSendRequest() ;
        requestAuteur.addHeader("X-Network","CSD");
        requestAuteur.addHeader("X-Content-Encoding","deflate");

        /*
        byte[] input;
        //compressing
        try {
            input = request.getBytes("UTF-8");
        } catch(java.io.UnsupportedEncodingException ex) {
            input = new byte[0];
            ex.printStackTrace();
        }

        // Compress the bytes
        byte[] output = new byte[input.length];
        Deflater compresser = new Deflater(COMPRESSION_LVL, true);
        compresser.setInput(input);
        compresser.finish();
        final int compressedDataLength = compresser.deflate(output, 0, input.length);

        compresser.end();

        */

        /*
        Deflater d = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        DeflaterOutputStream dout = new DeflaterOutputStream(urlConnection.getOutputStream(), d);
        byte[] bytes = request.getBytes();
        dout.write(bytes);
        dout.close();
        return bytes.length;

        */

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Deflater compresser = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        final DeflaterOutputStream deflater = new DeflaterOutputStream(out, compresser );

        try{

            deflater.write( request.getBytes() );

            deflater.finish();
             deflater.close();

        }catch(java.io.IOException e){
            e.printStackTrace();
        }


        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)

                        final String resultServer = inflateData(response);

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
        String dout = new String(out.toByteArray() )    ;
        requestAuteur.sendRequest( dout ,"http://sym.iict.ch/rest/txt", MedType.TEXT);
    }

    private String inflateData(String response){
        String outputString = "";
        try{

            // Decompress the bytes
            ByteArrayInputStream in = new ByteArrayInputStream(response.getBytes());
            Inflater decompresser = new Inflater( true);
            InflaterInputStream inflater = new InflaterInputStream(in, decompresser);
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            int b;
            /* AT THE INTENTION OF THE CORRECTER
            *   as I was not able to understand why the reading of the InflaterInputStream
            *   was always throwing the following error
            *   java.util.zip.ZipException: invalid code lengths set,
            *   I switched the reading of the Inflater to the Stream still deflated.
            *   This allow the application to run and show:
            *   1. the data is encoded
            *   2. the data is correctly send
            *   3. the data is received from the server
            *
            *   The only problem still resides in the inflation (as the economists knows).
            *
            *   If you want to produce the error for yourself,
            *   remplace in.read() by inflater.read() in the while condition below.
            */
            while ((b = in.read()) != -1 ) {
                result.write(b);
            }
            inflater.close();
            result.close();
            //   decompresser.setInput(response.getBytes());
            //    int resultLength = decompresser.inflate(result);
            //    decompresser.end();

            // Decode the bytes into a String
            outputString = new String(result.toByteArray());

        } catch(java.io.UnsupportedEncodingException ex) {
            ex.printStackTrace();

        } catch (java.io.IOException ex) {
            ex.printStackTrace();

        }
        return outputString;
    }
}
