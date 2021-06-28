# yap : Yet Another Pokedex

This service provides a couple of endpoints to retrive *Pokemon* information, with fun translations of their description.  
Two endpoints are provided:
- */pokemon/{name}* retrieves the information of a *Pokemon* named *name*
- */pokemon/translated/{name}* does the same, but the description will be translated to *yoda* language in case its habitat is *cave*, or it's a *legendary* *Pokemon*. If not, the description will be translated to *shakespearian* language.

## Build
A *Maven* environment is needed to build this service.  
Follow the instructions [here](https://maven.apache.org/install.html) to install it.  
Once finished, run the following command to build the artifact.
```sh
mvn clean package
```

## Usage
```sh
java -jar target/yap*
```
The service will be available on port 8080.

## Docker
To build the image:
```sh
docker build . -t yap
```
To run it:
```sh
docker run -p 8080:8080 yap
```

## Considerations
This service uses [PokeApi](https://pokeapi.co/) and [FunTranslations](https://funtranslations.com/) apis.  
The first has a fair use policy, while the second actively rate limits requests: 60 a day with a distribution of 5 an hour.  
I decided to use a reactive approach, since there's no blocking actor in the request handling chain and we are I/O bound, therefore I could retain all the benefits without the potential downsides.  
In a production environment I would cache the responses of third party services, to avoid the latencies of a remote call, since I expect the responses for the same *Pokemon* (or the same description translation) to rarely change. 
We could use the local cache of the client, with cache-control HTTP header for example, or some sort of distributed cache in case of a more complex scenario, with multiple instances of this service running together.
Anyway, in case of a production environment we should probably use a paid plan for these third party services, otherwise the rate limiter will kick us out almost immediately.  
I decided to use a reactive approach, because there's no blocking in the chain, therefore I could retain all the benefits of this approach, without the potential downsides.