package org.games.xlspaceship.web.resources;

import org.games.xlspaceship.impl.game.Grid;
import org.games.xlspaceship.impl.model.NewGameRequest;
import org.games.xlspaceship.impl.model.NewGameResponse;
import org.games.xlspaceship.impl.model.SpaceshipProtocol;
import org.games.xlspaceship.impl.services.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ProtocolResourceTest {

    @Test
    public void testCreateNewGame() {
        UserServices userServices = mock(UserServices.class);
        GridServices gridServices = mock(GridServices.class);
        GameServices gameServices = mock(GameServices.class);
        RestServices restServices = mock(RestServices.class);
        AIServices aiServices = mock(AIServices.class);

        XLSpaceshipServices spaceshipServices = new XLSpaceshipServices(userServices, gridServices, gameServices, restServices, aiServices);
        ValidationServices validationServices = new ValidationServices(spaceshipServices);

        when(userServices.getUserId()).thenReturn("AI");
        when(userServices.getFullName()).thenReturn("AI-1176");
        when(gameServices.nextUniqueGameId()).thenReturn("match-1-2");
        when(gridServices.newUnknownGrid()).thenReturn(new Grid());
        when(gridServices.newRandomGrid()).thenReturn(new Grid());
        when(gameServices.chooseStartingPlayer(anyString(), anyString())).thenReturn("AI");

        ProtocolResource protocolResource = new ProtocolResource(validationServices, spaceshipServices);
        NewGameResponse newGameResponse = protocolResource.createNewGame(createNewGameRequest());

        Assertions.assertNotNull(newGameResponse);
        Assertions.assertEquals("AI", newGameResponse.getUserId());
        Assertions.assertEquals("AI-1176", newGameResponse.getFullName());
        Assertions.assertEquals("match-1-2", newGameResponse.getGameId());
        Assertions.assertEquals("AI", newGameResponse.getStarting());
    }

    private NewGameRequest createNewGameRequest() {
        NewGameRequest newGameRequest = new NewGameRequest();
        newGameRequest.setFullName("Nikita"); // full name of the user who initiated the request
        newGameRequest.setUserId("nikilipa"); // id of the user who initiated the request
        SpaceshipProtocol sp = new SpaceshipProtocol();
        sp.setHostname("localhost"); // server which initiated the request
        sp.setPort("8077"); // port of the server which initiated the request
        newGameRequest.setSpaceshipProtocol(sp);
        return newGameRequest;
    }
}
