package org.vfs.server.commands;

import org.vfs.core.command.CommandValues;
import org.vfs.server.model.UserSession;

/**
 * @author Lipatov Nikita
 */
public interface Command {

    void apply(UserSession userSession, CommandValues values);
}
