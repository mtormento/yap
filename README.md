# yap : Yet Another Pokedex

This service provides a couple of endpoints to retrive *Pokemon* information, with fun translations of their description.  
Two endpoints are provided:
- */pokemon/{name}* retrieves the information of a *Pokemon* named *name*
- */pokemon/translated/{name}* does the same, but the description will be translated to *yoda* language in case its habitat is *cave*, or it's a *legendary* *Pokemon*. If not, the description will be translated to *shakespearian* language.

## Build
A *Maven* environment is needed to build this service.  
Follow the instructions [here](https://maven.apache.org/install.html) to install it.  
Once finished, run the following command to build.
```sh
mvn clean package
```

## Usage
```sh
java -jar target/yap*
```
The service will be available on port 8080.

## Considerations
This service uses [PokeApi](https://pokeapi.co/) and [FunTranslations](https://funtranslations.com/) apis.  
The first has a fair use policy, while the second actively limits requests.  
In a production environment I would cache the responses of these services, to avoid the latencies of a remote call.
We could use the local cache of the client, with cache-control HTTP header for example, or some sort of distributed cache in case of a more complex scenario, with multiple instances of this service running together.