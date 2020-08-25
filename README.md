# Serverless K-DSL Gradle Task

Serverless K-DSL is a small library that allows the generation of some of the Serverless Framework code that needs to be manually written.

The current version of Serverless K-DSL supports the generation of the following type of functions:

* HTTP Functions
* WebSocket Connectors
* Sqs Consumers
* EventBridge Listeners
* Authorizer Lambda Functions:
  * Token and Request functions
  * ExistingAuthorizerFunctions

The generator makes use of the Serverless-KDSL annotations library

In this README you can find:
* How to use the Annotations
* How to generate your code Using the Generator as a Gradle Task

### HTTP Functions:
Serverless K-DSL supports the generation of HTTP functions in Serverless, the methods GET, POST, PUT and DELETE are supported. 
*If no method is provided, GET will be used*

```
package io.suprgames.player

@HttpFunction(name = "register-player", method = HttpMethod.POST, path = "player/register")
class RegisterPlayerHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

// Code here

}
```

Will generate the following entry in the Serverless.yml file when the generation task is executed
```
  register-player:
    handler: io.suprgames.player.RegisterPlayerHandler
    events:
      - http:
          path: player/register
          method: post
```

### WebSocket Connectors

```
package io.suprgames.game

@WebSocketConnector(route = "game-action")
class GameActionConnector : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

// Code here

}
```

Will generate the following entry in the Serverless.yml file when the generation task is executed
```
  game-action-connector:
    handler: io.suprgames.game.GameActionConnector
    events:
      - websocket:
          route: game-action
```

The common WebSocket routes ($connect, $disconnect and $default) are provided in a convinient companion object in WebSocketRoutes:

`@WebSocketConnector(route = WebSocketRoutes.CONNECT)`

### Sqs Consumer
```
package io.suprgames.game

@SqsConsumer(sqsArn = "\${self:provider.environment.sqsArn}")
class GameQueueConsumer : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

// Code here

}
```

Will generate the following entry in the Serverless.yml file when the generation task is executed
```
  game-queue-consumer:
    handler: io.suprgames.game.GameQueueConsumer
    events:
      - sqs:
          arn: ${self:provider.environment.sqsArn}
```

*Note that providing a reference to an environment variable that already exisist in the serverless-base.yml is supported, the only thing to consider is that, since you are writting ${bla bla bla} and this looks like Kotlin code you will need to skip the $ symbol how we did in the example.*

### Event Listeners
We support the connection to EventBridge to Listen to events depending on their type, so we can implement a typical Event-Driven communication in our systems.

```
package io.suprgames.game

@EventBridgeListener(eventBusArn = "\${self:provider.environment.eventBusArn}"
                     eventToListen = GameStartRequestedEvent::class)
class GameStartListener  : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

// Code here

}
```

Will generate the following entry in the Serverless.yml file when the generation task is executed
```
  game-start-listener:
    handler: io.suprgames.games.GameStartListener
    events:
      - eventBridge:
          eventBus: ${self:provider.environment.eventBusArn}
          pattern:
            detail-type:
              - 'io.suprgames.games.GameStartRequestedEvent'
```

**Note 1** Providing a reference to an environment variable that already exisist in the serverless-base.yml is supported, the only thing to consider is that, since you are writting ${bla bla bla} and this looks like Kotlin code you will need to skip the $ symbol how we did in the example.

**Note 2** The event to listen is transformed as a detail-type pattern with the complete class qualified name. This is possible because when we publish the event in EventBridge we MAKE SURE that the Detail-Type attribute is filled with the Event qualified name

### Lambda Authorizers
Depending on having already in place the Lambda Authorizer in the system or we are generating it we will define it in a different way.

#### When the Lambda Authorizer doesn't already exist:

1) Create the Lambda Authorizer function and annotate it with the `AuthorizerFunction` annotation
```
package io.suprgames.auth

const val PLAYER_LOGGED_AUTHORIZATION = "player-logged-auth"

@AuthorizerFunction(name = PLAYER_LOGGED_AUTHORIZATION, ttl = 300, type = AuthorizerFunctionType.REQUEST, identitySources = ["method.request.header.Authorization"])
class PlayerLoggedAuthorization : RequestHandler<Map<String, Any>, Map<String, Any>> {

// Code here

}
``` 

Will generate the following entry in the Serverless.yml file when the generation task is executed
```
  player-logged-authorization:
    handler: io.suprgames.auth.PlayerLoggedAuthorization
```
**Note** The `name` field is **mandatory** for authorization functions since it needs to be referenced from the HTTP Function

2) Reference the Authorizer from the HTTP Function that requires the Authorization

```
package io.suprgames.games

@HttpFunction(name = "register-player", method = HttpMethod.GET, path = "player/listGames", authorizer = PLAYER_LOGGED_AUTHORIZATION")
class ListPlayerGames : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

// Code here

}
```
**Note** The most recommended way to write the reference is by using a constant, that way we are sure we about do not have typos

Will generate the following data for the HttpFunction entry in the Serverless.yml file when the generation task is executed

```
  list-player-games:
    handler: io.suprgames.games.ListPlayerGames
    events:
      - http:
          path: player/listGames
          method: get
          cors: true
          authorizer:
            name: player-logged-authorization
            resultTtlInSeconds: 300
            identitySource: method.request.header.Authorization
            type: request
```

#### When the lambda exists already:

We need to add the annotation ExistingAuthorizerFunction to the class where we have the HTTPFunction.
```
package io.suprgames.games

@ExistingAuthorizerFunction(arn = "xxx:xxx:Lambda-Name", ttl = 300, identitySources = ["method.request.header.Authorization"], type = AuthorizerFunctionType.REQUEST)
@HttpFunction(name = "register-player", method = HttpMethod.GET, path = "player/listGames")
class ListPlayerGames : RequestHandler<Map<String, Any>, ApiGatewayResponse> {

// Code here

}
```
**Note** The authorizer field is not required in the HttpFunction annotationbecause we are annotating the class explicitly  

```
  list-player-games:
    handler: io.suprgames.games.ListPlayerGames
    events:
      - http:
          path: player/listGames
          method: get
          cors: true
          authorizer:
            arn: xxx:xxx:Lambda-Name
            resultTtlInSeconds: 300
            identitySource: method.request.header.Authorization
            type: request
```

## Using Serverless KDSL Generator as a Gradle Task

>***The preferred way to use the library***

Add the following to your build.gradle.kts

 * 1) Make sure you have the `java` plugin in place, since we are going to run our generator as a JavaExec task
 
```
plugins {
	//...

	java
	//...
}
```

 * 2) The repository where we have the components published is jitpack, so you will need to add it to your repositories list
 
```
repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}
```

 * 3) Create the `JavaExec` task
 
 ```
task<JavaExec>("generate-serverless") {
    main = "io.suprgames.serverless.ServerlessDSLGeneratorMain"
    args = listOf("serverless-base.yml", "io.suprgames", "serverless.yml")
    classpath = sourceSets["main"].runtimeClasspath
    dependencies {
        implementation("com.github.suprgames:serverless-kdsl-generator:v0.0.1")
        implementation("org.reflections:reflections:0.9.12")
    }
}
 ```

 *Notes*:
   * The name "generate-serverless" could be anything you want
   * The arguments that we place in "args" are the following ones:
     * Base file
     * Base package that will be used to perform the generation
     * The serverless file that will be generated

