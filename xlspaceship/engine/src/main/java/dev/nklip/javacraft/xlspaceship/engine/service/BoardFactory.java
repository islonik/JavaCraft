package dev.nklip.javacraft.xlspaceship.engine.service;

import dev.nklip.javacraft.xlspaceship.engine.game.Board;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import dev.nklip.javacraft.xlspaceship.engine.game.ships.AClass;
import dev.nklip.javacraft.xlspaceship.engine.game.ships.Angle;
import dev.nklip.javacraft.xlspaceship.engine.game.ships.BClass;
import dev.nklip.javacraft.xlspaceship.engine.game.ships.SClass;
import dev.nklip.javacraft.xlspaceship.engine.game.ships.Spaceship;
import dev.nklip.javacraft.xlspaceship.engine.game.ships.Winger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardFactory {

    private final RandomProvider randomProvider;

    public Board newUnknownBoard() {
        return new Board();
    }

    public Board newRandomBoard() {
        Board board = new Board();

        List<Spaceship> spaceshipList = new ArrayList<>();
        spaceshipList.add(new BClass(randomProvider.generateOrientation())); // 10 lives
        spaceshipList.add(new Winger(randomProvider.generateOrientation())); // 9 lives
        spaceshipList.add(new SClass(randomProvider.generateOrientation())); // 8 lives
        spaceshipList.add(new AClass(randomProvider.generateOrientation())); // 8 lives
        spaceshipList.add(new Angle(randomProvider.generateOrientation()));  // 6 lives

        while (true) {
            boolean isSet = false;
            for (Spaceship spaceship : spaceshipList) {
                isSet = board.setSpaceship(randomProvider, spaceship);
                if (!isSet) {
                    board.clear();
                    break;
                }
            }
            if (isSet) {
                break;
            }
        }

        return board;
    }
}
