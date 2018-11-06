# SYM\_Labo2

### serveur applicatif

http://sym.iict.ch/
http://sym.iict.ch/rest/txt ( avec content type "text/plain" ) : retourne simplement le texte qu'il reçoit.
http://sym.iict.ch/rest/lorem/20 : retourne simplement du texte



# Questions :

### 1 : Traitement des erreurs

*Les interfaces AsyncSendRequest et CommunicationEventListener utilisées au point 3.1 restent très
(et certainement trop) simples pour être utilisables dans une vraie application : que se passe-t-il si le
serveur n’est pas joignable dans l’immédiat ou s’il retourne un code HTTP d’erreur ? Veuillez proposer
une nouvelle version, mieux adaptée, de ces deux interfaces pour vous aider à illustrer votre réponse.*

**Dans le cas ou nous sommes face à une 404, page not found : **

- Avec l'implémentation initiale, si l'adresse du serveur à joindre retourne une 404 aucune erreur ne s'affiche sur l'application et aucune excéption n'est levée. Nous recevrons simplement du contenu HTML correspondant à une page 404

**Dans le cas ou le serveur a un temps de réponse trop long, serveur pas joignable : **

- Avec l'implémentation initiale, si le serveur n'est pas joignable, aucune erreur ne s'affiche sur l'application, il ne se passera visuellement rien. Par contre dans le code nous aurons l'exception : java.net.ConnectException: Failed to connect to sym.iict.ch/193.134.218.22:80

**Dans le cas ou le serveur n'existe pas** :

- Avec l'implémentation initiale, si le serveur que l'on veut joindre n'existe pas, on reçois aussi une exception : java.net.UnknownHostException: Unable to resolve host "sym.iict.czh": No address associated with hostname

**Implémentation Initiale : **

```java
// CLASS AsyncSendRequest
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

//Interface CommunicationEventListener
public interface CommunicationEventListener extends EventListener {
    public boolean handleServerResponse(String response);
}

//Exemple simplifié d'utilisation
public void sendRequest(final String request){
        System.out.println("REQUEST : " + request);
        AsyncSendRequest requestAuteur = new AsyncSendRequest() ;
        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(final String response) {
                        // Code de traitement de la répons (tri etc...)
                        ArrayList<String> lines = ParseText(response);
                        System.out.println(response);
                        final String resultServer = lines.get(3);
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
        requestAuteur.sendRequest(request,"http://sym.iict.czh/rest/txt");
    }
```

** Implémentation Modifiée **

Dans cette implémentation, au lieu de passer une string contenant le body de la réponse au handleServerResponse, je passe directement l'objet Response.

L'objet response me permet ensuite (dans sendRequest) de vérifier si la requête a été "successfull" et en cas d'erreur de pouvoir tester le code d'erreur, il contient aussi d'autre informations utiles.

L'utilisation de l'objet response me permet donc de combler le manque que nous avions avec une simple string :

- Nous ne pouvions pas vérifier si la requête c'est effectué correctement.
  - corrigé via `response.isSuccessful()`
- Nous ne pouvions pas tester quel erreur a eu lieu durant la requête
  - ceci est corrigé via `response.code()` me permettant de tester si il y a une erreur `404`ou autre.

```java
// CLASS AsyncSendRequest
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
                        if(cel.handleServerResponse(response)) break;
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

//Interface CommunicationEventListener
public interface CommunicationEventListener extends EventListener {
    public boolean handleServerResponse(Response response);
}

//Exemple simplifié d'utilisation
public void sendRequest(final String request){
        System.out.println("REQUEST : " + request);
        AsyncSendRequest requestAuteur = new AsyncSendRequest() ;
        requestAuteur.addCommunicationEventListener(
                new CommunicationEventListener(){
                    public boolean handleServerResponse(Response response) {
                        if (response.isSuccessful()) {
                            // Code de traitement de la répons (tri etc...)
                            ArrayList<String> lines = null;
                            try {
                                lines = ParseText(response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(response);
                            final String resultServer = lines.get(0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resultInfoServer.setText(resultServer);
                                }
                            });
                        }
                        else {
                            // error case
                            switch (response.code()) {
                                case 404:
                                    Toast.makeText(asynchrone.this, "not found", 										Toast.LENGTH_SHORT).show();
                                    break;
                                case 500:
                                    Toast.makeText(asynchrone.this, "server broken", 									Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(asynchrone.this, "unknown error", 									 Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                        return true;
                    }
                }
        );
        requestAuteur.sendRequest(request,"http://sym.iict.czh/rest/txt");
    }
```



### 2 :  Authentification

*Si une authentification par le serveur est requise, peut-on utiliser un protocole asynchrone ? Quelles
seraient les restrictions ? Peut-on utiliser une transmission différée ?*

### 3 : Threads concurrents

*Lors de l'utilisation de protocoles asynchrones, c'est généralement deux threads différents qui se
préoccupent de la préparation, de l'envoi, de la réception et du traitement des données. Quels
problèmes cela peut-il poser ?*

### 4 : Ecriture différée

*Lorsque l'on implémente l'écriture différée, il arrive que l'on ait soudainement plusieurs transmissions
en attente qui deviennent possibles simultanément. Comment implémenter proprement cette
situation (sans réalisation pratique) ? Voici deux possibilités :*

- *Effectuer une connexion par transmission différée*
- *Multiplexer toutes les connexions vers un même serveur en une seule connexion de transport.
  Dans ce dernier cas, comment implémenter le protocole applicatif, quels avantages peut-on
  espérer de ce multiplexage, et surtout, comment doit-on planifier les réponses du serveur
  lorsque ces dernières s'avèrent nécessaires ?*

*Comparer les deux techniques (et éventuellement d'autres que vous pourriez imaginer) et discuter des
avantages et inconvénients respectifs.*

### 5 : Transmission d’objets

a. *Quel inconvénient y a-t-il à utiliser une infrastructure de type REST/JSON n'offrant aucun
service de validation (DTD, XML-schéma, WSDL) par rapport à une infrastructure comme SOAP
offrant ces possibilités ? Est-ce qu’il y a en revanche des avantages que vous pouvez citer ?*
b. *L’utilisation d’un mécanisme comme Protocol Buffers 8 est-elle compatible avec une
architecture basée sur HTTP ? Veuillez discuter des éventuelles avantages ou limitations par
rapport à un protocole basé sur JSON ou XML ?*
c. *Par rapport à l’API GraphQL mise à disposition pour ce laboratoire. Avez-vous constaté des
points qui pourraient être améliorés pour une utilisation mobile ? Veuillez en discuter, vous
pouvez élargir votre réflexion à une problématique plus large que la manipulation effectuée.*