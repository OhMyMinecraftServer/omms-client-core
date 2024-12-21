package icu.takeneko.omms.client.exception;

import lombok.Getter;

@Getter
public final class WhitelistNotFoundException extends Exception {
   private final String whitelistName;

   public WhitelistNotFoundException(String whitelistName) {
      super("Whitelist " + whitelistName + " not exist.");
      this.whitelistName = whitelistName;
   }

}
