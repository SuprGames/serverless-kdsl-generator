# Serverless K-DSL Gradle Task

Serverless K-DSL is a small library that allows the generation of some of the Serverless Framework code that needs to be manually written.

The current version of Serverless K-DSL supports the generation of the following type of functions:

* HTTP Functions
* WebSocket Connectors
* Sqs Consumers
* EventBridge Listeners

The generator makes use of the Serverless-KDSL annotations

### HTTP Functions:
Serverless K-DSL supports the generation of HTTP functions in Serverless, the methods GET, POST, PUT and DELETE are supported. 
*If no method is provided GET will be used*

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


## Using Serverless KDSL Generator as a Gradle Task

*The preferred way to use the library*

Add the following to your build.gradle.kts

 * 1) Make sure you have the `java` plugin in place, since we are going to run our generator as a JavaExec task
```
plugins {
	//...

	java
	//...
}

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

