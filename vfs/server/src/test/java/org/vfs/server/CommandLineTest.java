package org.vfs.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vfs.core.network.protocol.RequestFactory;
import org.vfs.core.network.protocol.Protocol;
import org.vfs.server.commands.Command;
import org.vfs.server.model.Timer;
import org.vfs.server.model.UserSession;
import org.vfs.server.network.ClientWriter;
import org.vfs.server.services.NodeService;
import org.vfs.server.services.UserSessionService;
import org.vfs.server.utils.NodePrinter;

import java.util.Map;

import org.mockito.ArgumentCaptor;


import static org.mockito.Mockito.*;

/**
 * All common tests, except LockTests.
 * @author Lipatov Nikita
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles(value = "test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommandLineTest {

    @Autowired
    private Map<String, Command> commands;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private NodePrinter nodePrinter;

    private UserSession nikitaSession;
    private UserSession r2d2Session;

    @BeforeEach
    public void setUp() {
        nodeService.initDirs();

        // UserSession #1
        ClientWriter clientWriter1 = mock(ClientWriter.class);
        UserSession userSession1 = userSessionService.startSession(clientWriter1, new Timer());
        String id1 = userSession1.getUser().getId();
        String login1 = "nikita";
        userSessionService.attachUser(id1, login1);
        nikitaSession = userSession1;

        // UserSession #2
        ClientWriter clientWriter2 = mock(ClientWriter.class);
        UserSession userSession2 = userSessionService.startSession(clientWriter2, new Timer());
        String id2 = userSession2.getUser().getId();
        String login2 = "r2d2";
        userSessionService.attachUser(id2, login2);
        r2d2Session = userSession2;
    }

    @Test
    public void testChangeDirectory() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        CommandLine cmd = new CommandLine(commands);
        String actualResponse;

        actualResponse = okResponse(nikitaSession, cmd, id, login, "cd ../..");
        Assertions.assertEquals("/", actualResponse);

        actualResponse = okResponse(nikitaSession, cmd, id, login, "cd ../..");
        Assertions.assertEquals("/", actualResponse);

        actualResponse = okResponse(nikitaSession, cmd, id, login, "cd");
        Assertions.assertEquals("/", actualResponse);

        Assertions.assertEquals(
                """
                /
                |__home
                |  |__nikita
                |  |__r2d2
                """,
                nodePrinter.print(nodeService.getRoot())
        );

        actualResponse = okResponse(nikitaSession, cmd, id, login, "mkfile test_file.txt");
        Assertions.assertEquals("New file '/test_file.txt' was created!", actualResponse);

        Assertions.assertEquals(
                """
                /
                |__home
                |  |__nikita
                |  |__r2d2
                |__test_file.txt
                """,
                nodePrinter.print(nodeService.getRoot())
        );

        actualResponse = failResponse(nikitaSession, cmd, id, login, "cd test_file.txt");
        Assertions.assertEquals("Destination node is file!", actualResponse);

        actualResponse = failResponse(nikitaSession, cmd, id, login, "cd not_existing_folder");
        Assertions.assertEquals("Destination node is not found!", actualResponse);

    }

    @Test
    public void testCopy() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        CommandLine cmd = new CommandLine(commands);

        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "cd ../.."));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "mkdir applications/servers/weblogic"));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "mkdir logs"));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "copy applications logs"));

        Assertions.assertEquals(
                """
                        /
                        |__applications
                        |  |__servers
                        |  |  |__weblogic
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        |__logs
                        |  |__applications
                        |  |  |__servers
                        |  |  |  |__weblogic
                        """,
                nodePrinter.print(nodeService.getRoot())
        );

        String actualResponse = failResponse(nikitaSession, cmd, id, login, "copy not_existing not_existing");
        Assertions.assertEquals("Source path/node is not found!", actualResponse);

        actualResponse = failResponse(nikitaSession, cmd, id, login, "copy home not_existing");
        Assertions.assertEquals("Destination path/node is not found!", actualResponse);

        actualResponse = okResponse(nikitaSession, cmd, id, login, "lock logs/applications");
        Assertions.assertEquals("You has locked the node by path '/logs/applications'", actualResponse);

        actualResponse = okResponse(nikitaSession, cmd, id, login, "mkfile copy_test.txt");
        Assertions.assertEquals("New file '/copy_test.txt' was created!", actualResponse);

        Assertions.assertEquals(
                """
                        /
                        |__applications
                        |  |__servers
                        |  |  |__weblogic
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        |__logs
                        |  |__applications [Locked by nikita ]
                        |  |  |__servers
                        |  |  |  |__weblogic
                        |__copy_test.txt
                        """,
                nodePrinter.print(nodeService.getRoot())
        );

        actualResponse = okResponse(nikitaSession, cmd, id, login, "copy copy_test.txt logs/applications");
        Assertions.assertEquals("Node or children nodes is/are locked!", actualResponse);

        actualResponse = failResponse(nikitaSession, cmd, id, login, "copy home/nikita copy_test.txt");
        Assertions.assertEquals("Destination path is not directory", actualResponse);
    }

    @Test
    public void testHelp() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        CommandLine cmd = new CommandLine(commands);

        String actualResponse = okResponse(nikitaSession, cmd, id, login, "help");

        Assertions.assertEquals("""
                You can use next commands:
                    * - cd directory\s
                    * - connect server_name:port login\s
                    * - copy node directory\s
                    * - help\s
                    * - lock [-r] node\s
                        -r - enable recursive mode\s
                    * - mkdir directory\s
                    * - mkfile file
                    * - move node directory\s
                    * - print\s
                    * - quit\s
                    * - rename node name\s
                    * - rm node\s
                    * - unlock [-r] node\s
                        -r - enable recursive mode\s
                """, actualResponse);
    }

    @Test
    public void testLock() {
        String id1 = nikitaSession.getUser().getId();
        String login1 = nikitaSession.getUser().getLogin();
        String id2 = r2d2Session.getUser().getId();
        String login2 = r2d2Session.getUser().getLogin();
        CommandLine cmd1 = new CommandLine(commands);
        CommandLine cmd2 = new CommandLine(commands);

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "cd ../.."));
        cmd2.onUserInput(r2d2Session,   RequestFactory.newRequest(id2, login2, "cd ../.."));

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "mkfile applications/servers/weblogic/logs/weblogic.log"));
        cmd2.onUserInput(r2d2Session, RequestFactory.newRequest(id2, login2, "mkfile applications/databases/oracle/bin/oracle.exe"));

        Assertions.assertEquals(
                """
                        /
                        |__applications
                        |  |__databases
                        |  |  |__oracle
                        |  |  |  |__bin
                        |  |  |  |  |__oracle.exe
                        |  |__servers
                        |  |  |__weblogic
                        |  |  |  |__logs
                        |  |  |  |  |__weblogic.log
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        """,
                nodePrinter.print(nodeService.getRoot())
        );

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "lock applications/databases"));

        Assertions.assertEquals(
                """
                /
                |__applications
                |  |__databases [Locked by nikita ]
                |  |  |__oracle
                |  |  |  |__bin
                |  |  |  |  |__oracle.exe
                |  |__servers
                |  |  |__weblogic
                |  |  |  |__logs
                |  |  |  |  |__weblogic.log
                |__home
                |  |__nikita
                |  |__r2d2
                """,
                nodePrinter.print(nodeService.getRoot())
        );

        cmd2.onUserInput(r2d2Session, RequestFactory.newRequest(id2, login2, "lock applications"));

        Assertions.assertEquals(
                """
                /
                |__applications [Locked by r2d2 ]
                |  |__databases [Locked by nikita ]
                |  |  |__oracle
                |  |  |  |__bin
                |  |  |  |  |__oracle.exe
                |  |__servers
                |  |  |__weblogic
                |  |  |  |__logs
                |  |  |  |  |__weblogic.log
                |__home
                |  |__nikita
                |  |__r2d2
                """,
                nodePrinter.print(nodeService.getRoot())
        );

        ClientWriter r2d2Writer = r2d2Session.getClientWriter();
        clearInvocations(r2d2Writer);

        cmd2.onUserInput(r2d2Session, RequestFactory.newRequest(id2, login2, "lock not_existing_folder"));

        ArgumentCaptor<Protocol.Response> responseCaptor = getResponseCaptorForFail(r2d2Writer);
        Assertions.assertEquals("Destination node is not found!", responseCaptor.getValue().getMessage());
    }

    @Test
    public void testLockRecursive() {
        String id1 = nikitaSession.getUser().getId();
        String login1 = nikitaSession.getUser().getLogin();
        String id2 = r2d2Session.getUser().getId();
        String login2 = r2d2Session.getUser().getLogin();
        CommandLine cmd1 = new CommandLine(commands);
        CommandLine cmd2 = new CommandLine(commands);

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "cd ../.."));
        cmd2.onUserInput(r2d2Session,   RequestFactory.newRequest(id2, login2, "cd ../.."));

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "mkfile applications/servers/weblogic/logs/weblogic.log"));
        cmd2.onUserInput(r2d2Session,   RequestFactory.newRequest(id2, login2, "mkfile applications/databases/oracle/bin/oracle.exe"));

        Assertions.assertEquals(
                """
                        /
                        |__applications
                        |  |__databases
                        |  |  |__oracle
                        |  |  |  |__bin
                        |  |  |  |  |__oracle.exe
                        |  |__servers
                        |  |  |__weblogic
                        |  |  |  |__logs
                        |  |  |  |  |__weblogic.log
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        """,
                nodePrinter.print(nodeService.getRoot())
        );

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "lock -r applications/databases"));

        Assertions.assertEquals(
                """
                /
                |__applications
                |  |__databases [Locked by nikita ]
                |  |  |__oracle [Locked by nikita ]
                |  |  |  |__bin [Locked by nikita ]
                |  |  |  |  |__oracle.exe [Locked by nikita ]
                |  |__servers
                |  |  |__weblogic
                |  |  |  |__logs
                |  |  |  |  |__weblogic.log
                |__home
                |  |__nikita
                |  |__r2d2
                """,
                nodePrinter.print(nodeService.getRoot())
        );

        cmd2.onUserInput(r2d2Session, RequestFactory.newRequest(id2, login2, "lock -r applications"));

        Assertions.assertEquals(
                """
                /
                |__applications
                |  |__databases [Locked by nikita ]
                |  |  |__oracle [Locked by nikita ]
                |  |  |  |__bin [Locked by nikita ]
                |  |  |  |  |__oracle.exe [Locked by nikita ]
                |  |__servers
                |  |  |__weblogic
                |  |  |  |__logs
                |  |  |  |  |__weblogic.log
                |__home
                |  |__nikita
                |  |__r2d2
                """,
                nodePrinter.print(nodeService.getRoot())
        );
    }

    @Test
    public void testMove() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        CommandLine cmd = new CommandLine(commands);

        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "cd ../.."));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "mkdir applications/servers/weblogic"));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "mkdir logs"));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "move applications logs"));

        Assertions.assertEquals(
                """
                        /
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        |__logs
                        |  |__applications
                        |  |  |__servers
                        |  |  |  |__weblogic
                        """,
                nodePrinter.print(nodeService.getRoot())
        );
    }

    @Test
    public void testRm() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        CommandLine cmd = new CommandLine(commands);

        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "cd ../.."));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "mkdir applications/servers"));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "mkdir logs"));
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "rm applications"));

        Assertions.assertEquals(
                """
                        /
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        |__logs
                        """,
                nodePrinter.print(nodeService.getRoot())
        );
    }

    @Test
    public void testRename() {
        String id1 = nikitaSession.getUser().getId();
        String login1 = nikitaSession.getUser().getLogin();
        String id2 = r2d2Session.getUser().getId();
        String login2 = r2d2Session.getUser().getLogin();
        CommandLine cmd1 = new CommandLine(commands);
        CommandLine cmd2 = new CommandLine(commands);

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "cd ../.."));
        cmd2.onUserInput(r2d2Session,   RequestFactory.newRequest(id2, login2, "cd ../.."));

        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "mkfile applications/servers/weblogic/logs/weblogic.log"));
        cmd1.onUserInput(nikitaSession, RequestFactory.newRequest(id1, login1, "lock -r applications/servers/weblogic"));

        cmd2.onUserInput(r2d2Session,   RequestFactory.newRequest(id2, login2, "rename applications/servers web-servers"));

        Assertions.assertEquals(
                """
                        /
                        |__applications
                        |  |__web-servers
                        |  |  |__weblogic [Locked by nikita ]
                        |  |  |  |__logs [Locked by nikita ]
                        |  |  |  |  |__weblogic.log [Locked by nikita ]
                        |__home
                        |  |__nikita
                        |  |__r2d2
                        """,
                nodePrinter.print(nodeService.getRoot())
        );

    }

    @Test
    public void testNoSuchCommand() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        CommandLine cmd = new CommandLine(commands);
        ClientWriter clientWriter = nikitaSession.getClientWriter();

        clearInvocations(clientWriter);
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "unknown-command"));

        ArgumentCaptor<Protocol.Response> responseCaptor = getResponseCaptorForOk(clientWriter);
        Assertions.assertEquals(
                "No such command! Please check you syntax or type 'help'!",
                responseCaptor.getValue().getMessage()
        );
    }

    @Test
    public void testExceptionCausedByCommand() {
        String id = nikitaSession.getUser().getId();
        String login = nikitaSession.getUser().getLogin();
        ClientWriter clientWriter = nikitaSession.getClientWriter();
        Command failingCommand = mock(Command.class);
        doThrow(new IllegalArgumentException("exception caused by a command")).when(failingCommand)
                .apply(eq(nikitaSession), any());
        CommandLine cmd = new CommandLine(Map.of("boom", failingCommand));

        clearInvocations(clientWriter);
        cmd.onUserInput(nikitaSession, RequestFactory.newRequest(id, login, "boom"));

        ArgumentCaptor<Protocol.Response> responseCaptor = getResponseCaptorForFail(clientWriter);
        Assertions.assertEquals("exception caused by a command", responseCaptor.getValue().getMessage());
    }

    private String okResponse(
            UserSession userSession,
            CommandLine cmd,
            String id,
            String login,
            String command) {
        clearInvocations(userSession.getClientWriter());

        cmd.onUserInput(userSession, RequestFactory.newRequest(id, login, command));

        ArgumentCaptor<Protocol.Response> responseCaptor = getResponseCaptorForOk(userSession.getClientWriter());
        return responseCaptor.getValue().getMessage();
    }

    private String failResponse(
            UserSession userSession,
            CommandLine cmd,
            String id,
            String login,
            String command) {
        clearInvocations(userSession.getClientWriter());

        cmd.onUserInput(userSession, RequestFactory.newRequest(id, login, command));

        ArgumentCaptor<Protocol.Response> responseCaptor = getResponseCaptorForFail(userSession.getClientWriter());
        return responseCaptor.getValue().getMessage();
    }

    private ArgumentCaptor<Protocol.Response> getResponseCaptorForOk(ClientWriter clientWriter) {
        ArgumentCaptor<Protocol.Response> responseCaptor =
                ArgumentCaptor.forClass(Protocol.Response.class);
        verify(clientWriter, times(1)).send(responseCaptor.capture());
        Assertions.assertEquals(
                Protocol.Response.ResponseType.OK,
                responseCaptor.getValue().getCode()
        );
        return responseCaptor;
    }

    private ArgumentCaptor<Protocol.Response> getResponseCaptorForFail(ClientWriter clientWriter) {
        ArgumentCaptor<Protocol.Response> responseCaptor =
                ArgumentCaptor.forClass(Protocol.Response.class);
        verify(clientWriter, times(1)).send(responseCaptor.capture());
        Assertions.assertEquals(
                Protocol.Response.ResponseType.FAIL,
                responseCaptor.getValue().getCode()
        );
        return responseCaptor;
    }



}
