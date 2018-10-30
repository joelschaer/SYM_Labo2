package ch.heigvd.sym.template;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Post {
    private String name;
    private String description;
    private String content;
    private String date;

    public Post(String name, String description, String content, String date){
        this.name = name;
        this.description = description;
        this.content = content;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}

class Auteur {
    private LinkedList<Post> posts = new LinkedList<>();
    private String first_Name;
    private String Second_Name;
    private String id;

    public Auteur(String first_Name, String Second_Name){
        this.first_Name = first_Name;
        this.Second_Name = Second_Name;
    }

    public Auteur(String first_Name, String Second_Name, LinkedList<Post> posts){
        this(first_Name,Second_Name);
        this.posts = posts;
    }

    public void setId (String id){
        this.id = id;
    }
    public LinkedList<Post> getPosts() {
        return posts;
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public String getFirst_Name() {
        return first_Name;
    }

    public String getSecond_Name() {
        return Second_Name;
    }

    public String getId() {
        return id;
    }
}

public class graphQl extends Activity {

    private Spinner spinner = null;
    private final LinkedList<Auteur> auteurs = new LinkedList<>();
    private ArrayAdapter<String> adapter;
    private LinearLayout myLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_ql);

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myLayout = findViewById(R.id.layoutBox);
                myLayout.removeAllViews();
                Auteur auteur = auteurs.get((int)id);
                requestPost(auteur);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        requestAuteur();
    }

    public void requestAuteur(){
        AsyncSendRequest requestAuteur = new AsyncSendRequest() ;
        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)
                        JSONObject jo = null;
                        try {
                            jo = new JSONObject(response);
                            JSONObject data = jo.getJSONObject("data");
                            JSONArray listAuteurs = data.getJSONArray("allAuthors");
                            for (int i=0; i < listAuteurs.length(); i++)
                            {
                                JSONObject oneAuteur = listAuteurs.getJSONObject(i);
                                // Pulling items from the array
                                String id = oneAuteur.getString("id");
                                String firstName = oneAuteur.getString("first_name");
                                String SecondName = oneAuteur.getString("last_name");
                                Auteur auteur = new Auteur(firstName,SecondName);
                                auteur.setId(id);
                                auteurs.add(auteur);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Code dans le UI-Thread pour l'affichage graphique

                                List<String> auteursNames = new ArrayList<>();
                                for (Auteur auteur : auteurs) {
                                    String name = auteur.getFirst_Name() + " " + auteur.getSecond_Name();
                                    auteursNames.add(name);
                                }

                                adapter = new ArrayAdapter<>(
                                        graphQl.this,
                                        android.R.layout.simple_spinner_item,
                                        auteursNames
                                );

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            }
                        });

                        return true;
                    }
                }
        );
        requestAuteur.sendRequest("{ \"query\": \"{allAuthors{id, first_name, last_name }}\" }","http://sym.iict.ch/api/graphql");
    }
    public void requestPost(final Auteur auteur){
        AsyncSendRequest requestPost = new AsyncSendRequest() ;
        requestPost.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)
                        JSONObject jo = null;
                        try {
                            jo = new JSONObject(response);
                            JSONObject data = jo.getJSONObject("data");
                            JSONArray listPosts = data.getJSONArray("allPostByAuthor");
                            for (int i=0; i < listPosts.length(); i++)
                            {
                                JSONObject onePost = listPosts.getJSONObject(i);
                                String title = onePost.getString("title");
                                String description = onePost.getString("description");
                                String content = onePost.getString("content");
                                String date = onePost.getString("date");
                                Post post = new Post(title, description, content, date);
                                auteur.addPost(post);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GradientDrawable border = new GradientDrawable();
                                border.setColor(Color.WHITE); //white background
                                border.setStroke(2, Color.BLUE); //black border with full opacity

                                List<Post> posts = auteur.getPosts();
                                for (Post post : posts){

                                    LinearLayout lineraLayout = new LinearLayout(graphQl.this);
                                    lineraLayout.setPadding(10,20,10,20);
                                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                        lineraLayout.setBackgroundDrawable(border);
                                    } else {
                                        lineraLayout.setBackground(border);
                                    }
                                    lineraLayout.setOrientation(LinearLayout.VERTICAL);
                                    LinearLayout.LayoutParams lineraLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                    lineraLayout.setLayoutParams(lineraLayoutParams);

                                    TextView labelName = new TextView(graphQl.this);
                                    labelName.setText(R.string.labelTitle);
                                    labelName.setTypeface(null, Typeface.BOLD);

                                    TextView labelDescription = new TextView(graphQl.this);
                                    labelDescription.setText(R.string.labelDescription);
                                    labelDescription.setTypeface(null, Typeface.BOLD);

                                    TextView labelContent = new TextView(graphQl.this);
                                    labelContent.setText(R.string.labelContent);
                                    labelContent.setTypeface(null, Typeface.BOLD);

                                    TextView labelDate = new TextView(graphQl.this);
                                    labelDate.setText(R.string.labelDate);
                                    labelDate.setTypeface(null, Typeface.BOLD);

                                    TextView name = new TextView(graphQl.this);
                                    name.setText(post.getName());
                                    TextView description = new TextView(graphQl.this);
                                    description.setText(post.getDescription());
                                    TextView content = new TextView(graphQl.this);
                                    content.setText(post.getContent());
                                    TextView date = new TextView(graphQl.this);
                                    date.setText(post.getDate());
                                    lineraLayout.addView(labelName);
                                    lineraLayout.addView(name);
                                    lineraLayout.addView(labelDescription);
                                    lineraLayout.addView(description);
                                    lineraLayout.addView(labelDate);
                                    lineraLayout.addView(date);
                                    lineraLayout.addView(labelContent);
                                    lineraLayout.addView(content);
                                    myLayout.addView(lineraLayout);
                                }
                            }
                        });

                        return true;
                    }
                }
        );
        requestPost.sendRequest("{\n" +
                "\"query\": \"{allPostByAuthor(authorId:" + auteur.getId() +"){title description content date}}\"}","http://sym.iict.ch/api/graphql");
    }
}
