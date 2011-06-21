package controllers;

import play.Logger;
import play.libs.F;

import static play.mvc.Http.WebSocketEvent.TextFrame;

import play.mvc.Http;
import play.mvc.WebSocketController;
import models.Room;
import models.User;
import models.Event;
import scala.Option;

import java.util.Arrays;
import java.util.List;

public class RoomsSocket extends WebSocketController {
    public static void join(String title, String username) {
        User user = new User(username);
        Option<Room> roomOption = Room.findByTitle(title);
        Room room = roomOption.isDefined() ? roomOption.get() : null;
        if (room == null) {
            room = Room.create(title);
        }
        F.EventStream<Event> events = room.events().eventStream();

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

            Logger.info(e.toString());
        }
    }
}