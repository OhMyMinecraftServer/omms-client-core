package icu.takeneko.omms.client.exception;

import lombok.Getter;

@Getter
public final class RequestUnauthorisedException extends IllegalArgumentException {
   private final String controllerName;

   public RequestUnauthorisedException(String controllerName) {
      super("Request to controller " + controllerName + " was refused.");
      this.controllerName = controllerName;
   }

}
