package chat

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.io.StdIn

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

object Server {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val chatRoom = system.actorOf(Props(new ChatRoom), "chat")

    def newUser(): Flow[Message, Message, NotUsed] = {
      // new connection - new user actor
      val userActor = system.actorOf(Props(new User(chatRoom)))

      val incomingMessages: Sink[Message, NotUsed] =
        Flow[Message].map {
          // transform websocket message to domain message
          case TextMessage.Strict(text) => User.IncomingMessage(text)
          case _ => User.IncomingMessage("??")
        }.to(Sink.actorRef[User.IncomingMessage](userActor, PoisonPill))

      val outgoingMessages: Source[Message, NotUsed] =
        Source.actorRef[User.OutgoingMessage](10, OverflowStrategy.dropNew)
        .mapMaterializedValue { outActor =>
          // give the user actor a way to send messages out
          userActor ! User.Connected(outActor)
          NotUsed
        }.map(
          // transform domain message to web socket message
          (outMsg: User.OutgoingMessage) => TextMessage(outMsg.text))

      // then combine both to a flow
      Flow.fromSinkAndSource(incomingMessages, outgoingMessages)
    }

    val route =
      concat (
        path("public.gif") {
          extractHost { host =>
            headerValueByName("User-Agent") { userAgent =>
              chatRoom ! ChatRoom.ChatMessage("{\"host\": \"" + host.toString() + "\", \"user-agent\": \"" + userAgent + "\"}")
              getFromFile("resources/minimal.gif")
            }
          }
        },

        path("browser") {
        concat (
          get {
            handleWebSocketMessages(newUser())
          },

          /*post {
            cors() {
              entity(as[String]) { data =>
                chatRoom ! ChatRoom.ChatMessage(data)
                complete("OK")
              }
            }
          }*/
        )
      }
      )

    val binding = Await.result(Http().bindAndHandle(route, "0.0.0.0", 5000), 3.seconds)


    // the rest of the sample code will go here
    println("Started server at 0.0.0.0:5000, press enter to kill server")
    StdIn.readLine()
    system.terminate()
  }
}
