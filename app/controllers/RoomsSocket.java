package controllers;

import models.*;
import play.Logger;
import play.libs.F;

import static play.libs.F.Matcher.ClassOf;

import static play.mvc.Http.WebSocketEvent.TextFrame;
import static play.mvc.Http.WebSocketEvent.SocketClosed;

import play.mvc.Http;
import play.mvc.WebSocketController;
import scala.Option;

import java.util.Arrays;
import java.util.List;

/**
 * WebSocketによるチャットサーバ
 */
public class RoomsSocket extends WebSocketController {
    public static void join(String title, String username) {
        User user = User.join(username, "password", "email", "iconPath");
        Option<Room> roomOption = Room.findByTitle(title);
        Room room = roomOption.isDefined() ? roomOption.get() : null;
        if (room == null) {
            Logger.info("Room created: %s", title);
            room = Room.create(title);
        }
        F.EventStream<Event> events = room.events().eventStream();

        room.join(user);

        while (inbound.isOpen()) {
            play.libs.F.Either<Http.WebSocketEvent, Event> e = await(F.Promise.waitEither(
                    inbound.nextEvent(),
                    events.nextEvent()
            ));

            for (String message : TextFrame.match(e._1)) {
                Logger.info("Received a message: %s", message);

                String[] methodAndArguments = message.split(":");
                String method = methodAndArguments[0];
                String[] args = new String[]{};
                if (methodAndArguments.length > 1) {
                    args = Arrays.copyOfRange(methodAndArguments, 1, methodAndArguments.length);
                }
                List<String> arguments = Arrays.asList(args);
                Logger.info("%s, %s", method, arguments);

                if (method.equals("join")) {
                    room.join(user);
                } else if (method.equals("say")) {
                    String what = null;
                    if (arguments.size() > 0) {
                        what = arguments.get(0);
                        room.say(user, what);
                    }
                    if (what == null) {
                        Logger.warn("Nothing to say!");
                    }
                }
            }

            for (Join join : ClassOf(Join.class).match(e._2)) {
                outbound.send("joined:%s", join.user().name());
            }

            for (Leave leave : ClassOf(Leave.class).match(e._2)) {
                outbound.send("left:%s", leave.user().name());
            }

            for (Say say : ClassOf(Say.class).match(e._2)) {
                outbound.send("said:%s:%s", say.user().name(), say.what());
            }

            for (Http.WebSocketClose webSocketClose : SocketClosed.match(e._1)) {
                room.leave(user);
                Logger.info("Disconnecting user: %s", user.name());
                disconnect();
            }

            Logger.info(e.toString());
        }
    }
}