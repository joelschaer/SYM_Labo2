# SYM\_Labo2



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

La meilleures solution pour répondre à ce genre d'utilisation asynchrone, serait de faire une authentification en amont qui donne une token d'authentification. L'application pourra ensuite présenter ce token à chaque fois qu'elle effectue une requête différée auprès du serveur.
Avec cette solution on ne requière aucune action utilisateur au moment de l'envoi effectif des requêtes.

### 3 : Threads concurrents

*Lors de l'utilisation de protocoles asynchrones, c'est généralement deux threads différents qui se
préoccupent de la préparation, de l'envoi, de la réception et du traitement des données. Quels
problèmes cela peut-il poser ?*

En fonction de la rapidité de traitement de chaque étape il se pourrait qu'une étape s'exécute plus vite que les autres. Le traitement des données ne se fait donc pas dans l'ordre et pose problème. Il faut donc s'assurer que l'ordre des étapes soient bien respectées.

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

En effectuant plusieurs connexion on assure que pour chaque requête on obtienne une réponse correspondante. Il n'est alors pas nécessaires de traiter particulièrement les retours du serveur. Ceux-ci sont lu dès qu'ils sont reçus.
Si le fonctionnement de l'application dépend de ces données, il peut alors s'avérer utilie de les envoyer séparément. 
Ce mode d'envoie est plus adapter si les données à envoyer constituent des gros packets ou que le risque d'interruption est fréquent.

Dans le cas du multiplexage, il est nécessaire de gérer au niveau applicatif la manière dont les requêtes sont combinée entre elle et sous quelle forme les réponses vont revenir. Cela demande un niveau de traitement supplémentaire qui, selon les données transmises peut avoir des avantages ou des inconvénients. 
En combinant les données à envoyer entre elle afin de les regouper, on ne va initier qu'une seule connexion pour tous les packets en attente. Si les packets sont petits on évite ainsi d'ouvrir beaucoup de petites connexions et on profite d'une seule connexion pour tout envoyer. 
Cette solution n'est pas très adaptée si les packets sont gros et que la connexion est lente ou souvent interrompue . Avec une envoi unique mais conséquent, il n'est pas possible de traiter les données qu'une fois l'ensemble du packet reçu. Si le débit est lent ou souvent interrompu, ce délait d'envoi peut devenir très long et va détériorer l'expérience de l'utilisateur qui devra attendre longtemps  avant de continuer l'utilisation.

Il est donc important de bien réfléchir au multiple situation qui pourraient de présenter et de trouver des solution adaptées.



### 5 : Transmission d’objets

**a.** *Quel inconvénient y a-t-il à utiliser une infrastructure de type REST/JSON n'offrant aucun
service de validation (DTD, XML-schéma, WSDL) par rapport à une infrastructure comme SOAP
offrant ces possibilités ? Est-ce qu’il y a en revanche des avantages que vous pouvez citer ?*

Avec une structure qui n'est pas validée, il est plus difficile de savoir si les données envoyées correspondes bien à ce qui est attendu. On pourrait se retrouver dans un cas de figure ou les valeurs sont manquantes ou corrompues. L'application pourrait donc cracher ou se retrouver dans un état instable. Afin d'éviter ces cas de figurer l'application doit prévoir et valider ce qu'elle reçoit. Cela demande un effort supplémentaire de la part de l'application qui est gérée automatiquement avec un système de validation (DTD par exemple) qui nous assurer que ce qu'on reçoit soit exactement ce que notre application est capable de gérer.

Ne pas avoir de système de validation permet cependant une plus grande flexibilité. L'application n'est pas limité à ce qui était prévu dans le système de validation. Elle peut ainsi évoluer plus facilement et tout en gardant une meilleures compatibilité avec les version antérieurs.

**b.** *L’utilisation d’un mécanisme comme Protocol Buffers 8 est-elle compatible avec une
architecture basée sur HTTP ? Veuillez discuter des éventuelles avantages ou limitations par rapport à un protocole basé sur JSON ou XML ?*

Oui, le mécanisme de Protocol Buffers est un système qui permet de sérialiser différents type d'objet. L'envoie ensuite en utilisant http est donc totalement possible.
Chaque protocol de sérialisation comporte ses avantages et inconvénients:

Json :

