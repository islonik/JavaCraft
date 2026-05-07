package dev.nklip.javacraft.xlspaceship.engine.service;

import dev.nklip.javacraft.xlspaceship.engine.model.NewGameResponse;
import dev.nklip.javacraft.xlspaceship.engine.model.SalvoRequest;
import dev.nklip.javacraft.xlspaceship.engine.model.SalvoResponse;
import dev.nklip.javacraft.xlspaceship.engine.model.SpaceshipProtocol;

public interface RemoteGameClient {

    int getCurrentPort();

    String getCurrentHostname();

    NewGameResponse sendPostNewGameRequest(String remoteHost, int remotePort, SpaceshipProtocol spaceshipProtocol);

    SalvoResponse fireShot(String remoteHost, int remotePort, String gameId, SalvoRequest salvoRequest);

    SalvoResponse fireShotByAi(String localHost, int localPort, String gameId, SalvoRequest salvoRequest);
}
