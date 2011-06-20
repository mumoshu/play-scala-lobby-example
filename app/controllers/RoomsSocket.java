package controllers;

import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.WebSocketController;
import models.Room;
import models.User;
import models.Event;

public class RoomsSocket extends WebSocketController {
    public static void join(String title, String username) {
//        User user = new User(username);
//        Room room = Room.findByTitle(title).getOrElse(Room.create(title));
//        F.EventStream<Event> events = room.events().eventStream();
//
//      while (inbound.isOpen()) {
//        play.libs.F.Either<Http.WebSocketEvent, Event> e = await(F.Promise.waitEither(
//                inbound.nextEvent(),
//                events.nextEvent()
//        ));
//
//        Logger.info(e.toString());
//      }
    }
}