- Lisible par un humain
- Il n'est pas nécessaire de connaître la structure de base pour le lire
- très largement supporté
- moins verbeux que xml

XML :

- Lisible par un humain
- Il n'est pas nécessaire de connaître la structure de base pour le lire
- standard pour SOAP etc.
- Beaucoup de librairies de traitement. (xsd, xslt, sax, dom, etc)
- très verbeux
- Peut facilement être validé avec des DTD ...

Protocol Buffers :

- N'est pas lisible par un humain.

- Donnée très dense (small outputs)
- Très difficile à décoder si on ne connait pas la structure des données initiales. (les formats de données sont ambigus entre eu et nécessite un schéma pour les différencier)
- Processing très rapide des données

Le choix du protocole va donc dépendre des besoins de l'application. Si celle-ci à besoin d'un système performant mais limité en compatibilité ou plus largement supporté mais beaucoup moins performant. Ou que celui-ci doit impérativement être validé afin d'assurer le respect de la structure de données imposée.



**c.** *Par rapport à l’API GraphQL mise à disposition pour ce laboratoire. Avez-vous constaté des
points qui pourraient être améliorés pour une utilisation mobile ? Veuillez en discuter, vous
pouvez élargir votre réflexion à une problématique plus large que la manipulation effectuée.*

Le premier problème que j'ai rencontré avec GraphQl dans ce laboratoire était le temps de réponse des requêtes, cela était du au fait que je récupérais trop de données d'un coup et aussi à cause du system de temps de réponse mis en place dans ce laboratoire. J'ai donc résolus ce problème en diminuant la taille des requêtes.

Du coup une des choses possible à améliorer est le fait de pouvoir mieux découper les donnnées à récupérer. Par exemple dans le cas ou il y a aurait un très grand nombre d'auteur ou de post, il faudrait pouvoir récupérer les "X" premiers posts.

Ca serait aussi intéressant de pouvoir définir des règles de tri pour les données retournées par graphQl, par exemple trier par nom d'auteur, par id etc...

Dans un cas plus large que ce laboratoire :

La possiblité d'avoir des requêtes retournant le nombre d'objet serait intéressant comme par exemple le nombre d'auteurs.

La possibilité de faire des recherches efficasses sur les données sur différents attributs d'un objet par exemple chercher les auteurs de minimum 30 ans ayant participé au Post "X". Vous allez me dire que on peut déjà faire des recherches comme récupérer les poste d'un auteur mais elles sont limitées. 

La majorité de ces idées ont pour but d'optimiser les requêtes envoyée à GraphQL afin de limiter l'utilisation du réseau (3G payant) et d'accélérer le temps de réponse de l'application.

Bien sûr certaines de ces idées existes déjà pour chez GraphQl ou existeront bientôt, mon expérience se résume à celle de ce laboratoire.

###6. Transmission compressée

*Quel gain peut-on constater en moyenne sur des fichiers texte (xml et json sont aussi du texte) en*
*utilisant de la compression du point 3.4 ? Vous comparerez vos résultats par rapport au gain théorique*
*d’une compression DEFLATE, vous enverrez aussi plusieurs tailles de contenu pour comparer.*

A l'aide de string pseudo-random, nous trouvons les valeurs suivantes:

```
Ratio compression for string length 6 : 133.33333333333331

Ratio compression for string length 22 : 95.45454545454545

Ratio compression for string length 37 : 81.08108108108108

Ratio compression for string length 47 : 89.36170212765957

Ratio compression for string length 59 : 77.96610169491525

Ratio compression for string length 232 : 53.44827586206896
```

On constate donc que le ratio de compression est inversement proportionnel à la longueur du texte (respectivement du fichier) compressé. 
On obtient (pour de longue stirngs) un ratio meilleur que celui des 75% du DEFLATE. 

(Avec une string de longueur 6 qui se répète, nous trouvons des valeurs bien plus faibles)

```
Ratio compression for string length 12 : 58.333333333333336

Ratio compression for string length 18 : 38.88888888888889

```

Cela correspond à l'hypothèse logique qui veut que la capacité de compression d'une chaîne de caractères soit limité par l'entropie et le rapport de compression (longueur de la chaîne divisée par le produit du nombre de caractères différents de l'alphabet utilsé et du nombre de caractères).