package ch.heigvd.sym.template;

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

    public Post(String name, String description, String content){
        this.name = name;
        this.description = description;
        this.content = content;
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
}

class Auteur {
    private LinkedList<Post> posts = new LinkedList<>();
    private String first_Name;
    private String Second_Name;

    public Auteur(String first_Name, String Second_Name){
        this.first_Name = first_Name;
        this.Second_Name = Second_Name;
    }

    public Auteur(String first_Name, String Second_Name, LinkedList<Post> posts){
        this(first_Name,Second_Name);
        this.posts = posts;
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
}

public class graphQl extends Activity implements AdapterView.OnItemSelectedListener {

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
                List<Post> posts = auteurs.get((int)id).getPosts();
                for (Post post : posts){
                    TextView name = new TextView(graphQl.this);
                    name.setText("Name : " +  post.getName());
                    TextView description = new TextView(graphQl.this);
                    description.setText("Description : " + post.getDescription());
                    TextView content = new TextView(graphQl.this);
                    content.setText("Content : " + post.getContent());
                    myLayout.addView(name);
                    myLayout.addView(description);
                    myLayout.addView(content);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                                    String firstName = oneAuteur.getString("first_name");
                                    String SecondName = oneAuteur.getString("last_name");
                                    JSONArray listPosts = oneAuteur.getJSONArray("posts");
                                    Auteur auteur = new Auteur(firstName,SecondName);

                                    for (int j=0; j < listPosts.length(); j++ ){
                                        JSONObject onePost = listPosts.getJSONObject(j);
                                        String content = onePost.getString("content");
                                        String title = onePost.getString("title");
                                        String description = onePost.getString("description");
                                        Post post = new Post(title,description,content);
                                        auteur.addPost(post);
                                    }
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
        requestAuteur.sendRequest("{ \"query\": \"{allAuthors{first_name, last_name posts{title, description, content}}}\" }","http://sym.iict.ch/api/graphql");

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
