package net.valentinc.nesthermostat;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.valentinc.ssh.SSHManager;

import org.junit.Test;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void testSendCommand() {
        System.out.println("sendCommand");

        String command = "ls FILE_NAME";
        String userName = "USER";
        String password = "PASSWORD";
        String connectionIP = "HOST";
        SSHManager instance = new SSHManager(userName, password, connectionIP, "");
        String errorMessage = instance.connect();

        if (errorMessage != null) {
            System.out.println(errorMessage);
            fail();
        }

        String expResult = "FILE_NAME\n";
        // call sendCommand for each command and the output
        //(without prompts) is returned
        String result = instance.sendCommand(command);
        // close only after all commands are sent
        instance.close();
        assertEquals(expResult, result);
    }
}